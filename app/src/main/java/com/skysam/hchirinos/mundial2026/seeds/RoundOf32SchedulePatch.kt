package com.skysam.hchirinos.mundial2026.seeds

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class RoundOf32GamePatch(
    val matchNumber: Long,
    val dateTimeVenezuela: String,
    val date: Timestamp,
    val venueId: String,
    val venueName: String,
    val venueLocation: String,
    val canPredicted: Boolean = true,
    val status: String = "SCHEDULED"
) {
    fun toFirestoreUpdateMap(): Map<String, Any> {
        return mapOf(
            "date" to date,
            "venueId" to venueId,
            "venueName" to venueName,
            "venueLocation" to venueLocation,
            "canPredicted" to canPredicted,
            "status" to status,
            "patchVersion" to RoundOf32SchedulePatch.PATCH_VERSION
        )
    }
}

object RoundOf32SchedulePatch {

    const val PATCH_VERSION = "round_of_32_schedule_patch_v1"

    private const val VENEZUELA_TIME_ZONE = "America/Caracas"

    val games: List<RoundOf32GamePatch> = listOf(
        game(
            matchNumber = 73,
            dateTimeVenezuela = "2026-06-28 15:00",
            venueId = "LOS_ANGELES_SOFI_STADIUM",
            venueName = "Los Angeles Stadium",
            venueLocation = "Los Angeles, United States"
        ),
        game(
            matchNumber = 74,
            dateTimeVenezuela = "2026-06-29 16:30",
            venueId = "BOSTON_GILLETTE_STADIUM",
            venueName = "Boston Stadium",
            venueLocation = "Boston, United States"
        ),
        game(
            matchNumber = 75,
            dateTimeVenezuela = "2026-06-29 21:00",
            venueId = "MONTERREY_BBVA",
            venueName = "Monterrey Stadium",
            venueLocation = "Monterrey, México"
        ),
        game(
            matchNumber = 76,
            dateTimeVenezuela = "2026-06-29 13:00",
            venueId = "HOUSTON_NRG_STADIUM",
            venueName = "Houston Stadium",
            venueLocation = "Houston, United States"
        ),
        game(
            matchNumber = 77,
            dateTimeVenezuela = "2026-06-30 17:00",
            venueId = "EAST_RUTHERFORD_METLIFE_STADIUM",
            venueName = "New York/New Jersey Stadium",
            venueLocation = "New Jersey, United States"
        ),
        game(
            matchNumber = 78,
            dateTimeVenezuela = "2026-06-30 13:00",
            venueId = "ARLINGTON_ATT_STADIUM",
            venueName = "Dallas Stadium",
            venueLocation = "Dallas, United States"
        ),
        game(
            matchNumber = 79,
            dateTimeVenezuela = "2026-06-30 21:00",
            venueId = "MEXICO_CITY_AZTECA",
            venueName = "Estadio Azteca",
            venueLocation = "Ciudad de México, México"
        ),
        game(
            matchNumber = 80,
            dateTimeVenezuela = "2026-07-01 12:00",
            venueId = "ATLANTA_MERCEDES_BENZ",
            venueName = "Atlanta Stadium",
            venueLocation = "Atlanta, United States"
        ),
        game(
            matchNumber = 81,
            dateTimeVenezuela = "2026-07-01 20:00",
            venueId = "SANTA_CLARA_LEVIS",
            venueName = "San Francisco Bay Area Stadium",
            venueLocation = "Santa Clara, United States"
        ),
        game(
            matchNumber = 82,
            dateTimeVenezuela = "2026-07-01 16:00",
            venueId = "SEATTLE_LUMEN_FIELD",
            venueName = "Seattle Stadium",
            venueLocation = "Seattle, United States"
        ),
        game(
            matchNumber = 83,
            dateTimeVenezuela = "2026-07-02 19:00",
            venueId = "TORONTO_BMO_FIELD",
            venueName = "Toronto Stadium",
            venueLocation = "Toronto, Canada"
        ),
        game(
            matchNumber = 84,
            dateTimeVenezuela = "2026-07-02 15:00",
            venueId = "LOS_ANGELES_SOFI_STADIUM",
            venueName = "Los Angeles Stadium",
            venueLocation = "Los Angeles, United States"
        ),
        game(
            matchNumber = 85,
            dateTimeVenezuela = "2026-07-02 23:00",
            venueId = "VANCOUVER_BC_PLACE",
            venueName = "BC Place",
            venueLocation = "Vancouver, Canada"
        ),
        game(
            matchNumber = 86,
            dateTimeVenezuela = "2026-07-03 18:00",
            venueId = "MIAMI_HARD_ROCK",
            venueName = "Miami Stadium",
            venueLocation = "Miami, United States"
        ),
        game(
            matchNumber = 87,
            dateTimeVenezuela = "2026-07-03 21:30",
            venueId = "KANSAS_CITY_ARROWHEAD_STADIUM",
            venueName = "Kansas City Stadium",
            venueLocation = "Kansas City, United States"
        ),
        game(
            matchNumber = 88,
            dateTimeVenezuela = "2026-07-03 14:00",
            venueId = "ARLINGTON_ATT_STADIUM",
            venueName = "Dallas Stadium",
            venueLocation = "Dallas, United States"
        )
    )

    private fun game(
        matchNumber: Long,
        dateTimeVenezuela: String,
        venueId: String,
        venueName: String,
        venueLocation: String,
        canPredicted: Boolean = true,
        status: String = "SCHEDULED"
    ): RoundOf32GamePatch {
        return RoundOf32GamePatch(
            matchNumber = matchNumber,
            dateTimeVenezuela = dateTimeVenezuela,
            date = venezuelaTimestamp(dateTimeVenezuela),
            venueId = venueId,
            venueName = venueName,
            venueLocation = venueLocation,
            canPredicted = canPredicted,
            status = status
        )
    }

    private fun venezuelaTimestamp(dateTime: String): Timestamp {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(VENEZUELA_TIME_ZONE)
            isLenient = false
        }

        val date = requireNotNull(formatter.parse(dateTime)) {
            "Invalid Venezuela dateTime: $dateTime"
        }

        return Timestamp(date)
    }
}