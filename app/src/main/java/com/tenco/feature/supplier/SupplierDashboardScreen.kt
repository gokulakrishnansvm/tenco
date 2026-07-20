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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.ShoppingCart
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val newOrders by viewModel.newOrderCount.collectAsStateWithLifecycle()
    val openComplaints by viewModel.openComplaintCount.collectAsStateWithLifecycle()
    val pendingCash by viewModel.pendingCashPayments.collectAsStateWithLifecycle()
    val actionUsage by viewModel.actionUsage.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val stockDeliveries by viewModel.deliveries.collectAsStateWithLifecycle()
    var stockExpanded by remember { mutableStateOf(false) }
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HomeHeaderBand(
                    earningsPaise = dashboard.totalEarningsPaise,
                    onNotifications = { onNavigate(Routes.NOTIFICATIONS) },
                    onProfile = { onNavigate(Routes.PROFILE) },
                )
            }
            item {
                com.tenco.ui.components.EntranceItem(2) {
                    val stockGross = purchases.sumOf { it.quantity } - stockDeliveries.sumOf { it.quantity }
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryChip(Icons.Rounded.Inventory2, stringResource(R.string.stock_on_hand), TileGreen, Modifier.weight(1f), onClick = { stockExpanded = !stockExpanded }) {
                                com.tenco.ui.components.AnimatedCount(stockGross)
                            }
                            SummaryChip(Icons.Rounded.CurrencyRupee, stringResource(R.string.dues_receivable), TileOrange, Modifier.weight(1f)) {
                                com.tenco.ui.components.AnimatedMoneyShort(dashboard.duesReceivablePaise)
                            }
                            SummaryChip(Icons.Rounded.TrendingDown, stringResource(R.string.losses), TileRed, Modifier.weight(1f), onClick = { onNavigate(Routes.SUPPLIER_ADJUSTMENTS) }) {
                                com.tenco.ui.components.AnimatedMoneyShort(dashboard.lossesPaise)
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(visible = stockExpanded) {
                            StockSummaryPanel(purchases, stockDeliveries)
                        }
                    }
                }
            }

            item { com.tenco.ui.components.EntranceItem(3) { Box(Modifier.padding(horizontal = 20.dp)) { SectionHeader(stringResource(R.string.quick_actions)) } } }
            item {
                com.tenco.ui.components.EntranceItem(4) {
                    val ordersLabel = stringResource(R.string.orders) + if (newOrders > 0) " ($newOrders)" else ""
                    val approvalsLabel = stringResource(R.string.approvals) + if (pendingCash.isNotEmpty()) " (${pendingCash.size})" else ""
                    val complaintsLabel = stringResource(R.string.menu_complaints) + if (openComplaints > 0) " ($openComplaints)" else ""
                    val defaultKeys = listOf("orders", "buy_stock", "dealers", "sell", "vendors", "pricing", "reports", "complaints", "inventory", "losses", "approvals")
                    // Freeze the order once per screen entry (less aggressive: re-sorts only on revisit, not per tap).
                    val keyOrder = remember { defaultKeys.sortedByDescending { actionUsage[it] ?: 0 } }
                    val actions = listOf(
                        QuickAction("orders", Icons.Rounded.ShoppingCart, ordersLabel, TileBlue, Routes.SUPPLIER_ORDERS),
                        QuickAction("buy_stock", Icons.Rounded.Storefront, stringResource(R.string.buy_stock), TileGreen, Routes.SUPPLIER_DEALERS),
                        QuickAction("dealers", Icons.Rounded.LocalShipping, stringResource(R.string.dealers), TilePurple, Routes.SUPPLIER_DEALERS_LIST),
                        QuickAction("sell", Icons.Rounded.Sell, stringResource(R.string.sell_to_vendor), TileTeal, Routes.SUPPLIER_SELL),
                        QuickAction("vendors", Icons.Rounded.Groups, stringResource(R.string.menu_vendors), TilePurple, Routes.SUPPLIER_VENDORS),
                        QuickAction("pricing", Icons.Rounded.PriceChange, stringResource(R.string.menu_pricing), TileOrange, Routes.SUPPLIER_PRICING),
                        QuickAction("reports", Icons.Rounded.Assessment, stringResource(R.string.menu_reports), TileTeal, Routes.SUPPLIER_REPORTS),
                        QuickAction("complaints", Icons.Rounded.ReportProblem, complaintsLabel, TileRed, Routes.SUPPLIER_COMPLAINTS),
                        QuickAction("inventory", Icons.Rounded.Inventory2, stringResource(R.string.menu_inventory), TileGreen, Routes.SUPPLIER_INVENTORY),
                        QuickAction("losses", Icons.Rounded.TrendingDown, stringResource(R.string.adjustments), TileRed, Routes.SUPPLIER_ADJUSTMENTS),
                        QuickAction("approvals", Icons.Rounded.Payments, approvalsLabel, TileOrange, Routes.SUPPLIER_CASH_APPROVALS),
                    ).sortedBy { keyOrder.indexOf(it.key).let { i -> if (i < 0) Int.MAX_VALUE else i } }
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        actions.chunked(3).forEach { rowItems ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { qa ->
                                    QuickActionTile(qa.icon, qa.label, qa.color, { viewModel.recordActionUse(qa.key); onNavigate(qa.route) }, Modifier.weight(1f))
                                }
                                repeat(3 - rowItems.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

        }
    }
}

private data class QuickAction(
    val key: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
    val route: String,
)

@Composable
private fun StockSummaryPanel(
    purchases: List<com.tenco.data.local.PurchaseEntity>,
    deliveries: List<com.tenco.data.local.DeliveryEntity>,
) {
    fun net(color: String, grade: String) =
        purchases.filter { it.color == color && it.grade == grade }.sumOf { it.quantity } -
            deliveries.filter { it.color == color && it.grade == grade }.sumOf { it.quantity }
    com.tenco.ui.components.TencoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader(stringResource(R.string.stock_summary))
            com.tenco.domain.CoconutColor.ALL.forEach { color ->
                val colorTotal = com.tenco.domain.CoconutGrade.ALL.sumOf { net(color, it) }
                if (colorTotal > 0) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).background(com.tenco.ui.components.coconutColorSwatch(color), androidx.compose.foundation.shape.CircleShape))
                        Text("  ${com.tenco.ui.components.coconutColorLabel(color)}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("$colorTotal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    com.tenco.domain.CoconutGrade.ALL.forEach { grade ->
                        val q = net(color, grade)
                        if (q > 0) {
                            Row(Modifier.padding(start = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(com.tenco.ui.components.coconutGradeLabel(grade), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("$q ${stringResource(R.string.coconuts)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeaderBand(earningsPaise: Long, onNotifications: () -> Unit, onProfile: () -> Unit) {
    val white = androidx.compose.ui.graphics.Color.White
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(com.tenco.ui.theme.Gradients.hero),
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.tenco.R.drawable.ic_palm_leaf),
            contentDescription = null,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(white),
            alpha = 0.10f,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).size(210.dp),
        )
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 16.dp, top = 22.dp, bottom = 30.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$greeting 👋", style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f))
                    Text(stringResource(R.string.role_supplier), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = white)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.IconButton(onClick = onNotifications) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.Notifications, contentDescription = stringResource(R.string.menu_notifications), tint = white)
                    }
                    Surface(shape = CircleShape, color = white.copy(alpha = 0.22f), modifier = Modifier.size(46.dp).clickable { onProfile() }) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.menu_profile), tint = white)
                        }
                    }
                }
            }
            Text(stringResource(R.string.total_earnings), style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f), modifier = Modifier.padding(top = 28.dp))
            com.tenco.ui.components.AnimatedRupees(
                paise = earningsPaise,
                style = MaterialTheme.typography.displaySmall,
                color = white,
            )
            Surface(shape = MaterialTheme.shapes.small, color = white.copy(alpha = 0.18f), modifier = Modifier.padding(top = 8.dp)) {
                Text("▲ ${stringResource(R.string.this_month)}", style = MaterialTheme.typography.labelMedium, color = white, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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
