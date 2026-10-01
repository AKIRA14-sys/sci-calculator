package com.supersci.calculator.math

import kotlin.math.abs
import kotlin.math.sqrt

sealed class SolutionResult {
    data class SingleVariable(val variable: String, val solutions: List<Double>) : SolutionResult()
    data class System(val solutions: Map<String, Double>) : SolutionResult()
    data class Message(val message: String) : SolutionResult()
    data class Error(val error: String) : SolutionResult()
}

class EquationSolver {

    private val evaluator = ExpressionEvaluator()

    fun solve(equationStr: String): SolutionResult {
        val trimmed = equationStr.replace(" ", "")
            .replace("²", "^2")
            .replace("³", "^3")

        if (!trimmed.contains("=")) {
            return SolutionResult.Error("Equation must contain '='")
        }

        val parts = trimmed.split("=")
        if (parts.size != 2) {
            return SolutionResult.Error("Multiple '=' signs found")
        }

        val lhs = parts[0]
        val rhs = parts[1]

        val vars = extractVariables("$lhs $rhs")
        if (vars.isEmpty()) {
            return try {
                val valL = evaluator.evaluate(lhs)
                val valR = evaluator.evaluate(rhs)
                if (abs(valL - valR) < 1e-6) {
                    SolutionResult.Message("Identity (Always True)")
                } else {
                    SolutionResult.Message("No Solution (False)")
                }
            } catch (e: Exception) {
                SolutionResult.Error("Invalid mathematical expression")
            }
        }

        if (vars.size > 1) {
            return SolutionResult.Error("For multiple variables, use system solver")
        }

        val variable = vars.first()

        // Attempt Quadratic Solve: ax^2 + bx + c = 0
        val quadCoeffs = tryParseQuadratic(lhs, rhs, variable)
        if (quadCoeffs != null) {
            val (a, b, c) = quadCoeffs
            if (abs(a) > 1e-9) {
                val discriminant = b * b - 4 * a * c
                return if (discriminant > 0) {
                    val x1 = (-b + sqrt(discriminant)) / (2 * a)
                    val x2 = (-b - sqrt(discriminant)) / (2 * a)
                    SolutionResult.SingleVariable(variable, listOf(x1, x2).sorted())
                } else if (abs(discriminant) < 1e-9) {
                    val x = -b / (2 * a)
                    SolutionResult.SingleVariable(variable, listOf(x))
                } else {
                    SolutionResult.Message("No Real Solutions (Complex roots)")
                }
            } else if (abs(b) > 1e-9) {
                val x = -c / b
                return SolutionResult.SingleVariable(variable, listOf(x))
            } else {
                return if (abs(c) < 1e-9) SolutionResult.Message("Identity (Always True)") else SolutionResult.Message("No Solution")
            }
        }

        // Fallback: Numerical Root Finding (Newton-Raphson / Bisection)
        val numericalRoots = solveNumerically(lhs, rhs, variable)
        if (numericalRoots.isNotEmpty()) {
            return SolutionResult.SingleVariable(variable, numericalRoots)
        }

        return SolutionResult.Error("Could not solve equation")
    }

    fun solveSystem(eq1Str: String, eq2Str: String): SolutionResult {
        // Solves 2x2 linear system
        val vars = extractVariables("$eq1Str $eq2Str").sorted()
        if (vars.size != 2) {
            return SolutionResult.Error("System requires exactly 2 variables (e.g. x and y)")
        }
        val v1 = vars[0]
        val v2 = vars[1]

        val c1 = tryParseLinear2Var(eq1Str, v1, v2) ?: return SolutionResult.Error("Failed to parse first linear equation")
        val c2 = tryParseLinear2Var(eq2Str, v1, v2) ?: return SolutionResult.Error("Failed to parse second linear equation")

        val (a1, b1, k1) = c1 // a1*v1 + b1*v2 = k1
        val (a2, b2, k2) = c2 // a2*v1 + b2*v2 = k2

        val det = a1 * b2 - a2 * b1
        if (abs(det) < 1e-9) {
            return SolutionResult.Error("System has no unique solution (Parallel or Dependent lines)")
        }

        val val1 = (k1 * b2 - k2 * b1) / det
        val val2 = (a1 * k2 - a2 * k1) / det

        return SolutionResult.System(mapOf(v1 to val1, v2 to val2))
    }

