package com.tenco.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tenco.R

/** Maps a stored complaint reason key to a localized label (falls back to raw legacy text). */
@Composable
fun complaintReasonLabel(key: String): String = when (key) {
    "spoiled" -> stringResource(R.string.reason_spoiled)
    "damaged" -> stringResource(R.string.reason_damaged)
    "short" -> stringResource(R.string.reason_short)
    "other" -> stringResource(R.string.reason_other)
    else -> key
}
