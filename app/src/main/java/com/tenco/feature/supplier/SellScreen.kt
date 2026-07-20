package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(onBack: () -> Unit, viewModel: SupplierViewModel = hiltViewModel()) {
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val deliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showHarvest by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(com.tenco.domain.CoconutColor.GREEN) }
    var grade by remember { mutableStateOf(com.tenco.domain.CoconutGrade.MEDIUM) }

    val latestPrice = prices.groupBy { it.vendorId }
        .mapValues { e -> e.value.maxByOrNull { it.effectiveFrom }?.unitPricePaise ?: 0L }

    var expanded by remember { mutableStateOf(false) }
    var selected by remember(vendors) { mutableStateOf(vendors.firstOrNull()) }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    // Prefill price from the selected vendor's latest price.
    val selectedId = selected?.id
    LaunchedEffectPrice(selectedId, latestPrice) { p -> if (p > 0) price = String.format("%.2f", p / 100.0) }

    val qtyInt = qty.toIntOrNull() ?: 0
    val priceRupees = price.toDoubleOrNull() ?: 0.0
    val totalPaise = (qtyInt * Money.rupeesToPaise(priceRupees))
    val stock = purchases.filter { it.color == color && it.grade == grade }.sumOf { it.quantity } -
        deliveries.filter { it.color == color && it.grade == grade }.sumOf { it.quantity }
    val overStock = qtyInt > stock

    TencoScaffold(title = stringResource(R.string.sell_to_vendor), onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TencoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${com.tenco.ui.components.coconutColorLabel(color)} ${com.tenco.ui.components.coconutGradeLabel(grade)} · ${stringResource(R.string.stock_on_hand)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$stock ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (stock <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.tenco.domain.CoconutColor.ALL.forEach { c ->
                    androidx.compose.material3.FilterChip(selected = color == c, onClick = { color = c }, label = { Text(com.tenco.ui.components.coconutColorLabel(c)) })
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.tenco.domain.CoconutGrade.ALL.forEach { g ->
                    androidx.compose.material3.FilterChip(selected = grade == g, onClick = { grade = g }, label = { Text(com.tenco.ui.components.coconutGradeLabel(g)) })
                }
            }

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
            OutlinedTextField(qty, { qty = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.quantity)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(price, { price = it }, label = { Text(stringResource(R.string.unit_price)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Money.format(totalPaise), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            if (overStock) {
                Text(stringResource(R.string.not_enough_stock), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val id = selected?.id
                    if (id != null && qtyInt > 0 && priceRupees > 0 && !overStock) {
                        viewModel.sellToVendor(id, qtyInt, Money.rupeesToPaise(priceRupees), color, grade)
                        Toast.makeText(context, R.string.sale_recorded, Toast.LENGTH_SHORT).show()
                        showHarvest = true
                    }
                },
                enabled = !overStock && stock > 0,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.sell_to_vendor), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    if (showHarvest) {
        com.tenco.ui.components.CoconutEventOverlay(com.tenco.ui.components.CoconutEvent.HARVEST) { onBack() }
    }
}

/** Small effect helper to prefill the price when the selected vendor changes. */
@Composable
private fun LaunchedEffectPrice(vendorId: String?, prices: Map<String, Long>, onPrice: (Long) -> Unit) {
    androidx.compose.runtime.LaunchedEffect(vendorId) {
        if (vendorId != null) onPrice(prices[vendorId] ?: 0L)
    }
}
