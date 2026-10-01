package com.supersci.calculator.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supersci.calculator.math.*
import com.supersci.calculator.ui.graphing.GraphView

enum class CalcSubMode(val title: String) {
    STANDARD("Scientific"),
    EQUATIONS("Equations"),
    ALGEBRA("Algebra"),
    FRACTIONS("Fractions"),
    MATRICES("Matrices"),
    COMPLEX("Complex"),
    STATISTICS("Statistics"),
    CONVERTER("Converter"),
    FORMULAS("Formulas"),
    GRAPHING("Graphing")
}

@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedSubMode by remember { mutableStateOf(CalcSubMode.STANDARD) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Mode selector chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcSubMode.entries.forEach { mode ->
                FilterChip(
                    selected = selectedSubMode == mode,
                    onClick = { selectedSubMode = mode },
                    label = { Text(mode.title) }
                )
            }
        }

        when (selectedSubMode) {
            CalcSubMode.STANDARD -> StandardCalculatorSubView()
            CalcSubMode.EQUATIONS -> EquationSolverSubView()
            CalcSubMode.ALGEBRA -> AlgebraSubView()
            CalcSubMode.FRACTIONS -> FractionSubView()
            CalcSubMode.MATRICES -> MatrixSubView()
            CalcSubMode.COMPLEX -> ComplexSubView()
            CalcSubMode.STATISTICS -> StatisticsSubView()
            CalcSubMode.CONVERTER -> ConverterSubView()
            CalcSubMode.FORMULAS -> FormulasSubView()
            CalcSubMode.GRAPHING -> GraphingSubView()
        }
    }
}

