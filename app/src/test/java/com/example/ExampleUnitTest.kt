package com.example

import com.example.model.*
import com.example.util.SampleBoards
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun verify_shape_types_count() {
    assertEquals(12, ShapeType.values().size)
  }

  @Test
  fun verify_interaction_modes() {
    assertTrue(InteractionMode.values().contains(InteractionMode.NAVIGATION))
    assertTrue(InteractionMode.values().contains(InteractionMode.EDITING))
    assertTrue(InteractionMode.values().contains(InteractionMode.DRAWING))
    assertTrue(InteractionMode.values().contains(InteractionMode.CONNECTING))
  }

  @Test
  fun verify_sample_starter_board_creation() {
    val (board, elements) = SampleBoards.createInitialBoard()
    assertEquals(SampleBoards.DEFAULT_BOARD_ID, board.id)
    assertTrue(elements.isNotEmpty())
    assertNotNull(elements.find { it.type == ElementType.MIND_MAP_NODE })
    assertNotNull(elements.find { it.type == ElementType.STICKY_NOTE })
  }

  @Test
  fun verify_template_generation() {
    val swot = SampleBoards.generateTemplate("SWOT", "test_board")
    assertEquals(4, swot.size)
    val kanban = SampleBoards.generateTemplate("KANBAN", "test_board")
    assertEquals(3, kanban.size)
  }
}
