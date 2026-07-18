package com.tenco.core

import java.text.NumberFormat
import java.util.Locale

/** All money is stored as integer paise to avoid floating-point errors. */
object Money {
    private val inr: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    /** Format paise as an Indian Rupee string, e.g. 120000L -> "₹1,200.00". */
    fun format(paise: Long): String = inr.format(paise / 100.0)

    /** Compact form without decimals, e.g. 120000L -> "₹1,200". */
    fun formatShort(paise: Long): String {
        val nf = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        nf.maximumFractionDigits = 0
        return nf.format(paise / 100.0)
    }

    fun rupeesToPaise(rupees: Double): Long = Math.round(rupees * 100)
}
