package com.example.model

enum class ElementType {
    TEXT,
    STICKY_NOTE,
    SHAPE,
    MIND_MAP_NODE,
    DRAWING,
    IMAGE,
    FILE_CARD,
    TASK_CARD
}

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECT,
    CIRCLE,
    ELLIPSE,
    DIAMOND,
    TRIANGLE,
    HEXAGON,
    STAR,
    CLOUD,
    SPEECH_BUBBLE,
    CYLINDER,
    ARROW
}

enum class ConnectorType {
    STRAIGHT,
    CURVED,
    ELBOW,
    ARROW
}

enum class InteractionMode {
    NAVIGATION, // Pan & zoom freely
    EDITING,    // Tap, select, drag, edit elements
    DRAWING,    // Freehand sketch strokes on canvas
    CONNECTING  // Tap source then target element to create connection line
}

enum class GridType {
    DOTS,
    LINES,
    BLUEPRINT,
    NONE
}

data class Point(val x: Float, val y: Float)

data class ChecklistItem(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)

data class DrawingStroke(
    val id: String,
    val points: List<Point>,
    val colorHex: String = "#38BDF8",
    val strokeWidth: Float = 4f,
    val isHighlighter: Boolean = false
)

data class CanvasElement(
    val id: String,
    val boardId: String,
    val type: ElementType,
    val posX: Float,
    val posY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val zIndex: Int = 0,
    val layerId: String = "default_layer",
    val groupId: String? = null,
    val isLocked: Boolean = false,
    val isPinned: Boolean = false,
    val isCollapsed: Boolean = false,
    val title: String = "",
    val content: String = "",
    val colorHex: String = "#FDE047", // Default sticky yellow
    val textColorHex: String = "#1E293B",
    val strokeColorHex: String = "#CBD5E1",
    val strokeWidth: Float = 2f,
    val opacity: Float = 1.0f,
    val fontSize: Float = 16f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textAlign: String = "START", // START, CENTER, END
    val shapeType: ShapeType = ShapeType.ROUNDED_RECT,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val status: String = "TODO", // TODO, IN_PROGRESS, DONE
    val dueDate: String = "",
    val tags: List<String> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val strokes: List<DrawingStroke> = emptyList(),
    val parentNodeId: String? = null,
    val fileType: String = "DOC",
    val imageUrl: String = "",
    val extraData: String = ""
)

data class ElementConnection(
    val id: String,
    val boardId: String,
    val fromElementId: String,
    val toElementId: String,
    val type: ConnectorType = ConnectorType.CURVED,
    val label: String = "",
    val colorHex: String = "#60A5FA",
    val strokeWidth: Float = 2.5f,
    val isDashed: Boolean = false
)

data class Layer(
    val id: String,
    val boardId: String,
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val orderIndex: Int = 0
)

data class Board(
    val id: String,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val theme: String = "DARK_SLATE", // DARK_SLATE, OLED_BLACK, CLEAN_LIGHT
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val gridType: GridType = GridType.DOTS,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
)

data class HistorySnapshot(
    val timestamp: Long,
    val label: String,
    val elements: List<CanvasElement>,
    val connections: List<ElementConnection>
)
