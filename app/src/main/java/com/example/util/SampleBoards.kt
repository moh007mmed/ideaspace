package com.example.util

import com.example.model.*
import java.util.UUID

object SampleBoards {

    const val DEFAULT_BOARD_ID = "board_main_starter"

    fun createInitialBoard(): Pair<Board, List<CanvasElement>> {
        val board = Board(
            id = DEFAULT_BOARD_ID,
            title = if (AppStrings.isArabic()) "مساحة الأفكار الرئيسية" else "Main IdeaSpace Canvas",
            description = if (AppStrings.isArabic()) "لوحة تفاعلية لا نهائية تجمع بين الملاحظات، الخرائط الذهنية، والأشكال" else "Infinite canvas with notes, mind map, shapes & connections",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            theme = "DARK_SLATE",
            zoom = 0.95f,
            panX = 150f,
            panY = 180f,
            gridType = GridType.DOTS,
            isFavorite = true,
            tags = listOf("Starter", "Workspace")
        )

        val elements = mutableListOf<CanvasElement>()

        // 1. Title Text Element
        elements.add(
            CanvasElement(
                id = "elem_title",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.TEXT,
                posX = 420f,
                posY = 80f,
                width = 380f,
                height = 70f,
                title = "IdeaSpace",
                content = if (AppStrings.isArabic()) "Think Freely. Connect Everything." else "Think Freely. Connect Everything.",
                textColorHex = "#38BDF8",
                fontSize = 24f,
                isBold = true,
                textAlign = "CENTER"
            )
        )

        // 2. Central Mind Map Node
        val centralMindMapId = "elem_mindmap_center"
        elements.add(
            CanvasElement(
                id = centralMindMapId,
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.MIND_MAP_NODE,
                posX = 460f,
                posY = 240f,
                width = 240f,
                height = 110f,
                title = if (AppStrings.isArabic()) "مشروع IdeaSpace" else "IdeaSpace Project",
                content = if (AppStrings.isArabic()) "المركز الرئيسي للأفكار والمشاريع" else "Central Hub for Creative Work",
                colorHex = "#6366F1",
                textColorHex = "#FFFFFF",
                strokeColorHex = "#A5B4FC",
                strokeWidth = 3f,
                fontSize = 18f,
                isBold = true
            )
        )

        // Mind Map Branches
        val branch1Id = "elem_mindmap_branch1"
        elements.add(
            CanvasElement(
                id = branch1Id,
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.MIND_MAP_NODE,
                posX = 140f,
                posY = 240f,
                width = 200f,
                height = 90f,
                title = if (AppStrings.isArabic()) "التفكير البصري" else "Visual Thinking",
                content = if (AppStrings.isArabic()) "لوحة لانهائية + أدوات رسم" else "Infinite Canvas & Sketching",
                colorHex = "#0EA5E9",
                textColorHex = "#FFFFFF",
                parentNodeId = centralMindMapId,
                fontSize = 15f
            )
        )

        val branch2Id = "elem_mindmap_branch2"
        elements.add(
            CanvasElement(
                id = branch2Id,
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.MIND_MAP_NODE,
                posX = 800f,
                posY = 240f,
                width = 210f,
                height = 90f,
                title = if (AppStrings.isArabic()) "إدارة المشاريع" else "Project Planning",
                content = if (AppStrings.isArabic()) "مهام كانبان وتتبع الإنجاز" else "Kanban & Task Tracking",
                colorHex = "#10B981",
                textColorHex = "#FFFFFF",
                parentNodeId = centralMindMapId,
                fontSize = 15f
            )
        )

        // 3. Sticky Notes with realistic paper vibes & checklist
        elements.add(
            CanvasElement(
                id = "elem_sticky_yellow",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.STICKY_NOTE,
                posX = 100f,
                posY = 410f,
                width = 240f,
                height = 230f,
                title = if (AppStrings.isArabic()) "مهام اللوحة اللانهائية" else "Canvas Milestones",
                content = if (AppStrings.isArabic()) "متابعة أهم وظائف اللوحة" else "Core canvas deliverables",
                colorHex = "#FEF08A", // Bright Sticky Yellow
                textColorHex = "#1E293B",
                priority = "HIGH",
                status = "IN_PROGRESS",
                tags = listOf("Canvas", "Core"),
                checklist = listOf(
                    ChecklistItem("chk_1", if (AppStrings.isArabic()) "التحريك والتكبير السلس" else "Smooth Pan & Zoom", true),
                    ChecklistItem("chk_2", if (AppStrings.isArabic()) "دعم اللمس والقلم المتعدد" else "Multi-touch & Stylus", true),
                    ChecklistItem("chk_3", if (AppStrings.isArabic()) "تصدير عالي الدقة" else "High-res export", false)
                )
            )
        )

        elements.add(
            CanvasElement(
                id = "elem_sticky_coral",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.STICKY_NOTE,
                posX = 800f,
                posY = 410f,
                width = 240f,
                height = 220f,
                title = if (AppStrings.isArabic()) "أفكار الإلهام" else "Inspiration Note",
                content = if (AppStrings.isArabic()) "كل فكرة عظيمة تبدأ بنقطة على مساحة بيضاء ثم تتسع لتصبح نظاماً متكاملاً." else "Every great idea starts with a simple point on a blank space and grows into a universe.",
                colorHex = "#FECDD3", // Coral Pink
                textColorHex = "#881337",
                priority = "MEDIUM",
                status = "TODO",
                tags = listOf("Design", "Thoughts")
            )
        )

        // 4. Geometric Shapes
        elements.add(
            CanvasElement(
                id = "elem_shape_diamond",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.SHAPE,
                posX = 480f,
                posY = 450f,
                width = 180f,
                height = 130f,
                shapeType = ShapeType.DIAMOND,
                title = if (AppStrings.isArabic()) "نقطة قرار" else "Decision",
                content = if (AppStrings.isArabic()) "جاهز للإطلاق؟" else "Ready to Launch?",
                colorHex = "#3B82F6",
                textColorHex = "#FFFFFF",
                strokeColorHex = "#93C5FD",
                strokeWidth = 2f
            )
        )

        elements.add(
            CanvasElement(
                id = "elem_shape_cloud",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.SHAPE,
                posX = 470f,
                posY = 650f,
                width = 210f,
                height = 120f,
                shapeType = ShapeType.CLOUD,
                title = if (AppStrings.isArabic()) "سحابة الأفكار" else "Brainstorm Cloud",
                content = if (AppStrings.isArabic()) "عصف ذهني حر بدون قيود" else "Unconstrained ideation",
                colorHex = "#8B5CF6",
                textColorHex = "#FFFFFF",
                strokeColorHex = "#C4B5FD",
                strokeWidth = 2f
            )
        )

        // 5. Task Card
        elements.add(
            CanvasElement(
                id = "elem_task_1",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.TASK_CARD,
                posX = 100f,
                posY = 680f,
                width = 250f,
                height = 160f,
                title = if (AppStrings.isArabic()) "بناء شبكة المعرفة" else "Build Knowledge Graph",
                content = if (AppStrings.isArabic()) "ربط العلاقات البصرية بين كافة عناصر اللوحة" else "Connect visual links across elements",
                colorHex = "#1E293B",
                textColorHex = "#F8FAFC",
                strokeColorHex = "#3B82F6",
                status = "TODO",
                priority = "HIGH",
                dueDate = "Tomorrow",
                tags = listOf("Graph", "Feature")
            )
        )

        // 6. File / Code Card
        elements.add(
            CanvasElement(
                id = "elem_file_card",
                boardId = DEFAULT_BOARD_ID,
                type = ElementType.FILE_CARD,
                posX = 800f,
                posY = 680f,
                width = 250f,
                height = 150f,
                title = "IdeaSpaceEngine.kt",
                content = "class InfiniteCanvas {\n  val pan = Offset.Zero\n  val zoom = 1.0f\n}",
                colorHex = "#0F172A",
                textColorHex = "#38BDF8",
                fileType = "CODE",
                tags = listOf("Kotlin", "Architecture")
            )
        )

        return Pair(board, elements)
    }

