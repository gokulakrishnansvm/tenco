package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.data.local.VendorEntity
import com.tenco.domain.ComplaintStatus
import com.tenco.domain.PaymentStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.StatusChip
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusFailed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
private fun ts(millis: Long) = dateFmt.format(Date(millis))

// ---------------- Dealers ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealersScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val dealers by viewModel.dealers.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    TencoScaffold(
        title = stringResource(R.string.dealers),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (purchases.isEmpty()) {
                EmptyState(stringResource(R.string.no_data))
            } else {
                val dealerNames = dealers.associate { it.id to it.name }
                LazyColumn(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(purchases) { p ->
                        InfoCard(
                            title = dealerNames[p.dealerId] ?: "-",
                            subtitle = "${p.quantity} ${stringResource(R.string.coconuts)} · ${ts(p.createdAt)}",
                            trailing = Money.format(p.quantity * p.unitCostPaise),
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddPurchaseDialog(
            dealers = dealers.map { it.id to it.name },
            onDismiss = { showDialog = false },
            onConfirm = { dealerId, qty, costPaise ->
                viewModel.addPurchase(dealerId, qty, costPaise)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPurchaseDialog(
    dealers: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(dealers.firstOrNull()) }
    var qty by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_purchase)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selected?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dealers)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        dealers.forEach { d ->
                            DropdownMenuItem(text = { Text(d.second) }, onClick = { selected = d; expanded = false })
                        }
                    }
                }
                NumberField(qty, { qty = it }, stringResource(R.string.quantity))
                NumberField(cost, { cost = it }, stringResource(R.string.unit_cost))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val q = qty.toIntOrNull() ?: 0
                    val c = cost.toDoubleOrNull() ?: 0.0
                    val id = selected?.first
                    if (id != null && q > 0) onConfirm(id, q, Money.rupeesToPaise(c))
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

// ---------------- Vendors ----------------
@Composable
fun VendorsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    TencoScaffold(
        title = stringResource(R.string.vendors),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (vendors.isEmpty()) {
                EmptyState(stringResource(R.string.no_data))
            } else {
                LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vendors) { v ->
                        InfoCard(title = v.name, subtitle = v.phone, trailing = v.upiVpa ?: "")
                    }
                }
            }
        }
    }
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var vpa by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.add)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.vendor_name)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(vpa, { vpa = it }, label = { Text("UPI VPA") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) { viewModel.addVendor(name, phone, vpa.ifBlank { null }); showDialog = false }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

// ---------------- Pricing ----------------
@Composable
fun PricingScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<VendorEntity?>(null) }

    TencoScaffold(title = stringResource(R.string.pricing), onBack = onBack) { padding ->
        Column(Modifier.padding(padding)) {
            val latest = prices.groupBy { it.vendorId }.mapValues { e -> e.value.maxByOrNull { it.effectiveFrom }?.unitPricePaise ?: 0L }
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(vendors) { v ->
                    Card(
                        onClick = { editing = v },
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(v.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(Money.format(latest[v.id] ?: 0L), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    editing?.let { vendor ->
        var price by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("${stringResource(R.string.set_price)} · ${vendor.name}") },
            text = { NumberField(price, { price = it }, stringResource(R.string.unit_price)) },
            confirmButton = {
                TextButton(onClick = {
                    val p = price.toDoubleOrNull()
                    if (p != null) { viewModel.setPrice(vendor.id, Money.rupeesToPaise(p)); editing = null }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

// ---------------- Transactions ----------------
@Composable
fun TransactionsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }

    TencoScaffold(title = stringResource(R.string.transaction_history), onBack = onBack) { padding ->
        val rows = buildList {
            deliveries.forEach { add(Row4(it.createdAt, names[it.vendorId] ?: "-", "${it.quantity} ${'@'} ${Money.formatShort(it.unitPricePaise)}", it.status)) }
            payments.forEach { add(Row4(it.createdAt, names[it.vendorId] ?: "-", Money.format(it.amountPaise), it.status)) }
        }.sortedByDescending { it.time }

        if (rows.isEmpty()) {
            EmptyState(stringResource(R.string.no_data))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rows) { r ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(r.vendor, fontWeight = FontWeight.SemiBold)
                                Text(r.detail, style = MaterialTheme.typography.bodyMedium)
                                Text(ts(r.time), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            StatusChip(r.status, statusLabel(r.status))
                        }
                    }
                }
            }
        }
    }
}

private data class Row4(val time: Long, val vendor: String, val detail: String, val status: String)

// ---------------- Reports (P&L) ----------------
@Composable
fun ReportsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val pnl by viewModel.pnl.collectAsStateWithLifecycle()
    TencoScaffold(title = stringResource(R.string.pnl_report), onBack = onBack) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PnlRow(stringResource(R.string.revenue), Money.format(pnl.revenuePaise))
            PnlRow(stringResource(R.string.purchase_cost), "- ${Money.format(pnl.purchaseCostPaise)}")
            PnlRow(stringResource(R.string.complaint_losses), "- ${Money.format(pnl.complaintLossesPaise)}")
            androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 8.dp))
            PnlRow(stringResource(R.string.net_profit), Money.format(pnl.netProfitPaise), emphasize = true, negative = pnl.netProfitPaise < 0)
        }
    }
}

