package com.supersci.calculator.math

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionEvaluatorTest {

    private val evaluator = ExpressionEvaluator()

    @Test
    fun testBasicArithmetic() {
        assertEquals(100.0, evaluator.evaluate("25 * (8 + 4) / 3"), 1e-6)
        assertEquals(4.0, evaluator.evaluate("2 + 2"), 1e-6)
        assertEquals(100.0, evaluator.evaluate("25 * 4"), 1e-6)
        assertEquals(1024.0, evaluator.evaluate("2^10"), 1e-6)
    }

    @Test
    fun testTrigonometryAndAngles() {
        assertEquals(1.0, evaluator.evaluate("sin(90)", AngleMode.DEG), 1e-6)
        assertEquals(1.0, evaluator.evaluate("sin(pi / 2)", AngleMode.RAD), 1e-6)
        assertEquals(1.0, evaluator.evaluate("sin(100)", AngleMode.GRAD), 1e-6)
    }

    @Test
    fun testFunctionsAndConstants() {
        assertEquals(12.0, evaluator.evaluate("√144"), 1e-6)
        assertEquals(120.0, evaluator.evaluate("5!"), 1e-6)
        assertEquals(Math.E, evaluator.evaluate("e"), 1e-6)
        assertEquals(Math.PI, evaluator.evaluate("pi"), 1e-6)
    }
}
