package com.supersci.calculator.math

enum class FormulaCategory {
    PHYSICS, MATHEMATICS, CHEMISTRY
}

data class FormulaVar(
    val symbol: String,
    val name: String,
    val unit: String = ""
)

data class FormulaItem(
    val id: String,
    val name: String,
    val category: FormulaCategory,
    val expressionStr: String, // E.g. "v = u + a * t"
    val description: String,
    val targetVar: String,
    val inputs: List<FormulaVar>,
    val compute: (Map<String, Double>) -> Double
)

class FormulaLibrary {

    companion object {
        val FORMULAS = listOf(
            FormulaItem(
                id = "p1",
                name = "Final Velocity (v = u + at)",
                category = FormulaCategory.PHYSICS,
                expressionStr = "v = u + at",
                description = "Calculates final velocity from initial velocity, acceleration, and time.",
                targetVar = "v",
                inputs = listOf(
                    FormulaVar("u", "Initial Velocity", "m/s"),
                    FormulaVar("a", "Acceleration", "m/s²"),
                    FormulaVar("t", "Time", "s")
                ),
                compute = { params -> (params["u"] ?: 0.0) + (params["a"] ?: 0.0) * (params["t"] ?: 0.0) }
            ),
            FormulaItem(
                id = "p2",
                name = "Displacement (s = ut + ½at²)",
                category = FormulaCategory.PHYSICS,
                expressionStr = "s = ut + ½at²",
                description = "Calculates displacement under uniform acceleration.",
                targetVar = "s",
                inputs = listOf(
                    FormulaVar("u", "Initial Velocity", "m/s"),
                    FormulaVar("a", "Acceleration", "m/s²"),
                    FormulaVar("t", "Time", "s")
                ),
                compute = { params ->
                    val u = params["u"] ?: 0.0
                    val a = params["a"] ?: 0.0
                    val t = params["t"] ?: 0.0
                    u * t + 0.5 * a * t * t
                }
            ),
            FormulaItem(
                id = "p3",
                name = "Newton's Second Law (F = ma)",
                category = FormulaCategory.PHYSICS,
                expressionStr = "F = ma",
                description = "Calculates net force acting on an object.",
                targetVar = "F",
                inputs = listOf(
                    FormulaVar("m", "Mass", "kg"),
                    FormulaVar("a", "Acceleration", "m/s²")
                ),
                compute = { params -> (params["m"] ?: 0.0) * (params["a"] ?: 0.0) }
            ),
            FormulaItem(
                id = "p4",
                name = "Ohm's Law (V = IR)",
                category = FormulaCategory.PHYSICS,
                expressionStr = "V = IR",
                description = "Calculates voltage across a conductor.",
                targetVar = "V",
                inputs = listOf(
                    FormulaVar("I", "Current", "A"),
                    FormulaVar("R", "Resistance", "Ω")
                ),
                compute = { params -> (params["I"] ?: 0.0) * (params["R"] ?: 0.0) }
            ),
            FormulaItem(
                id = "p5",
                name = "Mass-Energy Equivalence (E = mc²)",
                category = FormulaCategory.PHYSICS,
                expressionStr = "E = mc²",
                description = "Calculates energy equivalent of mass.",
                targetVar = "E",
                inputs = listOf(
                    FormulaVar("m", "Mass", "kg")
                ),
                compute = { params -> (params["m"] ?: 0.0) * 299792458.0 * 299792458.0 }
            ),
            FormulaItem(
                id = "m1",
                name = "Pythagorean Theorem (c = √(a² + b²))",
                category = FormulaCategory.MATHEMATICS,
                expressionStr = "c = √(a² + b²)",
                description = "Calculates hypotenuse of a right-angled triangle.",
                targetVar = "c",
                inputs = listOf(
                    FormulaVar("a", "Side a", ""),
                    FormulaVar("b", "Side b", "")
                ),
                compute = { params ->
                    val a = params["a"] ?: 0.0
                    val b = params["b"] ?: 0.0
                    kotlin.math.sqrt(a * a + b * b)
                }
            )
        )
    }

    fun search(query: String): List<FormulaItem> {
        if (query.isBlank()) return FORMULAS
        val q = query.lowercase()
        return FORMULAS.filter {
            it.name.lowercase().contains(q) ||
            it.expressionStr.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }
}
