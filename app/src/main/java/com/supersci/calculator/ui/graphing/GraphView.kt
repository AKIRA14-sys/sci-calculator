package com.supersci.calculator.ui.graphing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supersci.calculator.math.ExpressionEvaluator

@OptIn(ExperimentalTextApi::class)
@Composable
fun GraphView(
    functionExpression: String,
    modifier: Modifier = Modifier
) {
    val evaluator = remember { ExpressionEvaluator() }
    val textMeasurer = rememberTextMeasurer()

    var scale by remember { mutableFloatStateOf(50f) } // pixels per unit
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(10f, 300f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val originX = width / 2f + offsetX
        val originY = height / 2f + offsetY

        // Draw grid
        val step = scale
        var xGrid = originX % step
        while (xGrid < width) {
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(xGrid, 0f),
                end = Offset(xGrid, height),
                strokeWidth = 1.dp.toPx()
            )
            xGrid += step
        }

        var yGrid = originY % step
        while (yGrid < height) {
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, yGrid),
                end = Offset(width, yGrid),
                strokeWidth = 1.dp.toPx()
            )
            yGrid += step
        }

        // Draw Axes
        drawLine(
            color = Color.Gray,
            start = Offset(0f, originY),
            end = Offset(width, originY),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = Offset(originX, 0f),
            end = Offset(originX, height),
            strokeWidth = 2.dp.toPx()
        )

        // Draw Origin Label
        drawText(
            textMeasurer = textMeasurer,
            text = "0",
            style = TextStyle(color = Color.Gray, fontSize = 12.sp),
            topLeft = Offset(originX + 4f, originY + 4f)
        )

        // Plot Function
        if (functionExpression.isNotBlank()) {
            val path = Path()
            var firstPoint = true

            val sampleStep = 2f // pixels
            var pX = 0f
            while (pX <= width) {
                val mathX = (pX - originX) / scale
                try {
                    val expr = functionExpression.lowercase().replace("y=", "")
                    val mathY = evaluator.evaluate(expr, variables = mapOf("x" to mathX.toDouble()))
                    if (!mathY.isNaN() && !mathY.isInfinite()) {
                        val pY = originY - (mathY.toFloat() * scale)
                        if (pY in -height..height * 2) {
                            if (firstPoint) {
                                path.moveTo(pX, pY)
                                firstPoint = false
                            } else {
                                path.lineTo(pX, pY)
                            }
                        } else {
                            firstPoint = true
                        }
                    } else {
                        firstPoint = true
                    }
                } catch (e: Exception) {
                    firstPoint = true
                }
                pX += sampleStep
            }

            drawPath(
                path = path,
                color = Color(0xFF00E676),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
