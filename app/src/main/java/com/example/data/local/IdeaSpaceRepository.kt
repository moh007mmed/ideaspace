package com.example.data.local

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IdeaSpaceRepository(private val dao: IdeaSpaceDao) {

    fun getAllBoards(): Flow<List<Board>> =
        dao.getAllBoards().map { list -> list.map { it.toDomain() } }

    fun getBoard(boardId: String): Flow<Board?> =
        dao.getBoardById(boardId).map { it?.toDomain() }

    suspend fun saveBoard(board: Board) {
        dao.insertBoard(BoardEntity.fromDomain(board))
    }

    suspend fun deleteBoard(boardId: String) {
        dao.deleteBoardById(boardId)
        dao.deleteAllElementsByBoard(boardId)
        dao.deleteAllConnectionsByBoard(boardId)
        dao.deleteAllLayersByBoard(boardId)
    }

    // Elements
    fun getElements(boardId: String): Flow<List<CanvasElement>> =
        dao.getElementsByBoard(boardId).map { list -> list.map { it.toDomain() } }

    suspend fun saveElement(element: CanvasElement) {
        dao.insertElement(ElementEntity.fromDomain(element))
    }

    suspend fun saveElements(elements: List<CanvasElement>) {
        dao.insertElements(elements.map { ElementEntity.fromDomain(it) })
    }

    suspend fun deleteElement(elementId: String) {
        dao.deleteElementById(elementId)
        dao.deleteConnectionsForElement(elementId)
    }

    suspend fun deleteElements(elementIds: List<String>) {
        dao.deleteElementsByIds(elementIds)
    }

    // Connections
    fun getConnections(boardId: String): Flow<List<ElementConnection>> =
        dao.getConnectionsByBoard(boardId).map { list -> list.map { it.toDomain() } }

    suspend fun saveConnection(connection: ElementConnection) {
        dao.insertConnection(ConnectionEntity.fromDomain(connection))
    }

    suspend fun saveConnections(connections: List<ElementConnection>) {
        dao.insertConnections(connections.map { ConnectionEntity.fromDomain(it) })
    }

    suspend fun deleteConnection(connectionId: String) {
        dao.deleteConnectionById(connectionId)
    }

    // Layers
    fun getLayers(boardId: String): Flow<List<Layer>> =
        dao.getLayersByBoard(boardId).map { list -> list.map { it.toDomain() } }

    suspend fun saveLayer(layer: Layer) {
        dao.insertLayer(LayerEntity.fromDomain(layer))
    }

    suspend fun saveLayers(layers: List<Layer>) {
        dao.insertLayers(layers.map { LayerEntity.fromDomain(it) })
    }

    suspend fun deleteLayer(layerId: String) {
        dao.deleteLayerById(layerId)
    }
}
