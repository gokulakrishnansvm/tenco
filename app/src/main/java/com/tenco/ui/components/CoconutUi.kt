package com.tenco.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tenco.R
import com.tenco.domain.CoconutColor
import com.tenco.domain.CoconutGrade

@Composable
fun coconutColorLabel(color: String): String = when (color) {
    CoconutColor.GREEN -> stringResource(R.string.color_green)
    CoconutColor.RED -> stringResource(R.string.color_red)
    else -> color
}

@Composable
fun coconutGradeLabel(grade: String): String = when (grade) {
    CoconutGrade.BIG -> stringResource(R.string.grade_big)
    CoconutGrade.MEDIUM -> stringResource(R.string.grade_medium)
    CoconutGrade.SMALL -> stringResource(R.string.grade_small)
    else -> grade
}

/** Dot colour representing the coconut variety. */
fun coconutColorSwatch(color: String): Color = when (color) {
    CoconutColor.RED -> Color(0xFFC1502E)
    else -> Color(0xFF43A047)
}

/** A coconut icon: green/red variety, sized by grade (big > medium > small). */
@Composable
fun CoconutGlyph(color: String, grade: String, modifier: Modifier = Modifier) {
    val res = if (color == CoconutColor.RED) R.drawable.ic_coco_red else R.drawable.ic_coco_green
    val size = when (grade) {
        CoconutGrade.BIG -> 30.dp
        CoconutGrade.MEDIUM -> 24.dp
        else -> 18.dp
    }
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(res),
        contentDescription = "$color $grade",
        modifier = modifier.size(size),
    )
}
