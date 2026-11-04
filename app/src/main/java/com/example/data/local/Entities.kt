package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val theme: String,
    val zoom: Float,
    val panX: Float,
    val panY: Float,
    val gridType: String,
    val isFavorite: Boolean,
    val tagsJson: String
) {
    fun toDomain(): Board {
        val tagsList = mutableListOf<String>()
        if (tagsJson.isNotEmpty()) {
            try {
                val array = JSONArray(tagsJson)
                for (i in 0 until array.length()) {
                    tagsList.add(array.getString(i))
                }
            } catch (_: Exception) {}
        }
        val grid = try { GridType.valueOf(gridType) } catch (_: Exception) { GridType.DOTS }
        return Board(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            theme = theme,
            zoom = zoom,
            panX = panX,
            panY = panY,
            gridType = grid,
            isFavorite = isFavorite,
            tags = tagsList
        )
    }

    companion object {
        fun fromDomain(board: Board): BoardEntity {
            val tagsArr = JSONArray(board.tags)
            return BoardEntity(
                id = board.id,
                title = board.title,
                description = board.description,
                createdAt = board.createdAt,
                updatedAt = board.updatedAt,
                theme = board.theme,
                zoom = board.zoom,
                panX = board.panX,
                panY = board.panY,
                gridType = board.gridType.name,
                isFavorite = board.isFavorite,
                tagsJson = tagsArr.toString()
            )
        }
    }
}

