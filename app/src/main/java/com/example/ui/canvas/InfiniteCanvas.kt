package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.elements.*
import com.example.viewmodel.IdeaSpaceViewModel
import kotlin.math.roundToInt

@Composable
fun InfiniteCanvas(
    viewModel: IdeaSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val viewport by viewModel.viewport.collectAsState()
    val elements by viewModel.elements.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val mode by viewModel.interactionMode.collectAsState()
    val selectedIds by viewModel.selectedElementIds.collectAsState()
    val gridType by viewModel.gridType.collectAsState()
    val activeDrawingPoints by viewModel.activeDrawingPoints.collectAsState()
    val drawingColor by viewModel.drawingColorHex.collectAsState()
    val drawingStrokeW by viewModel.drawingStrokeWidth.collectAsState()
    val isHighlighter by viewModel.isDrawingHighlighter.collectAsState()
    val connectingSourceId by viewModel.connectingSourceId.collectAsState()

    val currentBoard by viewModel.currentBoard.collectAsState()
    val canvasBg = when (currentBoard?.theme) {
        "OLED_BLACK" -> Color(0xFF000000)
        "CLEAN_LIGHT" -> Color(0xFFF8FAFC)
        else -> Color(0xFF0B0F19) // Dark Slate default
    }

    val elementsMap = remember(elements) { elements.associateBy { it.id } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (currentBoard?.theme == "OLED_BLACK" || currentBoard?.theme == "CLEAN_LIGHT") canvasBg else Color.Transparent)
            // Background Canvas Gestures
            .pointerInput(mode) {
                when (mode) {
                    InteractionMode.NAVIGATION -> {
                        // Pinch to zoom and 1-finger pan
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            viewModel.updatePanZoom(
                                deltaX = pan.x,
                                deltaY = pan.y,
                                zoomFactor = zoom,
                                focusX = centroid.x,
                                focusY = centroid.y
                            )
                        }
                    }
                    InteractionMode.EDITING -> {
                        // In editing mode, 2-finger pan & pinch zoom on empty area
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            viewModel.updatePanZoom(
                                deltaX = pan.x,
                                deltaY = pan.y,
                                zoomFactor = zoom,
                                focusX = centroid.x,
                                focusY = centroid.y
                            )
                        }
                    }
                    InteractionMode.DRAWING -> {
                        // Freehand touch gestures
                        detectDragGestures(
                            onDragStart = { offset ->
                                val canvasX = (offset.x - viewport.panX) / viewport.zoom
                                val canvasY = (offset.y - viewport.panY) / viewport.zoom
                                viewModel.addDrawingPoint(Point(canvasX, canvasY))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val canvasX = (change.position.x - viewport.panX) / viewport.zoom
                                val canvasY = (change.position.y - viewport.panY) / viewport.zoom
                                viewModel.addDrawingPoint(Point(canvasX, canvasY))
                            },
                            onDragEnd = {
                                viewModel.finishDrawingStroke()
                            },
                            onDragCancel = {
                                viewModel.finishDrawingStroke()
                            }
                        )
                    }
                    InteractionMode.CONNECTING -> {
                        // Tap elements or canvas
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (mode == InteractionMode.EDITING) {
                            viewModel.clearSelection()
                        }
                    }
                )
            }
    ) {
        // 1. Grid Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCanvasGrid(
                gridType = gridType,
                panX = viewport.panX,
                panY = viewport.panY,
                zoom = viewport.zoom,
                backgroundColor = canvasBg
            )
        }

        // 2. Connections Layer Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCanvasConnections(
                connections = connections,
                elementsMap = elementsMap,
                panX = viewport.panX,
                panY = viewport.panY,
                zoom = viewport.zoom
            )
        }

        // 3. Elements Layer (Positioned by panX + x * zoom)
        elements.forEach { element ->
            val isSelected = selectedIds.contains(element.id)
            val isConnectingSource = connectingSourceId == element.id

            val screenX = (element.posX * viewport.zoom + viewport.panX).roundToInt()
            val screenY = (element.posY * viewport.zoom + viewport.panY).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(screenX, screenY) }
                    // Gestures on individual element
                    .pointerInput(element.id, mode) {
                        if (mode == InteractionMode.EDITING) {
                            detectDragGestures(
                                onDragStart = {
                                    viewModel.selectElement(element.id)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val deltaCanvasX = dragAmount.x / viewport.zoom
                                    val deltaCanvasY = dragAmount.y / viewport.zoom
                                    viewModel.moveElement(element.id, deltaCanvasX, deltaCanvasY)
                                }
                            )
                        }
                    }
                    .pointerInput(element.id, mode) {
                        detectTapGestures(
                            onTap = {
                                viewModel.selectElement(element.id)
                            },
                            onDoubleTap = {
                                viewModel.editingElement.value = element
                            }
                        )
                    }
            ) {
                when (element.type) {
                    ElementType.TEXT -> TextElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource,
                        onEdit = { viewModel.editingElement.value = element }
                    )
                    ElementType.STICKY_NOTE -> StickyNoteElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource,
                        onToggleChecklist = { checkId -> viewModel.toggleChecklistItem(element.id, checkId) },
                        onEdit = { viewModel.editingElement.value = element }
                    )
                    ElementType.SHAPE -> ShapeElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource
                    )
                    ElementType.MIND_MAP_NODE -> MindMapNodeView(
                        element = element,
                        isSelected = isSelected || isConnectingSource,
                        onAddChild = { viewModel.addMindMapChild(element.id) },
                        onAddSibling = { viewModel.addMindMapSibling(element.id) },
                        onEdit = { viewModel.editingElement.value = element }
                    )
                    ElementType.DRAWING -> DrawingElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource
                    )
                    ElementType.TASK_CARD -> TaskCardElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource,
                        onStatusChange = { newStatus -> viewModel.updateTaskStatus(element.id, newStatus) }
                    )
                    ElementType.FILE_CARD -> FileCardElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource
                    )
                    ElementType.IMAGE -> StickyNoteElementView(
                        element = element,
                        isSelected = isSelected || isConnectingSource,
                        onToggleChecklist = {},
                        onEdit = { viewModel.editingElement.value = element }
                    )
                }
            }
        }

        // 4. Live Freehand Stroke while finger is actively drawing
        if (mode == InteractionMode.DRAWING && activeDrawingPoints.size >= 2) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val first = activeDrawingPoints[0]
                val screenFirstX = first.x * viewport.zoom + viewport.panX
                val screenFirstY = first.y * viewport.zoom + viewport.panY
                path.moveTo(screenFirstX, screenFirstY)

                for (i in 1 until activeDrawingPoints.size) {
                    val p = activeDrawingPoints[i]
                    val sx = p.x * viewport.zoom + viewport.panX
                    val sy = p.y * viewport.zoom + viewport.panY
                    path.lineTo(sx, sy)
                }

                val color = parseHexColor(drawingColor, Color(0xFF38BDF8))
                    .copy(alpha = if (isHighlighter) 0.45f else 1.0f)

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = (drawingStrokeW * viewport.zoom).coerceIn(2f, 20f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
