package com.skysam.hchirinos.mundialcatar.ui.predicts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.common.Common.formatRound
import com.skysam.hchirinos.mundialcatar.common.Constants
import com.skysam.hchirinos.mundialcatar.common.FlagsMapper
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPredictsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStage
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStatus
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.ui.commonView.EditResultsDialog
import java.util.Calendar

class PredictsFragment : Fragment() {
    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PredictsViewModel by activityViewModels()
    private lateinit var predictsAdapter: PredictsAdapter
    private var games = listOf<Game>()
    private var gamesUser = listOf<GamePredictionEntity>()
    private var teams = listOf<Team>()
    private var moveList = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictsAdapter = PredictsAdapter {
            updatePredict(it)
        }
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = predictsAdapter
        }

        loadViewModel()
    }

    private fun loadViewModel() {
        viewModel.gamesUser.observe(viewLifecycleOwner) {
            if (_binding != null) {
                gamesUser = it
                joinData()
            }
        }
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding != null) {
                games = it
                joinData()
            }
        }
        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding != null) {
                teams = it
                joinData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun joinData() {
        if (games.isEmpty() || teams.isEmpty()) return

        // Mapear para evitar bucles anidados O(n²)
        val teamsById = teams.associateBy { it.id }
        val predictionsByMatch = gamesUser.associateBy { it.matchNumber }

        val gamesToView = games.map { game ->
            val home = teamsById[game.homeTeamId]
            val away = teamsById[game.awayTeamId]

            val homeName = home?.shortName ?: ""
            val awayName = away?.shortName ?: ""

            val flag1 = FlagsMapper.from(home?.flagCode)
            val flag2 = FlagsMapper.from(away?.flagCode)

            val prediction = predictionsByMatch[game.matchNumber]

            val goals1 = prediction?.predictedHomeGoals ?: 0
            val goals2 = prediction?.predictedAwayGoals ?: 0
            val points = prediction?.points ?: 0
            val hasPrediction = prediction != null

            GameToView(
                homeTeamName = homeName,
                awayTeamName = awayName,
                flag1Res = flag1,
                flag2Res = flag2,
                date = game.date,
                homeGoals = goals1,
                awayGoals = goals2,
                round = formatRound(game),
                number = game.matchNumber,
                points = points,
                hasPrediction = hasPrediction,
                stadiumName = game.venue.name,
                stadiumCity = game.venue.location
            )
        }

        fillData(gamesToView)
    }


    private fun fillData(gamesToView: List<GameToView>) {
        predictsAdapter.updateList(gamesToView)
        binding.rvGames.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (moveList) {
            val index = gamesToView.indexOfFirst { it.date.after(calendar.time) }
            if (index != -1) {
                binding.rvGames.scrollToPosition(index)
                moveList = false
            }
        }
    }

    private fun updatePredict(gameToView: GameToView) {
        // Buscar el Game real por matchNumber
        val game = games.firstOrNull { it.matchNumber == gameToView.number }

        if (game == null) {
            Snackbar.make(binding.coordinator, "Error interno: juego no encontrado", Snackbar.LENGTH_SHORT).show()
            return
        }

        val now = Calendar.getInstance()

        val cutoff = Calendar.getInstance().apply {
            time = game.date
            add(Calendar.MINUTE, -10) // límite de edición 10 minutos antes
        }

        val gameAlreadyStarted = game.status != MatchStatus.SCHEDULED

        val canEdit = now.time.before(cutoff.time) && !gameAlreadyStarted

        if (canEdit) {
            viewModel.editPredict(gameToView)
            val editResultsDialog = EditResultsDialog(false)
            editResultsDialog.show(requireActivity().supportFragmentManager, tag)
        } else {
            Snackbar.make(
                binding.coordinator,
                "Juego iniciado o fuera de tiempo. No puede crear/editar predicción",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}