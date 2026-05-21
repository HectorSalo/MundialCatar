package com.skysam.hchirinos.mundial2026.common

import com.google.firebase.Timestamp
import com.skysam.hchirinos.mundial2026.dataclass.MatchScheduleSlotEntity
import com.skysam.hchirinos.mundial2026.dataclass.MatchStage
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Slots oficiales del calendario de eliminatorias del Mundial 2026.
 *
 * Rango: matchNumber 73..104 (32 slots).
 *  - 73..88  ROUND_OF_32   (16 slots)
 *  - 89..96  ROUND_OF_16   ( 8 slots)
 *  - 97..100 QUARTER_FINAL ( 4 slots)
 *  - 101..102 SEMI_FINAL   ( 2 slots)
 *  - 103     THIRD_PLACE   ( 1 slot)
 *  - 104     FINAL         ( 1 slot)
 *
 * NO se siembra fase de grupos: eso vive en `games`.
 * NO se guardan equipos ni placeholders de equipos.
 *
 * Conversión de hora local del estadio a Timestamp Firestore:
 *   - Cada [SlotSeed] declara su propia [ZoneId] (zona horaria de la sede).
 *   - [toEntity] hace `localDateTime.atZone(zoneId).toInstant()` y envuelve el
 *     resultado en `com.google.firebase.Timestamp(seconds, nanos)`. Así la
 *     `date` queda en Firestore como Timestamp nativo, NUNCA como String, y
 *     representa el instante absoluto correcto sin depender de la zona del
 *     dispositivo que ejecute el seed.
 *
 * Fuente del calendario (fecha/hora local + sede):
 *   - Roadtrips, tabla completa de calendario Mundial 2026 (líneas 73..104).
 *   - Validación cruzada con CBS (bracket por número de partido y sede).
 */
object WorldCup2026MatchScheduleSlots {

    data class SlotSeed(
        val matchNumber: Int,
        val localDateTime: LocalDateTime,
        val zoneId: ZoneId,
        val stage: MatchStage,
        val venueId: String,
        val venueName: String,
        val venueLocation: String,
        val title: String
    )

    // Zonas horarias por sede.
    private val ZONE_LOS_ANGELES = ZoneId.of("America/Los_Angeles")
    private val ZONE_NEW_YORK = ZoneId.of("America/New_York")
    private val ZONE_MONTERREY = ZoneId.of("America/Monterrey")
    private val ZONE_CHICAGO = ZoneId.of("America/Chicago")
    private val ZONE_MEXICO_CITY = ZoneId.of("America/Mexico_City")
    private val ZONE_TORONTO = ZoneId.of("America/Toronto")
    private val ZONE_VANCOUVER = ZoneId.of("America/Vancouver")

    // Títulos por fase.
    private const val TITLE_R32 = "Ronda de 32"
    private const val TITLE_R16 = "Octavos de final"
    private const val TITLE_QF = "Cuartos de final"
    private const val TITLE_SF = "Semifinal"
    private const val TITLE_THIRD_PLACE = "Tercer lugar"
    private const val TITLE_FINAL = "Final del Mundial"

