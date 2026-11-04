package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.theme.ThemePickerDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GridType
import com.example.model.InteractionMode
import com.example.ui.canvas.CanvasMiniMap
import com.example.ui.canvas.InfiniteCanvas
import com.example.ui.dialogs.*
import com.example.ui.elements.parseHexColor
import com.example.ui.views.KanbanView
import com.example.ui.views.MindMapOutlineView
import com.example.ui.views.PresentationModeView
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.viewmodel.IdeaSpaceViewModel
import com.example.viewmodel.WorkspaceView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaSpaceMainScreen(
    viewModel: IdeaSpaceViewModel,
    modifier: Modifier = Modifier,
    themeId: String = "classic",
    onThemeChange: (String) -> Unit = {}
) {
    val language by viewModel.appLanguage.collectAsState()
    val layoutDirection = if (language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val currentBoard by viewModel.currentBoard.collectAsState()
        val currentView by viewModel.workspaceView.collectAsState()
        val mode by viewModel.interactionMode.collectAsState()
        val viewport by viewModel.viewport.collectAsState()
        val elements by viewModel.elements.collectAsState()
        val selectedIds by viewModel.selectedElementIds.collectAsState()
        val gridType by viewModel.gridType.collectAsState()
        val snapToGrid by viewModel.snapToGrid.collectAsState()
        val undoStack by viewModel.undoStack.collectAsState()
        val redoStack by viewModel.redoStack.collectAsState()

        val showAddSheet by viewModel.showAddDialog.collectAsState()
        val editingElement by viewModel.editingElement.collectAsState()
        val showTemplates by viewModel.showTemplatesDialog.collectAsState()
        val showExportImport by viewModel.showExportImportDialog.collectAsState()
        val showQuickCapture by viewModel.showQuickCaptureDialog.collectAsState()
        val showBoardsDrawer by viewModel.showBoardsDrawer.collectAsState()

        var showMiniMap by remember { mutableStateOf(false) }
        var showThemePicker by remember { mutableStateOf(false) }
        if (showThemePicker) {
            ThemePickerDialog(currentId = themeId, onSelect = onThemeChange, onDismiss = { showThemePicker = false })
        }

        // BackHandler for secondary views
        if (currentView != WorkspaceView.CANVAS) {
            BackHandler {
                viewModel.setWorkspaceView(WorkspaceView.CANVAS)
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (currentView != WorkspaceView.PRESENTATION) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentBoard?.title ?: AppStrings.appName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = AppStrings.tagline,
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.showBoardsDrawer.value = true }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Boards", tint = Color.White)
                            }
                        },
                        actions = {
                            // Language Switcher
                            TextButton(onClick = { viewModel.toggleLanguage() }) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "EN" else "عربي",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            // Undo / Redo
                            IconButton(
                                onClick = { viewModel.undo() },
                                enabled = undoStack.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Undo,
                                    contentDescription = AppStrings.undo,
                                    tint = if (undoStack.isNotEmpty()) Color.White else Color(0xFF475569)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.redo() },
                                enabled = redoStack.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Redo,
                                    contentDescription = AppStrings.redo,
                                    tint = if (redoStack.isNotEmpty()) Color.White else Color(0xFF475569)
                                )
                            }

                            // Theme
                            IconButton(onClick = { showThemePicker = true }) {
                                Icon(imageVector = Icons.Default.Palette, contentDescription = AppStrings.theme, tint = MaterialTheme.colorScheme.primary)
                            }

                            // Templates
                            IconButton(onClick = { viewModel.showTemplatesDialog.value = true }) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = AppStrings.templates, tint = Color(0xFFFBBF24))
                            }

                            // Backup & Export
                            IconButton(onClick = { viewModel.showExportImportDialog.value = true }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = AppStrings.exportImport, tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                            titleContentColor = Color.White
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Workspace Content based on currentView
                when (currentView) {
                    WorkspaceView.CANVAS -> {
                        InfiniteCanvas(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    WorkspaceView.KANBAN -> {
                        KanbanView(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    WorkspaceView.OUTLINE -> {
                        MindMapOutlineView(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    WorkspaceView.PRESENTATION -> {
                        PresentationModeView(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        InfiniteCanvas(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                    }
                }

                // If in Canvas view, show the overlay control bars
                if (currentView == WorkspaceView.CANVAS) {
                    // 1. Top Mode Selector Bar (Navigation vs Editing vs Drawing vs Connecting)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModeIconButton(
                                icon = Icons.Default.PanTool,
                                label = AppStrings.modeNavigation,
                                isSelected = mode == InteractionMode.NAVIGATION,
                                onClick = { viewModel.setInteractionMode(InteractionMode.NAVIGATION) }
                            )
                            ModeIconButton(
                                icon = Icons.Default.NearMe,
                                label = AppStrings.modeEditing,
                                isSelected = mode == InteractionMode.EDITING,
                                onClick = { viewModel.setInteractionMode(InteractionMode.EDITING) }
                            )
                            ModeIconButton(
                                icon = Icons.Default.Edit,
                                label = AppStrings.modeDrawing,
                                isSelected = mode == InteractionMode.DRAWING,
                                onClick = { viewModel.setInteractionMode(InteractionMode.DRAWING) }
                            )
                            ModeIconButton(
                                icon = Icons.Default.Polyline,
                                label = AppStrings.modeConnecting,
                                isSelected = mode == InteractionMode.CONNECTING,
                                onClick = { viewModel.setInteractionMode(InteractionMode.CONNECTING) }
                            )
                        }
                    }

                    // 2. View Switcher Pill (Canvas / Kanban / Outline / Presentation)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 66.dp, start = 12.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.9f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ViewChip(
                                title = AppStrings.canvasView,
                                isSelected = currentView == WorkspaceView.CANVAS,
                                onClick = { viewModel.setWorkspaceView(WorkspaceView.CANVAS) }
                            )
                            ViewChip(
                                title = AppStrings.kanbanView,
                                isSelected = currentView == WorkspaceView.KANBAN,
                                onClick = { viewModel.setWorkspaceView(WorkspaceView.KANBAN) }
                            )
                            ViewChip(
                                title = AppStrings.outlineView,
                                isSelected = currentView == WorkspaceView.OUTLINE,
                                onClick = { viewModel.setWorkspaceView(WorkspaceView.OUTLINE) }
                            )
                            ViewChip(
                                title = AppStrings.presentationMode,
                                isSelected = currentView == WorkspaceView.PRESENTATION,
                                onClick = { viewModel.setWorkspaceView(WorkspaceView.PRESENTATION) }
                            )
                        }
                    }

                    // 3. Right Floating Zoom & Grid Toolbar
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.92f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                IconButton(onClick = { viewModel.zoomIn() }, modifier = Modifier.size(38.dp)) {
                                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = AppStrings.zoomIn, tint = Color.White)
                                }
                                Text(
                                    text = "${(viewport.zoom * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                IconButton(onClick = { viewModel.zoomOut() }, modifier = Modifier.size(38.dp)) {
                                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = AppStrings.zoomOut, tint = Color.White)
                                }
                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.width(24.dp))
                                IconButton(onClick = { viewModel.fitAllContent() }, modifier = Modifier.size(38.dp)) {
                                    Icon(imageVector = Icons.Default.FitScreen, contentDescription = AppStrings.fitContent, tint = Color(0xFF38BDF8))
                                }
                                IconButton(onClick = { viewModel.centerView() }, modifier = Modifier.size(38.dp)) {
                                    Icon(imageVector = Icons.Default.FilterCenterFocus, contentDescription = AppStrings.centerView, tint = Color.White)
                                }
                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.width(24.dp))
                                // Grid toggle
                                IconButton(
                                    onClick = {
                                        val next = when (gridType) {
                                            GridType.DOTS -> GridType.LINES
                                            GridType.LINES -> GridType.BLUEPRINT
                                            GridType.BLUEPRINT -> GridType.NONE
                                            GridType.NONE -> GridType.DOTS
                                        }
                                        viewModel.setGridType(next)
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = AppStrings.grid,
                                        tint = if (gridType != GridType.NONE) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                }
                                // Snap to Grid
                                IconButton(
                                    onClick = { viewModel.toggleSnapToGrid() },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SquareFoot,
                                        contentDescription = AppStrings.snapToGrid,
                                        tint = if (snapToGrid) Color(0xFF10B981) else Color(0xFF64748B)
                                    )
                                }
                                // MiniMap toggle
                                IconButton(
                                    onClick = { showMiniMap = !showMiniMap },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = AppStrings.miniMap,
                                        tint = if (showMiniMap) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }

                    // 4. MiniMap overlay at bottom left
                    if (showMiniMap) {
                        CanvasMiniMap(
                            elements = elements,
                            panX = viewport.panX,
                            panY = viewport.panY,
                            zoom = viewport.zoom,
                            onNavigate = { nx, ny -> viewModel.updatePanZoom(nx, ny) },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 90.dp)
                        )
                    }

                    // 5. Drawing Mode Floating Palette
                    if (mode == InteractionMode.DRAWING) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 90.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val paletteColors = listOf("#38BDF8", "#F43F5E", "#10B981", "#F59E0B", "#A855F7", "#FFFFFF")
                                paletteColors.forEach { hex ->
                                    val isCur = viewModel.drawingColorHex.collectAsState().value == hex
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(parseHexColor(hex), CircleShape)
                                            .then(if (isCur) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                            .clickable { viewModel.drawingColorHex.value = hex }
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Highlighter toggle
                                val isHl by viewModel.isDrawingHighlighter.collectAsState()
                                IconButton(
                                    onClick = { viewModel.isDrawingHighlighter.value = !isHl },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Highlight,
                                        contentDescription = "Highlighter",
                                        tint = if (isHl) Color(0xFFFBBF24) else Color(0xFF64748B)
                                    )
                                }
                                // Exit drawing button
                                Button(
                                    onClick = { viewModel.setInteractionMode(InteractionMode.EDITING) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text(text = if (AppStrings.isArabic()) "تم" else "Done", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 6. Selected Elements Context Toolbar
                    if (selectedIds.isNotEmpty() && mode == InteractionMode.EDITING) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 86.dp)
                                .shadow(10.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.95f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val first = elements.find { selectedIds.contains(it.id) }
                                        if (first != null) viewModel.editingElement.value = first
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = AppStrings.editElement, tint = Color.White)
                                }

                                IconButton(
                                    onClick = { viewModel.duplicateSelectedElements() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = AppStrings.duplicateElement, tint = Color.White)
                                }

                                IconButton(
                                    onClick = { viewModel.toggleLockSelected() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = AppStrings.lockElement, tint = Color.White)
                                }

                                IconButton(
                                    onClick = { viewModel.togglePinSelected() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PushPin, contentDescription = AppStrings.pinElement, tint = Color.White)
                                }

                                IconButton(
                                    onClick = { viewModel.bringSelectedToFront() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.FlipToFront, contentDescription = AppStrings.bringToFront, tint = Color.White)
                                }

                                IconButton(
                                    onClick = { viewModel.deleteSelectedElements() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = AppStrings.deleteElement, tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }

                    // 7. Bottom Primary FAB & Quick Capture
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Capture button
                        SmallFloatingActionButton(
                            onClick = { viewModel.showQuickCaptureDialog.value = true },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = Color(0xFFFBBF24),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = AppStrings.quickCapture)
                        }

                        // Primary Add Element FAB
                        FloatingActionButton(
                            onClick = { viewModel.showAddDialog.value = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF0F172A),
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = AppStrings.addElement, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }

        // Sheets & Dialogs
        if (showAddSheet) {
            AddElementBottomSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.showAddDialog.value = false }
            )
        }

        editingElement?.let { elem ->
            ElementEditorDialog(
                element = elem,
                onSave = { updated -> viewModel.updateElement(updated) },
                onDelete = {
                    viewModel.deleteSelectedElements()
                    viewModel.editingElement.value = null
                },
                onDismiss = { viewModel.editingElement.value = null }
            )
        }

        if (showTemplates) {
            TemplatesDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showTemplatesDialog.value = false }
            )
        }

        if (showExportImport) {
            ExportImportDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showExportImportDialog.value = false }
            )
        }

        if (showQuickCapture) {
            QuickCaptureDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showQuickCaptureDialog.value = false }
            )
        }

        if (showBoardsDrawer) {
            BoardsDrawerSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.showBoardsDrawer.value = false }
            )
        }
    }
}

@Composable
fun ModeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun ViewChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8)
        )
    }
}
