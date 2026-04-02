package com.skysam.hchirinos.mundialcatar.common

/**
 * Created by Hector Chirinos in the home office on 7 dic. 2025
 */
object WorldCup2026Teams {
    const val TOURNAMENT_ID = "world_cup_2026"

    data class TeamSeed(
        val code: String,
        val name: String,          // nombre en español
        val shortName: String,     // nombre corto para la UI
        val confederation: String, // CONMEBOL, UEFA, etc.
        val flagCode: String,      // para armar URL o drawable de bandera
        val group: String          // A, B, C... L
    )

    val qualifiedTeams = listOf(
        // ---------- GRUPO A ----------
        TeamSeed("MEX", "México", "México", "CONCACAF", "MEX", "A"),
        TeamSeed("KOR", "Corea del Sur", "Corea del Sur", "AFC", "KOR", "A"),
        TeamSeed("RSA", "Sudáfrica", "Sudáfrica", "CAF", "RSA", "A"),
        TeamSeed("CZE", "República Checa", "Chequia", "UEFA", "CZE", "A"),

        // ---------- GRUPO B ----------
        TeamSeed("CAN", "Canadá", "Canadá", "CONCACAF", "CAN", "B"),
        TeamSeed("SUI", "Suiza", "Suiza", "UEFA", "SUI", "B"),
        TeamSeed("QAT", "Catar", "Catar", "AFC", "QAT", "B"),
        TeamSeed("BIH", "Bosnia y Herzegovina", "Bosnia", "UEFA", "BIH", "B"),

        // ---------- GRUPO C ----------
        TeamSeed("BRA", "Brasil", "Brasil", "CONMEBOL", "BRA", "C"),
        TeamSeed("MAR", "Marruecos", "Marruecos", "CAF", "MAR", "C"),
        TeamSeed("SCO", "Escocia", "Escocia", "UEFA", "SCO", "C"),
        TeamSeed("HAI", "Haití", "Haití", "CONCACAF", "HAI", "C"),

        // ---------- GRUPO D ----------
        TeamSeed("USA", "Estados Unidos", "EE. UU.", "CONCACAF", "USA", "D"),
        TeamSeed("AUS", "Australia", "Australia", "AFC", "AUS", "D"),
        TeamSeed("PAR", "Paraguay", "Paraguay", "CONMEBOL", "PAR", "D"),
        TeamSeed("TUR", "Turquía", "Turquía", "UEFA", "TUR", "D"),

        // ---------- GRUPO E ----------
        TeamSeed("GER", "Alemania", "Alemania", "UEFA", "GER", "E"),
        TeamSeed("ECU", "Ecuador", "Ecuador", "CONMEBOL", "ECU", "E"),
        TeamSeed("CIV", "Costa de Marfil", "C. de Marfil", "CAF", "CIV", "E"),
        TeamSeed("CUW", "Curazao", "Curazao", "CONCACAF", "CUW", "E"),

        // ---------- GRUPO F ----------
        TeamSeed("NED", "Países Bajos", "Países Bajos", "UEFA", "NED", "F"),
        TeamSeed("JPN", "Japón", "Japón", "AFC", "JPN", "F"),
        TeamSeed("TUN", "Túnez", "Túnez", "CAF", "TUN", "F"),
        TeamSeed("SWE", "Suecia", "Suecia", "UEFA", "SWE", "F"),

        // ---------- GRUPO G ----------
        TeamSeed("BEL", "Bélgica", "Bélgica", "UEFA", "BEL", "G"),
        TeamSeed("IRN", "Irán", "Irán", "AFC", "IRN", "G"),
        TeamSeed("EGY", "Egipto", "Egipto", "CAF", "EGY", "G"),
        TeamSeed("NZL", "Nueva Zelanda", "Nueva Zelanda", "OFC", "NZL", "G"),

        // ---------- GRUPO H ----------
        TeamSeed("ESP", "España", "España", "UEFA", "ESP", "H"),
        TeamSeed("URU", "Uruguay", "Uruguay", "CONMEBOL", "URU", "H"),
        TeamSeed("KSA", "Arabia Saudita", "Arabia Saudita", "AFC", "KSA", "H"),
        TeamSeed("CPV", "Cabo Verde", "Cabo Verde", "CAF", "CPV", "H"),

        // ---------- GRUPO I ----------
        TeamSeed("FRA", "Francia", "Francia", "UEFA", "FRA", "I"),
        TeamSeed("SEN", "Senegal", "Senegal", "CAF", "SEN", "I"),
        TeamSeed("NOR", "Noruega", "Noruega", "UEFA", "NOR", "I"),
        TeamSeed("IRQ", "Iraq", "Iraq", "AFC", "IRQ", "I"),

        // ---------- GRUPO J ----------
        TeamSeed("ARG", "Argentina", "Argentina", "CONMEBOL", "ARG", "J"),
        TeamSeed("AUT", "Austria", "Austria", "UEFA", "AUT", "J"),
        TeamSeed("ALG", "Argelia", "Argelia", "CAF", "ALG", "J"),
        TeamSeed("JOR", "Jordania", "Jordania", "AFC", "JOR", "J"),

        // ---------- GRUPO K ----------
        TeamSeed("POR", "Portugal", "Portugal", "UEFA", "POR", "K"),
        TeamSeed("COL", "Colombia", "Colombia", "CONMEBOL", "COL", "K"),
        TeamSeed("UZB", "Uzbekistán", "Uzbekistán", "AFC", "UZB", "K"),
        TeamSeed("COD", "RD del Congo", "RD Congo", "CAF", "COD", "K"),

        // ---------- GRUPO L ----------
        TeamSeed("ENG", "Inglaterra", "Inglaterra", "UEFA", "ENG", "L"),
        TeamSeed("CRO", "Croacia", "Croacia", "UEFA", "CRO", "L"),
        TeamSeed("PAN", "Panamá", "Panamá", "CONCACAF", "PAN", "L"),
        TeamSeed("GHA", "Ghana", "Ghana", "CAF", "GHA", "L")
    )
}