@Composable
fun StandardCalculatorSubView() {
    var displayExpr by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var angleMode by remember { mutableStateOf(AngleMode.DEG) }
    var lastAns by remember { mutableDoubleStateOf(0.0) }

    val evaluator = remember { ExpressionEvaluator() }

    fun onCalcClick(label: String) {
        when (label) {
            "C" -> {
                displayExpr = ""
                resultText = "0"
            }
            "DEL" -> {
                if (displayExpr.isNotEmpty()) displayExpr = displayExpr.dropLast(1)
            }
            "=" -> {
                if (displayExpr.isNotBlank()) {
                    try {
                        val res = evaluator.evaluate(displayExpr, angleMode = angleMode, ans = lastAns)
                        lastAns = res
                        resultText = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                    } catch (e: Exception) {
                        resultText = "Error: ${e.message}"
                    }
                }
            }
            "MODE" -> {
                angleMode = when (angleMode) {
                    AngleMode.DEG -> AngleMode.RAD
                    AngleMode.RAD -> AngleMode.GRAD
                    AngleMode.GRAD -> AngleMode.DEG
                }
            }
            "ANS" -> displayExpr += "ANS"
            "π" -> displayExpr += "π"
            "e" -> displayExpr += "e"
            "√" -> displayExpr += "√("
            "sin" -> displayExpr += "sin("
            "cos" -> displayExpr += "cos("
            "tan" -> displayExpr += "tan("
            "log" -> displayExpr += "log("
            "ln" -> displayExpr += "ln("
            "x²" -> displayExpr += "^2"
            "x³" -> displayExpr += "^3"
            "xʸ" -> displayExpr += "^"
            "1/x" -> displayExpr += "^(-1)"
            "x!" -> displayExpr += "!"
            "abs" -> displayExpr += "abs("
            else -> displayExpr += label
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUPER SCI CALCULATOR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(angleMode.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(displayExpr.ifEmpty { "0" }, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, maxLines = 2, modifier = Modifier.horizontalScroll(rememberScrollState()))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(resultText, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End, maxLines = 1)
                }
            }
        }

        val keys = listOf(
            listOf("MODE", "sin", "cos", "tan", "C"),
            listOf("x²", "√", "log", "ln", "DEL"),
            listOf("xʸ", "(", ")", "x!", "÷"),
            listOf("7", "8", "9", "π", "×"),
            listOf("4", "5", "6", "e", "−"),
            listOf("1", "2", "3", "ANS", "+"),
            listOf("0", ".", "abs", "%", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { label ->
                        CalcButton(label = label, modifier = Modifier.weight(1f), onClick = { onCalcClick(label) })
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOp = label in listOf("+", "−", "×", "÷", "=")
    val isClear = label in listOf("C", "DEL")
    val isFunc = label in listOf("MODE", "sin", "cos", "tan", "log", "ln", "x²", "√", "xʸ", "x!", "abs", "π", "e", "ANS")

    val containerColor = when {
        isClear -> MaterialTheme.colorScheme.errorContainer
        isOp -> MaterialTheme.colorScheme.primary
        isFunc -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isClear -> MaterialTheme.colorScheme.onErrorContainer
        isOp -> MaterialTheme.colorScheme.onPrimary
        isFunc -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = if (label.length > 3) 12.sp else 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EquationSolverSubView() {
    val solver = remember { EquationSolver() }
    var eqInput by remember { mutableStateOf("2x + 5 = 15") }
    var resultText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Casio-Style Equation Solver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = eqInput,
            onValueChange = { eqInput = it },
            label = { Text("Enter Equation (e.g. 2x + 5 = 15 or x² - 5x + 6 = 0)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val res = solver.solve(eqInput)
                resultText = when (res) {
                    is SolutionResult.SingleVariable -> "${res.variable} = ${res.solutions.joinToString(", ")}"
                    is SolutionResult.System -> res.solutions.entries.joinToString("\n") { "${it.key} = ${it.value}" }
                    is SolutionResult.Message -> res.message
                    is SolutionResult.Error -> "Error: ${res.error}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SOLVE EQUATION")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(resultText.ifEmpty { "Result will appear here..." }, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun AlgebraSubView() {
    val algebra = remember { AlgebraEngine() }
    var input by remember { mutableStateOf("2(x + 3)") }
    var resultText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Algebra Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Expression") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { resultText = "Expanded: " + algebra.expand(input) }, modifier = Modifier.weight(1f)) { Text("Expand") }
            Button(onClick = { resultText = "Factored: " + algebra.factor(input) }, modifier = Modifier.weight(1f)) { Text("Factor") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(resultText.ifEmpty { "Result..." }, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun FractionSubView() {
    var f1Str by remember { mutableStateOf("1/2") }
    var f2Str by remember { mutableStateOf("3/4") }
    var resultText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Exact Fractions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = f1Str, onValueChange = { f1Str = it }, label = { Text("Fraction 1 (e.g. 1/2)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = f2Str, onValueChange = { f2Str = it }, label = { Text("Fraction 2 (e.g. 3/4)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                try {
                    val res = Fraction.parse(f1Str) + Fraction.parse(f2Str)
                    resultText = "Sum: $res (${res.toMixedString()}) = ${res.toDecimal()}"
                } catch (e: Exception) { resultText = "Error" }
            }, modifier = Modifier.weight(1f)) { Text("Add (+)") }
            Button(onClick = {
                try {
                    val res = Fraction.parse(f1Str) * Fraction.parse(f2Str)
                    resultText = "Product: $res (${res.toMixedString()}) = ${res.toDecimal()}"
                } catch (e: Exception) { resultText = "Error" }
            }, modifier = Modifier.weight(1f)) { Text("Multiply (×)") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(resultText, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun MatrixSubView() {
    var a00 by remember { mutableStateOf("1") }; var a01 by remember { mutableStateOf("2") }
    var a10 by remember { mutableStateOf("3") }; var a11 by remember { mutableStateOf("4") }
    var resultText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("2x2 Matrix Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = a00, onValueChange = { a00 = it }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = a01, onValueChange = { a01 = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = a10, onValueChange = { a10 = it }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = a11, onValueChange = { a11 = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val m = Matrix(2, 2, arrayOf(doubleArrayOf(a00.toDouble(), a01.toDouble()), doubleArrayOf(a10.toDouble(), a11.toDouble())))
                resultText = "Determinant: ${m.determinant()}"
            }, modifier = Modifier.weight(1f)) { Text("Det") }
            Button(onClick = {
                try {
                    val m = Matrix(2, 2, arrayOf(doubleArrayOf(a00.toDouble(), a01.toDouble()), doubleArrayOf(a10.toDouble(), a11.toDouble())))
                    val inv = m.inverse()
                    resultText = "Inverse:\n[${inv.data[0][0]}, ${inv.data[0][1]}]\n[${inv.data[1][0]}, ${inv.data[1][1]}]"
                } catch (e: Exception) { resultText = "Error: Singular matrix" }
            }, modifier = Modifier.weight(1f)) { Text("Inverse") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { Text(resultText, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

@Composable
fun ComplexSubView() {
    var c1Str by remember { mutableStateOf("3 + 4i") }
    var c2Str by remember { mutableStateOf("1 - 2i") }
    var resultText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Complex Numbers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = c1Str, onValueChange = { c1Str = it }, label = { Text("Complex 1") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = c2Str, onValueChange = { c2Str = it }, label = { Text("Complex 2") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            try {
                val c1 = Complex.parse(c1Str)
                val c2 = Complex.parse(c2Str)
                val prod = c1 * c2
                resultText = "Product: $prod | |C1| = ${c1.magnitude()}"
            } catch (e: Exception) { resultText = "Error" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Multiply") }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { Text(resultText, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

@Composable
fun StatisticsSubView() {
    var valuesStr by remember { mutableStateOf("1, 2, 3, 4, 5") }
    var resultText by remember { mutableStateOf("") }
    val calc = remember { StatisticsCalculator() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Statistics Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = valuesStr, onValueChange = { valuesStr = it }, label = { Text("Comma-separated values") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            try {
                val list = valuesStr.split(",").map { it.trim().toDouble() }
                val res = calc.calculate(list)
                resultText = "Mean: ${res.mean}\nMedian: ${res.median}\nSum: ${res.sum}\nStdDev: ${res.standardDeviation}\nMin/Max: ${res.min} / ${res.max}"
            } catch (e: Exception) { resultText = "Error in values" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Calculate Stats") }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { Text(resultText, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

@Composable
fun ConverterSubView() {
    var valStr by remember { mutableStateOf("10") }
    var resultText by remember { mutableStateOf("") }
    val converter = remember { UnitConverter() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Unit Converter (Length)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = valStr, onValueChange = { valStr = it }, label = { Text("Value in Kilometers") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            val d = valStr.toDoubleOrNull() ?: 0.0
            val km = UnitConverter.LENGTH_UNITS.first { it.symbol == "km" }
            val mi = UnitConverter.LENGTH_UNITS.first { it.symbol == "mi" }
            val miles = converter.convert(d, km, mi, UnitCategory.LENGTH)
            resultText = "$d km = $miles miles"
        }, modifier = Modifier.fillMaxWidth()) { Text("Convert to Miles") }
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) { Text(resultText, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

@Composable
fun FormulasSubView() {
    var query by remember { mutableStateOf("") }
    val lib = remember { FormulaLibrary() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Formula Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search physics/math formulas") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        val formulas = lib.search(query)
        Card(modifier = Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                formulas.forEach { item ->
                    Text(item.name, fontWeight = FontWeight.Bold)
                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun GraphingSubView() {
    var expr by remember { mutableStateOf("x^2") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Function Grapher", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = expr, onValueChange = { expr = it }, label = { Text("Function y = f(x)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            GraphView(functionExpression = expr)
        }
    }
}
