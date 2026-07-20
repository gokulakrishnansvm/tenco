package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.tenco.domain.OrderStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.OrderStatusChip
import com.tenco.ui.components.OrderTimeline
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold

@Composable
fun SupplierOrdersScreen(onBack: () -> Unit, onOpenOrder: (String) -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }
    val coconuts = stringResource(R.string.coconuts)

    TencoScaffold(title = stringResource(R.string.orders), onBack = onBack) { padding ->
        val visible = orders.filter { names.containsKey(it.vendorId) }
        if (visible.isEmpty()) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_orders))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val groups = visible.groupBy { it.groupId.ifBlank { it.id } }
                    .toList().sortedByDescending { it.second.maxOf { o -> o.updatedAt } }
                items(groups, key = { it.first }) { (_, lines) ->
                    val head = lines.first()
                    val priced = lines.all { it.unitPricePaise != null }
                    val total = lines.sumOf { (it.unitPricePaise ?: 0L) * it.quantity }
                    TencoCard(Modifier.fillMaxWidth(), onClick = { onOpenOrder(head.id) }) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(names[head.vendorId] ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                lines.forEach { o ->
                                    Text("${com.tenco.ui.components.coconutColorLabel(o.color)} ${com.tenco.ui.components.coconutGradeLabel(o.grade)} · ${o.quantity} $coconuts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OrderStatusChip(head.status)
                                if (priced) Text(Money.format(total), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierOrderDetailScreen(orderId: String, onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val order by viewModel.observeOrder(orderId).collectAsStateWithLifecycle(initialValue = null)
    val allOrders by viewModel.orders.collectAsStateWithLifecycle()
    val vendors by viewModel.allVendors.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }
    var priceText by remember { mutableStateOf("") }
    val coconuts = stringResource(R.string.coconuts)

    TencoScaffold(title = "${stringResource(R.string.order_label)} #${orderId.take(6).uppercase()}", onBack = onBack) { padding ->
        val o = order
        if (o == null) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.no_orders))
        } else {
            val groupKey = o.groupId.ifBlank { o.id }
            val lines = allOrders.filter { it.groupId.ifBlank { it.id } == groupKey }
            val priced = lines.all { it.unitPricePaise != null }
            val total = lines.sumOf { (it.unitPricePaise ?: 0L) * it.quantity }
            Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TencoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(names[o.vendorId] ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        lines.forEach { l ->
                            Text("${com.tenco.ui.components.coconutColorLabel(l.color)} ${com.tenco.ui.components.coconutGradeLabel(l.grade)} · ${l.quantity} $coconuts", style = MaterialTheme.typography.bodyLarge)
                        }
                        if (priced) {
                            Text("${stringResource(R.string.order_total)}: ${Money.format(total)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                TencoCard(Modifier.fillMaxWidth()) {
                    OrderTimeline(o.status, Modifier.padding(16.dp))
                }

                if (o.status == OrderStatus.CANCEL_REQUESTED) {
                    Button(
                        onClick = { lines.forEach { viewModel.confirmOrderCancel(it.id) } },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text(stringResource(R.string.confirm_cancellation)) }
                } else if (o.status == OrderStatus.CANCELLED) {
                    // Terminal state — no actions.
                } else if (!priced) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(stringResource(R.string.unit_price)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { priceText.toDoubleOrNull()?.let { p -> lines.forEach { viewModel.setOrderPrice(it.id, Money.rupeesToPaise(p)) } } },
                        enabled = priceText.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text(stringResource(R.string.set_price)) }
                } else if (o.status != OrderStatus.DELIVERED) {
                    val next = OrderStatus.next(o.status)
                    if (next != null) {
                        Button(
                            onClick = { lines.forEach { viewModel.advanceOrder(it.id, next) } },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Text(advanceLabel(next)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun advanceLabel(next: String): String = when (next) {
    OrderStatus.CONFIRMED -> stringResource(R.string.ord_confirmed)
    OrderStatus.IN_PROGRESS -> stringResource(R.string.start_preparing)
    OrderStatus.IN_TRANSIT -> stringResource(R.string.out_for_delivery)
    OrderStatus.DELIVERED -> stringResource(R.string.mark_delivered)
    else -> next
}