    private fun extractVariables(expr: String): Set<String> {
        val regex = Regex("[a-zA-Z]+")
        return regex.findAll(expr)
            .map { it.value.lowercase() }
            .filter { it !in setOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "cbrt", "abs", "pi", "e", "ans") }
            .toSet()
    }

    private fun tryParseQuadratic(lhs: String, rhs: String, v: String): Triple<Double, Double, Double>? {
        return try {
            fun evalAt(xVal: Double): Double {
                val map = mapOf(v to xVal)
                return evaluator.evaluate(lhs, variables = map) - evaluator.evaluate(rhs, variables = map)
            }

            val f0 = evalAt(0.0)
            val f1 = evalAt(1.0)
            val fMinus1 = evalAt(-1.0)

            // f(x) = a*x^2 + b*x + c
            // c = f0
            val c = f0
            // f(1) = a + b + c => a + b = f1 - c
            // f(-1) = a - b + c => a - b = fMinus1 - c
            val sum = (f1 - c) + (fMinus1 - c) // 2a
            val diff = (f1 - c) - (fMinus1 - c) // 2b

            val a = sum / 2.0
            val b = diff / 2.0

            // Verify with f(2)
            val f2Actual = evalAt(2.0)
            val f2Expected = a * 4.0 + b * 2.0 + c

            if (abs(f2Actual - f2Expected) < 1e-5) {
                Triple(a, b, c)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun tryParseLinear2Var(eqStr: String, v1: String, v2: String): Triple<Double, Double, Double>? {
        return try {
            val parts = eqStr.replace(" ", "").split("=")
            if (parts.size != 2) return null
            val lhs = parts[0]
            val rhs = parts[1]

            fun evalAt(val1: Double, val2: Double): Double {
                val map = mapOf(v1 to val1, v2 to val2)
                return evaluator.evaluate(lhs, variables = map) - evaluator.evaluate(rhs, variables = map)
            }

            // f(v1, v2) = a*v1 + b*v2 + const = 0 => a*v1 + b*v2 = -const
            val f00 = evalAt(0.0, 0.0)
            val f10 = evalAt(1.0, 0.0)
            val f01 = evalAt(0.0, 1.0)

            val a = f10 - f00
            val b = f01 - f00
            val k = -f00

            // Verify with (1,1)
            val f11 = evalAt(1.0, 1.0)
            if (abs(f11 - (a + b + f00)) < 1e-5) {
                Triple(a, b, k)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun solveNumerically(lhs: String, rhs: String, v: String): List<Double> {
        val results = mutableListOf<Double>()
        fun f(x: Double): Double {
            return try {
                evaluator.evaluate(lhs, variables = mapOf(v to x)) - evaluator.evaluate(rhs, variables = mapOf(v to x))
            } catch (e: Exception) {
                Double.NaN
            }
        }

        // Bisection / Newton search in intervals
        for (i in -100..100) {
            val x0 = i.toDouble()
            val x1 = i + 1.0
            val y0 = f(x0)
            val y1 = f(x1)

            if (!y0.isNaN() && !y1.isNaN()) {
                if (y0 * y1 <= 0) {
                    // Bisection
                    var low = x0
                    var high = x1
                    var mid = low
                    for (step in 0..50) {
                        mid = (low + high) / 2.0
                        val fMid = f(mid)
                        if (abs(fMid) < 1e-7 || (high - low) < 1e-7) break
                        if (f(low) * fMid <= 0) {
                            high = mid
                        } else {
                            low = mid
                        }
                    }
                    if (results.none { abs(it - mid) < 1e-4 }) {
                        results.add((mid * 10000).toLong() / 10000.0)
                    }
                }
            }
        }
        return results
    }
}
