package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.IdeaSpaceRepository
import com.example.model.*
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.SampleBoards
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class WorkspaceView {
    CANVAS,
    KANBAN,
    OUTLINE,
    KNOWLEDGE_GRAPH,
    PRESENTATION,
    IDEA_UNIVERSE
}

data class CanvasViewport(
    val panX: Float = 0f,
    val panY: Float = 0f,
    val zoom: Float = 1.0f
)

class IdeaSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IdeaSpaceRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = IdeaSpaceRepository(db.ideaSpaceDao())
    }

    // Boards
    val boards: StateFlow<List<Board>> = repository.getAllBoards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentBoardId = MutableStateFlow<String>(SampleBoards.DEFAULT_BOARD_ID)
    val currentBoardId: StateFlow<String> = _currentBoardId.asStateFlow()

    val currentBoard: StateFlow<Board?> = _currentBoardId.flatMapLatest { id ->
        repository.getBoard(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val elements: StateFlow<List<CanvasElement>> = _currentBoardId.flatMapLatest { id ->
        repository.getElements(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connections: StateFlow<List<ElementConnection>> = _currentBoardId.flatMapLatest { id ->
        repository.getConnections(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val layers: StateFlow<List<Layer>> = _currentBoardId.flatMapLatest { id ->
        repository.getLayers(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Canvas & Viewport State
    private val _viewport = MutableStateFlow(CanvasViewport(panX = 120f, panY = 150f, zoom = 0.95f))
    val viewport: StateFlow<CanvasViewport> = _viewport.asStateFlow()

    private val _interactionMode = MutableStateFlow(InteractionMode.EDITING)
    val interactionMode: StateFlow<InteractionMode> = _interactionMode.asStateFlow()

    private val _selectedElementIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedElementIds: StateFlow<Set<String>> = _selectedElementIds.asStateFlow()

    private val _gridType = MutableStateFlow(GridType.DOTS)
    val gridType: StateFlow<GridType> = _gridType.asStateFlow()

    private val _snapToGrid = MutableStateFlow(false)
    val snapToGrid: StateFlow<Boolean> = _snapToGrid.asStateFlow()

    private val _workspaceView = MutableStateFlow(WorkspaceView.CANVAS)
    val workspaceView: StateFlow<WorkspaceView> = _workspaceView.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Freehand Drawing live state
    private val _activeDrawingPoints = MutableStateFlow<List<Point>>(emptyList())
    val activeDrawingPoints: StateFlow<List<Point>> = _activeDrawingPoints.asStateFlow()

    val drawingColorHex = MutableStateFlow("#38BDF8")
    val drawingStrokeWidth = MutableStateFlow(4f)
    val isDrawingHighlighter = MutableStateFlow(false)

    // Connection in progress
    private val _connectingSourceId = MutableStateFlow<String?>(null)
    val connectingSourceId: StateFlow<String?> = _connectingSourceId.asStateFlow()

    // UI Sheets & Dialogs
    val showAddDialog = MutableStateFlow(false)
    val editingElement = MutableStateFlow<CanvasElement?>(null)
    val showTemplatesDialog = MutableStateFlow(false)
    val showLayersSheet = MutableStateFlow(false)
    val showExportImportDialog = MutableStateFlow(false)
    val showTimeTravelDialog = MutableStateFlow(false)
    val showQuickCaptureDialog = MutableStateFlow(false)
    val showBoardsDrawer = MutableStateFlow(false)

    // History Snapshots for Undo/Redo & Time Travel
    private val _undoStack = MutableStateFlow<List<HistorySnapshot>>(emptyList())
    val undoStack: StateFlow<List<HistorySnapshot>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<HistorySnapshot>>(emptyList())
    val redoStack: StateFlow<List<HistorySnapshot>> = _redoStack.asStateFlow()

    val historySnapshots = MutableStateFlow<List<HistorySnapshot>>(emptyList())

    // Language state
    val appLanguage = MutableStateFlow(AppLanguage.ARABIC)

    init {
        // Ensure starter board exists
        viewModelScope.launch {
            repository.getAllBoards().first().let { existing ->
                if (existing.isEmpty()) {
                    val (starterBoard, starterElements) = SampleBoards.createInitialBoard()
                    val starterConnections = SampleBoards.createInitialConnections()
                    val starterLayers = SampleBoards.createInitialLayers()
                    repository.saveBoard(starterBoard)
                    repository.saveElements(starterElements)
                    repository.saveConnections(starterConnections)
                    repository.saveLayers(starterLayers)
                    recordSnapshot("Initial Seed", starterElements, starterConnections)
                } else {
                    _currentBoardId.value = existing.first().id
                    val cur = existing.first()
                    _viewport.value = CanvasViewport(cur.panX, cur.panY, cur.zoom)
                    _gridType.value = cur.gridType
                }
            }
        }
    }

    fun toggleLanguage() {
        val next = if (appLanguage.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
        appLanguage.value = next
        AppStrings.currentLanguage = next
    }

    fun selectBoard(boardId: String) {
        _currentBoardId.value = boardId
        _selectedElementIds.value = emptySet()
        showBoardsDrawer.value = false
        viewModelScope.launch {
            repository.getBoard(boardId).first()?.let { b ->
                _viewport.value = CanvasViewport(b.panX, b.panY, b.zoom)
                _gridType.value = b.gridType
            }
        }
    }

    fun createNewBoard(title: String) {
        val newId = "board_" + UUID.randomUUID().toString().take(8)
        val board = Board(
            id = newId,
            title = if (title.isBlank()) (if (AppStrings.isArabic()) "لوحة فكرة جديدة" else "New Idea Board") else title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveBoard(board)
            repository.saveLayers(listOf(Layer("layer_default_$newId", newId, "Default Layer", true, false, 1.0f, 0)))
            _currentBoardId.value = newId
            _viewport.value = CanvasViewport(0f, 0f, 1.0f)
            _selectedElementIds.value = emptySet()
            showBoardsDrawer.value = false
        }
    }

    fun deleteCurrentBoard() {
        val id = _currentBoardId.value
        viewModelScope.launch {
            repository.deleteBoard(id)
            val remaining = repository.getAllBoards().first()
            if (remaining.isNotEmpty()) {
                selectBoard(remaining.first().id)
            } else {
                val (starterBoard, starterElements) = SampleBoards.createInitialBoard()
                repository.saveBoard(starterBoard)
                repository.saveElements(starterElements)
                selectBoard(starterBoard.id)
            }
        }
    }

    // Viewport manipulations
    fun updatePanZoom(deltaX: Float, deltaY: Float, zoomFactor: Float = 1.0f, focusX: Float = 0f, focusY: Float = 0f) {
        val current = _viewport.value
        val newZoom = (current.zoom * zoomFactor).coerceIn(0.18f, 3.5f)
        val newPanX = current.panX + deltaX
        val newPanY = current.panY + deltaY
        _viewport.value = CanvasViewport(newPanX, newPanY, newZoom)
    }

    fun centerView() {
        _viewport.value = CanvasViewport(panX = 0f, panY = 0f, zoom = 1.0f)
    }

    fun fitAllContent() {
        val currentElements = elements.value
        if (currentElements.isEmpty()) {
            centerView()
            return
        }
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (e in currentElements) {
            minX = minOf(minX, e.posX)
            minY = minOf(minY, e.posY)
            maxX = maxOf(maxX, e.posX + e.width)
            maxY = maxOf(maxY, e.posY + e.height)
        }

        val padding = 120f
        val contentW = (maxX - minX) + padding * 2
        val contentH = (maxY - minY) + padding * 2

        val screenW = 1080f
        val screenH = 1920f

        val scaleX = screenW / maxOf(contentW, 100f)
        val scaleY = screenH / maxOf(contentH, 100f)
        val targetZoom = minOf(scaleX, scaleY).coerceIn(0.25f, 1.5f)

        val targetPanX = -minX * targetZoom + (screenW - contentW * targetZoom) / 2f
        val targetPanY = -minY * targetZoom + (screenH - contentH * targetZoom) / 2f

        _viewport.value = CanvasViewport(targetPanX, targetPanY, targetZoom)
    }

    fun zoomIn() {
        val v = _viewport.value
        _viewport.value = v.copy(zoom = (v.zoom * 1.25f).coerceAtMost(3.5f))
    }

    fun zoomOut() {
        val v = _viewport.value
        _viewport.value = v.copy(zoom = (v.zoom / 1.25f).coerceAtLeast(0.18f))
    }

    fun setInteractionMode(mode: InteractionMode) {
        _interactionMode.value = mode
        if (mode != InteractionMode.CONNECTING) {
            _connectingSourceId.value = null
        }
    }

    fun setWorkspaceView(view: WorkspaceView) {
        _workspaceView.value = view
    }

    fun setGridType(type: GridType) {
        _gridType.value = type
    }

    fun toggleSnapToGrid() {
        _snapToGrid.value = !_snapToGrid.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Element selection
    fun selectElement(elementId: String, multiSelect: Boolean = false) {
        if (_interactionMode.value == InteractionMode.CONNECTING) {
            val source = _connectingSourceId.value
            if (source == null) {
                _connectingSourceId.value = elementId
            } else if (source != elementId) {
                createConnection(source, elementId, if (AppStrings.isArabic()) "يرتبط بـ" else "Connected to")
                _connectingSourceId.value = null
                _interactionMode.value = InteractionMode.EDITING
            }
            return
        }

        if (multiSelect) {
            val current = _selectedElementIds.value.toMutableSet()
            if (current.contains(elementId)) current.remove(elementId) else current.add(elementId)
            _selectedElementIds.value = current
        } else {
            _selectedElementIds.value = setOf(elementId)
        }
    }

    fun clearSelection() {
        _selectedElementIds.value = emptySet()
    }

    // Move element with optional snap
    fun moveElement(elementId: String, deltaX: Float, deltaY: Float) {
        val elem = elements.value.find { it.id == elementId } ?: return
        if (elem.isLocked || elem.isPinned) return

        var newX = elem.posX + deltaX
        var newY = elem.posY + deltaY

        if (_snapToGrid.value) {
            val gridSize = 40f
            newX = kotlin.math.round(newX / gridSize) * gridSize
            newY = kotlin.math.round(newY / gridSize) * gridSize
        }

        val updated = elem.copy(posX = newX, posY = newY)
        viewModelScope.launch {
            repository.saveElement(updated)
        }
    }

    // CRUD Elements
    fun addElement(element: CanvasElement) {
        saveSnapshot("Add ${element.type.name}")
        viewModelScope.launch {
            repository.saveElement(element)
            _selectedElementIds.value = setOf(element.id)
        }
    }

    fun updateElement(element: CanvasElement) {
        viewModelScope.launch {
            repository.saveElement(element)
        }
    }

    fun deleteSelectedElements() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        saveSnapshot("Delete Elements")
        viewModelScope.launch {
            repository.deleteElements(selected.toList())
            _selectedElementIds.value = emptySet()
        }
    }

    fun duplicateSelectedElements() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        saveSnapshot("Duplicate Elements")
        val current = elements.value.filter { selected.contains(it.id) }
        val newOnes = current.map {
            it.copy(
                id = "elem_" + UUID.randomUUID().toString().take(8),
                posX = it.posX + 40f,
                posY = it.posY + 40f,
                title = if (it.title.isNotBlank()) "${it.title} (Copy)" else ""
            )
        }
        viewModelScope.launch {
            repository.saveElements(newOnes)
            _selectedElementIds.value = newOnes.map { it.id }.toSet()
        }
    }

    fun toggleLockSelected() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        val current = elements.value.filter { selected.contains(it.id) }
        val updated = current.map { it.copy(isLocked = !it.isLocked) }
        viewModelScope.launch {
            repository.saveElements(updated)
        }
    }

    fun togglePinSelected() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        val current = elements.value.filter { selected.contains(it.id) }
        val updated = current.map { it.copy(isPinned = !it.isPinned) }
        viewModelScope.launch {
            repository.saveElements(updated)
        }
    }

    fun bringSelectedToFront() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        val maxZ = (elements.value.maxOfOrNull { it.zIndex } ?: 0) + 1
        val updated = elements.value.filter { selected.contains(it.id) }.map { it.copy(zIndex = maxZ) }
        viewModelScope.launch {
            repository.saveElements(updated)
        }
    }

    fun sendSelectedToBack() {
        val selected = _selectedElementIds.value
        if (selected.isEmpty()) return
        val minZ = (elements.value.minOfOrNull { it.zIndex } ?: 0) - 1
        val updated = elements.value.filter { selected.contains(it.id) }.map { it.copy(zIndex = minZ) }
        viewModelScope.launch {
            repository.saveElements(updated)
        }
    }

    // Mind Map helpers
    fun addMindMapChild(parentId: String) {
        val parent = elements.value.find { it.id == parentId } ?: return
        saveSnapshot("Add Mind Map Branch")
        val newId = "mm_node_" + UUID.randomUUID().toString().take(8)
        val child = CanvasElement(
            id = newId,
            boardId = _currentBoardId.value,
            type = ElementType.MIND_MAP_NODE,
            posX = parent.posX + parent.width + 120f,
            posY = parent.posY + 30f,
            width = 180f,
            height = 80f,
            title = if (AppStrings.isArabic()) "فرع جديد" else "New Branch",
            content = "",
            colorHex = "#38BDF8",
            textColorHex = "#FFFFFF",
            parentNodeId = parentId,
            fontSize = 14f
        )
        val connection = ElementConnection(
            id = "conn_" + UUID.randomUUID().toString().take(8),
            boardId = _currentBoardId.value,
            fromElementId = parentId,
            toElementId = newId,
            type = ConnectorType.CURVED,
            label = "",
            colorHex = "#38BDF8"
        )
        viewModelScope.launch {
            repository.saveElement(child)
            repository.saveConnection(connection)
            _selectedElementIds.value = setOf(newId)
        }
    }

    fun addMindMapSibling(nodeId: String) {
        val node = elements.value.find { it.id == nodeId } ?: return
        val parentId = node.parentNodeId
        saveSnapshot("Add Mind Map Sibling")
        val newId = "mm_sibling_" + UUID.randomUUID().toString().take(8)
        val sibling = CanvasElement(
            id = newId,
            boardId = _currentBoardId.value,
            type = ElementType.MIND_MAP_NODE,
            posX = node.posX,
            posY = node.posY + node.height + 40f,
            width = node.width,
            height = node.height,
            title = if (AppStrings.isArabic()) "فكرة موازية" else "Sibling Idea",
            content = "",
            colorHex = node.colorHex,
            textColorHex = node.textColorHex,
            parentNodeId = parentId,
            fontSize = 14f
        )
        viewModelScope.launch {
            repository.saveElement(sibling)
            if (parentId != null) {
                val conn = ElementConnection(
                    id = "conn_" + UUID.randomUUID().toString().take(8),
                    boardId = _currentBoardId.value,
                    fromElementId = parentId,
                    toElementId = newId,
                    type = ConnectorType.CURVED,
                    label = "",
                    colorHex = node.colorHex
                )
                repository.saveConnection(conn)
            }
            _selectedElementIds.value = setOf(newId)
        }
    }

    // Connections
    fun createConnection(fromId: String, toId: String, label: String = "") {
        saveSnapshot("Create Connection")
        val conn = ElementConnection(
            id = "conn_" + UUID.randomUUID().toString().take(8),
            boardId = _currentBoardId.value,
            fromElementId = fromId,
            toElementId = toId,
            type = ConnectorType.CURVED,
            label = label,
            colorHex = "#60A5FA"
        )
        viewModelScope.launch {
            repository.saveConnection(conn)
        }
    }

    fun deleteConnection(connId: String) {
        saveSnapshot("Delete Connection")
        viewModelScope.launch {
            repository.deleteConnection(connId)
        }
    }

    // Checklist inside sticky note
    fun toggleChecklistItem(elementId: String, itemId: String) {
        val elem = elements.value.find { it.id == elementId } ?: return
        val updatedChecklist = elem.checklist.map {
            if (it.id == itemId) it.copy(isDone = !it.isDone) else it
        }
        val updated = elem.copy(checklist = updatedChecklist)
        viewModelScope.launch {
            repository.saveElement(updated)
        }
    }

    // Task Card Status (Kanban)
    fun updateTaskStatus(elementId: String, newStatus: String) {
        val elem = elements.value.find { it.id == elementId } ?: return
        val updated = elem.copy(status = newStatus)
        viewModelScope.launch {
            repository.saveElement(updated)
        }
    }

    // Freehand Drawing stroke lifecycle
    fun addDrawingPoint(point: Point) {
        val current = _activeDrawingPoints.value.toMutableList()
        current.add(point)
        _activeDrawingPoints.value = current
    }

    fun finishDrawingStroke() {
        val pts = _activeDrawingPoints.value
        if (pts.size >= 2) {
            saveSnapshot("Add Sketch Stroke")
            val stroke = DrawingStroke(
                id = UUID.randomUUID().toString(),
                points = pts,
                colorHex = drawingColorHex.value,
                strokeWidth = drawingStrokeWidth.value,
                isHighlighter = isDrawingHighlighter.value
            )
            // Bounding box of stroke
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            for (p in pts) {
                minX = minOf(minX, p.x)
                minY = minOf(minY, p.y)
                maxX = maxOf(maxX, p.x)
                maxY = maxOf(maxY, p.y)
            }
            val w = maxOf(maxX - minX, 40f)
            val h = maxOf(maxY - minY, 40f)

            val drawingElement = CanvasElement(
                id = "drawing_" + UUID.randomUUID().toString().take(8),
                boardId = _currentBoardId.value,
                type = ElementType.DRAWING,
                posX = minX,
                posY = minY,
                width = w,
                height = h,
                strokes = listOf(stroke),
                colorHex = drawingColorHex.value
            )
            viewModelScope.launch {
                repository.saveElement(drawingElement)
            }
        }
        _activeDrawingPoints.value = emptyList()
    }

    // Templates
    fun applyTemplate(templateType: String) {
        saveSnapshot("Apply Template $templateType")
        val newElements = SampleBoards.generateTemplate(templateType, _currentBoardId.value)
        viewModelScope.launch {
            repository.saveElements(newElements)
            showTemplatesDialog.value = false
            fitAllContent()
        }
    }

    // History Snapshots & Undo / Redo
    private fun saveSnapshot(label: String) {
        val currentElements = elements.value
        val currentConnections = connections.value
        recordSnapshot(label, currentElements, currentConnections)
    }

    private fun recordSnapshot(label: String, elems: List<CanvasElement>, conns: List<ElementConnection>) {
        val snapshot = HistorySnapshot(System.currentTimeMillis(), label, elems, conns)
        val curUndo = _undoStack.value.toMutableList()
        curUndo.add(snapshot)
        if (curUndo.size > 25) curUndo.removeAt(0)
        _undoStack.value = curUndo
        _redoStack.value = emptyList()

        val allSnapshots = historySnapshots.value.toMutableList()
        allSnapshots.add(snapshot)
        if (allSnapshots.size > 30) allSnapshots.removeAt(0)
        historySnapshots.value = allSnapshots
    }

    fun undo() {
        val undoList = _undoStack.value.toMutableList()
        if (undoList.isEmpty()) return
        val last = undoList.removeAt(undoList.size - 1)
        _undoStack.value = undoList

        val redoList = _redoStack.value.toMutableList()
        redoList.add(HistorySnapshot(System.currentTimeMillis(), "Current State", elements.value, connections.value))
        _redoStack.value = redoList

        restoreSnapshot(last)
    }

    fun redo() {
        val redoList = _redoStack.value.toMutableList()
        if (redoList.isEmpty()) return
        val next = redoList.removeAt(redoList.size - 1)
        _redoStack.value = redoList

        val undoList = _undoStack.value.toMutableList()
        undoList.add(HistorySnapshot(System.currentTimeMillis(), "Undo State", elements.value, connections.value))
        _undoStack.value = undoList

        restoreSnapshot(next)
    }

    fun restoreSnapshot(snapshot: HistorySnapshot) {
        viewModelScope.launch {
            repository.deleteBoard(_currentBoardId.value)
            val b = currentBoard.value ?: Board(_currentBoardId.value, "IdeaSpace")
            repository.saveBoard(b)
            repository.saveElements(snapshot.elements)
            repository.saveConnections(snapshot.connections)
        }
    }

    // Export & Import
    fun exportBoardToJson(): String {
        val root = JSONObject()
        val b = currentBoard.value
        root.put("version", "1.0")
        root.put("title", b?.title ?: "IdeaSpace")
        root.put("description", b?.description ?: "")

        val elemsArr = JSONArray()
        for (e in elements.value) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("type", e.type.name)
            obj.put("posX", e.posX.toDouble())
            obj.put("posY", e.posY.toDouble())
            obj.put("width", e.width.toDouble())
            obj.put("height", e.height.toDouble())
            obj.put("title", e.title)
            obj.put("content", e.content)
            obj.put("colorHex", e.colorHex)
            obj.put("priority", e.priority)
            obj.put("status", e.status)
            obj.put("shapeType", e.shapeType.name)
            obj.put("parentNodeId", e.parentNodeId ?: "")
            elemsArr.put(obj)
        }
        root.put("elements", elemsArr)

        val connsArr = JSONArray()
        for (c in connections.value) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("from", c.fromElementId)
            obj.put("to", c.toElementId)
            obj.put("label", c.label)
            obj.put("color", c.colorHex)
            connsArr.put(obj)
        }
        root.put("connections", connsArr)
        return root.toString(2)
    }

    fun importBoardFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val newBoardId = "imported_" + UUID.randomUUID().toString().take(8)
            val title = root.optString("title", "Imported Workspace")
            val newBoard = Board(id = newBoardId, title = title)

            val elemsList = mutableListOf<CanvasElement>()
            val elemsArr = root.optJSONArray("elements")
            if (elemsArr != null) {
                for (i in 0 until elemsArr.length()) {
                    val o = elemsArr.getJSONObject(i)
                    val t = try { ElementType.valueOf(o.getString("type")) } catch (_: Exception) { ElementType.STICKY_NOTE }
                    val st = try { ShapeType.valueOf(o.optString("shapeType", "ROUNDED_RECT")) } catch (_: Exception) { ShapeType.ROUNDED_RECT }
                    elemsList.add(
                        CanvasElement(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            boardId = newBoardId,
                            type = t,
                            posX = o.optDouble("posX", 100.0).toFloat(),
                            posY = o.optDouble("posY", 100.0).toFloat(),
                            width = o.optDouble("width", 200.0).toFloat(),
                            height = o.optDouble("height", 150.0).toFloat(),
                            title = o.optString("title", ""),
                            content = o.optString("content", ""),
                            colorHex = o.optString("colorHex", "#FDE047"),
                            priority = o.optString("priority", "MEDIUM"),
                            status = o.optString("status", "TODO"),
                            shapeType = st,
                            parentNodeId = o.optString("parentNodeId").takeIf { it.isNotEmpty() }
                        )
                    )
                }
            }

            val connsList = mutableListOf<ElementConnection>()
            val connsArr = root.optJSONArray("connections")
            if (connsArr != null) {
                for (i in 0 until connsArr.length()) {
                    val o = connsArr.getJSONObject(i)
                    connsList.add(
                        ElementConnection(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            boardId = newBoardId,
                            fromElementId = o.getString("from"),
                            toElementId = o.getString("to"),
                            label = o.optString("label", ""),
                            colorHex = o.optString("color", "#60A5FA")
                        )
                    )
                }
            }

            viewModelScope.launch {
                repository.saveBoard(newBoard)
                repository.saveElements(elemsList)
                repository.saveConnections(connsList)
                _currentBoardId.value = newBoardId
                fitAllContent()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun exportBoardToMarkdown(): String {
        val b = currentBoard.value
        val sb = StringBuilder()
        sb.append("# ${b?.title ?: "IdeaSpace Workspace"}\n\n")
        if (!b?.description.isNullOrBlank()) sb.append("${b?.description}\n\n")

        sb.append("## Elements & Ideas\n\n")
        for (e in elements.value) {
            sb.append("### [${e.type.name}] ${if (e.title.isNotBlank()) e.title else "Untitled"}\n")
            if (e.content.isNotBlank()) sb.append("${e.content}\n")
            if (e.checklist.isNotEmpty()) {
                sb.append("\n**Checklist:**\n")
                for (c in e.checklist) {
                    sb.append("- [${if (c.isDone) "x" else " "}] ${c.text}\n")
                }
            }
            sb.append("\n---\n\n")
        }

        if (connections.value.isNotEmpty()) {
            sb.append("## Connections\n\n")
            for (c in connections.value) {
                val fromName = elements.value.find { it.id == c.fromElementId }?.title ?: c.fromElementId
                val toName = elements.value.find { it.id == c.toElementId }?.title ?: c.toElementId
                sb.append("- **$fromName** -> *(${c.label})* -> **$toName**\n")
            }
        }
        return sb.toString()
    }
}
