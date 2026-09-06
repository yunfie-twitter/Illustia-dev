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

    @Test
    fun `recommended tag columns scale for large screens`() {
        assertEquals(3, recommendedTagColumnsForWidth(360))
        assertEquals(3, recommendedTagColumnsForWidth(599))
        assertEquals(4, recommendedTagColumnsForWidth(600))
        assertEquals(6, recommendedTagColumnsForWidth(840))
        assertEquals(7, recommendedTagColumnsForWidth(1_100))
        assertEquals(8, recommendedTagColumnsForWidth(1_400))
        assertEquals(8, recommendedTagColumnsForWidth(1_920))
    }

    @Test
    fun `related illust columns respect manual and dynamic settings`() {
        // Explicit configuration (2..5) must be preserved across any screen width
        assertEquals(2, relatedIllustColumnsForWidth(360, configuredColumns = 2))
        assertEquals(2, relatedIllustColumnsForWidth(1_400, configuredColumns = 2))
        assertEquals(3, relatedIllustColumnsForWidth(360, configuredColumns = 3))
        assertEquals(4, relatedIllustColumnsForWidth(360, configuredColumns = 4))
        assertEquals(5, relatedIllustColumnsForWidth(360, configuredColumns = 5))

        // Dynamic configuration (configuredColumns = 0): base 3, never 2, scales to 4 and 5 on large screens
        assertEquals(3, relatedIllustColumnsForWidth(360, configuredColumns = 0))
        assertEquals(3, relatedIllustColumnsForWidth(599, configuredColumns = 0))
        assertEquals(3, relatedIllustColumnsForWidth(600, configuredColumns = 0))
        assertEquals(4, relatedIllustColumnsForWidth(840, configuredColumns = 0))
        assertEquals(4, relatedIllustColumnsForWidth(1_199, configuredColumns = 0))
        assertEquals(5, relatedIllustColumnsForWidth(1_200, configuredColumns = 0))
        assertEquals(5, relatedIllustColumnsForWidth(1_920, configuredColumns = 0))
    }
}
