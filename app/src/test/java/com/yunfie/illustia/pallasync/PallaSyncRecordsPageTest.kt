package com.yunfie.illustia.pallasync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PallaSyncRecordsPageTest :
    StringSpec({
        "a page advances when the cursor increases" {
            recordsPage(nextSeq = 11L, hasMore = true).advancesFrom(10L) shouldBe true
        }

        "an empty terminal page may keep the current cursor" {
            recordsPage(nextSeq = 10L, hasMore = false).advancesFrom(10L) shouldBe true
        }

        "a cursor may not stay fixed while work remains" {
            recordsPage(nextSeq = 10L, hasMore = true).advancesFrom(10L) shouldBe false
            recordsPage(
                nextSeq = 10L,
                hasMore = false,
                records = listOf(PallaSyncPageRecord(null, "{}")),
            ).advancesFrom(10L) shouldBe false
        }

        "a page may not move the cursor backwards" {
            recordsPage(nextSeq = 9L, hasMore = false).advancesFrom(10L) shouldBe false
        }
    })

private fun recordsPage(
    nextSeq: Long,
    hasMore: Boolean,
    records: List<PallaSyncPageRecord> = emptyList(),
) = PallaSyncRecordsPage(
    records = records,
    nextSeq = nextSeq,
    hasMore = hasMore,
)
