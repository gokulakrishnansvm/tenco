package com.tenco.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Reusable premium slide-up bottom sheet with a title. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TencoBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            content()
        }
    }
}

/** Standard Cancel / Save row for bottom-sheet forms. */
@Composable
fun SheetActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    saveText: String = "Save",
) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        androidx.compose.material3.OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(52.dp)) {
            Text(androidx.compose.ui.res.stringResource(com.tenco.R.string.cancel))
        }
        androidx.compose.material3.Button(onClick = onSave, enabled = saveEnabled, modifier = Modifier.weight(1f).height(52.dp)) {
            Text(saveText)
        }
    }
}
