package com.tenco.feature.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.navigation.Routes
import com.tenco.ui.components.SectionTitle
import com.tenco.ui.components.StatCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusFailed

@Composable
fun SupplierDashboardScreen(
    onNavigate: (String) -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SupplierViewModel = hiltViewModel(),
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()

    TencoScaffold(
        title = stringResource(R.string.supplier_dashboard),
        actions = {
            IconButton(onClick = onChangeLanguage) {
                Icon(Icons.Filled.Translate, contentDescription = stringResource(R.string.change_language))
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = stringResource(R.string.stock_on_hand),
                        value = "${dashboard.stockOnHand}",
                        icon = Icons.Filled.Inventory,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.total_earnings),
                        value = Money.formatShort(dashboard.totalEarningsPaise),
                        icon = Icons.Filled.CurrencyRupee,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = stringResource(R.string.dues_receivable),
                        value = Money.formatShort(dashboard.duesReceivablePaise),
                        icon = Icons.Filled.LocalShipping,
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.losses),
                        value = Money.formatShort(dashboard.lossesPaise),
                        icon = Icons.Filled.TrendingDown,
                        accent = StatusFailed,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.quick_actions)) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuRow(
                        listOf(
                            Triple(R.string.menu_dealers, Icons.Filled.Store, Routes.SUPPLIER_DEALERS),
                            Triple(R.string.menu_vendors, Icons.Filled.Groups, Routes.SUPPLIER_VENDORS),
                        ),
                        onNavigate,
                    )
                    MenuRow(
                        listOf(
                            Triple(R.string.menu_pricing, Icons.Filled.PriceChange, Routes.SUPPLIER_PRICING),
                            Triple(R.string.menu_transactions, Icons.Filled.LocalShipping, Routes.SUPPLIER_TRANSACTIONS),
                        ),
                        onNavigate,
                    )
                    MenuRow(
                        listOf(
                            Triple(R.string.menu_reports, Icons.Filled.Assessment, Routes.SUPPLIER_REPORTS),
                            Triple(R.string.menu_complaints, Icons.Filled.ReportProblem, Routes.SUPPLIER_COMPLAINTS),
                        ),
                        onNavigate,
                    )
                }
            }

            item { SectionTitle(stringResource(R.string.vendor_distribution)) }
            items(dashboard.vendorDistribution) { v ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(v.vendorName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text("${v.quantity} ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            Money.formatShort(v.duesPaise),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(
    items: List<Triple<Int, androidx.compose.ui.graphics.vector.ImageVector, String>>,
    onNavigate: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (labelRes, icon, route) ->
            Card(
                onClick = { onNavigate(route) },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        stringResource(labelRes),
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
