package com.tenco.feature.supplier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun DealersScreen(onBack: () -> Unit, onOpenDealer: (String) -> Unit = {}, viewModel: SupplierViewModel = hiltViewModel()) {
    val dealers by viewModel.dealers.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var showDealer by remember { mutableStateOf(false) }

    TencoScaffold(
        title = stringResource(R.string.buy_stock),
        onBack = onBack,
        actions = {
            androidx.compose.material3.TextButton(onClick = { showDealer = true }) {
                Text(stringResource(R.string.add_dealer), color = MaterialTheme.colorScheme.primary)
            }
        },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_purchase)) },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (purchases.isEmpty()) {
                EmptyState(Icons.Rounded.Storefront, stringResource(R.string.empty_purchases), stringResource(R.string.empty_purchases_sub))
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
                            onClick = { onOpenDealer(p.dealerId) },
                        )
                    }
                }
            }
        }
    }

    if (showDealer) {
        var name by remember { mutableStateOf("") }
        var market by remember { mutableStateOf("") }
        val duplicate = name.isNotBlank() && dealers.any { it.name.trim().equals(name.trim(), ignoreCase = true) }
        com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.add_dealer), onDismiss = { showDealer = false }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    name, { name = it },
                    label = { Text(stringResource(R.string.dealers)) },
                    isError = duplicate,
                    supportingText = if (duplicate) { { Text(stringResource(R.string.dealer_exists), color = MaterialTheme.colorScheme.error) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(market, { market = it }, label = { Text(stringResource(R.string.market)) }, modifier = Modifier.fillMaxWidth())
                com.tenco.ui.components.SheetActions(
                    onCancel = { showDealer = false },
                    onSave = { viewModel.addDealer(name, market); showDealer = false },
                    saveEnabled = name.isNotBlank() && !duplicate,
                    saveText = stringResource(R.string.save),
                )
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

    com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.add_purchase), onDismiss = onDismiss) {
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
            com.tenco.ui.components.SheetActions(
                onCancel = onDismiss,
                onSave = {
                    val q = qty.toIntOrNull() ?: 0
                    val c = cost.toDoubleOrNull() ?: 0.0
                    val id = selected?.first
                    if (id != null && q > 0) onConfirm(id, q, Money.rupeesToPaise(c))
                },
                saveEnabled = (qty.toIntOrNull() ?: 0) > 0 && selected != null,
                saveText = stringResource(R.string.save),
            )
        }
    }
}

// ---------------- Vendors ----------------
@Composable
fun VendorsScreen(onBack: () -> Unit, onOpenVendor: (String) -> Unit = {}, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    TencoScaffold(
        title = stringResource(R.string.vendors),
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (vendors.isEmpty()) {
                EmptyState(Icons.Rounded.Groups, stringResource(R.string.empty_vendors), stringResource(R.string.empty_vendors_sub))
            } else {
                LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vendors) { v ->
                        InfoCard(title = v.name, subtitle = v.phone, trailing = v.upiVpa ?: "", onClick = { onOpenVendor(v.id) })
                    }
                }
            }
        }
    }
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var vpa by remember { mutableStateOf("") }
        val phoneKey = phone.filter(Char::isDigit).takeLast(10)
        val duplicate = phoneKey.length >= 10 && vendors.any { it.phone.filter(Char::isDigit).takeLast(10) == phoneKey }
        com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.add), onDismiss = { showDialog = false }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.vendor_name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    phone, { phone = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.phone)) },
                    isError = duplicate,
                    supportingText = if (duplicate) { { Text(stringResource(R.string.vendor_exists), color = MaterialTheme.colorScheme.error) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(vpa, { vpa = it }, label = { Text("UPI VPA") }, modifier = Modifier.fillMaxWidth())
                com.tenco.ui.components.SheetActions(
                    onCancel = { showDialog = false },
                    onSave = { viewModel.addVendor(name, phone, vpa.ifBlank { null }); showDialog = false },
                    saveEnabled = name.isNotBlank() && !duplicate,
                    saveText = stringResource(R.string.save),
                )
            }
        }
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
        com.tenco.ui.components.TencoBottomSheet(title = "${stringResource(R.string.set_price)} · ${vendor.name}", onDismiss = { editing = null }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(price, { price = it }, stringResource(R.string.unit_price))
                com.tenco.ui.components.SheetActions(
                    onCancel = { editing = null },
                    onSave = { price.toDoubleOrNull()?.let { viewModel.setPrice(vendor.id, Money.rupeesToPaise(it)); editing = null } },
                    saveEnabled = price.toDoubleOrNull() != null,
                    saveText = stringResource(R.string.save),
                )
            }
        }
    }
}

