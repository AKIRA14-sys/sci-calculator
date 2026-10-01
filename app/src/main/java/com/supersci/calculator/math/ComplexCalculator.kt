package com.supersci.calculator.math

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

data class Complex(val real: Double, val imag: Double) {

    operator fun plus(other: Complex): Complex = Complex(real + other.real, imag + other.imag)
    operator fun minus(other: Complex): Complex = Complex(real - other.real, imag - other.imag)

    operator fun times(other: Complex): Complex {
        return Complex(
            real * other.real - imag * other.imag,
            real * other.imag + imag * other.real
        )
    }

    operator fun div(other: Complex): Complex {
        val denom = other.real * other.real + other.imag * other.imag
        require(denom != 0.0) { "Division by zero complex number" }
        return Complex(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        )
    }

    fun magnitude(): Double = hypot(real, imag)
    fun conjugate(): Complex = Complex(real, -imag)
    fun argument(): Double = atan2(imag, real)

    override fun toString(): String {
        return when {
            imag == 0.0 -> String.format("%.4f", real).trimEnd('0').trimEnd('.')
            real == 0.0 -> "${String.format("%.4f", imag).trimEnd('0').trimEnd('.')}i"
            imag < 0 -> "${String.format("%.4f", real).trimEnd('0').trimEnd('.')} - ${String.format("%.4f", abs(imag)).trimEnd('0').trimEnd('.')}i"
            else -> "${String.format("%.4f", real).trimEnd('0').trimEnd('.')} + ${String.format("%.4f", imag).trimEnd('0').trimEnd('.')}i"
        }
    }

    companion object {
        fun parse(str: String): Complex {
            val clean = str.replace(" ", "")
            if (!clean.contains("i")) {
                return Complex(clean.toDouble(), 0.0)
            }
            val withI = clean.replace("i", "")
            if (withI.isEmpty()) return Complex(0.0, 1.0)
            if (withI == "+") return Complex(0.0, 1.0)
            if (withI == "-") return Complex(0.0, -1.0)

            val regex = Regex("([+-]?[0-9.]+)?([+-][0-9.]+)?")
            val matches = regex.find(withI)
            if (matches != null) {
                val realPart = matches.groupValues[1].toDoubleOrNull() ?: 0.0
                val imagPart = matches.groupValues[2].toDoubleOrNull() ?: 1.0
                return Complex(realPart, imagPart)
            }
            return Complex(0.0, withI.toDoubleOrNull() ?: 0.0)
        }
    }
}
