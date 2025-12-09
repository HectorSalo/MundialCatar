package com.skysam.hchirinos.mundialcatar.common

import androidx.annotation.DrawableRes
import com.skysam.hchirinos.mundialcatar.R

object FlagsMapper {
    @DrawableRes
    fun from(flagCode: String?): Int {
        return when (flagCode?.uppercase()) {
            // ---------- CONMEBOL ----------
            "ARG" -> R.drawable.flag_arg
            "BRA" -> R.drawable.flag_bra
            "PAR" -> R.drawable.flag_par
            "URU" -> R.drawable.flag_uru
            "ECU" -> R.drawable.flag_ecu
            "COL" -> R.drawable.flag_col

            // ---------- UEFA ----------
            "MEX" -> R.drawable.flag_mex      // CONCACAF, lo separo solo por orden visual
            "CAN" -> R.drawable.flag_can
            "USA" -> R.drawable.flag_usa

            "GER" -> R.drawable.flag_ger
            "FRA" -> R.drawable.flag_fra
            "ESP" -> R.drawable.flag_esp
            "POR" -> R.drawable.flag_por
            "ENG" -> R.drawable.flag_eng
            "NED" -> R.drawable.flag_ned
            "BEL" -> R.drawable.flag_bel
            "SUI" -> R.drawable.flag_sui
            "SCO" -> R.drawable.flag_sco
            "AUT" -> R.drawable.flag_aut
            "CRO" -> R.drawable.flag_cro
            "NOR" -> R.drawable.flag_nor
            "TUN" -> R.drawable.flag_tun

            // ---------- AFC ----------
            "KOR" -> R.drawable.flag_kor
            "JPN" -> R.drawable.flag_jpn
            "AUS" -> R.drawable.flag_aus
            "IRN" -> R.drawable.flag_irn
            "KSA" -> R.drawable.flag_ksa
            "QAT" -> R.drawable.flag_qat
            "UZB" -> R.drawable.flag_uzb
            "JOR" -> R.drawable.flag_jor

            // ---------- CAF ----------
            "RSA" -> R.drawable.flag_rsa
            "MAR" -> R.drawable.flag_mar
            "CIV" -> R.drawable.flag_civ
            "EGY" -> R.drawable.flag_egy
            "CPV" -> R.drawable.flag_cpv
            "SEN" -> R.drawable.flag_sen
            "ALG" -> R.drawable.flag_alg
            "GHA" -> R.drawable.flag_gha

            // ---------- CONCACAF ----------
            "HAI" -> R.drawable.flag_hai
            "CUW" -> R.drawable.flag_cuw
            "PAN" -> R.drawable.flag_pan

            // ---------- OFC ----------
            "NZL" -> R.drawable.flag_nzl

            // ---------- PLACEHOLDERS / REPESCAS ----------
            "PL_A",
            "PL_B",
            "PL_D",
            "PL_F",
            "PL_I",
            "PL_K" -> R.drawable.ic_flag_24

            // Fallback para cualquier código desconocido
            else -> R.drawable.ic_flag_24
        }
    }
}