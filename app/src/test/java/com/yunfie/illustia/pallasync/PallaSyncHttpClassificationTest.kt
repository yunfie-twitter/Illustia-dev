package com.yunfie.illustia.pallasync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PallaSyncHttpClassificationTest :
    StringSpec({
        "only 410 is destructive gone" {
            classifyPallaSyncHttpStatus(410) shouldBe PallaSyncHttpResult.Gone
            classifyPallaSyncHttpStatus(404) shouldBe
                PallaSyncHttpResult.ProtocolError(
                    "PallaSync server returned HTTP 404",
                    404,
                )
        }

        "rate limits and server failures are retryable while 2xx continues parsing" {
            (classifyPallaSyncHttpStatus(429) is PallaSyncHttpResult.Retryable) shouldBe true
            (classifyPallaSyncHttpStatus(500) is PallaSyncHttpResult.Retryable) shouldBe true
            classifyPallaSyncHttpStatus(200) shouldBe null
        }
    })
