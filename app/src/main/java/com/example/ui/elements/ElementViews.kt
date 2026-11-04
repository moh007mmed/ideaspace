package com.example.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.util.AppStrings

// Helper to parse hex color safely
fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF64748B)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> "FF$clean".toLong(16)
            8 -> clean.toLong(16)
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (_: Exception) {
        defaultColor
    }
}

// 1. TEXT ELEMENT VIEW
@Composable
fun TextElementView(
    element: CanvasElement,
    isSelected: Boolean,
    onEdit: () -> Unit
) {
    val textColor = parseHexColor(element.textColorHex, Color.White)
    val align = when (element.textAlign) {
        "CENTER" -> TextAlign.Center
        "END" -> TextAlign.End
        else -> TextAlign.Start
    }

    Box(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .rotate(element.rotation)
            .then(
                if (isSelected) Modifier.border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                else Modifier
            )
            .padding(6.dp),
        contentAlignment = when (element.textAlign) {
            "CENTER" -> Alignment.Center
            "END" -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = when (element.textAlign) {
                "CENTER" -> Alignment.CenterHorizontally
                "END" -> Alignment.End
                else -> Alignment.Start
            }
        ) {
            if (element.title.isNotBlank()) {
                Text(
                    text = element.title,
                    fontSize = (element.fontSize + 4).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = align,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (element.content.isNotBlank()) {
                Text(
                    text = element.content,
                    fontSize = element.fontSize.sp,
                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (element.isUnderline) TextDecoration.Underline else TextDecoration.None,
                    color = textColor.copy(alpha = 0.9f),
                    textAlign = align,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 2. STICKY NOTE VIEW
@Composable
fun StickyNoteElementView(
    element: CanvasElement,
    isSelected: Boolean,
    onToggleChecklist: (String) -> Unit,
    onEdit: () -> Unit
) {
    val bgColor = parseHexColor(element.colorHex, Color(0xFFFEF08A))
    val textColor = parseHexColor(element.textColorHex, Color(0xFF1E293B))

    Surface(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .rotate(element.rotation)
            .shadow(if (isSelected) 10.dp else 4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF38BDF8)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // Header Row: Pin / Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (element.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (element.priority.isNotBlank()) {
                    val priorityColor = when (element.priority) {
                        "HIGH" -> Color(0xFFEF4444)
                        "MEDIUM" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = element.priority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor
                        )
                    }
                }
            }

            // Note Content & Checklist
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 22.dp, bottom = 4.dp)
            ) {
                if (element.title.isNotBlank()) {
                    Text(
                        text = element.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (element.content.isNotBlank()) {
                    Text(
                        text = element.content,
                        fontSize = 13.sp,
                        color = textColor.copy(alpha = 0.85f),
                        lineHeight = 17.sp,
                        maxLines = if (element.checklist.isEmpty()) 7 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Checklist
                if (element.checklist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    element.checklist.take(4).forEach { check ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleChecklist(check.id) }
                                .padding(vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = if (check.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (check.isDone) Color(0xFF10B981) else textColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = check.text,
                                fontSize = 12.sp,
                                color = if (check.isDone) textColor.copy(alpha = 0.5f) else textColor,
                                textDecoration = if (check.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Tags
                if (element.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        element.tags.take(2).forEach { tag ->
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. SHAPE ELEMENT VIEW
@Composable
fun ShapeElementView(
    element: CanvasElement,
    isSelected: Boolean
) {
    val fillColor = parseHexColor(element.colorHex, Color(0xFF3B82F6)).copy(alpha = element.opacity)
    val strokeColor = if (isSelected) Color(0xFF38BDF8) else parseHexColor(element.strokeColorHex, Color(0xFF93C5FD))
    val textColor = parseHexColor(element.textColorHex, Color.White)
    val strokeW = if (isSelected) (element.strokeWidth + 1.5f).dp else element.strokeWidth.dp

    Box(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .rotate(element.rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGeometryShape(
                shapeType = element.shapeType,
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidthPx = strokeW.toPx()
            )
        }

        // Centered Text Label inside shape
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (element.title.isNotBlank()) {
                Text(
                    text = element.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (element.content.isNotBlank()) {
                Text(
                    text = element.content,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun DrawScope.drawGeometryShape(
    shapeType: ShapeType,
    fillColor: Color,
    strokeColor: Color,
    strokeWidthPx: Float
) {
    val w = size.width
    val h = size.height

    when (shapeType) {
        ShapeType.RECTANGLE -> {
            drawRect(fillColor)
            drawRect(strokeColor, style = Stroke(strokeWidthPx))
        }
        ShapeType.ROUNDED_RECT -> {
            val r = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            drawRoundRect(fillColor, cornerRadius = r)
            drawRoundRect(strokeColor, cornerRadius = r, style = Stroke(strokeWidthPx))
        }
        ShapeType.CIRCLE, ShapeType.ELLIPSE -> {
            drawOval(fillColor)
            drawOval(strokeColor, style = Stroke(strokeWidthPx))
        }
        ShapeType.DIAMOND -> {
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h / 2f)
                lineTo(w / 2f, h)
                lineTo(0f, h / 2f)
                close()
            }
            drawPath(path, fillColor)
            drawPath(path, strokeColor, style = Stroke(strokeWidthPx))
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo(w / 2f, 0f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, fillColor)
            drawPath(path, strokeColor, style = Stroke(strokeWidthPx))
        }
        ShapeType.HEXAGON -> {
            val path = Path().apply {
                moveTo(w * 0.25f, 0f)
                lineTo(w * 0.75f, 0f)
                lineTo(w, h * 0.5f)
                lineTo(w * 0.75f, h)
                lineTo(w * 0.25f, h)
                lineTo(0f, h * 0.5f)
                close()
            }
            drawPath(path, fillColor)
            drawPath(path, strokeColor, style = Stroke(strokeWidthPx))
        }
        ShapeType.CLOUD, ShapeType.SPEECH_BUBBLE -> {
            val r = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
            drawRoundRect(fillColor, cornerRadius = r)
            drawRoundRect(strokeColor, cornerRadius = r, style = Stroke(strokeWidthPx))
        }
        else -> {
            val r = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            drawRoundRect(fillColor, cornerRadius = r)
            drawRoundRect(strokeColor, cornerRadius = r, style = Stroke(strokeWidthPx))
        }
    }
}

// 4. MIND MAP NODE VIEW
@Composable
fun MindMapNodeView(
    element: CanvasElement,
    isSelected: Boolean,
    onAddChild: () -> Unit,
    onAddSibling: () -> Unit,
    onEdit: () -> Unit
) {
    val nodeColor = parseHexColor(element.colorHex, Color(0xFF6366F1))
    val textColor = parseHexColor(element.textColorHex, Color.White)
    val isRoot = element.parentNodeId == null

    Column(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .rotate(element.rotation),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(if (isRoot) 20.dp else 12.dp)),
            shape = RoundedCornerShape(if (isRoot) 20.dp else 12.dp),
            color = nodeColor,
            border = if (isSelected) BorderStroke(2.5.dp, Color(0xFF38BDF8)) else null
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = element.title.ifBlank { "Mind Map Idea" },
                        fontSize = (if (isRoot) 16 else 14).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (element.content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = element.content,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Add Branch Button (+) when selected
                if (isSelected) {
                    IconButton(
                        onClick = onAddChild,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Branch",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 5. DRAWING STROKE ELEMENT VIEW
@Composable
fun DrawingElementView(
    element: CanvasElement,
    isSelected: Boolean
) {
    Canvas(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .then(
                if (isSelected) Modifier.border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                else Modifier
            )
    ) {
        for (stroke in element.strokes) {
            val color = parseHexColor(stroke.colorHex, Color(0xFF38BDF8))
                .copy(alpha = if (stroke.isHighlighter) 0.45f else 1.0f)
            val strokeW = stroke.strokeWidth.dp.toPx()

            if (stroke.points.size >= 2) {
                val path = Path()
                // Offset relative to element's bounding box
                val first = stroke.points[0]
                path.moveTo(first.x - element.posX, first.y - element.posY)
                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    path.lineTo(p.x - element.posX, p.y - element.posY)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeW,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

// 6. TASK / KANBAN CARD VIEW
@Composable
fun TaskCardElementView(
    element: CanvasElement,
    isSelected: Boolean,
    onStatusChange: (String) -> Unit
) {
    val statusColor = when (element.status) {
        "DONE" -> Color(0xFF10B981)
        "IN_PROGRESS" -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    Surface(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .rotate(element.rotation)
            .shadow(if (isSelected) 10.dp else 4.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .clickable {
                            val next = when (element.status) {
                                "TODO" -> "IN_PROGRESS"
                                "IN_PROGRESS" -> "DONE"
                                else -> "TODO"
                            }
                            onStatusChange(next)
                        }
                ) {
                    Text(
                        text = when (element.status) {
                            "TODO" -> AppStrings.todo
                            "IN_PROGRESS" -> AppStrings.inProgress
                            else -> AppStrings.done
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (element.priority.isNotBlank()) {
                    Text(
                        text = element.priority,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = element.title.ifBlank { "Untitled Task" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (element.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = element.content,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (element.dueDate.isNotBlank()) {
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = element.dueDate, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

// 7. FILE & CODE CARD VIEW
@Composable
fun FileCardElementView(
    element: CanvasElement,
    isSelected: Boolean
) {
    Surface(
        modifier = Modifier
            .size(element.width.dp, element.height.dp)
            .shadow(if (isSelected) 10.dp else 4.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (element.fileType) {
                        "CODE" -> Icons.Default.Code
                        "MD" -> Icons.Default.Description
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = element.title.ifBlank { "Document" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = element.content,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFF38BDF8).copy(alpha = 0.9f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
