package com.tenco.feature.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Money
import com.tenco.navigation.Routes
import com.tenco.ui.components.HeroEarningsCard
import com.tenco.ui.components.QuickActionTile
import com.tenco.ui.components.SectionHeader
import com.tenco.ui.components.SummaryChip
import com.tenco.ui.components.TencoCard
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusFailed
import com.tenco.ui.theme.StatusPending
import com.tenco.ui.theme.TileBlue
import com.tenco.ui.theme.TileGreen
import com.tenco.ui.theme.TileOrange
import com.tenco.ui.theme.TilePurple
import com.tenco.ui.theme.TileRed
import com.tenco.ui.theme.TileTeal
import java.util.Calendar

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SupplierDashboardScreen(
    onNavigate: (String) -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SupplierViewModel = hiltViewModel(),
) {
    var tab by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(0) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { SupplierBottomBar(tab) { tab = it } },
    ) { padding ->
        androidx.compose.animation.AnimatedContent(
            targetState = tab,
            transitionSpec = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(260)) togetherWith
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180))
            },
            label = "supplierTab",
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
        ) { t ->
            when (t) {
                0 -> SupplierHomeTab(onNavigate, viewModel)
                1 -> InventoryScreen()
                2 -> TransactionsScreen()
                3 -> InsightsScreen()
                else -> com.tenco.feature.profile.ProfileScreen(
                    onChangeLanguage = onChangeLanguage,
                    onNotifications = { onNavigate(Routes.NOTIFICATIONS) },
                    onLogout = onLogout,
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SupplierHomeTab(onNavigate: (String) -> Unit, viewModel: SupplierViewModel) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val vendors by viewModel.vendors.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val names = vendors.associate { it.id to it.name }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { com.tenco.ui.components.EntranceItem(0) { GreetingHeader(onNotifications = { onNavigate(Routes.NOTIFICATIONS) }, onProfile = { onNavigate(Routes.PROFILE) }) } }
            item {
                com.tenco.ui.components.EntranceItem(1) {
                    HeroEarningsCard(
                        label = stringResource(R.string.total_earnings),
                        paise = dashboard.totalEarningsPaise,
                        caption = "▲ ${stringResource(R.string.this_month)}",
                    )
                }
            }
            item {
                com.tenco.ui.components.EntranceItem(2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryChip(Icons.Rounded.Inventory2, stringResource(R.string.stock_on_hand), TileGreen, Modifier.weight(1f)) {
                            com.tenco.ui.components.AnimatedCount(dashboard.stockOnHand)
                        }
                        SummaryChip(Icons.Rounded.CurrencyRupee, stringResource(R.string.dues_receivable), TileOrange, Modifier.weight(1f)) {
                            com.tenco.ui.components.AnimatedMoneyShort(dashboard.duesReceivablePaise)
                        }
                        SummaryChip(Icons.Rounded.TrendingDown, stringResource(R.string.losses), TileRed, Modifier.weight(1f)) {
                            com.tenco.ui.components.AnimatedMoneyShort(dashboard.lossesPaise)
                        }
                    }
                }
            }

            item { com.tenco.ui.components.EntranceItem(3) { SectionHeader(stringResource(R.string.quick_actions)) } }
            item {
                com.tenco.ui.components.EntranceItem(4) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionTile(Icons.Rounded.Storefront, stringResource(R.string.buy_stock), TileGreen, { onNavigate(Routes.SUPPLIER_DEALERS) }, Modifier.weight(1f))
                        QuickActionTile(Icons.Rounded.Sell, stringResource(R.string.sell_to_vendor), TileBlue, { onNavigate(Routes.SUPPLIER_SELL) }, Modifier.weight(1f))
                        QuickActionTile(Icons.Rounded.Groups, stringResource(R.string.menu_vendors), TilePurple, { onNavigate(Routes.SUPPLIER_VENDORS) }, Modifier.weight(1f))
                    }
                }
            }
            item {
                com.tenco.ui.components.EntranceItem(5) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionTile(Icons.Rounded.PriceChange, stringResource(R.string.menu_pricing), TileOrange, { onNavigate(Routes.SUPPLIER_PRICING) }, Modifier.weight(1f))
                        QuickActionTile(Icons.Rounded.Assessment, stringResource(R.string.menu_reports), TileTeal, { onNavigate(Routes.SUPPLIER_REPORTS) }, Modifier.weight(1f))
                        QuickActionTile(Icons.Rounded.ReportProblem, stringResource(R.string.menu_complaints), TileRed, { onNavigate(Routes.SUPPLIER_COMPLAINTS) }, Modifier.weight(1f))
                    }
                }
            }

            item { com.tenco.ui.components.EntranceItem(6) { SectionHeader(stringResource(R.string.transaction_history)) } }
            if (payments.isEmpty()) {
                item { Text(stringResource(R.string.no_data), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(payments.take(6)) { p ->
                    TransactionRow(
                        name = names[p.vendorId] ?: "-",
                        amount = Money.format(p.amountPaise),
                        status = p.status,
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(onNotifications: () -> Unit, onProfile: () -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("$greeting 👋", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.role_supplier), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = onNotifications) {
                Icon(androidx.compose.material.icons.Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp).clickable { onProfile() },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(name: String, amount: String, status: String) {
    val statusColor = when (status) {
        "COMPLETED" -> StatusCompleted
        "FAILED" -> StatusFailed
        else -> StatusPending
    }
    TencoCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.14f)) {
                    Text(status.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SupplierBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    com.tenco.ui.components.TencoBottomNav(
        items = listOf(
            com.tenco.ui.components.NavItem(Icons.Rounded.Home, stringResource(R.string.nav_home)),
            com.tenco.ui.components.NavItem(Icons.Rounded.Inventory2, stringResource(R.string.nav_stock)),
            com.tenco.ui.components.NavItem(Icons.Rounded.ReceiptLong, stringResource(R.string.nav_txns)),
            com.tenco.ui.components.NavItem(Icons.Rounded.AccountBalanceWallet, stringResource(R.string.nav_money)),
            com.tenco.ui.components.NavItem(Icons.Rounded.Person, stringResource(R.string.menu_profile)),
        ),
        selected = selected,
        onSelect = onSelect,
    )
}
