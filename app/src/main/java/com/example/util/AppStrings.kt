package com.example.util

/**
 * Bilingual localization manager for IdeaSpace supporting both Arabic (RTL) and English (LTR).
 */
enum class AppLanguage(val code: String, val displayName: String) {
    ARABIC("ar", "العربية"),
    ENGLISH("en", "English")
}

object AppStrings {
    var currentLanguage: AppLanguage = AppLanguage.ARABIC

    fun isArabic(): Boolean = currentLanguage == AppLanguage.ARABIC

    // General & Branding
    val appName: String get() = if (isArabic()) "IdeaSpace — مساحة الأفكار" else "IdeaSpace"
    val tagline: String get() = if (isArabic()) "فكّر بحرية. اربط كل شيء." else "Think Freely. Connect Everything."
    
    // Modes
    val modeNavigation: String get() = if (isArabic()) "وضع الاستكشاف (Pan)" else "Navigation Mode"
    val modeEditing: String get() = if (isArabic()) "وضع التعديل (Edit)" else "Editing Mode"
    val modeDrawing: String get() = if (isArabic()) "وضع الرسم (Draw)" else "Drawing Mode"
    val modeConnecting: String get() = if (isArabic()) "وضع الربط (Connect)" else "Connect Mode"

    // Canvas actions
    val fitContent: String get() = if (isArabic()) "ملاءمة المحتوى" else "Fit Content"
    val centerView: String get() = if (isArabic()) "توسيط اللوحة" else "Center View"
    val resetZoom: String get() = if (isArabic()) "100% تكبير" else "100% Zoom"
    val zoomIn: String get() = if (isArabic()) "تكبير" else "Zoom In"
    val zoomOut: String get() = if (isArabic()) "تصغير" else "Zoom Out"
    val undo: String get() = if (isArabic()) "تراجع" else "Undo"
    val redo: String get() = if (isArabic()) "إعادة" else "Redo"
    val layers: String get() = if (isArabic()) "الطبقات" else "Layers"
    val grid: String get() = if (isArabic()) "الشبكة" else "Grid"
    val snapToGrid: String get() = if (isArabic()) "المحاذاة للشبكة" else "Snap to Grid"
    val miniMap: String get() = if (isArabic()) "الخريطة المصغرة" else "MiniMap"
    val search: String get() = if (isArabic()) "بحث في اللوحة..." else "Search canvas..."
    val quickCapture: String get() = if (isArabic()) "التقاط سريع" else "Quick Capture"

    // Element Types
    val text: String get() = if (isArabic()) "نص حر" else "Free Text"
    val stickyNote: String get() = if (isArabic()) "ملاحظة لاصقة" else "Sticky Note"
    val shape: String get() = if (isArabic()) "شكل هندسي" else "Shape"
    val mindMapNode: String get() = if (isArabic()) "عقدة خريطة ذهنية" else "Mind Map Node"
    val drawing: String get() = if (isArabic()) "رسم حر" else "Drawing"
    val image: String get() = if (isArabic()) "صورة" else "Image"
    val fileCard: String get() = if (isArabic()) "بطاقة ملف / كود" else "File / Code Card"
    val taskCard: String get() = if (isArabic()) "مهمة / Kanban" else "Task Card"

    // Element Actions
    val addElement: String get() = if (isArabic()) "إضافة عنصر" else "Add Element"
    val editElement: String get() = if (isArabic()) "تعديل" else "Edit"
    val deleteElement: String get() = if (isArabic()) "حذف" else "Delete"
    val duplicateElement: String get() = if (isArabic()) "مضاعفة" else "Duplicate"
    val lockElement: String get() = if (isArabic()) "قفل" else "Lock"
    val unlockElement: String get() = if (isArabic()) "إلغاء القفل" else "Unlock"
    val pinElement: String get() = if (isArabic()) "تثبيت" else "Pin"
    val unpinElement: String get() = if (isArabic()) "إلغاء التثبيت" else "Unpin"
    val bringToFront: String get() = if (isArabic()) "إلى الأمام" else "Bring to Front"
    val sendToBack: String get() = if (isArabic()) "إلى الخلف" else "Send to Back"
    val connectTo: String get() = if (isArabic()) "ربط مع عنصر آخر" else "Connect to..."
    val addChildNode: String get() = if (isArabic()) "إضافة فرع (+)" else "Add Branch (+)"
    val addSiblingNode: String get() = if (isArabic()) "إضافة فكرة موازية" else "Add Sibling"
    val expandCollapse: String get() = if (isArabic()) "طي / توسيع" else "Collapse / Expand"
    val changeColor: String get() = if (isArabic()) "تغيير اللون" else "Change Color"

