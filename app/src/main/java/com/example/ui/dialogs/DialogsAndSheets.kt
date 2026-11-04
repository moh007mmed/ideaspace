package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.elements.parseHexColor
import com.example.util.AppStrings
import com.example.viewmodel.IdeaSpaceViewModel
import java.util.UUID

// 1. ADD ELEMENT BOTTOM SHEET
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddElementBottomSheet(
    viewModel: IdeaSpaceViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val viewport by viewModel.viewport.collectAsState()
    val curBoardId by viewModel.currentBoardId.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = AppStrings.addElement,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Grid of element options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddElementTile(
                    title = AppStrings.stickyNote,
                    icon = Icons.Default.Note,
                    color = Color(0xFFFEF08A),
                    iconTint = Color(0xFF854D0E),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "sticky_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.STICKY_NOTE,
                            posX = centerX,
                            posY = centerY,
                            width = 220f,
                            height = 180f,
                            title = if (AppStrings.isArabic()) "ملاحظة جديدة" else "New Note",
                            content = "",
                            colorHex = "#FEF08A"
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                AddElementTile(
                    title = AppStrings.text,
                    icon = Icons.Default.TextFields,
                    color = Color(0xFF38BDF8),
                    iconTint = Color(0xFF0C4A6E),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "text_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.TEXT,
                            posX = centerX,
                            posY = centerY,
                            width = 260f,
                            height = 80f,
                            title = if (AppStrings.isArabic()) "فكرة جديدة" else "New Idea",
                            content = "",
                            textColorHex = "#38BDF8",
                            fontSize = 20f,
                            isBold = true
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddElementTile(
                    title = AppStrings.mindMapNode,
                    icon = Icons.Default.AccountTree,
                    color = Color(0xFF818CF8),
                    iconTint = Color(0xFF312E81),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "mm_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.MIND_MAP_NODE,
                            posX = centerX,
                            posY = centerY,
                            width = 200f,
                            height = 90f,
                            title = if (AppStrings.isArabic()) "فكرة خريطة ذهنية" else "Mind Map Topic",
                            content = "",
                            colorHex = "#6366F1",
                            textColorHex = "#FFFFFF"
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                AddElementTile(
                    title = AppStrings.taskCard,
                    icon = Icons.Default.Checklist,
                    color = Color(0xFF34D399),
                    iconTint = Color(0xFF064E3B),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "task_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.TASK_CARD,
                            posX = centerX,
                            posY = centerY,
                            width = 240f,
                            height = 150f,
                            title = if (AppStrings.isArabic()) "مهمة عمل جديدة" else "New Action Task",
                            content = "",
                            priority = "MEDIUM",
                            status = "TODO"
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddElementTile(
                    title = AppStrings.shape,
                    icon = Icons.Default.Category,
                    color = Color(0xFFA78BFA),
                    iconTint = Color(0xFF4C1D95),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "shape_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.SHAPE,
                            shapeType = ShapeType.DIAMOND,
                            posX = centerX,
                            posY = centerY,
                            width = 170f,
                            height = 120f,
                            title = if (AppStrings.isArabic()) "قرار" else "Decision",
                            colorHex = "#3B82F6",
                            textColorHex = "#FFFFFF"
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )

                AddElementTile(
                    title = AppStrings.fileCard,
                    icon = Icons.Default.Code,
                    color = Color(0xFFF472B6),
                    iconTint = Color(0xFF831843),
                    onClick = {
                        val centerX = -viewport.panX / viewport.zoom + 100f
                        val centerY = -viewport.panY / viewport.zoom + 200f
                        val newElem = CanvasElement(
                            id = "file_" + UUID.randomUUID().toString().take(8),
                            boardId = curBoardId,
                            type = ElementType.FILE_CARD,
                            posX = centerX,
                            posY = centerY,
                            width = 240f,
                            height = 140f,
                            title = "Snippet.kt",
                            content = "// IdeaSpace logic\nval infinite = true",
                            fileType = "CODE"
                        )
                        viewModel.addElement(newElem)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun AddElementTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// 2. ELEMENT EDITOR DIALOG
@Composable
fun ElementEditorDialog(
    element: CanvasElement,
    onSave: (CanvasElement) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(element.title) }
    var content by remember { mutableStateOf(element.content) }
    var colorHex by remember { mutableStateOf(element.colorHex) }
    var priority by remember { mutableStateOf(element.priority) }
    var shapeType by remember { mutableStateOf(element.shapeType) }
    var newChecklistText by remember { mutableStateOf("") }
    val checklist = remember { mutableStateListOf(*element.checklist.toTypedArray()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${AppStrings.editElement}: ${element.type.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.title) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(AppStrings.content) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Shape Type Picker if element is SHAPE
                if (element.type == ElementType.SHAPE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = AppStrings.shape, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(ShapeType.values()) { st ->
                            FilterChip(
                                selected = shapeType == st,
                                onClick = { shapeType = st },
                                label = { Text(st.name.take(6), fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Color Palette
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = AppStrings.changeColor, fontSize = 12.sp, color = Color(0xFF94A3B8))
                val colors = listOf("#FEF08A", "#FECDD3", "#BBF7D0", "#BAE6FD", "#E9D5FF", "#38BDF8", "#3B82F6", "#6366F1", "#1E293B")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(colors) { hex ->
                        val c = parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c, CircleShape)
                                .then(
                                    if (colorHex == hex) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                // Checklist Items (For Sticky Notes or Tasks)
                if (element.type == ElementType.STICKY_NOTE || element.type == ElementType.TASK_CARD) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newChecklistText,
                            onValueChange = { newChecklistText = it },
                            placeholder = { Text(if (AppStrings.isArabic()) "إضافة بند مهام..." else "Add checklist item...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (newChecklistText.isNotBlank()) {
                                checklist.add(ChecklistItem(UUID.randomUUID().toString(), newChecklistText, false))
                                newChecklistText = ""
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF38BDF8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel, color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = element.copy(
                                title = title,
                                content = content,
                                colorHex = colorHex,
                                priority = priority,
                                shapeType = shapeType,
                                checklist = checklist.toList()
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text(AppStrings.save, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. TEMPLATES DIALOG
@Composable
fun TemplatesDialog(
    viewModel: IdeaSpaceViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppStrings.templates,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))

                TemplateItemCard(
                    title = if (AppStrings.isArabic()) "📊 تحليل سوات (SWOT Analysis)" else "📊 SWOT Analysis",
                    desc = if (AppStrings.isArabic()) "نقاط القوة، الضعف، الفرص، والتهديدات بألوان واضحة" else "Strengths, Weaknesses, Opportunities, Threats",
                    onClick = { viewModel.applyTemplate("SWOT") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                TemplateItemCard(
                    title = if (AppStrings.isArabic()) "📅 لوحة كانبان للسباق (Sprint Kanban)" else "📅 Sprint Kanban Board",
                    desc = if (AppStrings.isArabic()) "أعمدة المهام قيد الانتظار والعمل والمكتملة" else "To Do, In Progress, Done sprint cards",
                    onClick = { viewModel.applyTemplate("KANBAN") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                TemplateItemCard(
                    title = if (AppStrings.isArabic()) "🧠 بداية خريطة ذهنية (Mind Map Starter)" else "🧠 Mind Map Starter",
                    desc = if (AppStrings.isArabic()) "فكرة مركزية مع تفريعات جاهزة للإطلاق" else "Central topic with expandable branches",
                    onClick = { viewModel.applyTemplate("BRAINSTORM") }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.close, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateItemCard(
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = desc, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
    }
}

// 4. EXPORT & IMPORT DIALOG
@Composable
fun ExportImportDialog(
    viewModel: IdeaSpaceViewModel,
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var importText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppStrings.exportImport,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (AppStrings.isArabic()) "تصدير" else "Export") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (AppStrings.isArabic()) "استيراد" else "Import") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Export Actions
                    Button(
                        onClick = {
                            val json = viewModel.exportBoardToJson()
                            clipboard.setText(AnnotatedString(json))
                            Toast.makeText(context, AppStrings.copiedToClipboard, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.exportJson, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val md = viewModel.exportBoardToMarkdown()
                            clipboard.setText(AnnotatedString(md))
                            Toast.makeText(context, AppStrings.copiedToClipboard, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.exportMarkdown)
                    }
                } else {
                    // Import JSON
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text(if (AppStrings.isArabic()) "الصق نص الـ JSON هنا..." else "Paste JSON backup here...", fontSize = 12.sp) },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (importText.isNotBlank()) {
                                val success = viewModel.importBoardFromJson(importText)
                                if (success) {
                                    Toast.makeText(context, AppStrings.backupRestored, Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text(AppStrings.importJson, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.close, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

// 5. QUICK CAPTURE DIALOG
@Composable
fun QuickCaptureDialog(
    viewModel: IdeaSpaceViewModel,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ElementType.STICKY_NOTE) }
    val viewport by viewModel.viewport.collectAsState()
    val curBoardId by viewModel.currentBoardId.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppStrings.quickCapture,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == ElementType.STICKY_NOTE,
                        onClick = { type = ElementType.STICKY_NOTE },
                        label = { Text(AppStrings.stickyNote) }
                    )
                    FilterChip(
                        selected = type == ElementType.TASK_CARD,
                        onClick = { type = ElementType.TASK_CARD },
                        label = { Text(AppStrings.taskCard) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(if (AppStrings.isArabic()) "ما الفكرة التي تدور في ذهنك؟" else "What idea is on your mind?", fontSize = 13.sp) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel, color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                val centerX = -viewport.panX / viewport.zoom + 100f
                                val centerY = -viewport.panY / viewport.zoom + 200f
                                val elem = CanvasElement(
                                    id = "quick_" + UUID.randomUUID().toString().take(8),
                                    boardId = curBoardId,
                                    type = type,
                                    posX = centerX,
                                    posY = centerY,
                                    width = 220f,
                                    height = 160f,
                                    title = text.lines().firstOrNull().orEmpty(),
                                    content = text.lines().drop(1).joinToString("\n"),
                                    colorHex = if (type == ElementType.STICKY_NOTE) "#FEF08A" else "#1E293B"
                                )
                                viewModel.addElement(elem)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text(AppStrings.save, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
