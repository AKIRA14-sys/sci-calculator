package com.supersci.calculator.math

class AlgebraEngine {

    private val evaluator = ExpressionEvaluator()

    fun simplify(expr: String): String {
        var clean = expr.replace(" ", "")
        // Handle basic expansion: e.g., 2(x + 3) -> 2x + 6
        val distRegex = Regex("([0-9.]+)\\(([a-zA-Z]+)\\+([0-9.]+)\\)")
        val distMatch = distRegex.find(clean)
        if (distMatch != null) {
            val factor = distMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val varName = distMatch.groupValues[2]
            val constant = distMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val newConst = factor * constant
            val factorStr = if (factor == 1.0) "" else if (factor % 1.0 == 0.0) factor.toInt().toString() else factor.toString()
            val constStr = if (newConst % 1.0 == 0.0) newConst.toInt().toString() else newConst.toString()
            return "$factorStr$varName + $constStr"
        }

        val distMinusRegex = Regex("([0-9.]+)\\(([a-zA-Z]+)-([0-9.]+)\\)")
        val distMinusMatch = distMinusRegex.find(clean)
        if (distMinusMatch != null) {
            val factor = distMinusMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            val varName = distMinusMatch.groupValues[2]
            val constant = distMinusMatch.groupValues[3].toDoubleOrNull() ?: 0.0
            val newConst = factor * constant
            val factorStr = if (factor == 1.0) "" else if (factor % 1.0 == 0.0) factor.toInt().toString() else factor.toString()
            val constStr = if (newConst % 1.0 == 0.0) newConst.toInt().toString() else newConst.toString()
            return "$factorStr$varName - $constStr"
        }

        return expr
    }

    fun expand(expr: String): String {
        return simplify(expr)
    }

    fun factor(expr: String): String {
        var clean = expr.replace(" ", "").replace("²", "^2")
        // Check x^2 - a^2 pattern
        val diffSqRegex = Regex("([a-zA-Z]+)\\^2-([0-9.]+)")
        val match = diffSqRegex.find(clean)
        if (match != null) {
            val varName = match.groupValues[1]
            val valSq = match.groupValues[2].toDoubleOrNull() ?: 0.0
            val root = kotlin.math.sqrt(valSq)
            if (root % 1.0 == 0.0) {
                val rInt = root.toInt()
                return "($varName - $rInt)($varName + $rInt)"
            }
        }
        return expr
    }

    fun substitute(expr: String, varName: String, value: Double): Double {
        return evaluator.evaluate(expr, variables = mapOf(varName to value))
    }
}
