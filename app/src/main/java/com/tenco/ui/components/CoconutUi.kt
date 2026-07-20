package com.tenco.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