    // Workspaces & Views
    val canvasView: String get() = if (isArabic()) "اللوحة اللانهائية" else "Infinite Canvas"
    val kanbanView: String get() = if (isArabic()) "لوحة كانبان" else "Kanban Board"
    val outlineView: String get() = if (isArabic()) "مخطط الأفكار" else "Outline Tree"
    val graphView: String get() = if (isArabic()) "شبكة المعرفة" else "Knowledge Graph"
    val presentationMode: String get() = if (isArabic()) "وضع العرض التقديمي" else "Presentation Mode"
    val ideaUniverse: String get() = if (isArabic()) "كون الأفكار" else "Idea Universe"
    val timeTravel: String get() = if (isArabic()) "السفر عبر الزمن" else "Time Travel"
    val templates: String get() = if (isArabic()) "قوالب ذكية" else "Smart Templates"
    val boards: String get() = if (isArabic()) "اللوحات والمشاريع" else "Boards & Projects"
    val newBoard: String get() = if (isArabic()) "لوحة جديدة" else "New Board"

    // Kanban statuses
    val todo: String get() = if (isArabic()) "قيد الانتظار" else "To Do"
    val inProgress: String get() = if (isArabic()) "قيد العمل" else "In Progress"
    val done: String get() = if (isArabic()) "مكتمل" else "Done"

    // Priorities
    val priorityHigh: String get() = if (isArabic()) "عالية الأهمية" else "High Priority"
    val priorityMedium: String get() = if (isArabic()) "متوسطة" else "Medium Priority"
    val priorityLow: String get() = if (isArabic()) "عادية" else "Low Priority"

    // Connectors
    val relationRelated: String get() = if (isArabic()) "مرتبط بـ" else "Related To"
    val relationDepends: String get() = if (isArabic()) "يعتمد على" else "Depends On"
    val relationCauses: String get() = if (isArabic()) "يؤدي إلى" else "Causes"
    val relationSupports: String get() = if (isArabic()) "يدعم" else "Supports"
    val relationChildOf: String get() = if (isArabic()) "فرع من" else "Child Of"

    // Export & Backup
    val exportImport: String get() = if (isArabic()) "النسخ والتصدير" else "Backup & Export"
    val exportJson: String get() = if (isArabic()) "تصدير نسخة JSON احتياطية" else "Export JSON Backup"
    val importJson: String get() = if (isArabic()) "استيراد نسخة JSON" else "Import JSON Backup"
    val exportMarkdown: String get() = if (isArabic()) "تصدير ملخص Markdown" else "Export Markdown Summary"
    val copiedToClipboard: String get() = if (isArabic()) "تم النسخ إلى الحافظة!" else "Copied to clipboard!"
    val backupRestored: String get() = if (isArabic()) "تم استعادة النسخة الاحتياطية بنجاح!" else "Backup restored successfully!"

    // Dialogs & Feedback
    val title: String get() = if (isArabic()) "العنوان" else "Title"
    val content: String get() = if (isArabic()) "المحتوى" else "Content"
    val save: String get() = if (isArabic()) "حفظ" else "Save"
    val cancel: String get() = if (isArabic()) "إلغاء" else "Cancel"
    val clear: String get() = if (isArabic()) "مسح" else "Clear"
    val close: String get() = if (isArabic()) "إغلاق" else "Close"
    val select: String get() = if (isArabic()) "اختيار" else "Select"
    val language: String get() = if (isArabic()) "اللغة" else "Language"
    val theme: String get() = if (isArabic()) "المظهر" else "Theme"
    val darkSlate: String get() = if (isArabic()) "داكن أزرق (Dark Slate)" else "Dark Slate"
    val oledBlack: String get() = if (isArabic()) "أسود فاحم (OLED Black)" else "OLED Black"
    val cleanLight: String get() = if (isArabic()) "فاتح عصري (Clean Light)" else "Clean Light"

    val emptyBoardTitle: String get() = if (isArabic()) "اللوحة فارغة، ابدأ بإطلاق أفكارك!" else "Your canvas is empty, start creating!"
    val emptyBoardTip: String get() = if (isArabic()) "اضغط على زر (+) في الأسفل لإضافة نصوص، ملاحظات، أشكال، أو اختر قالباً جاهزاً." else "Tap the (+) button below to add text, sticky notes, shapes, or pick a smart template."
}
