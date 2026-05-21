package com.skysam.hchirinos.mundial2026.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.skysam.hchirinos.mundial2026.R
import com.skysam.hchirinos.mundial2026.common.Common
import com.skysam.hchirinos.mundial2026.common.Common.formatRound
import com.skysam.hchirinos.mundial2026.common.FlagsMapper
import com.skysam.hchirinos.mundial2026.databinding.FragmentBrowseByDateBinding
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.GameToView
import com.skysam.hchirinos.mundial2026.dataclass.Team
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

@AndroidEntryPoint
class BrowseByDateFragment : Fragment() {

    private var _binding: FragmentBrowseByDateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BrowseByDateViewModel by viewModels()
    private lateinit var browseAdapter: BrowseByDateAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseByDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        browseAdapter = BrowseByDateAdapter()
        binding.rvGames.apply {
            setHasFixedSize(true)
            adapter = browseAdapter
        }

        binding.btnBack.setOnClickListener {
            val popped = findNavController().popBackStack(R.id.navigation_extras, false)
            if (!popped) {
                findNavController().navigate(R.id.navigation_extras)
            }
        }
        binding.btnPickDate.setOnClickListener { showDatePicker() }

        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (date == null) {
                binding.tvSelectedDate.text = getString(R.string.text_no_date_selected)
            } else {
                binding.tvSelectedDate.text = Common.convertDateToString(date)
            }
            renderState()
        }
        viewModel.items.observe(viewLifecycleOwner) {
            renderItems(it, viewModel.teams.value)
        }
        viewModel.teams.observe(viewLifecycleOwner) {
            renderItems(viewModel.items.value, it)
        }
    }

    private fun showDatePicker() {
        val zone = ZoneId.systemDefault()

        val startMillis = localDateToUtcMillis(TOURNAMENT_START)
        val endMillis = localDateToUtcMillis(TOURNAMENT_END)

        val constraints = CalendarConstraints.Builder()
            .setStart(startMillis)
            .setEnd(endMillis)
            .setValidator(
                CompositeDateValidator.allOf(
                    listOf(
                        DateValidatorPointForward.from(startMillis),
                        DateValidatorPointBackward.before(endMillis)
                    )
                )
            )
            .build()

        val current = viewModel.selectedDate.value
            ?.let { localDateToUtcMillis(dateToLocalDate(it, zone)) }
            ?: startMillis

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.text_pick_date)
            .setCalendarConstraints(constraints)
            .setSelection(current)
            .build()

        picker.addOnPositiveButtonClickListener { utcMillis ->
            // MaterialDatePicker devuelve milisegundos UTC para la medianoche del día
            // seleccionado. Reinterpretamos ese día como medianoche en la zona local
            // para que el filtro coincida con el calendario del usuario.
            val pickedLocalDate = utcMillisToLocalDate(utcMillis)
            val localMidnight = Date.from(pickedLocalDate.atStartOfDay(zone).toInstant())
            viewModel.setSelectedDate(localMidnight)
        }
        picker.show(parentFragmentManager, "browse_date_picker")
    }

    private fun localDateToUtcMillis(localDate: LocalDate): Long =
        localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun utcMillisToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    private fun dateToLocalDate(date: Date, zone: ZoneId): LocalDate =
        date.toInstant().atZone(zone).toLocalDate()

    private companion object {
        val TOURNAMENT_START: LocalDate = LocalDate.of(2026, 6, 11)
        val TOURNAMENT_END: LocalDate = LocalDate.of(2026, 7, 19)
    }

    private fun renderItems(
        items: List<BrowseByDateItem>?,
        teamsList: List<Team>?
    ) {
        if (_binding == null) return
        if (viewModel.selectedDate.value == null) {
            renderState()
            return
        }

        // Si la lista incluye al menos un GameItem necesitamos teams para mapear
        // banderas y nombres. Si solo hay slots, podemos renderizar sin teams.
        val hasGameItems = items.orEmpty().any { it is BrowseByDateItem.GameItem }
        if (hasGameItems && teamsList.isNullOrEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            return
        }

        val rows = items.orEmpty().map { item ->
            when (item) {
                is BrowseByDateItem.GameItem ->
                    BrowseByDateAdapter.Row.GameRow(
                        mapGameToView(item.game, teamsList.orEmpty())
                    )
                is BrowseByDateItem.SlotItem ->
                    BrowseByDateAdapter.Row.SlotRow(item.slot)
            }
        }

        browseAdapter.updateList(rows)
        renderState()
    }

    private fun mapGameToView(game: Game, teams: List<Team>): GameToView {
        val home = teams.firstOrNull { it.id == game.homeTeamId }
        val away = teams.firstOrNull { it.id == game.awayTeamId }
        return GameToView(
            homeTeamName = home?.shortName ?: "",
            awayTeamName = away?.shortName ?: "",
            flag1Res = FlagsMapper.from(home?.flagCode),
            flag2Res = FlagsMapper.from(away?.flagCode),
            date = game.date,
            homeGoals = game.score?.homeGoals,
            awayGoals = game.score?.awayGoals,
            round = formatRound(game),
            number = game.matchNumber,
            points = 0,
            hasPrediction = false,
            stadiumName = game.venue.name,
            stadiumCity = game.venue.location
        )
    }

    private fun renderState() {
        if (_binding == null) return

        val hasDate = viewModel.selectedDate.value != null
        val items = viewModel.items.value.orEmpty()
        val needsTeams = items.any { it is BrowseByDateItem.GameItem }
        val teamsReady = !needsTeams || !viewModel.teams.value.isNullOrEmpty()

        when {
            !hasDate -> {
                binding.rvGames.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
            !teamsReady -> {
                binding.rvGames.visibility = View.GONE
                binding.emptyState.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            items.isEmpty() -> {
                binding.rvGames.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
            else -> {
                binding.rvGames.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
