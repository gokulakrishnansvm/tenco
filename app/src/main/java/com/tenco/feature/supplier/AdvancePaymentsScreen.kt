package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.domain.AdvanceType
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancePaymentsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val advances by viewModel.advances.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val names = remember(vendors) { vendors.associate { it.id to it.name } }
    val df = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    var expanded by remember { mutableStateOf(false) }
    var selected by remember(vendors) { mutableStateOf(vendors.firstOrNull()) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    fun submit(type: String) {
        val paise = Money.rupeesToPaise(amount.toDoubleOrNull() ?: 0.0)
        val vid = selected?.id
        if (paise > 0 && vid != null) {
            viewModel.addAdvance(vid, paise, type, note.trim())
            amount = ""; note = ""
        }
    }

    val byVendor = advances.groupBy { it.vendorId }

    TencoScaffold(title = stringResource(R.string.advance_payments), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Entry form
            item {
                TencoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = selected?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.vendors)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                vendors.forEach { v ->
                                    DropdownMenuItem(text = { Text(v.name) }, onClick = { selected = v; expanded = false })
                                }
                            }
                        }
                        OutlinedTextField(amount, { amount = it }, label = { Text(stringResource(R.string.amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.note_optional)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilledTonalButton(onClick = { submit(AdvanceType.RECEIVED) }, modifier = Modifier.weight(1f)) {
                                Text("+ ${stringResource(R.string.add_advance)}")
                            }
                            OutlinedButton(onClick = { submit(AdvanceType.RETURNED) }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.return_advance))
                            }
                        }
                    }
                }
            }

            if (byVendor.isEmpty()) {
                item { EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_advances)) }
            }

            items(byVendor.entries.toList(), key = { it.key }) { (vid, entries) ->
                val received = entries.filter { it.type == AdvanceType.RECEIVED }.sumOf { it.amountPaise }
                val returned = entries.filter { it.type == AdvanceType.RETURNED }.sumOf { it.amountPaise }
                val balance = received - returned
                TencoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(names[vid] ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(R.string.advance_balance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Money.format(balance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        entries.forEach { e ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(
                                        if (e.type == AdvanceType.RECEIVED) stringResource(R.string.received) else stringResource(R.string.returned),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (e.type == AdvanceType.RECEIVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    )
                                    if (e.note.isNotBlank()) Text(e.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(df.format(Date(e.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    (if (e.type == AdvanceType.RETURNED) "- " else "+ ") + Money.format(e.amountPaise),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
