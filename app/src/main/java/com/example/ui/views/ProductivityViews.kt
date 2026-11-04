package com.example.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.elements.parseHexColor
import com.example.util.AppStrings
import com.example.viewmodel.IdeaSpaceViewModel
import java.util.UUID

// 1. KANBAN BOARD VIEW
@Composable
fun KanbanView(
    viewModel: IdeaSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val elements by viewModel.elements.collectAsState()
    val tasksAndNotes = remember(elements) {
        elements.filter { it.type == ElementType.TASK_CARD || it.type == ElementType.STICKY_NOTE }
    }

    val todoItems = remember(tasksAndNotes) { tasksAndNotes.filter { it.status == "TODO" } }
    val inProgressItems = remember(tasksAndNotes) { tasksAndNotes.filter { it.status == "IN_PROGRESS" } }
    val doneItems = remember(tasksAndNotes) { tasksAndNotes.filter { it.status == "DONE" } }

    var selectedColumn by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Tab Selector for Columns on Mobile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanTabButton(
                title = "${AppStrings.todo} (${todoItems.size})",
                color = Color(0xFFF59E0B),
                isSelected = selectedColumn == "TODO" || selectedColumn == "ALL",
                onClick = { selectedColumn = if (selectedColumn == "TODO") "ALL" else "TODO" },
                modifier = Modifier.weight(1f)
            )
            KanbanTabButton(
                title = "${AppStrings.inProgress} (${inProgressItems.size})",
                color = Color(0xFF3B82F6),
                isSelected = selectedColumn == "IN_PROGRESS" || selectedColumn == "ALL",
                onClick = { selectedColumn = if (selectedColumn == "IN_PROGRESS") "ALL" else "IN_PROGRESS" },
                modifier = Modifier.weight(1f)
            )
            KanbanTabButton(
                title = "${AppStrings.done} (${doneItems.size})",
                color = Color(0xFF10B981),
                isSelected = selectedColumn == "DONE" || selectedColumn == "ALL",
                onClick = { selectedColumn = if (selectedColumn == "DONE") "ALL" else "DONE" },
                modifier = Modifier.weight(1f)
            )
        }

        // Kanban Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val displayList = when (selectedColumn) {
                "TODO" -> todoItems
                "IN_PROGRESS" -> inProgressItems
                "DONE" -> doneItems
                else -> tasksAndNotes
            }

            if (displayList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (AppStrings.isArabic()) "لا توجد مهام في هذا العمود حالياً" else "No tasks in this column",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(displayList, key = { it.id }) { item ->
                KanbanCardItem(
                    item = item,
                    onMoveStatus = { newStatus -> viewModel.updateTaskStatus(item.id, newStatus) },
                    onEdit = { viewModel.editingElement.value = item }
                )
            }
        }
    }
}