    fun createInitialConnections(): List<ElementConnection> {
        return listOf(
            ElementConnection(
                id = "conn_1",
                boardId = DEFAULT_BOARD_ID,
                fromElementId = "elem_mindmap_center",
                toElementId = "elem_mindmap_branch1",
                type = ConnectorType.CURVED,
                label = if (AppStrings.isArabic()) "يتضمن" else "Includes",
                colorHex = "#38BDF8",
                strokeWidth = 3f
            ),
            ElementConnection(
                id = "conn_2",
                boardId = DEFAULT_BOARD_ID,
                fromElementId = "elem_mindmap_center",
                toElementId = "elem_mindmap_branch2",
                type = ConnectorType.CURVED,
                label = if (AppStrings.isArabic()) "يوجه" else "Guides",
                colorHex = "#34D399",
                strokeWidth = 3f
            ),
            ElementConnection(
                id = "conn_3",
                boardId = DEFAULT_BOARD_ID,
                fromElementId = "elem_mindmap_center",
                toElementId = "elem_shape_diamond",
                type = ConnectorType.ARROW,
                label = if (AppStrings.isArabic()) "يؤدي إلى" else "Leads to",
                colorHex = "#60A5FA",
                strokeWidth = 2.5f
            ),
            ElementConnection(
                id = "conn_4",
                boardId = DEFAULT_BOARD_ID,
                fromElementId = "elem_shape_diamond",
                toElementId = "elem_shape_cloud",
                type = ConnectorType.ELBOW,
                label = if (AppStrings.isArabic()) "توسيع" else "Expand",
                colorHex = "#A78BFA",
                strokeWidth = 2.5f
            )
        )
    }

