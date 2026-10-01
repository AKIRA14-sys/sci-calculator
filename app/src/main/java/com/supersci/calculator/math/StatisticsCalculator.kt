package com.supersci.calculator.math

import kotlin.math.pow
import kotlin.math.sqrt

data class StatisticsResult(
    val count: Int,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double,
    val sum: Double,
    val variance: Double,
    val standardDeviation: Double
)

class StatisticsCalculator {

    fun calculate(values: List<Double>): StatisticsResult {
        require(values.isNotEmpty()) { "Values cannot be empty" }

        val count = values.size
        val sum = values.sum()
        val mean = sum / count
        val sorted = values.sorted()

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        val median = if (count % 2 == 0) {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        } else {
            sorted[count / 2]
        }

        val freqMap = values.groupingBy { it }.eachCount()
        val maxFreq = freqMap.values.maxOrNull() ?: 0
        val mode = if (maxFreq > 1) {
            freqMap.filter { it.value == maxFreq }.keys.toList().sorted()
        } else {
            emptyList()
        }

        val variance = if (count > 1) {
            values.sumOf { (it - mean).pow(2) } / (count - 1)
        } else {
            0.0
        }

        val stdDev = sqrt(variance)

        return StatisticsResult(
            count = count,
            mean = mean,
            median = median,
            mode = mode,
            min = min,
            max = max,
            range = range,
            sum = sum,
            variance = variance,
            standardDeviation = stdDev
        )
    }
}