@Composable
fun KanbanTabButton(
    title: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF1E293B),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun KanbanCardItem(
    item: CanvasElement,
    onMoveStatus: (String) -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEdit() },
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title.ifBlank { "Untitled Task" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Status chip
                val statusColor = when (item.status) {
                    "DONE" -> Color(0xFF10B981)
                    "IN_PROGRESS" -> Color(0xFF3B82F6)
                    else -> Color(0xFFF59E0B)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (item.status) {
                            "TODO" -> AppStrings.todo
                            "IN_PROGRESS" -> AppStrings.inProgress
                            else -> AppStrings.done
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            if (item.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.content,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Move status left/right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.status != "TODO") {
                    TextButton(onClick = {
                        val prev = if (item.status == "DONE") "IN_PROGRESS" else "TODO"
                        onMoveStatus(prev)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (item.status == "DONE") AppStrings.inProgress else AppStrings.todo, fontSize = 11.sp)
                    }
                }
                if (item.status != "DONE") {
                    TextButton(onClick = {
                        val next = if (item.status == "TODO") "IN_PROGRESS" else "DONE"
                        onMoveStatus(next)
                    }) {
                        Text(text = if (item.status == "TODO") AppStrings.inProgress else AppStrings.done, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// 2. MIND MAP OUTLINE VIEW
@Composable
fun MindMapOutlineView(
    viewModel: IdeaSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val elements by viewModel.elements.collectAsState()
    val mindMapNodes = remember(elements) { elements.filter { it.type == ElementType.MIND_MAP_NODE } }
    val rootNodes = remember(mindMapNodes) { mindMapNodes.filter { it.parentNodeId == null } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        Text(
            text = AppStrings.outlineView,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = if (AppStrings.isArabic()) "شجرة الأفكار والمستويات المنظمة" else "Structured hierarchical breakdown",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (mindMapNodes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (AppStrings.isArabic()) "لا توجد عقد خرائط ذهنية حالياً. أنشئ عقدة من القائمة!" else "No mind map nodes found. Create one from the menu!",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rootNodes, key = { it.id }) { root ->
                    MindMapTreeNodeItem(
                        node = root,
                        allNodes = mindMapNodes,
                        level = 0,
                        onAddChild = { viewModel.addMindMapChild(root.id) },
                        onEdit = { viewModel.editingElement.value = root }
                    )
                }
            }
        }
    }
}

@Composable
fun MindMapTreeNodeItem(
    node: CanvasElement,
    allNodes: List<CanvasElement>,
    level: Int,
    onAddChild: () -> Unit,
    onEdit: () -> Unit
) {
    val children = remember(allNodes, node.id) { allNodes.filter { it.parentNodeId == node.id } }
    val nodeColor = parseHexColor(node.colorHex, Color(0xFF6366F1))

    Column(modifier = Modifier.fillMaxWidth().padding(start = (level * 20).dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onEdit() },
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, nodeColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(nodeColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.title.ifBlank { "Branch Node" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (node.content.isNotBlank()) {
                        Text(
                            text = node.content,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onAddChild, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Branch", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
            }
        }

        // Render sub-children recursively
        children.forEach { child ->
            Spacer(modifier = Modifier.height(4.dp))
            MindMapTreeNodeItem(
                node = child,
                allNodes = allNodes,
                level = level + 1,
                onAddChild = onAddChild,
                onEdit = onEdit
            )
        }
    }
}

// 3. PRESENTATION MODE VIEW
@Composable
fun PresentationModeView(
    viewModel: IdeaSpaceViewModel,
    modifier: Modifier = Modifier
) {
    val elements by viewModel.elements.collectAsState()
    val displayable = remember(elements) {
        elements.filter { it.type != ElementType.DRAWING }
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    if (displayable.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(Color(0xFF020617)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (AppStrings.isArabic()) "أضف عناصر أولاً لعرضها كعرض تقديمي!" else "Add elements first to present them!",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        return
    }

    val currentItem = displayable.getOrElse(currentIndex.coerceIn(0, displayable.lastIndex)) { displayable.first() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Step Indicator & Exit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentIndex + 1} / ${displayable.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            IconButton(onClick = { viewModel.setWorkspaceView(com.example.viewmodel.WorkspaceView.CANVAS) }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = Color.White)
            }
        }

        // Main Slide Presentation Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentItem.type.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentItem.title.ifBlank { "Key Idea" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (currentItem.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = currentItem.content,
                        fontSize = 16.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }

                if (currentItem.checklist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    currentItem.checklist.forEach { chk ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (chk.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (chk.isDone) Color(0xFF10B981) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = chk.text, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Bottom Controls: Prev / Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
            ) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (AppStrings.isArabic()) "السابق" else "Previous")
            }

            Button(
                onClick = { if (currentIndex < displayable.lastIndex) currentIndex++ },
                enabled = currentIndex < displayable.lastIndex,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
            ) {
                Text(text = if (AppStrings.isArabic()) "التالي" else "Next", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF0F172A))
            }
        }
    }
}
