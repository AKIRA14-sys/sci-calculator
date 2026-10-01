package com.supersci.calculator.math

import kotlin.math.abs

data class Fraction(val numerator: Long, val denominator: Long = 1) {
    init {
        require(denominator != 0L) { "Denominator cannot be zero" }
    }

    private val gcdVal = gcd(abs(numerator), abs(denominator))
    val num: Long = (if (denominator < 0) -numerator else numerator) / gcdVal
    val den: Long = abs(denominator) / gcdVal

    operator fun plus(other: Fraction): Fraction {
        return Fraction(num * other.den + other.num * den, den * other.den)
    }

    operator fun minus(other: Fraction): Fraction {
        return Fraction(num * other.den - other.num * den, den * other.den)
    }

    operator fun times(other: Fraction): Fraction {
        return Fraction(num * other.num, den * other.den)
    }

    operator fun div(other: Fraction): Fraction {
        return Fraction(num * other.den, den * other.num)
    }

    fun toDecimal(): Double = num.toDouble() / den.toDouble()

    fun toMixedString(): String {
        if (den == 1L) return "$num"
        val whole = num / den
        val remainder = abs(num % den)
        return if (whole != 0L) {
            "$whole $remainder/$den"
        } else {
            "$num/$den"
        }
    }

    override fun toString(): String {
        return if (den == 1L) "$num" else "$num/$den"
    }

    companion object {
        private fun gcd(a: Long, b: Long): Long {
            var x = a
            var y = b
            while (y != 0L) {
                val temp = y
                y = x % y
                x = temp
            }
            return if (x == 0L) 1L else x
        }

        fun fromDecimal(decimal: Double, tolerance: Double = 1e-6): Fraction {
            var sign = 1.0
            var d = decimal
            if (d < 0) {
                sign = -1.0
                d = -d
            }

            var h1 = 1L; var h2 = 0L
            var k1 = 0L; var k2 = 1L
            var b = d
            do {
                val a = b.toLong()
                var aux = h1
                h1 = a * h1 + h2
                h2 = aux
                aux = k1
                k1 = a * k1 + k2
                k2 = aux
                b = 1.0 / (b - a)
            } while (abs(d - h1.toDouble() / k1.toDouble()) > d * tolerance && k1 < 10000)

            return Fraction((sign * h1).toLong(), k1)
        }

        fun parse(str: String): Fraction {
            val clean = str.replace(" ", "")
            if (clean.contains("/")) {
                val parts = clean.split("/")
                val n = parts[0].toLong()
                val d = parts[1].toLong()
                return Fraction(n, d)
            }
            val dVal = clean.toDouble()
            return fromDecimal(dVal)
        }
    }
}
