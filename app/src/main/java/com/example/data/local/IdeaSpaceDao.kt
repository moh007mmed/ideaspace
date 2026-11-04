package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaSpaceDao {
    @Query("SELECT * FROM boards ORDER BY updatedAt DESC")
    fun getAllBoards(): Flow<List<BoardEntity>>

    @Query("SELECT * FROM boards WHERE id = :boardId LIMIT 1")
    fun getBoardById(boardId: String): Flow<BoardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity)

    @Delete
    suspend fun deleteBoard(board: BoardEntity)

    @Query("DELETE FROM boards WHERE id = :boardId")
    suspend fun deleteBoardById(boardId: String)

    // Elements
    @Query("SELECT * FROM elements WHERE boardId = :boardId ORDER BY zIndex ASC")
    fun getElementsByBoard(boardId: String): Flow<List<ElementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: ElementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElements(elements: List<ElementEntity>)

    @Query("DELETE FROM elements WHERE id = :elementId")
    suspend fun deleteElementById(elementId: String)

    @Query("DELETE FROM elements WHERE id IN (:elementIds)")
    suspend fun deleteElementsByIds(elementIds: List<String>)

    @Query("DELETE FROM elements WHERE boardId = :boardId")
    suspend fun deleteAllElementsByBoard(boardId: String)

    // Connections
    @Query("SELECT * FROM connections WHERE boardId = :boardId")
    fun getConnectionsByBoard(boardId: String): Flow<List<ConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConnectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<ConnectionEntity>)

    @Query("DELETE FROM connections WHERE id = :connectionId")
    suspend fun deleteConnectionById(connectionId: String)

    @Query("DELETE FROM connections WHERE fromElementId = :elementId OR toElementId = :elementId")
    suspend fun deleteConnectionsForElement(elementId: String)

    @Query("DELETE FROM connections WHERE boardId = :boardId")
    suspend fun deleteAllConnectionsByBoard(boardId: String)

    // Layers
    @Query("SELECT * FROM layers WHERE boardId = :boardId ORDER BY orderIndex ASC")
    fun getLayersByBoard(boardId: String): Flow<List<LayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayer(layer: LayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayers(layers: List<LayerEntity>)

    @Query("DELETE FROM layers WHERE id = :layerId")
    suspend fun deleteLayerById(layerId: String)

    @Query("DELETE FROM layers WHERE boardId = :boardId")
    suspend fun deleteAllLayersByBoard(boardId: String)
}
