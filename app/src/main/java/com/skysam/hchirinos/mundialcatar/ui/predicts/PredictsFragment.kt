package com.skysam.hchirinos.mundialcatar.ui.predicts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.skysam.hchirinos.mundialcatar.common.Common.formatRound
import com.skysam.hchirinos.mundialcatar.common.FlagsMapper
import com.skysam.hchirinos.mundialcatar.databinding.FragmentPredictsBinding
import com.skysam.hchirinos.mundialcatar.dataclass.Game
import com.skysam.hchirinos.mundialcatar.dataclass.GamePredictionEntity
import com.skysam.hchirinos.mundialcatar.dataclass.GameToView
import com.skysam.hchirinos.mundialcatar.dataclass.MatchStatus
import com.skysam.hchirinos.mundialcatar.dataclass.Team
import com.skysam.hchirinos.mundialcatar.repositories.Auth
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class PredictsFragment : Fragment() {

    private var _binding: FragmentPredictsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PredictsViewModel by activityViewModels()

    @Inject lateinit var auth: Auth

    private lateinit var predictsAdapter: PredictsAdapter

    private var games = listOf<Game>()
    private var gamesUser = listOf<GamePredictionEntity>()
    private var teams = listOf<Team>()
    private var lastDraftMap: Map<Int, Pair<Int, Int>> = emptyMap()
    private var lastSyncMap: Map<Int, PredictsAdapter.SyncUiState> = emptyMap()
    private var canEditMap: Map<Int, Boolean> = emptyMap()
    private val editabilityHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var moveList = true

    private val editabilityRunnable = object : Runnable {
        override fun run() {
            if (_binding == null) return

            if (games.isNotEmpty()) {
                recalcEditabilityAndUpdateAdapter()
            }

            editabilityHandler.postDelayed(this, 30_000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        predictsAdapter = PredictsAdapter(
            onGameClick = { onCardClick(it) },
            onDraftChange = { matchNumber, home, away ->
                viewModel.setDraft(matchNumber, home, away)
            },
            onDraftClear = { matchNumber ->
                viewModel.clearDraft(matchNumber)
            },
            onSaveClick = { matchNumber ->
                onSavePrediction(matchNumber)
            }
        )

        (binding.rvGames.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)
            ?.supportsChangeAnimations = false

        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = predictsAdapter
        }

        loadViewModel()
    }

    private fun loadViewModel() {
        viewModel.gamesUser.observe(viewLifecycleOwner) {
            if (_binding == null) return@observe
            gamesUser = it
            viewModel.setPersistedPredictions(it)
            joinData()
        }
        viewModel.games.observe(viewLifecycleOwner) {
            if (_binding == null) return@observe
            games = it
            joinData()
        }
        viewModel.teams.observe(viewLifecycleOwner) {
            if (_binding == null) return@observe
            teams = it
            joinData()
        }
        viewModel.uiMessage.observe(viewLifecycleOwner) { msg ->
            if (_binding == null) return@observe
            if (msg.isNullOrBlank()) return@observe

            Snackbar.make(binding.coordinator, msg, Snackbar.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
        viewModel.draftScores.observe(viewLifecycleOwner) { newMap ->
            predictsAdapter.setDraftCache(newMap)

            val oldMap = lastDraftMap
            lastDraftMap = newMap

            val changedKeys = (oldMap.keys + newMap.keys).filter { oldMap[it] != newMap[it] }
            changedKeys.forEach { matchNumber ->
                predictsAdapter.notifyDraftChanged(matchNumber)
            }
        }
        viewModel.syncStates.observe(viewLifecycleOwner) { newMap ->
            if (_binding == null) return@observe
            predictsAdapter.setSyncStateMap(newMap)

            val oldMap = lastSyncMap
            lastSyncMap = newMap

            val changedKeys = (oldMap.keys + newMap.keys).filter { oldMap[it] != newMap[it] }
            changedKeys.forEach { matchNumber ->
                predictsAdapter.notifySyncChanged(matchNumber)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        editabilityHandler.post(editabilityRunnable)
    }

    override fun onPause() {
        super.onPause()
        editabilityHandler.removeCallbacks(editabilityRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        editabilityHandler.removeCallbacks(editabilityRunnable)
        _binding = null
    }

    private fun joinData() {
        if (games.isEmpty() || teams.isEmpty()) return

        val teamsById = teams.associateBy { it.id }
        val predictionsByMatch = gamesUser.associateBy { it.matchNumber }

        val now = Calendar.getInstance()

        canEditMap = games.associate { game ->
            val cutoff = Calendar.getInstance().apply {
                time = game.date
                add(Calendar.MINUTE, -10)
            }
            val gameAlreadyStarted = game.status != MatchStatus.SCHEDULED
            val canEdit = now.time.before(cutoff.time) && !gameAlreadyStarted
            game.matchNumber to canEdit
        }

        val gamesToView = games.map { game ->
            val home = teamsById[game.homeTeamId]
            val away = teamsById[game.awayTeamId]

            val homeName = home?.shortName.orEmpty()
            val awayName = away?.shortName.orEmpty()

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

        fillData(gamesToView, canEditMap)
    }

    private fun fillData(gamesToView: List<GameToView>, canEditMap: Map<Int, Boolean>) {
        predictsAdapter.updateList(gamesToView)
        predictsAdapter.updateEditabilityMap(canEditMap)

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

    private fun onCardClick(gameToView: GameToView) {
        val canEdit = canEditMap[gameToView.number] ?: true
        if (!canEdit) {
            Snackbar.make(
                binding.coordinator,
                "Predicción bloqueada. El juego está por iniciar o ya inició.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun onSavePrediction(matchNumber: Int) {
        val game = games.firstOrNull { it.matchNumber == matchNumber }
        if (game == null) {
            Snackbar.make(binding.coordinator, "Error interno: juego no encontrado", Snackbar.LENGTH_SHORT).show()
            return
        }

        val canEdit = canEditMap[matchNumber] ?: true
        if (!canEdit) {
            Snackbar.make(binding.coordinator, "Predicción bloqueada. No se puede guardar.", Snackbar.LENGTH_SHORT).show()
            return
        }

        val userId = auth.getCurrentUser()?.uid
        if (userId.isNullOrBlank()) {
            Snackbar.make(binding.coordinator, "Sesión no válida. Inicia sesión nuevamente.", Snackbar.LENGTH_SHORT).show()
            return
        }

        viewModel.saveDraft(
            matchNumber = matchNumber,
            userId = userId,
            gameId = game.id,
            tournamentId = game.tournamentId
        )
    }

    private fun recalcEditabilityAndUpdateAdapter() {
        val now = Calendar.getInstance()

        val newMap = games.associate { game ->
            val cutoff = Calendar.getInstance().apply {
                time = game.date
                add(Calendar.MINUTE, -10)
            }
            val gameAlreadyStarted = game.status != MatchStatus.SCHEDULED
            val canEdit = now.time.before(cutoff.time) && !gameAlreadyStarted
            game.matchNumber to canEdit
        }

        if (newMap == canEditMap) return

        canEditMap = newMap
        predictsAdapter.updateEditabilityMap(canEditMap)
    }
}