// ---------------- Transactions ----------------
@Composable
fun TransactionsScreen(onBack: (() -> Unit)? = null, viewModel: SupplierViewModel = hiltViewModel()) {
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val dealers by viewModel.dealers.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }
    val dealerNames = dealers.associate { it.id to it.name }

    TencoScaffold(title = stringResource(R.string.transaction_history), onBack = onBack) { padding ->
        val purchaseLabel = stringResource(R.string.buy_stock)
        val rows = buildList {
            purchases.forEach { add(Row4(it.createdAt, dealerNames[it.dealerId] ?: "-", "$purchaseLabel · ${it.quantity} @ ${Money.formatShort(it.unitCostPaise)}", "PURCHASE")) }
            deliveries.forEach { add(Row4(it.createdAt, names[it.vendorId] ?: "-", "${it.quantity} ${'@'} ${Money.formatShort(it.unitPricePaise)}", it.status)) }
            payments.forEach { add(Row4(it.createdAt, names[it.vendorId] ?: "-", Money.format(it.amountPaise), it.status)) }
        }.sortedByDescending { it.time }

        if (rows.isEmpty()) {
            EmptyState(Icons.Rounded.ReceiptLong, stringResource(R.string.empty_transactions))
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val pnl by viewModel.pnl.collectAsStateWithLifecycle()
    val period by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    TencoScaffold(
        title = stringResource(R.string.pnl_report),
        onBack = onBack,
        actions = {
            IconButton(onClick = {
                val names = vendors.associate { it.id to it.name }
                val csv = com.tenco.core.export.CsvExporter.buildReport(pnl, deliveries, payments, names)
                com.tenco.core.export.CsvExporter.share(context, csv)
            }) {
                Icon(androidx.compose.material.icons.Icons.Rounded.Share, contentDescription = stringResource(R.string.export_csv))
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.FilterChip(
                    selected = period == ReportPeriod.ALL,
                    onClick = { viewModel.setPeriod(ReportPeriod.ALL) },
                    label = { Text(stringResource(R.string.all_time)) },
                )
                androidx.compose.material3.FilterChip(
                    selected = period == ReportPeriod.THIS_MONTH,
                    onClick = { viewModel.setPeriod(ReportPeriod.THIS_MONTH) },
                    label = { Text(stringResource(R.string.this_month)) },
                )
            }
            Spacer(Modifier.height(8.dp))
            com.tenco.ui.components.TencoCard(Modifier.fillMaxWidth()) {
                val slices = listOf(
                    com.tenco.ui.components.ChartSlice(stringResource(R.string.revenue), pnl.revenuePaise.toFloat(), com.tenco.ui.theme.TileGreen),
                    com.tenco.ui.components.ChartSlice(stringResource(R.string.purchase_cost), pnl.purchaseCostPaise.toFloat(), com.tenco.ui.theme.TileOrange),
                    com.tenco.ui.components.ChartSlice(stringResource(R.string.complaint_losses), pnl.complaintLossesPaise.toFloat().coerceAtLeast(1f), StatusFailed),
                )
                Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    com.tenco.ui.components.DonutChart(slices, stringResource(R.string.net_profit), Money.formatShort(pnl.netProfitPaise))
                    Spacer(Modifier.width(16.dp))
                    com.tenco.ui.components.ChartLegend(slices, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            com.tenco.ui.components.TencoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.revenue_trend), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    val now = System.currentTimeMillis()
                    val day = 86_400_000L
                    val buckets = FloatArray(7)
                    deliveries.forEach {
                        val idx = 6 - ((now - it.createdAt) / day).toInt()
                        if (idx in 0..6) buckets[idx] += (it.quantity * it.unitPricePaise) / 100f
                    }
                    com.tenco.ui.components.TrendChart(buckets.toList(), com.tenco.ui.theme.TileGreen, Modifier.fillMaxWidth().height(110.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
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
            EmptyState(Icons.Rounded.ReportProblem, stringResource(R.string.empty_complaints))
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
                            when (c.status) {
                                ComplaintStatus.RESOLVED ->
                                    Text("${stringResource(R.string.price_adjustments)}: ${Money.format(c.adjustmentPaise)}", style = MaterialTheme.typography.bodyMedium)
                                ComplaintStatus.REJECTED -> {}
                                else -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (c.status == ComplaintStatus.OPEN) {
                                        TextButton(onClick = { viewModel.setComplaintStatus(c.id, ComplaintStatus.UNDER_REVIEW) }) {
                                            Text(stringResource(R.string.review))
                                        }
                                    }
                                    TextButton(onClick = { resolvingId = c.id }) { Text(stringResource(R.string.resolve)) }
                                    TextButton(onClick = { viewModel.setComplaintStatus(c.id, ComplaintStatus.REJECTED) }) {
                                        Text(stringResource(R.string.reject))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    resolvingId?.let { id ->
        var amount by remember { mutableStateOf("") }
        com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.adjust_price), onDismiss = { resolvingId = null }) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(amount, { amount = it }, stringResource(R.string.price_adjustments))
                com.tenco.ui.components.SheetActions(
                    onCancel = { resolvingId = null },
                    onSave = {
                        val a = amount.toDoubleOrNull() ?: 0.0
                        viewModel.resolveComplaint(id, Money.rupeesToPaise(a)); resolvingId = null
                    },
                    saveText = stringResource(R.string.resolve),
                )
            }
        }
    }
}

// ---------------- Shared small pieces ----------------
@Composable
private fun InfoCard(title: String, subtitle: String, trailing: String, onClick: (() -> Unit)? = null) {
    val mod = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable { onClick() } else it }
    Card(mod, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
        FloatingActionButton(onClick = onClick) { Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add)) }
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
    ComplaintStatus.UNDER_REVIEW -> stringResource(R.string.status_under_review)
    ComplaintStatus.REJECTED -> stringResource(R.string.status_rejected)
    ComplaintStatus.RESOLVED -> stringResource(R.string.status_resolved)
    "PURCHASE" -> stringResource(R.string.buy_stock)
    else -> stringResource(R.string.status_pending)
}
