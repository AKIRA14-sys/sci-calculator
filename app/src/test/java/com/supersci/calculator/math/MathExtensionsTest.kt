package com.supersci.calculator.math

import org.junit.Assert.assertEquals
import org.junit.Test

class MathExtensionsTest {

    @Test
    fun testFractions() {
        val f1 = Fraction(1, 2)
        val f2 = Fraction(3, 4)
        val sum = f1 + f2
        assertEquals("5/4", sum.toString())
        assertEquals("1 1/4", sum.toMixedString())
        assertEquals(1.25, sum.toDecimal(), 1e-6)
    }

    @Test
    fun testMatrix() {
        val m1 = Matrix(2, 2, arrayOf(doubleArrayOf(1.0, 2.0), doubleArrayOf(3.0, 4.0)))
        assertEquals(-2.0, m1.determinant(), 1e-6)

        val inv = m1.inverse()
        val prod = m1 * inv
        assertEquals(1.0, prod.data[0][0], 1e-6)
        assertEquals(0.0, prod.data[0][1], 1e-6)
        assertEquals(0.0, prod.data[1][0], 1e-6)
        assertEquals(1.0, prod.data[1][1], 1e-6)
    }

    @Test
    fun testComplexNumbers() {
        val c1 = Complex(3.0, 4.0)
        assertEquals(5.0, c1.magnitude(), 1e-6)
        val c2 = Complex(1.0, -2.0)
        val prod = c1 * c2
        assertEquals(11.0, prod.real, 1e-6)
        assertEquals(-2.0, prod.imag, 1e-6)
    }

    @Test
    fun testStatistics() {
        val statsCalc = StatisticsCalculator()
        val res = statsCalc.calculate(listOf(1.0, 2.0, 3.0, 4.0, 5.0))
        assertEquals(3.0, res.mean, 1e-6)
        assertEquals(3.0, res.median, 1e-6)
        assertEquals(1.0, res.min, 1e-6)
        assertEquals(5.0, res.max, 1e-6)
        assertEquals(15.0, res.sum, 1e-6)
    }

    @Test
    fun testUnitConverter() {
        val converter = UnitConverter()
        val km = UnitConverter.LENGTH_UNITS.first { it.symbol == "km" }
        val mi = UnitConverter.LENGTH_UNITS.first { it.symbol == "mi" }
        val miles = converter.convert(10.0, km, mi, UnitCategory.LENGTH)
        assertEquals(6.21371, miles, 1e-4)
    }
}
