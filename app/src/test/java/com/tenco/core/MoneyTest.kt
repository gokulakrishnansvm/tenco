package com.tenco.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun `format renders rupees with two decimals`() {
        // 120000 paise = 1200.00 rupees
        assertTrue(Money.format(120000).contains("1,200"))
        assertTrue(Money.format(120000).endsWith(".00"))
    }

    @Test
    fun `formatShort drops decimals`() {
        val s = Money.formatShort(120000)
        assertTrue(s.contains("1,200"))
        assertTrue(!s.contains(".00"))
    }

    @Test
    fun `rupeesToPaise rounds correctly`() {
        assertEquals(1234L, Money.rupeesToPaise(12.34))
        assertEquals(87000L, Money.rupeesToPaise(870.0))
    }
}
