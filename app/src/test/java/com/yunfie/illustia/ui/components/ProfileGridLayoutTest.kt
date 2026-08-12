package com.yunfie.illustia.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileGridLayoutTest {
    @Test
    fun `profile grid scales across phone and tablet widths`() {
        assertEquals(2, profileGridColumnsForWidth(599))
        assertEquals(3, profileGridColumnsForWidth(600))
        assertEquals(4, profileGridColumnsForWidth(840))
        assertEquals(5, profileGridColumnsForWidth(1_200))
    }
}