@Composable
private fun PnlRow(label: String, value: String, emphasize: Boolean = false, negative: Boolean = false) {
    val emphasisColor = if (negative) StatusFailed else MaterialTheme.colorScheme.primary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) emphasisColor else MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------- Complaints ----------------
@Composable
fun ComplaintsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }
    var resolvingId by remember { mutableStateOf<String?>(null) }

    TencoScaffold(title = stringResource(R.string.menu_complaints), onBack = onBack) { padding ->
        if (complaints.isEmpty()) {
            EmptyState(stringResource(R.string.no_data))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(complaints) { c ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(names[c.vendorId] ?: "-", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                StatusChip(c.status, statusLabel(c.status))
                            }
                            Text(c.reason, style = MaterialTheme.typography.bodyMedium)
                            if (c.status == ComplaintStatus.OPEN) {
                                TextButton(onClick = { resolvingId = c.id }) { Text(stringResource(R.string.resolve)) }
                            } else {
                                Text("${stringResource(R.string.price_adjustments)}: ${Money.format(c.adjustmentPaise)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
    resolvingId?.let { id ->
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { resolvingId = null },
            title = { Text(stringResource(R.string.adjust_price)) },
            text = { NumberField(amount, { amount = it }, stringResource(R.string.price_adjustments)) },
            confirmButton = {
                TextButton(onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    viewModel.resolveComplaint(id, Money.rupeesToPaise(a)); resolvingId = null
                }) { Text(stringResource(R.string.resolve)) }
            },
            dismissButton = { TextButton(onClick = { resolvingId = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

// ---------------- Shared small pieces ----------------
@Composable
private fun InfoCard(title: String, subtitle: String, trailing: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Text(trailing, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Fab(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.End) {
        FloatingActionButton(onClick = onClick) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add)) }
    }
}

@Composable
private fun statusLabel(status: String): String = when (status) {
    PaymentStatus.COMPLETED -> stringResource(R.string.status_completed)
    PaymentStatus.FAILED -> stringResource(R.string.status_failed)
    PaymentStatus.PENDING_VERIFICATION -> stringResource(R.string.status_pending_verification)
    com.tenco.domain.DeliveryStatus.CONFIRMED -> stringResource(R.string.status_confirmed)
    com.tenco.domain.DeliveryStatus.DELIVERED -> stringResource(R.string.status_delivered)
    ComplaintStatus.OPEN -> stringResource(R.string.status_open)
    ComplaintStatus.RESOLVED -> stringResource(R.string.status_resolved)
    else -> stringResource(R.string.status_pending)
}
