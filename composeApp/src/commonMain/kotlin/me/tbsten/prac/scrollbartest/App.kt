package me.tbsten.prac.scrollbartest

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.prac.scrollbartest.theme.AppTheme
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun App(percent: Float? = null) = AppTheme {
    val scrollState = rememberScrollState()
    val outerRadiusDp = 20.dp
    val shape = RoundedCornerShape(outerRadiusDp)

    Column(
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFF363636), shape = shape)
            .widthIn(max = 300.dp)
            .heightIn(max = 300.dp)
            .drawWithContent {
                drawContent()
                drawScrollBar(
                    scrollState = scrollState,
                    percent = percent?:scrollState.percent,
                    outerRadiusDp = outerRadiusDp,
                    innerGapDp = outerRadiusDp / 2,
                )
            }
            .verticalScroll(scrollState)
            .padding(outerRadiusDp),
    ) {
        repeat(50) {
            CompositionLocalProvider(
                LocalContentColor provides Color.White
            ) {
                Item(
                    index = it,
                    onClick = { },
                )
            }
        }
    }
}

val ScrollState.percent: Float
    get() = value.toFloat() / maxValue

private fun DrawScope.drawScrollBar(
    scrollState: ScrollState,
    outerRadiusDp: Dp,
    innerGapDp: Dp = 10.dp,
    percent: Float? = null,
) {
    val outerRadius = outerRadiusDp.toPx()
    val innerGap = innerGapDp.toPx()
    val innerRadius = (outerRadiusDp - innerGapDp).toPx()
    val totalPercent = (percent ?: scrollState.percent)
//        .also { check(it in 0f..1f) { "invalid percent: $it"} }
        .coerceIn(0f, 1f)
    println("totalPercent: $totalPercent")

    drawPath(
        innerPathAppliedPercent(
            outerRadius = outerRadius,
            size = size,
            innerGap = innerGap,
            innerRadius = innerRadius,
            totalPercent = totalPercent,
        ),
        Color(0xFFFF8263),
        style = Stroke(width = 4.dp.toPx()),
//        style = Stroke(width = 1f),
    )
}

infix fun ClosedFloatingPointRange<Float>.intersection(other: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float>? =
    when {
        other.endInclusive < this.start -> null
        this.endInclusive < other.start -> null
        else -> max(this.start, other.start)..min(this.endInclusive, other.endInclusive)
    }


fun innerPathAppliedPercent(
    outerRadius: Float,
    size: Size,
    innerGap: Float,
    innerRadius: Float,
    totalPercent: Float,
) = Path().apply {
    val oneHorizontalEdgeLength = size.width - outerRadius * 2
    val oneVerticalEdgeLength = size.height - outerRadius * 2
    val cornerLength = innerRadius * 2 * PI.toFloat() / 4
    val totalLength =
        (oneHorizontalEdgeLength * 2 + oneVerticalEdgeLength * 2 +
                        cornerLength * 4
                )
    val usableTotalLength = totalLength * totalPercent
    var usedLength = 0f

    moveTo(outerRadius, innerGap)

    // 上辺
    val topLength = oneHorizontalEdgeLength
    val topPercent = (min(usableTotalLength - usedLength, topLength) / topLength).coerceIn(0f, 1f)
    lineTo(outerRadius + topLength * topPercent, innerGap)
    usedLength += topLength

    println("上辺: usedLength:$usedLength totalLength:$totalLength usableTotalLength:$usableTotalLength usedLength:$usedLength topLength:$topLength topPercent:$topPercent")
    if (usableTotalLength <= usedLength) return@apply

    // 右上の角丸
    val topRightLength = cornerLength
    val topRightPercent =
        (min(usableTotalLength - usedLength, topRightLength) / topRightLength).coerceIn(0f, 1f)
    arcTo(
        Rect(
            Offset(size.width - outerRadius - innerRadius, innerGap),
            Size(innerRadius * 2, innerRadius * 2),
        ),
        startAngleDegrees = -90f,
        sweepAngleDegrees = 90f * topRightPercent,
        forceMoveTo = false
    )
    usedLength += topRightLength

    println("右上の角丸: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 右辺
    val rightLength = oneVerticalEdgeLength
    val rightPercent =
        (min(usableTotalLength - usedLength, rightLength) / rightLength).coerceIn(0f, 1f)
    lineTo(size.width - innerGap, outerRadius + rightLength * rightPercent)
    usedLength += rightLength

    println("右辺: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 右下の角丸
    val bottomRightLength = cornerLength
    val bottomRightPercent =
        (min(usableTotalLength - usedLength, bottomRightLength) / bottomRightLength).coerceIn(
            0f,
            1f
        )
    arcTo(
        Rect(
            Offset(
                size.width - innerGap - innerRadius * 2,
                size.height - innerGap - innerRadius * 2
            ),
            Size(innerRadius * 2, innerRadius * 2),
        ),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 90f * bottomRightPercent,
        forceMoveTo = false
    )
    usedLength += bottomRightLength

    println("右下の角丸: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 下の辺
    val bottomLength = oneHorizontalEdgeLength
    val bottomPercent =
        (min(usableTotalLength - usedLength, bottomLength) / bottomLength).coerceIn(0f, 1f)
    lineTo((size.width - outerRadius) - bottomLength * bottomPercent, size.height - innerGap)
    usedLength += bottomLength

    println("下の辺: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 左下の角丸
    val bottomLeftLength = cornerLength
    val bottomLeftPercent =
        (min(usableTotalLength - usedLength, bottomLeftLength) / bottomLeftLength).coerceIn(0f, 1f)
    arcTo(
        Rect(
            Offset(innerGap, size.height - innerGap - innerRadius * 2),
            Size(innerRadius * 2, innerRadius * 2),
        ),
        startAngleDegrees = 90f,
        sweepAngleDegrees = 90f * bottomLeftPercent,
        forceMoveTo = false
    )
    usedLength += bottomLeftLength

    println("左下の角丸: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 左辺
    val leftLength = oneVerticalEdgeLength
    val leftPercent =
        (min(usableTotalLength - usedLength, leftLength) / leftLength).coerceIn(0f, 1f)
    lineTo(innerGap, (size.height - outerRadius) - leftLength * leftPercent)
    usedLength += leftLength

    println("左辺: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    // 左上の角丸
    val leftTopLength = cornerLength
    val leftTopPercent =
        (min(usableTotalLength - usedLength, leftTopLength) / leftTopLength).coerceIn(0f, 1f)
    arcTo(
        Rect(
            Offset(innerGap, innerGap),
            Size(innerRadius * 2, innerRadius * 2),
        ),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 90f * leftTopPercent,
        forceMoveTo = false
    )
    usedLength += leftTopLength

    println("左上の角丸: usedLength:$usedLength totalLength:$totalLength")
    if (usableTotalLength <= usedLength) return@apply

    close()
}

@Composable
private fun Item(
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Row(modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)) {
            Text("$index")
        }
        HorizontalDivider()
    }
}
