package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.domain.ComplaintStatus
import com.tenco.domain.PaymentStatus
import com.tenco.ui.components.HeroEarningsCard
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.StatusChip
import com.tenco.ui.components.TencoCard
import androidx.compose.material.icons.rounded.Edit
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusFailed
import com.tenco.ui.theme.TileBlue
import com.tenco.ui.theme.TileGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

private data class LedgerEntry(val time: Long, val title: String, val amount: String, val amountColor: androidx.compose.ui.graphics.Color, val status: String, val statusLabel: String)

@Composable
fun VendorDetailScreen(vendorId: String, onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()

    val vendor = vendors.firstOrNull { it.id == vendorId }
    val vDeliveries = deliveries.filter { it.vendorId == vendorId }
    val vPayments = payments.filter { it.vendorId == vendorId }
    val vComplaints = complaints.filter { it.vendorId == vendorId }
    val billed = vDeliveries.sumOf { it.quantity * it.unitPricePaise }
    val paid = vPayments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
    val adjust = vComplaints.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
    val dues = (billed - adjust - paid).coerceAtLeast(0)

    val soldLabel = stringResource(R.string.sell_to_vendor)
    val paymentLabel = stringResource(R.string.received)
    val cashLabel = stringResource(R.string.cash)
    val upiLabel = stringResource(R.string.upi)
    val entries = buildList {
        vDeliveries.forEach { add(LedgerEntry(it.createdAt, "$soldLabel · ${it.quantity} @ ${Money.formatShort(it.unitPricePaise)}", "+" + Money.formatShort(it.quantity * it.unitPricePaise), TileBlue, it.status, statusLabelOf(it.status))) }
        vPayments.forEach {
            val method = if (it.method == com.tenco.domain.PaymentMethod.CASH) cashLabel else upiLabel
            add(LedgerEntry(it.createdAt, "$paymentLabel · $method", "-" + Money.formatShort(it.amountPaise), StatusCompleted, it.status, statusLabelOf(it.status)))
        }
    }.sortedByDescending { it.time }

    var showCash by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showEdit by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    TencoScaffold(
        title = vendor?.name ?: stringResource(R.string.vendors),
        onBack = onBack,
        actions = {
            androidx.compose.material3.IconButton(onClick = { showEdit = true }) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit), tint = MaterialTheme.colorScheme.onSurface)
            }
            androidx.compose.material3.IconButton(onClick = { showDelete = true }) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurface)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroEarningsCard(
                    label = stringResource(R.string.pending_dues),
                    paise = dues,
                    caption = "${vendor?.phone ?: ""}  ·  ${vendor?.upiVpa ?: "-"}",
                )
            }
            item {
                androidx.compose.material3.Button(
                    onClick = { showCash = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.record_cash_payment))
                }
            }
            item { SectionHeader(stringResource(R.string.transaction_history)) }
            items(entries) { e ->
                TencoCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(e.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(dFmt.format(Date(e.time)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(e.amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = e.amountColor)
                    }
                }
            }
        }
    }

    if (showEdit && vendor != null) {
        var eName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(vendor.name) }
        var ePhone by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(vendor.phone) }
        var eCity by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(vendor.city) }
        var eUpi by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(vendor.upiVpa ?: "") }
        com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.edit), onDismiss = { showEdit = false }) {
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(eName, { eName = it }, label = { Text(stringResource(R.string.vendor_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(
                    ePhone, { ePhone = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.phone)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.material3.OutlinedTextField(eCity, { eCity = it }, label = { Text(stringResource(R.string.city)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.OutlinedTextField(eUpi, { eUpi = it }, label = { Text(stringResource(R.string.upi_id)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                com.tenco.ui.components.SheetActions(
                    onCancel = { showEdit = false },
                    onSave = { viewModel.updateVendor(vendorId, eName.trim(), ePhone.trim(), eUpi.trim().ifBlank { null }, eCity.trim()); showEdit = false },
                    saveEnabled = eName.isNotBlank(),
                    saveText = stringResource(R.string.save),
                )
            }
        }
    }
    if (showCash) {
        var amount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(String.format("%.2f", dues / 100.0)) }
        com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.record_cash_payment), onDismiss = { showCash = false }) {
            androidx.compose.material3.OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount_hint)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            com.tenco.ui.components.SheetActions(
                onCancel = { showCash = false },
                onSave = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) { viewModel.recordCashPayment(vendorId, Money.rupeesToPaise(amt)); showCash = false }
                },
                saveEnabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                saveText = stringResource(R.string.save),
            )
        }
    }

    if (showDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("${stringResource(R.string.delete)} ${vendor?.name ?: ""}") },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteVendor(vendorId); showDelete = false; onBack()
                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun statusLabelOf(status: String): String = status.lowercase().replaceFirstChar { it.uppercase() }
