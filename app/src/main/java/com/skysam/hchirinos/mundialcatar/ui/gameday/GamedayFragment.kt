package com.skysam.hchirinos.mundialcatar.ui.gameday

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
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common.formatRound
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.FlagsMapper
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
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
        loadViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        val zone = ZoneId.systemDefault()
        val target = ZonedDateTime.of(2026, 6, 11, 14, 0,0,0, zone).toInstant()

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

        // Tomamos la fecha de la primera jornada futura (getGamesAfter ya viene ordenado ASC)
        val referenceDate = gamesList.first().date
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
                homeGoals = game.score?.homeGoals ?: 0,
                awayGoals = game.score?.awayGoals ?: 0,
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
        val tournamentStart = ZonedDateTime.of(2026, 6, 11, 14, 0, 0, 0, zone).toInstant()
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
            val editResultsDialog = EditResultsDialog(true)
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        }
    }
}