package com.yunfie.illustia.pallasync.util

import java.security.SecureRandom
import java.util.UUID

object UuidV7 {
    private val secureRandom = SecureRandom()
    private var lastTimestampMs = -1L
    private var seq = 0

    @Synchronized
    fun generate(): UUID {
        var currentMs = System.currentTimeMillis()

        if (currentMs > lastTimestampMs) {
            lastTimestampMs = currentMs
            seq = 0
        } else {
            // If time goes backward or we generate multiple within same ms, increment seq
            seq++
            if (seq > 0xFFF) { // 12-bit sequence exhaustion, wait for next ms
                while (currentMs <= lastTimestampMs) {
                    currentMs = System.currentTimeMillis()
                }
                lastTimestampMs = currentMs
                seq = 0
            }
        }

        val randomBytes = ByteArray(10)
        secureRandom.nextBytes(randomBytes)

        val msb = (currentMs shl 16) or ((0x7L shl 12)) or (seq.toLong() and 0xFFFL)

        // We use 64 bits for LSB. The top 2 bits of LSB must be 10 (variant 1)
        var lsb = 0L
        for (i in 0..7) {
            lsb = (lsb shl 8) or (randomBytes[i].toLong() and 0xFF)
        }
        lsb = (lsb and 0x3FFFFFFFFFFFFFFFL) or Long.MIN_VALUE // Set variant to 10xx

        return UUID(msb, lsb)
    }

    fun generateString(): String = generate().toString().lowercase()
}
