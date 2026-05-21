package com.skysam.hchirinos.mundial2026.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.skysam.hchirinos.mundial2026.dataclass.Game
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlot
import com.skysam.hchirinos.mundial2026.dataclass.Team
import com.skysam.hchirinos.mundial2026.repositories.GamesRepository
import com.skysam.hchirinos.mundial2026.repositories.MatchScheduleSlotsRepository
import com.skysam.hchirinos.mundial2026.repositories.TeamsRespository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class BrowseByDateViewModel @Inject constructor(
    private val gamesRepository: GamesRepository,
    private val slotsRepository: MatchScheduleSlotsRepository,
    private val teamsRespository: TeamsRespository
) : ViewModel() {

    private val _selectedDate = MutableLiveData<Date?>(null)
    val selectedDate: LiveData<Date?> get() = _selectedDate

    /**
     * Lista combinada de Games reales + Slots oficiales para la fecha
     * seleccionada. Si un slot comparte matchNumber con un Game presente en
     * el mismo día, el slot se descarta (gana el Game real).
     */
    val items: LiveData<List<BrowseByDateItem>> = _selectedDate.switchMap { date ->
        if (date == null) {
            flowOf(emptyList<BrowseByDateItem>()).asLiveData()
        } else {
            gamesRepository.getGamesByDate(date)
                .combine(slotsRepository.getSlotsByDate(date)) { games, slots ->
                    merge(games, slots)
                }
                .asLiveData()
        }
    }

    val teams: LiveData<List<Team>> = teamsRespository.getAllTeams().asLiveData()

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    private fun merge(
        games: List<Game>,
        slots: List<MatchScheduleSlot>
    ): List<BrowseByDateItem> {
        val gameNumbers = games.map { it.matchNumber }.toSet()
        val visibleSlots = slots.filter { it.matchNumber !in gameNumbers }

        val all: List<BrowseByDateItem> =
            games.map { BrowseByDateItem.GameItem(it) } +
                visibleSlots.map { BrowseByDateItem.SlotItem(it) }

        return all.sortedWith(
            compareBy(
                { item ->
                    when (item) {
                        is BrowseByDateItem.GameItem -> item.game.date
                        is BrowseByDateItem.SlotItem -> item.slot.date
                    }
                },
                { item ->
                    when (item) {
                        is BrowseByDateItem.GameItem -> item.game.matchNumber
                        is BrowseByDateItem.SlotItem -> item.slot.matchNumber
                    }
                }
            )
        )
    }
}