    fun createInitialLayers(): List<Layer> {
        return listOf(
            Layer("layer_bg", DEFAULT_BOARD_ID, if (AppStrings.isArabic()) "خلفية والشبكة" else "Background & Grid", true, false, 1.0f, 0),
            Layer("layer_default", DEFAULT_BOARD_ID, if (AppStrings.isArabic()) "العناصر والملاحظات" else "Elements & Notes", true, false, 1.0f, 1),
            Layer("layer_drawings", DEFAULT_BOARD_ID, if (AppStrings.isArabic()) "الرسومات والتعليقات" else "Sketches & Annotations", true, false, 1.0f, 2)
        )
    }

    // Smart Templates
    fun generateTemplate(templateType: String, boardId: String): List<CanvasElement> {
        val list = mutableListOf<CanvasElement>()
        when (templateType) {
            "SWOT" -> {
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.STICKY_NOTE,
                        posX = 200f,
                        posY = 150f,
                        width = 240f,
                        height = 200f,
                        title = if (AppStrings.isArabic()) "نقاط القوة (Strengths)" else "Strengths",
                        content = if (AppStrings.isArabic()) "• تجربة مستخدم سلسة\n• عمل محلي بالكامل دون إنترنت\n• تصميم بصري جذاب" else "• Offline first\n• Smooth gestures\n• High fidelity",
                        colorHex = "#BBF7D0",
                        textColorHex = "#14532D"
                    )
                )
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.STICKY_NOTE,
                        posX = 500f,
                        posY = 150f,
                        width = 240f,
                        height = 200f,
                        title = if (AppStrings.isArabic()) "نقاط الضعف (Weaknesses)" else "Weaknesses",
                        content = if (AppStrings.isArabic()) "• الحاجة لمزيد من القوالب\n• تحسين استهلاك البطارية" else "• Needs more templates\n• Battery tuning",
                        colorHex = "#FED7AA",
                        textColorHex = "#7C2D12"
                    )
                )
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.STICKY_NOTE,
                        posX = 200f,
                        posY = 400f,
                        width = 240f,
                        height = 200f,
                        title = if (AppStrings.isArabic()) "الفرص (Opportunities)" else "Opportunities",
                        content = if (AppStrings.isArabic()) "• التوسع للأجهزة اللوحية\n• دمج أدوات ذكاء اصطناعي محلية" else "• Expand to tablets\n• AI assistance",
                        colorHex = "#BAE6FD",
                        textColorHex = "#0C4A6E"
                    )
                )
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.STICKY_NOTE,
                        posX = 500f,
                        posY = 400f,
                        width = 240f,
                        height = 200f,
                        title = if (AppStrings.isArabic()) "التهديدات (Threats)" else "Threats",
                        content = if (AppStrings.isArabic()) "• منافسة تطبيقات الملاحظات\n• تعقيد اللوحات الضخمة" else "• Notes app competition\n• Heavy canvas sizes",
                        colorHex = "#FECDD3",
                        textColorHex = "#881337"
                    )
                )
            }
            "KANBAN" -> {
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.TASK_CARD,
                        posX = 150f,
                        posY = 200f,
                        width = 230f,
                        height = 150f,
                        title = if (AppStrings.isArabic()) "تخطيط واجهة المستخدم" else "UI Layout Wireframe",
                        content = if (AppStrings.isArabic()) "رسم المخططات المبدئية للشاشات" else "Wireframe preliminary screens",
                        status = "TODO",
                        priority = "HIGH",
                        colorHex = "#1E293B",
                        textColorHex = "#F8FAFC"
                    )
                )
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.TASK_CARD,
                        posX = 420f,
                        posY = 200f,
                        width = 230f,
                        height = 150f,
                        title = if (AppStrings.isArabic()) "محرك اللوحة اللانهائية" else "Infinite Canvas Engine",
                        content = if (AppStrings.isArabic()) "التحريك والتكبير والحفظ التلقائي" else "Pan, zoom, gesture recognition",
                        status = "IN_PROGRESS",
                        priority = "HIGH",
                        colorHex = "#1E293B",
                        textColorHex = "#F8FAFC"
                    )
                )
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.TASK_CARD,
                        posX = 690f,
                        posY = 200f,
                        width = 230f,
                        height = 150f,
                        title = if (AppStrings.isArabic()) "إعداد قاعدة البيانات المحلية" else "Local Room DB Setup",
                        content = if (AppStrings.isArabic()) "جداول اللوحات والعناصر والروابط" else "Boards, elements, and relations",
                        status = "DONE",
                        priority = "MEDIUM",
                        colorHex = "#1E293B",
                        textColorHex = "#F8FAFC"
                    )
                )
            }
            else -> { // BRAINSTORM
                list.add(
                    CanvasElement(
                        id = UUID.randomUUID().toString(),
                        boardId = boardId,
                        type = ElementType.MIND_MAP_NODE,
                        posX = 450f,
                        posY = 250f,
                        width = 220f,
                        height = 100f,
                        title = if (AppStrings.isArabic()) "الفكرة الرئيسية" else "Main Concept",
                        content = if (AppStrings.isArabic()) "ابدأ بالتفريع من هنا" else "Branch out from here",
                        colorHex = "#6366F1",
                        textColorHex = "#FFFFFF"
                    )
                )
            }
        }
        return list
    }
}