    val eliminationSlots: List<SlotSeed> = listOf(
        // ---- Round of 32 (73..88) ----
        SlotSeed(
            matchNumber = 73,
            localDateTime = LocalDateTime.of(2026, 6, 28, 12, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.ROUND_OF_32,
            venueId = "LOS_ANGELES_SOFI_STADIUM",
            venueName = "SoFi Stadium",
            venueLocation = "Los Angeles, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 74,
            localDateTime = LocalDateTime.of(2026, 6, 29, 16, 30),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_32,
            venueId = "BOSTON_GILLETTE_STADIUM",
            venueName = "Gillette Stadium",
            venueLocation = "Boston, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 75,
            localDateTime = LocalDateTime.of(2026, 6, 29, 19, 0),
            zoneId = ZONE_MONTERREY,
            stage = MatchStage.ROUND_OF_32,
            venueId = "MONTERREY_ESTADIO_BBVA",
            venueName = "Estadio BBVA",
            venueLocation = "Monterrey, Mexico",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 76,
            localDateTime = LocalDateTime.of(2026, 6, 29, 12, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_32,
            venueId = "HOUSTON_NRG_STADIUM",
            venueName = "NRG Stadium",
            venueLocation = "Houston, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 77,
            localDateTime = LocalDateTime.of(2026, 6, 30, 17, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_32,
            venueId = "EAST_RUTHERFORD_METLIFE_STADIUM",
            venueName = "MetLife Stadium",
            venueLocation = "New York/New Jersey, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 78,
            localDateTime = LocalDateTime.of(2026, 6, 30, 12, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_32,
            venueId = "DALLAS_ATT_STADIUM",
            venueName = "AT&T Stadium",
            venueLocation = "Dallas, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 79,
            localDateTime = LocalDateTime.of(2026, 6, 30, 19, 0),
            zoneId = ZONE_MEXICO_CITY,
            stage = MatchStage.ROUND_OF_32,
            venueId = "MEXICO_CITY_ESTADIO_AZTECA",
            venueName = "Estadio Azteca",
            venueLocation = "Mexico City, Mexico",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 80,
            localDateTime = LocalDateTime.of(2026, 7, 1, 12, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_32,
            venueId = "ATLANTA_MERCEDES_BENZ_STADIUM",
            venueName = "Mercedes-Benz Stadium",
            venueLocation = "Atlanta, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 81,
            localDateTime = LocalDateTime.of(2026, 7, 1, 17, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.ROUND_OF_32,
            venueId = "SAN_FRANCISCO_LEVIS_STADIUM",
            venueName = "Levi's Stadium",
            venueLocation = "San Francisco Bay Area, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 82,
            localDateTime = LocalDateTime.of(2026, 7, 1, 13, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.ROUND_OF_32,
            venueId = "SEATTLE_LUMEN_FIELD",
            venueName = "Lumen Field",
            venueLocation = "Seattle, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 83,
            localDateTime = LocalDateTime.of(2026, 7, 2, 19, 0),
            zoneId = ZONE_TORONTO,
            stage = MatchStage.ROUND_OF_32,
            venueId = "TORONTO_BMO_FIELD",
            venueName = "BMO Field",
            venueLocation = "Toronto, Canada",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 84,
            localDateTime = LocalDateTime.of(2026, 7, 2, 12, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.ROUND_OF_32,
            venueId = "LOS_ANGELES_SOFI_STADIUM",
            venueName = "SoFi Stadium",
            venueLocation = "Los Angeles, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 85,
            localDateTime = LocalDateTime.of(2026, 7, 2, 20, 0),
            zoneId = ZONE_VANCOUVER,
            stage = MatchStage.ROUND_OF_32,
            venueId = "VANCOUVER_BC_PLACE",
            venueName = "BC Place",
            venueLocation = "Vancouver, Canada",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 86,
            localDateTime = LocalDateTime.of(2026, 7, 3, 18, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_32,
            venueId = "MIAMI_GARDENS_HARD_ROCK_STADIUM",
            venueName = "Hard Rock Stadium",
            venueLocation = "Miami, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 87,
            localDateTime = LocalDateTime.of(2026, 7, 3, 20, 30),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_32,
            venueId = "KANSAS_CITY_ARROWHEAD_STADIUM",
            venueName = "Arrowhead Stadium",
            venueLocation = "Kansas City, United States",
            title = TITLE_R32
        ),
        SlotSeed(
            matchNumber = 88,
            localDateTime = LocalDateTime.of(2026, 7, 3, 13, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_32,
            venueId = "DALLAS_ATT_STADIUM",
            venueName = "AT&T Stadium",
            venueLocation = "Dallas, United States",
            title = TITLE_R32
        ),

        // ---- Round of 16 (89..96) ----
        SlotSeed(
            matchNumber = 89,
            localDateTime = LocalDateTime.of(2026, 7, 4, 17, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_16,
            venueId = "PHILADELPHIA_LINCOLN_FINANCIAL_FIELD",
            venueName = "Lincoln Financial Field",
            venueLocation = "Philadelphia, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 90,
            localDateTime = LocalDateTime.of(2026, 7, 4, 12, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_16,
            venueId = "HOUSTON_NRG_STADIUM",
            venueName = "NRG Stadium",
            venueLocation = "Houston, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 91,
            localDateTime = LocalDateTime.of(2026, 7, 5, 16, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_16,
            venueId = "EAST_RUTHERFORD_METLIFE_STADIUM",
            venueName = "MetLife Stadium",
            venueLocation = "New York/New Jersey, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 92,
            localDateTime = LocalDateTime.of(2026, 7, 5, 18, 0),
            zoneId = ZONE_MEXICO_CITY,
            stage = MatchStage.ROUND_OF_16,
            venueId = "MEXICO_CITY_ESTADIO_AZTECA",
            venueName = "Estadio Azteca",
            venueLocation = "Mexico City, Mexico",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 93,
            localDateTime = LocalDateTime.of(2026, 7, 6, 14, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.ROUND_OF_16,
            venueId = "DALLAS_ATT_STADIUM",
            venueName = "AT&T Stadium",
            venueLocation = "Dallas, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 94,
            localDateTime = LocalDateTime.of(2026, 7, 6, 17, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.ROUND_OF_16,
            venueId = "SEATTLE_LUMEN_FIELD",
            venueName = "Lumen Field",
            venueLocation = "Seattle, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 95,
            localDateTime = LocalDateTime.of(2026, 7, 7, 12, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.ROUND_OF_16,
            venueId = "ATLANTA_MERCEDES_BENZ_STADIUM",
            venueName = "Mercedes-Benz Stadium",
            venueLocation = "Atlanta, United States",
            title = TITLE_R16
        ),
        SlotSeed(
            matchNumber = 96,
            localDateTime = LocalDateTime.of(2026, 7, 7, 13, 0),
            zoneId = ZONE_VANCOUVER,
            stage = MatchStage.ROUND_OF_16,
            venueId = "VANCOUVER_BC_PLACE",
            venueName = "BC Place",
            venueLocation = "Vancouver, Canada",
            title = TITLE_R16
        ),

        // ---- Quarter-finals (97..100) ----
        SlotSeed(
            matchNumber = 97,
            localDateTime = LocalDateTime.of(2026, 7, 9, 16, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.QUARTER_FINAL,
            venueId = "BOSTON_GILLETTE_STADIUM",
            venueName = "Gillette Stadium",
            venueLocation = "Boston, United States",
            title = TITLE_QF
        ),
        SlotSeed(
            matchNumber = 98,
            localDateTime = LocalDateTime.of(2026, 7, 10, 12, 0),
            zoneId = ZONE_LOS_ANGELES,
            stage = MatchStage.QUARTER_FINAL,
            venueId = "LOS_ANGELES_SOFI_STADIUM",
            venueName = "SoFi Stadium",
            venueLocation = "Los Angeles, United States",
            title = TITLE_QF
        ),
        SlotSeed(
            matchNumber = 99,
            localDateTime = LocalDateTime.of(2026, 7, 11, 17, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.QUARTER_FINAL,
            venueId = "MIAMI_GARDENS_HARD_ROCK_STADIUM",
            venueName = "Hard Rock Stadium",
            venueLocation = "Miami, United States",
            title = TITLE_QF
        ),
        SlotSeed(
            matchNumber = 100,
            localDateTime = LocalDateTime.of(2026, 7, 11, 20, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.QUARTER_FINAL,
            venueId = "KANSAS_CITY_ARROWHEAD_STADIUM",
            venueName = "Arrowhead Stadium",
            venueLocation = "Kansas City, United States",
            title = TITLE_QF
        ),

        // ---- Semi-finals (101..102) ----
        SlotSeed(
            matchNumber = 101,
            localDateTime = LocalDateTime.of(2026, 7, 14, 14, 0),
            zoneId = ZONE_CHICAGO,
            stage = MatchStage.SEMI_FINAL,
            venueId = "DALLAS_ATT_STADIUM",
            venueName = "AT&T Stadium",
            venueLocation = "Dallas, United States",
            title = TITLE_SF
        ),
        SlotSeed(
            matchNumber = 102,
            localDateTime = LocalDateTime.of(2026, 7, 15, 15, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.SEMI_FINAL,
            venueId = "ATLANTA_MERCEDES_BENZ_STADIUM",
            venueName = "Mercedes-Benz Stadium",
            venueLocation = "Atlanta, United States",
            title = TITLE_SF
        ),

        // ---- Third place (103) ----
        SlotSeed(
            matchNumber = 103,
            localDateTime = LocalDateTime.of(2026, 7, 18, 17, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.THIRD_PLACE,
            venueId = "MIAMI_GARDENS_HARD_ROCK_STADIUM",
            venueName = "Hard Rock Stadium",
            venueLocation = "Miami, United States",
            title = TITLE_THIRD_PLACE
        ),

        // ---- Final (104) ----
        SlotSeed(
            matchNumber = 104,
            localDateTime = LocalDateTime.of(2026, 7, 19, 15, 0),
            zoneId = ZONE_NEW_YORK,
            stage = MatchStage.FINAL,
            venueId = "EAST_RUTHERFORD_METLIFE_STADIUM",
            venueName = "MetLife Stadium",
            venueLocation = "New York/New Jersey, United States",
            title = TITLE_FINAL
        )
    )

    /**
     * Convierte el seed a entidad Firestore.
     *
     * `localDateTime + zoneId → Instant → Timestamp` garantiza que la fecha
     * en Firestore sea un Timestamp nativo (no String) y que el instante
     * absoluto sea correcto sin depender de la zona del dispositivo que
     * ejecute el seed.
     */
    fun SlotSeed.toEntity(
        tournamentId: String = WorldCup2026Teams.TOURNAMENT_ID
    ): MatchScheduleSlotEntity {
        val instant = localDateTime.atZone(zoneId).toInstant()
        val timestamp = Timestamp(instant.epochSecond, instant.nano)

        return MatchScheduleSlotEntity(
            tournamentId = tournamentId,
            matchNumber = matchNumber,
            date = timestamp,
            stage = stage.name,
            venueId = venueId,
            venueName = venueName,
            venueLocation = venueLocation,
            title = title,
            linkedGameId = null,
            createdAt = null,
            updatedAt = null
        )
    }
}
