package com.tenco.feature.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tenco.domain.OrderStatus
import com.tenco.ui.components.OrderStatusChip
import com.tenco.ui.components.OrderTimeline
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold

@Composable
fun VendorOrdersScreen(vendorId: String, onBack: (() -> Unit)? = null, viewModel: VendorViewModel = hiltViewModel()) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var qty by remember { mutableStateOf("") }
    var orderColor by remember { mutableStateOf(com.tenco.domain.CoconutColor.GREEN) }
    var orderGrade by remember { mutableStateOf(com.tenco.domain.CoconutGrade.MEDIUM) }
    var showOrderAnim by remember { mutableStateOf(false) }
    val orderLines = remember { androidx.compose.runtime.mutableStateListOf<com.tenco.data.repository.TencoRepository.OrderLine>() }
    val coconuts = stringResource(R.string.coconuts)

    TencoScaffold(title = stringResource(R.string.my_orders), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TencoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.place_order), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.tenco.domain.CoconutColor.ALL.forEach { c ->
                                androidx.compose.material3.FilterChip(selected = orderColor == c, onClick = { orderColor = c }, label = { Text(com.tenco.ui.components.coconutColorLabel(c)) })
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            com.tenco.domain.CoconutGrade.ALL.forEach { g ->
                                androidx.compose.material3.FilterChip(selected = orderGrade == g, onClick = { orderGrade = g }, label = { Text(com.tenco.ui.components.coconutGradeLabel(g)) })
                            }
                        }
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it.filter(Char::isDigit) },
                            label = { Text(stringResource(R.string.order_qty_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                val q = qty.toIntOrNull() ?: 0
                                if (q > 0) { orderLines.add(com.tenco.data.repository.TencoRepository.OrderLine(orderColor, orderGrade, q)); qty = "" }
                            },
                            enabled = (qty.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("+ ${stringResource(R.string.add_line)}") }

                        orderLines.forEachIndexed { i, l ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${com.tenco.ui.components.coconutColorLabel(l.color)} ${com.tenco.ui.components.coconutGradeLabel(l.grade)} · ${l.quantity} $coconuts", style = MaterialTheme.typography.bodyMedium)
                                androidx.compose.material3.IconButton(onClick = { orderLines.removeAt(i) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.placeOrders(orderLines.toList()); orderLines.clear(); showOrderAnim = true },
                            enabled = orderLines.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Text("${stringResource(R.string.place_order)} (${orderLines.size})") }
                    }
                }
            }
        }
    }
    if (showOrderAnim) {
        com.tenco.ui.components.CoconutEventOverlay(com.tenco.ui.components.CoconutEvent.HARVEST) { showOrderAnim = false }
    }
}


@Composable
fun VendorMyOrdersScreen(vendorId: String, onBack: (() -> Unit)? = null, viewModel: VendorViewModel = hiltViewModel()) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val coconuts = stringResource(R.string.coconuts)

    TencoScaffold(title = stringResource(R.string.my_orders), onBack = onBack) { padding ->
        val groups = orders.groupBy { it.groupId.ifBlank { it.id } }
            .toList().sortedByDescending { it.second.maxOf { o -> o.updatedAt } }
        if (groups.isEmpty()) {
            com.tenco.ui.components.EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_orders))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(groups, key = { it.first }) { (_, lines) ->
                    val status = lines.first().status
                    val priced = lines.all { it.unitPricePaise != null }
                    val total = lines.sumOf { (it.unitPricePaise ?: 0L) * it.quantity }
                    val paid = lines.all { it.paid }
                    TencoCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${lines.size} ${stringResource(R.string.orders)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                OrderStatusChip(status)
                            }
                            lines.forEach { o ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${com.tenco.ui.components.coconutColorLabel(o.color)} ${com.tenco.ui.components.coconutGradeLabel(o.grade)} · ${o.quantity} $coconuts", style = MaterialTheme.typography.bodyMedium)
                                    o.unitPricePaise?.let { Text(Money.format(it * o.quantity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) }
                                }
                            }
                            OrderTimeline(status)
                            if (priced) {
                                Text("${stringResource(R.string.order_total)}: ${Money.format(total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text(stringResource(R.string.awaiting_price), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (priced && !paid && status != OrderStatus.CANCELLED) {
                                Button(
                                    onClick = {
                                        val launched = com.tenco.core.UpiPayment.launch(context, dashboard?.supplierVpa ?: com.tenco.core.Demo.SUPPLIER_VPA, com.tenco.core.Demo.SUPPLIER_NAME, total / 100.0, "TENCO order")
                                        if (launched) lines.forEach { if (!it.paid) viewModel.payOrder(it.id) }
                                        else android.widget.Toast.makeText(context, R.string.no_upi_app, android.widget.Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                ) { Text("${stringResource(R.string.pay_now)} · ${Money.format(total)}") }
                            } else if (paid) {
                                OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.status_completed)) }
                            }
                            if (OrderStatus.cancellable(status) && !paid) {
                                OutlinedButton(
                                    onClick = { lines.forEach { viewModel.cancelOrder(it.id) } },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                ) { Text(stringResource(R.string.cancel_order)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
