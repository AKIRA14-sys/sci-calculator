package com.supersci.calculator.math

data class Matrix(val rows: Int, val cols: Int, val data: Array<DoubleArray>) {

    operator fun plus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrix dimensions must match for addition" }
        val result = Array(rows) { DoubleArray(cols) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = data[i][j] + other.data[i][j]
            }
        }
        return Matrix(rows, cols, result)
    }

    operator fun minus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Matrix dimensions must match for subtraction" }
        val result = Array(rows) { DoubleArray(cols) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i][j] = data[i][j] - other.data[i][j]
            }
        }
        return Matrix(rows, cols, result)
    }

    operator fun times(other: Matrix): Matrix {
        require(cols == other.rows) { "Inner matrix dimensions must match for multiplication" }
        val result = Array(rows) { DoubleArray(other.cols) }
        for (i in 0 until rows) {
            for (j in 0 until other.cols) {
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += data[i][k] * other.data[k][j]
                }
                result[i][j] = sum
            }
        }
        return Matrix(rows, other.cols, result)
    }

    fun transpose(): Matrix {
        val result = Array(cols) { DoubleArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[j][i] = data[i][j]
            }
        }
        return Matrix(cols, rows, result)
    }

    fun determinant(): Double {
        require(rows == cols) { "Determinant requires a square matrix" }
        return when (rows) {
            1 -> data[0][0]
            2 -> data[0][0] * data[1][1] - data[0][1] * data[1][0]
            3 -> {
                val a = data[0][0]; val b = data[0][1]; val c = data[0][2]
                val d = data[1][0]; val e = data[1][1]; val f = data[1][2]
                val g = data[2][0]; val h = data[2][1]; val i = data[2][2]
                a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g)
            }
            else -> throw IllegalArgumentException("Only 1x1, 2x2 and 3x3 determinants supported")
        }
    }

    fun inverse(): Matrix {
        val det = determinant()
        require(kotlin.math.abs(det) > 1e-9) { "Matrix is singular (determinant is 0)" }
        return when (rows) {
            2 -> {
                val a = data[0][0]; val b = data[0][1]
                val c = data[1][0]; val d = data[1][1]
                val invData = arrayOf(
                    doubleArrayOf(d / det, -b / det),
                    doubleArrayOf(-c / det, a / det)
                )
                Matrix(2, 2, invData)
            }
            3 -> {
                val a = data[0][0]; val b = data[0][1]; val c = data[0][2]
                val d = data[1][0]; val e = data[1][1]; val f = data[1][2]
                val g = data[2][0]; val h = data[2][1]; val i = data[2][2]

                val invData = arrayOf(
                    doubleArrayOf((e * i - f * h) / det, (c * h - b * i) / det, (b * f - c * e) / det),
                    doubleArrayOf((f * g - d * i) / det, (a * i - c * g) / det, (c * d - a * f) / det),
                    doubleArrayOf((d * h - e * g) / det, (g * b - a * h) / det, (a * e - b * d) / det)
                )
                Matrix(3, 3, invData)
            }
            else -> throw IllegalArgumentException("Only 2x2 and 3x3 inverses supported")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        if (rows != other.rows || cols != other.cols) return false
        for (i in 0 until rows) {
            if (!data[i].contentEquals(other.data[i])) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + data.contentDeepHashCode()
        return result
    }
}
