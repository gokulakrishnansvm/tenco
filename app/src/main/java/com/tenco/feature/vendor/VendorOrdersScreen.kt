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
import androidx.compose.material3.Button
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
    var qty by remember { mutableStateOf("") }
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
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it.filter(Char::isDigit) },
                            label = { Text(stringResource(R.string.order_qty_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(
                            onClick = { qty.toIntOrNull()?.let { viewModel.placeOrder(it); qty = "" } },
                            enabled = (qty.toIntOrNull() ?: 0) > 0,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Text(stringResource(R.string.place_order)) }
                    }
                }
            }

            items(orders) { o ->
                TencoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${o.quantity} $coconuts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    o.unitPricePaise?.let { "${stringResource(R.string.order_total)}: ${Money.format(it * o.quantity)}" }
                                        ?: stringResource(R.string.awaiting_price),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OrderStatusChip(o.status)
                        }
                        OrderTimeline(o.status)
                        if (o.unitPricePaise != null && !o.paid && o.status != OrderStatus.DELIVERED) {
                            Button(onClick = { viewModel.payOrder(o.id) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                                Text("${stringResource(R.string.pay_now)} · ${Money.format(o.unitPricePaise * o.quantity)}")
                            }
                        } else if (o.paid) {
                            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.status_completed))
                            }
                        }
                    }
                }
            }
        }
    }
}
