package com.supersci.calculator.math

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EquationSolverTest {

    private val solver = EquationSolver()
    private val algebra = AlgebraEngine()

    @Test
    fun testLinearEquation() {
        val result = solver.solve("2x + 5 = 15")
        assertTrue(result is SolutionResult.SingleVariable)
        val single = result as SolutionResult.SingleVariable
        assertEquals("x", single.variable)
        assertEquals(1, single.solutions.size)
        assertEquals(5.0, single.solutions[0], 1e-6)
    }

    @Test
    fun testQuadraticEquation() {
        val result = solver.solve("x² - 5x + 6 = 0")
        assertTrue(result is SolutionResult.SingleVariable)
        val single = result as SolutionResult.SingleVariable
        assertEquals("x", single.variable)
        assertEquals(2, single.solutions.size)
        assertEquals(2.0, single.solutions[0], 1e-6)
        assertEquals(3.0, single.solutions[1], 1e-6)
    }

    @Test
    fun testSystemOfEquations() {
        val result = solver.solveSystem("x + y = 10", "2x - y = 5")
        assertTrue(result is SolutionResult.System)
        val sys = result as SolutionResult.System
        assertEquals(5.0, sys.solutions["x"]!!, 1e-6)
        assertEquals(5.0, sys.solutions["y"]!!, 1e-6)
    }

    @Test
    fun testAlgebraEngine() {
        assertEquals("2x + 6", algebra.expand("2(x + 3)"))
        assertEquals("(x - 3)(x + 3)", algebra.factor("x² - 9"))
    }
}
