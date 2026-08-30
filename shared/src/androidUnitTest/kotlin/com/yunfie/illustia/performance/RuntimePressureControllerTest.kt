package com.yunfie.illustia.performance

import android.os.PowerManager
import io.kotest.matchers.shouldBe
import org.junit.Test

class RuntimePressureControllerTest {
    @Test
    fun `grid velocity uses ewma and separate enter exit thresholds`() {
        val controller = AdaptiveGridVelocityController()

        controller.sample(4_300.0).quality shouldBe AdaptiveImageQuality.VERY_LOW
        controller.sample(3_900.0).quality shouldBe AdaptiveImageQuality.VERY_LOW
        controller.sample(2_000.0).quality shouldBe AdaptiveImageQuality.LOW
        controller.sample(2_000.0).quality shouldBe AdaptiveImageQuality.LOW
        controller.sample(0.0).quality shouldBe AdaptiveImageQuality.LOW
        controller.sample(0.0).quality shouldBe AdaptiveImageQuality.MID
    }

    @Test
    fun `three high samples lower one step and cooldown discards samples`() {
        val controller = RuntimePressureController()

        controller.sample(50, stable = false, nowMs = 0) shouldBe RuntimePressureLevel.NORMAL
        controller.sample(50, stable = false, nowMs = 1_000) shouldBe RuntimePressureLevel.NORMAL
        controller.sample(50, stable = false, nowMs = 2_000) shouldBe RuntimePressureLevel.ELEVATED

        controller.sample(90, stable = false, nowMs = 3_000) shouldBe RuntimePressureLevel.ELEVATED
        controller.sample(90, stable = false, nowMs = 4_000) shouldBe RuntimePressureLevel.ELEVATED
        controller.sample(90, stable = false, nowMs = 5_000) shouldBe RuntimePressureLevel.ELEVATED
        controller.sample(50, stable = false, nowMs = 6_000) shouldBe RuntimePressureLevel.ELEVATED
        controller.sample(50, stable = false, nowMs = 7_000) shouldBe RuntimePressureLevel.ELEVATED
        controller.sample(50, stable = false, nowMs = 8_000) shouldBe RuntimePressureLevel.HIGH
    }

    @Test
    fun `recovery needs eight new stable samples for each step`() {
        val controller = RuntimePressureController()
        controller.requestImmediateElevation(0) shouldBe RuntimePressureLevel.ELEVATED
        controller.requestImmediateElevation(1) shouldBe RuntimePressureLevel.HIGH

        repeat(8) { index ->
            controller.sample(10, stable = true, nowMs = 4_001L + index * 1_000L)
        }
        controller.level shouldBe RuntimePressureLevel.ELEVATED

        repeat(8) { index ->
            controller.sample(10, stable = true, nowMs = 15_002L + index * 1_000L)
        }
        controller.level shouldBe RuntimePressureLevel.NORMAL
    }

    @Test
    fun `direct memory or thermal signals bypass polling cooldown by one step`() {
        val controller = RuntimePressureController()

        controller.requestImmediateElevation(0) shouldBe RuntimePressureLevel.ELEVATED
        controller.requestImmediateElevation(100) shouldBe RuntimePressureLevel.HIGH
    }

    @Test
    fun `jank score is continuous and app cpu distinguishes external system load`() {
        DevicePerformance.jankPressurePoints(0.029) shouldBe 0
        DevicePerformance.jankPressurePoints(0.03) shouldBe 5
        DevicePerformance.jankPressurePoints(0.06) shouldBe 15
        DevicePerformance.jankPressurePoints(0.10) shouldBe 30
        DevicePerformance.jankPressurePoints(0.15) shouldBe 50
        DevicePerformance.jankPressurePoints(0.25) shouldBe 70

        DevicePerformance.calculatePressureScore(
            systemCpuLoad = 0.95,
            appCpuLoad = 0.08,
            memoryHeadroom = 0.30,
            lowMemory = false,
            jankRatio = 0.0,
            thermalStatus = PowerManager.THERMAL_STATUS_NONE,
            thermalHeadroom = 0f,
            batterySaver = false,
        ) shouldBe 3
        DevicePerformance.calculatePressureScore(
            systemCpuLoad = 0.70,
            appCpuLoad = 0.55,
            memoryHeadroom = 0.30,
            lowMemory = false,
            jankRatio = 0.18,
            thermalStatus = PowerManager.THERMAL_STATUS_NONE,
            thermalHeadroom = 0f,
            batterySaver = false,
        ) shouldBe 95
    }

    @Test
    fun `device thermal thresholds replace fallback headroom cutoffs`() {
        DevicePerformance.calculatePressureScore(
            systemCpuLoad = 0.0,
            appCpuLoad = 0.0,
            memoryHeadroom = 0.5,
            lowMemory = false,
            jankRatio = 0.0,
            thermalStatus = PowerManager.THERMAL_STATUS_NONE,
            thermalHeadroom = 0.65f,
            batterySaver = false,
            moderateThermalHeadroomThreshold = 0.6f,
            severeThermalHeadroomThreshold = 0.9f,
        ) shouldBe 25
    }
}
