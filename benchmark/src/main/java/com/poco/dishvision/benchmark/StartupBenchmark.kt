/**
 * @file StartupBenchmark.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供 Phase 1 的冷启动 Macrobenchmark smoke test。
 */
package com.poco.dishvision.benchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 冷启动基线测试，至少输出一组启动耗时结果。
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "com.poco.dishvision",
        metrics = listOf(StartupTimingMetric()),
        iterations = 1,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
        },
    ) {
        startActivityAndWait()
    }
}
