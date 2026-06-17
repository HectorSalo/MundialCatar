package com.skysam.hchirinos.mundial2026.ui.gameday

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.skysam.hchirinos.mundial2026.BuildConfig
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common.formatRound
import com.skysam.hchirinos.mundial2026.common.Constants
import com.skysam.hchirinos.mundial2026.common.FlagsMapper
import com.skysam.hchirinos.mundial2026.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GameToView
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.Auth
import com.skysam.hchirinos.mundial2026.ui.commonView.EditResultsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class GamedayFragment : Fragment() {

    private var _binding: FragmentGamedayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GamedayViewModel by activityViewModels()
    @Inject
    lateinit var auth: Auth
    private var gamesForDay = mutableListOf<Game>()
    private lateinit var gamedayAdapter: GamedayAdapter
    private lateinit var calendar: Calendar
    private var countdownJob: Job? = null
    private var dayWatcherJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamedayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val canEdit = auth.getCurrentUser()?.email in setOf(
            Constants.USER_MAIN,
            Constants.USER_TEST
        )

        gamedayAdapter = GamedayAdapter(canEdit) {
            onGameClicked(it)
        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = gamedayAdapter
        }
        calendar = Calendar.getInstance()

        startCountdown()
        startDayChangeWatcher()
        loadViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Si volvemos a la app tras haber cruzado la medianoche en segundo plano,
        // refrescamos la jornada con la fecha actual.
        val games = viewModel.games.value
        if (_binding != null && !games.isNullOrEmpty()) {
            renderGames(games, viewModel.teams.value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Re-renderiza la jornada justo al cambiar de día. El [Flow] de juegos solo
     * re-emite ante cambios en Firestore, así que sin este disparador la vista
     * seguiría mostrando el día anterior hasta reabrir la app.
     */
    private fun startDayChangeWatcher() {
        dayWatcherJob?.cancel()
        dayWatcherJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive && _binding != null) {
                delay(millisUntilNextMidnight())
                if (_binding != null) {
                    renderGames(viewModel.games.value, viewModel.teams.value)
                }
            }
        }
    }

    private fun millisUntilNextMidnight(): Long {
        val now = Calendar.getInstance()
        val nextMidnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // +1s de margen para asegurar que ya estamos en el nuevo día al renderizar.
        return (nextMidnight.timeInMillis - now.timeInMillis + 1000L).coerceAtLeast(1000L)
    }

    private fun loadViewModel() {
        viewModel.infoApp.observe(viewLifecycleOwner) {
            if (_binding != null) {
                if (it.versionCode > BuildConfig.VERSION_CODE) {
                    showSheetUpdate()
                }
            }
        }
        viewModel.games.observe(viewLifecycleOwner) { gamesList ->
            renderGames(gamesList, viewModel.teams.value)
        }

        viewModel.teams.observe(viewLifecycleOwner) { teamsList ->
            renderGames(viewModel.games.value, teamsList)
        }
    }

    private fun startCountdown() {
        val target = getWorldCupStartInstant()

        countdownJob?.cancel()
        countdownJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive && _binding != null) {
                val remaining = Duration.between(Instant.now(), target)

                if (!remaining.isNegative && !remaining.isZero) {
                    binding.cardCountdown.visibility = View.VISIBLE
                    binding.tvCountdownValue.text = formatRemaining(remaining)
                } else {
                    binding.cardCountdown.visibility = View.GONE
                }

                delay(1000L)
            }
        }
    }

    private fun formatRemaining(d: Duration): String {
        val days = d.toDays()
        val hours = d.minusDays(days).toHours()
        val minutes = d.minusDays(days).minusHours(hours).toMinutes()
        val seconds = d.minusDays(days).minusHours(hours).minusMinutes(minutes).seconds

        return String.format("%d días · %02d:%02d:%02d", days, hours, minutes, seconds)
    }

    private fun renderGames(
        gamesList: List<Game>?,
        teamsList: List<Team>?
    ) {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE

        if (gamesList.isNullOrEmpty()) {
            binding.rvGames.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            return
        }

        if (teamsList.isNullOrEmpty()) {
            // Aún no han llegado los equipos, esperamos
            return
        }

        // Tomamos la primera jornada de hoy en adelante (getGamesAfter viene ordenado ASC).
        // Se evalúa contra la fecha actual —no contra el primer elemento de la lista—,
        // para que al cambiar de día (medianoche) se muestre la jornada correcta sin
        // tener que reabrir la app.
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val referenceDate = (gamesList.firstOrNull { !it.date.before(startOfToday) }
            ?: gamesList.last()).date
        val gamesSameDay = gamesList.filter { validateDates(referenceDate, it.date) }
        updateHeader(referenceDate)

        // Guardamos solo los juegos de esa jornada en la propiedad local
        gamesForDay.clear()
        gamesForDay.addAll(gamesSameDay)

        // Mapear Game + Team → GameToView
        val gamesToView = gamesSameDay.map { game ->
            val home = teamsList.firstOrNull { it.id == game.homeTeamId }
            val away = teamsList.firstOrNull { it.id == game.awayTeamId }

            val homeName = home?.shortName ?: ""
            val awayName = away?.shortName ?: ""

            GameToView(
                homeTeamName = homeName,
                awayTeamName = awayName,
                flag1Res = FlagsMapper.from(home?.flagCode),
                flag2Res = FlagsMapper.from(away?.flagCode),
                date = game.date,
                homeGoals = game.score?.homeGoals,
                awayGoals = game.score?.awayGoals,
                round = formatRound(game),
                number = game.matchNumber,
                points = 0, // si luego quieres mostrar puntos por predicción, se ajusta aquí,
                hasPrediction = false,
                stadiumName = game.venue.name,
                stadiumCity = game.venue.location
            )
        }

        gamedayAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun validateDates(firstDate: Date, secondDate: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = firstDate }
        val calendar2 = Calendar.getInstance().apply { time = secondDate }

        return calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
    }

    private fun updateHeader(referenceDate: Date) {
        val zone = ZoneId.systemDefault()
        val tournamentStart = getWorldCupStartInstant()
        val now = Instant.now()

        val today = now.atZone(zone).toLocalDate()
        val refLocalDate = referenceDate.toInstant().atZone(zone).toLocalDate()
        val hasStarted = now >= tournamentStart

        when {
            !hasStarted -> {
                binding.titleGameday.text = getString(R.string.title_gameday_pre_tournament)
                binding.subtitleGameday.text = getString(R.string.subtitle_gameday_pre_tournament)
            }
            refLocalDate == today -> {
                binding.titleGameday.text = getString(R.string.title_gameday_today)
                binding.subtitleGameday.text = getString(R.string.subtitle_gameday_today)
            }
            else -> {
                binding.titleGameday.text = getString(R.string.title_gameday_no_games_today)
                binding.subtitleGameday.text = getString(R.string.subtitle_gameday_next_games)
            }
        }
    }


    private fun showSheetUpdate() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.layout_sheet_update)
        bottomSheetDialog.dismissWithAnimation = true
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.show()
        val viewSheet: View? = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        val btnNotification: MaterialButton = viewSheet!!.findViewById(R.id.btn_update)
        btnNotification.setOnClickListener {
            val appPackageName = requireContext().packageName
            val url = "https://play.google.com/store/apps/details?id=$appPackageName"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            requireActivity().finishAffinity()
        }
    }

    private fun onGameClicked(gameToView: GameToView) {
        val setGame = gamesForDay.firstOrNull { it.matchNumber == gameToView.number }
        if (setGame != null) {
            viewModel.setGame(setGame)
            val editResultsDialog = EditResultsDialog()
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        }
    }

    private fun getWorldCupStartInstant(): Instant {
        return Instant.parse("2026-06-11T19:00:00Z")
    }
}