package com.skysam.hchirinos.mundialcatar.ui.gameday

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.skysam.hchirinos.mundialcatar.BuildConfig
import com.skysam.hchirinos.mundialcatar.R
import com.skysam.hchirinos.mundialcatar.common.Common
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.databinding.FragmentGamedayBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import dagger.hilt.android.AndroidEntryPoint
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

        loadViewModel()

        viewModel.seedWorldCup2026IfNeeded()
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

        // Guardamos solo los juegos de esa jornada en la propiedad local
        gamesForDay.clear()
        gamesForDay.addAll(gamesSameDay)

        // Título: ¿la jornada es hoy?
        val todayStr = Common.convertDateToString(calendar.time)
        val refStr = Common.convertDateToString(referenceDate)
        binding.titleGameday.text = if (todayStr == refStr) {
            getString(R.string.title_gameday_yes)
        } else {
            getString(R.string.title_gameday_no)
        }

        // Mapear Game + Team → GameToView
        val gamesToView = gamesSameDay.map { game ->
            val home = teamsList.firstOrNull { it.id == game.homeTeamId }
            val away = teamsList.firstOrNull { it.id == game.awayTeamId }

            val homeName = home?.shortName ?: ""
            val awayName = away?.shortName ?: ""

            GameToView(
                homeTeamName = homeName,
                awayTeamName = awayName,
                flag1 = home?.flagCode?.toFlagUrl() ?: "",
                flag2 = away?.flagCode?.toFlagUrl() ?: "",
                date = game.date,
                homeGoals = game.score?.homeGoals ?: 0,
                awayGoals = game.score?.awayGoals ?: 0,
                round = formatRound(game),
                number = game.matchNumber,
                points = 0, // si luego quieres mostrar puntos por predicción, se ajusta aquí,
                hasPrediction = false
            )
        }

        gamedayAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun formatRound(game: Game): String =
        when (game.stage) {
            MatchStage.GROUP -> when (game.group) {
                "A" -> Constants.GROUP_A
                "B" -> Constants.GROUP_B
                "C" -> Constants.GROUP_C
                "D" -> Constants.GROUP_D
                "E" -> Constants.GROUP_E
                "F" -> Constants.GROUP_F
                "G" -> Constants.GROUP_G
                "H" -> Constants.GROUP_H
                "I" -> Constants.GROUP_I
                "J" -> Constants.GROUP_J
                "K" -> Constants.GROUP_K
                "L" -> Constants.GROUP_L
                else -> "Fase de grupos"
            }
            MatchStage.ROUND_OF_32 -> Constants.ROUND_OF_32
            MatchStage.ROUND_OF_16 -> Constants.ROUND_OF_16
            MatchStage.QUARTER_FINAL -> Constants.ROUND_OF_8
            MatchStage.SEMI_FINAL -> Constants.SEMIFINAL
            MatchStage.THIRD_PLACE -> Constants.THIRD_PLACE
            MatchStage.FINAL -> Constants.FINAL
        }

    private fun String.toFlagUrl(): String {
        // TODO: reemplazar por la URL real de tus banderas
        // Ejemplo:
        // return "https://firebasestorage.googleapis.com/v0/b/tu-bucket/o/flags%2F$this.png?alt=media"
        return this
    }

    private fun validateDates(firstDate: Date, secondDate: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = firstDate }
        val calendar2 = Calendar.getInstance().apply { time = secondDate }

        return calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
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