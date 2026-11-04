package com.example.ui.theme

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.util.AppStrings
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class Scenery { NONE, DUNES, STARS, OASIS, SEA, PAPER }

data class IdeaPalette(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val sky: List<Color>,
    val primary: Color,
    val secondary: Color,
    val surface: Color,
    val onSurfaceVariant: Color,
    val scenery: Scenery
)

object IdeaThemes {
    private val classic = IdeaPalette("classic", "الكلاسيكي", "Classic",
        listOf(Color(0xFF0B0F19), Color(0xFF0B0F19)), IdeaCyan, IdeaViolet, SurfaceDark, TextSecondaryDark, Scenery.NONE)
    private val andalus = IdeaPalette("andalus", "ليل أندلسي", "Andalusian Night",
        listOf(Color(0xFF1B1446), Color(0xFF0E1236), Color(0xFF070A22)), Color(0xFFE9B949), Color(0xFF8B93FF), Color(0xFF15133A), Color(0xFFB8B4D9), Scenery.STARS)
    private val dunes = IdeaPalette("dunes", "كثبان الغروب", "Dunes at Dusk",
        listOf(Color(0xFF241636), Color(0xFF6B2D4F), Color(0xFFB4573F)), Color(0xFFFFB86B), Color(0xFFF472B6), Color(0xFF2A1A2E), Color(0xFFE2C4C0), Scenery.DUNES)
    private val oasis = IdeaPalette("oasis", "واحة", "Oasis",
        listOf(Color(0xFF062B2A), Color(0xFF0B4B3F), Color(0xFF0F6B4F)), Color(0xFF5EEAD4), Color(0xFFFACC15), Color(0xFF0A2F2B), Color(0xFFB5D9CF), Scenery.OASIS)
    private val sea = IdeaPalette("sea", "بحر وسماء", "Sea & Sky",
        listOf(Color(0xFF071A3A), Color(0xFF0B3A6B), Color(0xFF0E5E86)), Color(0xFF67E8F9), Color(0xFFA5B4FC), Color(0xFF0A2347), Color(0xFFB4C6E0), Scenery.SEA)
    private val parchment = IdeaPalette("parchment", "حبر وورق", "Ink & Parchment",
        listOf(Color(0xFF3A2A1E), Color(0xFF5A4030), Color(0xFF47322A)), Color(0xFFE3B778), Color(0xFFE08A73), Color(0xFF33241A), Color(0xFFD9C3A5), Scenery.PAPER)

    val all = listOf(andalus, dunes, oasis, sea, parchment, classic)

    fun byId(id: String?): IdeaPalette = all.firstOrNull { it.id == id } ?: all[0]

    private const val PREFS = "ideaspace_prefs"
    fun load(ctx: Context): String =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("palette", "andalus") ?: "andalus"
    fun save(ctx: Context, id: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("palette", id).apply()
    }
}

private fun DrawScope.hills(color: Color, baseY: Float, amp: Float, waves: Float, phase: Float) {
    val steps = 48
    val p = Path()
    p.moveTo(0f, size.height)
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        p.lineTo(size.width * t, baseY + amp * sin(t * waves * 2f * PI.toFloat() + phase))
    }
    p.lineTo(size.width, size.height)
    p.close()
    drawPath(p, color)
}

private fun DrawScope.stars(count: Int, color: Color, maxAlpha: Float = 0.8f) {
    var s = 12345L
    repeat(count) {
        s = (s * 1103515245L + 12345L) and 0x7fffffffL
        val x = ((s shr 8) % 1000L) / 1000f * size.width
        s = (s * 1103515245L + 12345L) and 0x7fffffffL
        val y = ((s shr 8) % 1000L) / 1000f * size.height * 0.8f
        s = (s * 1103515245L + 12345L) and 0x7fffffffL
        val k = ((s shr 8) % 100L) / 100f
        drawCircle(color.copy(alpha = (0.2f + k * maxAlpha).coerceAtMost(1f)), 0.7f + k * 1.5f, Offset(x, y))
    }
}

// Eight-point star lattice (two overlapping squares per cell), a classic geometric motif
private fun DrawScope.lattice(color: Color, cell: Float) {
    val r = cell * 0.42f
    var y = 0f
    while (y < size.height + cell) {
        var x = 0f
        while (x < size.width + cell) {
            for (k in 0..1) {
                val path = Path()
                for (i in 0..3) {
                    val a = (PI / 2 * i + (if (k == 0) 0.0 else PI / 4)).toFloat()
                    val px = x + r * cos(a)
                    val py = y + r * sin(a)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, color, style = Stroke(width = 1f))
            }
            x += cell
        }
        y += cell
    }
}

@Composable
fun ThemedBackdrop(p: IdeaPalette, modifier: Modifier = Modifier) {
    Box(
        modifier.drawBehind {
            val w = size.width
            val h = size.height
            drawRect(Brush.verticalGradient(p.sky))
            when (p.scenery) {
                Scenery.STARS -> {
                    lattice(Color(0xFFE9B949).copy(alpha = 0.07f), 130f)
                    stars(70, Color.White)
                }
                Scenery.DUNES -> {
                    drawCircle(Color(0xFFFFD29A).copy(alpha = 0.25f), w * 0.22f, Offset(w * 0.72f, h * 0.62f))
                    hills(Color(0xFF7A3B3A).copy(alpha = 0.9f), h * 0.80f, h * 0.035f, 1.3f, 0.6f)
                    hills(Color(0xFF4A2530), h * 0.88f, h * 0.03f, 1.7f, 2.2f)
                    hills(Color(0xFF2B1626), h * 0.95f, h * 0.02f, 2.1f, 4.0f)
                }
                Scenery.OASIS -> {
                    stars(25, Color(0xFFFDE68A), 0.5f)
                    hills(Color(0xFF0E5A45).copy(alpha = 0.85f), h * 0.78f, h * 0.05f, 1.1f, 1.0f)
                    hills(Color(0xFF0A3F34), h * 0.88f, h * 0.04f, 1.6f, 3.0f)
                    hills(Color(0xFF062B26), h * 0.96f, h * 0.025f, 2.2f, 5.0f)
                }
                Scenery.SEA -> {
                    stars(20, Color.White, 0.5f)
                    hills(Color(0xFF1AA6B7).copy(alpha = 0.22f), h * 0.80f, h * 0.02f, 3.5f, 0.5f)
                    hills(Color(0xFF0E7C99).copy(alpha = 0.35f), h * 0.87f, h * 0.022f, 3.0f, 2.0f)
                    hills(Color(0xFF083A5E).copy(alpha = 0.7f), h * 0.94f, h * 0.02f, 2.6f, 4.0f)
                }
                Scenery.PAPER -> {
                    stars(140, Color(0xFFE3B778), 0.25f)
                    drawRect(
                        Brush.radialGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                            center = Offset(w / 2f, h / 2f),
                            radius = maxOf(w, h) * 0.8f
                        )
                    )
                }
                Scenery.NONE -> {}
            }
        }
    )
}

@Composable
fun ThemePickerDialog(currentId: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val ar = AppStrings.isArabic()
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (ar) "اختر مظهر المساحة" else "Choose your space",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IdeaThemes.all.forEach { t ->
                    val sel = t.id == currentId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(t.sky))
                            .border(
                                if (sel) 2.dp else 1.dp,
                                if (sel) t.primary else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(t.id); onDismiss() }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(14.dp).background(t.primary, CircleShape))
                        Spacer(Modifier.width(10.dp))
                        Text(if (ar) t.nameAr else t.nameEn, color = Color.White)
                    }
                }
            }
        }
    }
}
