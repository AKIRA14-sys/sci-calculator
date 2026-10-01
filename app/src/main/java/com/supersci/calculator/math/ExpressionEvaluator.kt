package com.supersci.calculator.math

import kotlin.math.*

enum class AngleMode {
    DEG, RAD, GRAD
}

class ExpressionEvaluator {

    companion object {
        const val PI = Math.PI
        const val E = Math.E
    }

    sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String) : Token()
        data class Variable(val name: String) : Token()
        data object LeftParen : Token()
        data object RightParen : Token()
        data object Comma : Token()
        data object Factorial : Token()
    }

    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        ans: Double = 0.0,
        variables: Map<String, Double> = emptyMap()
    ): Double {
        val sanitized = sanitize(expression)
        val tokens = tokenize(sanitized, ans, variables)
        val rpn = shuntingYard(tokens)
        return evaluateRpn(rpn, angleMode)
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace("ANS", "ans")
    }

    private fun tokenize(expr: String, ans: Double, variables: Map<String, Double>): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c in '0'..'9' || c == '.') {
                if (!expectUnary) {
                    tokens.add(Token.Operator('*', 2))
                }
                val start = i
                while (i < expr.length && (expr[i] in '0'..'9' || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                    if ((expr[i] == 'E' || expr[i] == 'e') && i + 1 < expr.length && (expr[i + 1] == '+' || expr[i + 1] == '-')) {
                        i += 2
                    } else {
                        i++
                    }
                }
                val numStr = expr.substring(start, i)
                val num = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                tokens.add(Token.Number(num))
                expectUnary = false
                continue
            }

            if (c.isLetter()) {
                if (!expectUnary) {
                    tokens.add(Token.Operator('*', 2))
                }
                val start = i
                while (i < expr.length && expr[i].isLetter()) {
                    i++
                }
                val name = expr.substring(start, i).lowercase()
                when (name) {
                    "pi" -> {
                        tokens.add(Token.Number(PI))
                        expectUnary = false
                    }
                    "e" -> {
                        tokens.add(Token.Number(E))
                        expectUnary = false
                    }
                    "ans" -> {
                        tokens.add(Token.Number(ans))
                        expectUnary = false
                    }
                    else -> {
                        if (variables.containsKey(name)) {
                            tokens.add(Token.Number(variables[name]!!))
                            expectUnary = false
                        } else if (isKnownFunction(name)) {
                            tokens.add(Token.Function(name))
                            expectUnary = true
                        } else {
                            tokens.add(Token.Variable(name))
                            expectUnary = false
                        }
                    }
                }
                continue
            }

            if (c == '+' || c == '-') {
                if (expectUnary) {
                    if (c == '-') {
                        tokens.add(Token.Operator('u', 4, true))
                    }
                } else {
                    tokens.add(Token.Operator(c, 1))
                    expectUnary = true
                }
                i++
                continue
            }

            if (c == '*' || c == '/' || c == '%') {
                tokens.add(Token.Operator(c, 2))
                expectUnary = true
                i++
                continue
            }

            if (c == '^') {
                tokens.add(Token.Operator('^', 3, isRightAssociative = true))
                expectUnary = true
                i++
                continue
            }

            if (c == '!') {
                tokens.add(Token.Factorial)
                expectUnary = false
                i++
                continue
            }

            if (c == '(') {
                if (!expectUnary) {
                    tokens.add(Token.Operator('*', 2))
                }
                tokens.add(Token.LeftParen)
                expectUnary = true
                i++
                continue
            }

            if (c == ')') {
                tokens.add(Token.RightParen)
                expectUnary = false
                i++
                continue
            }

            if (c == ',') {
                tokens.add(Token.Comma)
                expectUnary = true
                i++
                continue
            }

            throw IllegalArgumentException("Unexpected character: $c")
        }

        return tokens
    }

    private fun isKnownFunction(name: String): Boolean {
        return name in setOf(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "log", "ln", "log10",
            "sqrt", "cbrt", "abs", "nroot"
        )
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = mutableListOf<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number, is Token.Variable -> output.add(token)
                is Token.Factorial -> output.add(token)
                is Token.Function -> stack.add(token)
                is Token.Operator -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Operator) {
                            if ((!token.isRightAssociative && token.precedence <= top.precedence) ||
                                (token.isRightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(stack.removeAt(stack.size - 1))
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    stack.add(token)
                }
                is Token.LeftParen -> stack.add(token)
                is Token.RightParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeAt(stack.size - 1))
                    }
                    if (stack.isEmpty()) {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                    stack.removeAt(stack.size - 1)
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeAt(stack.size - 1))
                    }
                }
                is Token.Comma -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeAt(stack.size - 1))
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeAt(stack.size - 1)
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRpn(rpn: List<Token>, angleMode: AngleMode): Double {
        val stack = mutableListOf<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.add(token.value)
                is Token.Variable -> throw IllegalArgumentException("Undefined variable: ${token.name}")
                is Token.Factorial -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid factorial expression")
                    val n = stack.removeAt(stack.size - 1)
                    stack.add(factorial(n))
                }
                is Token.Operator -> {
                    if (token.op == 'u') {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid unary minus")
                        val a = stack.removeAt(stack.size - 1)
                        stack.add(-a)
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid binary operation for '${token.op}'")
                        val b = stack.removeAt(stack.size - 1)
                        val a = stack.removeAt(stack.size - 1)
                        val res = when (token.op) {
                            '+' -> a + b
                            '-' -> a - b
                            '*' -> a * b
                            '/' -> {
                                if (b == 0.0) throw ArithmeticException("Division by zero")
                                a / b
                            }
                            '%' -> a % b
                            '^' -> a.pow(b)
                            else -> throw IllegalArgumentException("Unknown operator: ${token.op}")
                        }
                        stack.add(res)
                    }
                }
                is Token.Function -> {
                    when (token.name) {
                        "sin" -> stack.add(sin(toRadians(stack.removeAt(stack.size - 1), angleMode)))
                        "cos" -> stack.add(cos(toRadians(stack.removeAt(stack.size - 1), angleMode)))
                        "tan" -> stack.add(tan(toRadians(stack.removeAt(stack.size - 1), angleMode)))
                        "asin" -> stack.add(fromRadians(asin(stack.removeAt(stack.size - 1)), angleMode))
                        "acos" -> stack.add(fromRadians(acos(stack.removeAt(stack.size - 1)), angleMode))
                        "atan" -> stack.add(fromRadians(atan(stack.removeAt(stack.size - 1)), angleMode))
                        "sinh" -> stack.add(sinh(stack.removeAt(stack.size - 1)))
                        "cosh" -> stack.add(cosh(stack.removeAt(stack.size - 1)))
                        "tanh" -> stack.add(tanh(stack.removeAt(stack.size - 1)))
                        "log", "log10" -> stack.add(log10(stack.removeAt(stack.size - 1)))
                        "ln" -> stack.add(ln(stack.removeAt(stack.size - 1)))
                        "sqrt" -> stack.add(sqrt(stack.removeAt(stack.size - 1)))
                        "cbrt" -> stack.add(cbrt(stack.removeAt(stack.size - 1)))
                        "abs" -> stack.add(abs(stack.removeAt(stack.size - 1)))
                        "nroot" -> {
                            val n = stack.removeAt(stack.size - 1)
                            val x = stack.removeAt(stack.size - 1)
                            stack.add(x.pow(1.0 / n))
                        }
                        else -> throw IllegalArgumentException("Unknown function: ${token.name}")
                    }
                }
                else -> throw IllegalArgumentException("Invalid token in RPN")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid expression evaluation")
        }

        return stack.first()
    }

    private fun toRadians(angle: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> angle
            AngleMode.DEG -> Math.toRadians(angle)
            AngleMode.GRAD -> angle * (PI / 200.0)
        }
    }

    private fun fromRadians(rad: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> rad
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.GRAD -> rad * (200.0 / PI)
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial requires non-negative integer")
        var result = 1.0
        for (i in 2..n.toInt()) {
            result *= i
        }
        return result
    }
}
