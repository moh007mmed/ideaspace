package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.elements.parseHexColor
import kotlin.math.*

fun DrawScope.drawCanvasGrid(
    gridType: GridType,
    panX: Float,
    panY: Float,
    zoom: Float,
    backgroundColor: Color
) {
    if (gridType == GridType.NONE) return

    val step = 40f * zoom
    if (step < 10f) return // Avoid ultra dense rendering when zoomed far out

    val width = size.width
    val height = size.height

    val startX = (panX % step)
    val startY = (panY % step)

    val gridColor = if (backgroundColor.luminance() > 0.5f) {
        Color(0xFFCBD5E1).copy(alpha = 0.5f)
    } else {
        Color(0xFF334155).copy(alpha = 0.45f)
    }

    when (gridType) {
        GridType.DOTS -> {
            var x = startX
            val radius = 1.6f * minOf(zoom, 1.5f)
            while (x < width) {
                var y = startY
                while (y < height) {
                    drawCircle(gridColor, radius = radius, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }
        GridType.LINES -> {
            var x = startX
            while (x < width) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
                x += step
            }
            var y = startY
            while (y < height) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
                y += step
            }
        }
        GridType.BLUEPRINT -> {
            // Blueprint subgrid
            val fineStep = step / 4f
            if (fineStep > 6f) {
                var fx = (panX % fineStep)
                val fineColor = gridColor.copy(alpha = 0.2f)
                while (fx < width) {
                    drawLine(fineColor, start = Offset(fx, 0f), end = Offset(fx, height), strokeWidth = 0.7f)
                    fx += fineStep
                }
                var fy = (panY % fineStep)
                while (fy < height) {
                    drawLine(fineColor, start = Offset(0f, fy), end = Offset(width, fy), strokeWidth = 0.7f)
                    fy += fineStep
                }
            }
            var x = startX
            while (x < width) {
                drawLine(gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1.2f)
                x += step
            }
            var y = startY
            while (y < height) {
                drawLine(gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.2f)
                y += step
            }
        }
        GridType.NONE -> {}
    }
}

fun DrawScope.drawCanvasConnections(
    connections: List<ElementConnection>,
    elementsMap: Map<String, CanvasElement>,
    panX: Float,
    panY: Float,
    zoom: Float
) {
    for (conn in connections) {
        val fromElem = elementsMap[conn.fromElementId] ?: continue
        val toElem = elementsMap[conn.toElementId] ?: continue

        // Center points in canvas coordinates
        val fromCenterX = fromElem.posX + fromElem.width / 2f
        val fromCenterY = fromElem.posY + fromElem.height / 2f
        val toCenterX = toElem.posX + toElem.width / 2f
        val toCenterY = toElem.posY + toElem.height / 2f

        // Convert to screen coordinates
        val startX = fromCenterX * zoom + panX
        val startY = fromCenterY * zoom + panY
        val endX = toCenterX * zoom + panX
        val endY = toCenterY * zoom + panY

        val color = parseHexColor(conn.colorHex, Color(0xFF60A5FA))
        val strokeW = (conn.strokeWidth * zoom).coerceIn(1.5f, 8f)

        val pathEffect = if (conn.isDashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f) else null

        val path = Path()
        when (conn.type) {
            ConnectorType.STRAIGHT -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)
            }
            ConnectorType.CURVED -> {
                path.moveTo(startX, startY)
                val midX = (startX + endX) / 2f
                val midY = (startY + endY) / 2f
                val dx = endX - startX
                val dy = endY - startY
                // Control offset perpendicular to line
                val ctrlX = midX - dy * 0.15f
                val ctrlY = midY + dx * 0.15f
                path.quadraticTo(ctrlX, ctrlY, endX, endY)
            }
            ConnectorType.ELBOW -> {
                path.moveTo(startX, startY)
                val midX = (startX + endX) / 2f
                path.lineTo(midX, startY)
                path.lineTo(midX, endY)
                path.lineTo(endX, endY)
            }
            ConnectorType.ARROW -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeW,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = pathEffect
            )
        )

        // Arrow head at end point
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        val arrowLength = 16f * zoom.coerceIn(0.6f, 1.5f)
        val arrowAngle = Math.PI / 6.0

        val x1 = endX - arrowLength * cos(angle - arrowAngle).toFloat()
        val y1 = endY - arrowLength * sin(angle - arrowAngle).toFloat()
        val x2 = endX - arrowLength * cos(angle + arrowAngle).toFloat()
        val y2 = endY - arrowLength * sin(angle + arrowAngle).toFloat()

        val arrowPath = Path().apply {
            moveTo(endX, endY)
            lineTo(x1, y1)
            lineTo(x2, y2)
            close()
        }
        drawPath(arrowPath, color)
    }
}

// MiniMap Composable
@Composable
fun CanvasMiniMap(
    elements: List<CanvasElement>,
    panX: Float,
    panY: Float,
    zoom: Float,
    onNavigate: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (elements.isEmpty()) return

    Box(
        modifier = modifier
            .size(130.dp, 90.dp)
            .background(Color(0xFF0F172A).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val mapW = size.width
            val mapH = size.height

            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE

            for (e in elements) {
                minX = minOf(minX, e.posX)
                minY = minOf(minY, e.posY)
                maxX = maxOf(maxX, e.posX + e.width)
                maxY = maxOf(maxY, e.posY + e.height)
            }

            val contentW = maxOf(maxX - minX, 100f) + 200f
            val contentH = maxOf(maxY - minY, 100f) + 200f

            val scale = minOf(mapW / contentW, mapH / contentH)

            // Draw elements as mini dots/rects
            for (e in elements) {
                val ex = (e.posX - minX + 100f) * scale
                val ey = (e.posY - minY + 100f) * scale
                val ew = maxOf(e.width * scale, 3f)
                val eh = maxOf(e.height * scale, 2f)
                val c = parseHexColor(e.colorHex, Color(0xFF38BDF8)).copy(alpha = 0.8f)
                drawRoundRect(
                    c,
                    topLeft = Offset(ex, ey),
                    size = androidx.compose.ui.geometry.Size(ew, eh),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            // Draw viewport indicator
            val screenW = 1080f
            val screenH = 1920f
            val viewLeft = (-panX / zoom - minX + 100f) * scale
            val viewTop = (-panY / zoom - minY + 100f) * scale
            val viewW = (screenW / zoom) * scale
            val viewH = (screenH / zoom) * scale

            drawRect(
                Color(0xFF38BDF8),
                topLeft = Offset(viewLeft, viewTop),
                size = androidx.compose.ui.geometry.Size(viewW.coerceAtLeast(8f), viewH.coerceAtLeast(6f)),
                style = Stroke(1.2f)
            )
        }
    }
}