@Entity(tableName = "elements")
data class ElementEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val type: String,
    val posX: Float,
    val posY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val zIndex: Int,
    val layerId: String,
    val groupId: String?,
    val isLocked: Boolean,
    val isPinned: Boolean,
    val isCollapsed: Boolean,
    val title: String,
    val content: String,
    val colorHex: String,
    val textColorHex: String,
    val strokeColorHex: String,
    val strokeWidth: Float,
    val opacity: Float,
    val fontSize: Float,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val textAlign: String,
    val shapeType: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val tagsJson: String,
    val checklistJson: String,
    val strokesJson: String,
    val parentNodeId: String?,
    val fileType: String,
    val imageUrl: String,
    val extraData: String
) {
    fun toDomain(): CanvasElement {
        val elemType = try { ElementType.valueOf(type) } catch (_: Exception) { ElementType.STICKY_NOTE }
        val sType = try { ShapeType.valueOf(shapeType) } catch (_: Exception) { ShapeType.ROUNDED_RECT }

        val tagsList = mutableListOf<String>()
        if (tagsJson.isNotEmpty()) {
            try {
                val arr = JSONArray(tagsJson)
                for (i in 0 until arr.length()) tagsList.add(arr.getString(i))
            } catch (_: Exception) {}
        }

        val checklistList = mutableListOf<ChecklistItem>()
        if (checklistJson.isNotEmpty()) {
            try {
                val arr = JSONArray(checklistJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    checklistList.add(
                        ChecklistItem(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            isDone = obj.optBoolean("isDone", false)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        val strokesList = mutableListOf<DrawingStroke>()
        if (strokesJson.isNotEmpty()) {
            try {
                val arr = JSONArray(strokesJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val ptsArr = obj.getJSONArray("points")
                    val pts = mutableListOf<Point>()
                    for (j in 0 until ptsArr.length()) {
                        val pObj = ptsArr.getJSONObject(j)
                        pts.add(Point(pObj.getDouble("x").toFloat(), pObj.getDouble("y").toFloat()))
                    }
                    strokesList.add(
                        DrawingStroke(
                            id = obj.optString("id", i.toString()),
                            points = pts,
                            colorHex = obj.optString("colorHex", "#38BDF8"),
                            strokeWidth = obj.optDouble("strokeWidth", 4.0).toFloat(),
                            isHighlighter = obj.optBoolean("isHighlighter", false)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        return CanvasElement(
            id = id,
            boardId = boardId,
            type = elemType,
            posX = posX,
            posY = posY,
            width = width,
            height = height,
            rotation = rotation,
            zIndex = zIndex,
            layerId = layerId,
            groupId = groupId,
            isLocked = isLocked,
            isPinned = isPinned,
            isCollapsed = isCollapsed,
            title = title,
            content = content,
            colorHex = colorHex,
            textColorHex = textColorHex,
            strokeColorHex = strokeColorHex,
            strokeWidth = strokeWidth,
            opacity = opacity,
            fontSize = fontSize,
            isBold = isBold,
            isItalic = isItalic,
            isUnderline = isUnderline,
            textAlign = textAlign,
            shapeType = sType,
            priority = priority,
            status = status,
            dueDate = dueDate,
            tags = tagsList,
            checklist = checklistList,
            strokes = strokesList,
            parentNodeId = parentNodeId,
            fileType = fileType,
            imageUrl = imageUrl,
            extraData = extraData
        )
    }

    companion object {
        fun fromDomain(element: CanvasElement): ElementEntity {
            val tagsArr = JSONArray(element.tags)

            val checklistArr = JSONArray()
            for (c in element.checklist) {
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("text", c.text)
                obj.put("isDone", c.isDone)
                checklistArr.put(obj)
            }

            val strokesArr = JSONArray()
            for (s in element.strokes) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("colorHex", s.colorHex)
                obj.put("strokeWidth", s.strokeWidth.toDouble())
                obj.put("isHighlighter", s.isHighlighter)
                val ptsArr = JSONArray()
                for (p in s.points) {
                    val pObj = JSONObject()
                    pObj.put("x", p.x.toDouble())
                    pObj.put("y", p.y.toDouble())
                    ptsArr.put(pObj)
                }
                obj.put("points", ptsArr)
                strokesArr.put(obj)
            }

            return ElementEntity(
                id = element.id,
                boardId = element.boardId,
                type = element.type.name,
                posX = element.posX,
                posY = element.posY,
                width = element.width,
                height = element.height,
                rotation = element.rotation,
                zIndex = element.zIndex,
                layerId = element.layerId,
                groupId = element.groupId,
                isLocked = element.isLocked,
                isPinned = element.isPinned,
                isCollapsed = element.isCollapsed,
                title = element.title,
                content = element.content,
                colorHex = element.colorHex,
                textColorHex = element.textColorHex,
                strokeColorHex = element.strokeColorHex,
                strokeWidth = element.strokeWidth,
                opacity = element.opacity,
                fontSize = element.fontSize,
                isBold = element.isBold,
                isItalic = element.isItalic,
                isUnderline = element.isUnderline,
                textAlign = element.textAlign,
                shapeType = element.shapeType.name,
                priority = element.priority,
                status = element.status,
                dueDate = element.dueDate,
                tagsJson = tagsArr.toString(),
                checklistJson = checklistArr.toString(),
                strokesJson = strokesArr.toString(),
                parentNodeId = element.parentNodeId,
                fileType = element.fileType,
                imageUrl = element.imageUrl,
                extraData = element.extraData
            )
        }
    }
}

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val fromElementId: String,
    val toElementId: String,
    val type: String,
    val label: String,
    val colorHex: String,
    val strokeWidth: Float,
    val isDashed: Boolean
) {
    fun toDomain(): ElementConnection {
        val cType = try { ConnectorType.valueOf(type) } catch (_: Exception) { ConnectorType.CURVED }
        return ElementConnection(
            id = id,
            boardId = boardId,
            fromElementId = fromElementId,
            toElementId = toElementId,
            type = cType,
            label = label,
            colorHex = colorHex,
            strokeWidth = strokeWidth,
            isDashed = isDashed
        )
    }

    companion object {
        fun fromDomain(c: ElementConnection): ConnectionEntity {
            return ConnectionEntity(
                id = c.id,
                boardId = c.boardId,
                fromElementId = c.fromElementId,
                toElementId = c.toElementId,
                type = c.type.name,
                label = c.label,
                colorHex = c.colorHex,
                strokeWidth = c.strokeWidth,
                isDashed = c.isDashed
            )
        }
    }
}

@Entity(tableName = "layers")
data class LayerEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val name: String,
    val isVisible: Boolean,
    val isLocked: Boolean,
    val opacity: Float,
    val orderIndex: Int
) {
    fun toDomain(): Layer = Layer(id, boardId, name, isVisible, isLocked, opacity, orderIndex)

    companion object {
        fun fromDomain(layer: Layer): LayerEntity =
            LayerEntity(layer.id, layer.boardId, layer.name, layer.isVisible, layer.isLocked, layer.opacity, layer.orderIndex)
    }
}
