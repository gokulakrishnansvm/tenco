package com.tenco.feature.vendor

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.Demo
import com.tenco.core.Money
import com.tenco.core.UpiPayment
import com.tenco.domain.PaymentStatus
import com.tenco.ui.components.EmptyState
import com.tenco.ui.components.StatCard
import com.tenco.ui.components.StatusChip
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusPending
import com.tenco.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

// ---------------- Dashboard ----------------
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    vendorId: String,
    onNavigate: (String) -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    var tab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { VendorBottomBar(tab, onSelect = { tab = it }, onOrder = { onNavigate(Routes.VENDOR_ORDERS) }) },
    ) { padding ->
        androidx.compose.animation.AnimatedContent(
            targetState = tab,
            transitionSpec = {
                androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(260)) togetherWith
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(180))
            },
            label = "vendorTab",
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
        ) { t ->
            when (t) {
                0 -> VendorHomeTab(onChangeLanguage, onLogout, { tab = it }, { onNavigate(Routes.VENDOR_ORDERS) }, viewModel)
                1 -> VendorPayScreen(vendorId, onBack = { tab = 0 })
                2 -> VendorHistoryScreen(vendorId, onBack = { tab = 0 })
                3 -> VendorComplaintScreen(vendorId, onBack = { tab = 0 })
                else -> com.tenco.feature.profile.ProfileScreen(
                    onChangeLanguage = onChangeLanguage,
                    onNotifications = { onNavigate(Routes.NOTIFICATIONS) },
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun VendorHomeTab(
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
    onTab: (Int) -> Unit,
    onOrders: () -> Unit,
    viewModel: VendorViewModel,
) {
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val pendingCash = payments.filter { it.method == com.tenco.domain.PaymentMethod.CASH && it.status == com.tenco.domain.PaymentStatus.PENDING_VERIFICATION }
    var showCashSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val d = dashboard
    val langTag = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language
    val receivedLabel = stringResource(R.string.received)
    val coconutsLabel = stringResource(R.string.coconuts)
    val pendingLabel = stringResource(R.string.pending_dues)

    Column(Modifier.fillMaxSize()) {
        VendorHeaderBand(
            name = d?.vendorName?.ifBlank { stringResource(R.string.role_vendor) } ?: stringResource(R.string.role_vendor),
            duesPaise = d?.pendingDuesPaise ?: 0L,
            caption = "$receivedLabel ${d?.receivedQty ?: 0} @ ${Money.formatShort(d?.lastUnitPricePaise ?: 0L)}",
            onSpeak = {
                val rupees = (d?.pendingDuesPaise ?: 0L) / 100
                viewModel.speak(langTag, "$receivedLabel ${d?.receivedQty ?: 0} $coconutsLabel. $pendingLabel ₹$rupees")
            },
            onChangeLanguage = onChangeLanguage,
            onLogout = onLogout,
        )
        Column(
            Modifier.fillMaxWidth().weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.material3.Button(
                onClick = { onTab(1) },
                modifier = Modifier.weight(1f).height(58.dp),
            ) {
                Icon(Icons.Rounded.CurrencyRupee, contentDescription = null)
                Text("  " + stringResource(R.string.pay_via_upi), fontWeight = FontWeight.Bold)
            }
            androidx.compose.material3.OutlinedButton(
                onClick = { showCashSheet = true },
                modifier = Modifier.weight(1f).height(58.dp),
            ) {
                Text(stringResource(R.string.pay_via_cash), fontWeight = FontWeight.Bold)
            }
        }

        if (pendingCash.isNotEmpty()) {
            com.tenco.ui.components.SectionHeader(stringResource(R.string.in_review))
            pendingCash.forEach { p ->
                com.tenco.ui.components.TencoCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(Money.format(p.amountPaise), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        com.tenco.ui.components.StatusChip(p.status, stringResource(R.string.in_review))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.tenco.ui.components.QuickActionTile(Icons.Rounded.ReportProblem, stringResource(R.string.raise_complaint), com.tenco.ui.theme.TileRed, { onTab(3) }, Modifier.weight(1f))
            com.tenco.ui.components.QuickActionTile(Icons.Rounded.History, stringResource(R.string.history), com.tenco.ui.theme.TileBlue, { onTab(2) }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            com.tenco.ui.components.QuickActionTile(Icons.Rounded.Chat, stringResource(R.string.contact_supplier), com.tenco.ui.theme.TileOrange, {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${Demo.SUPPLIER_PHONE.removePrefix("+")}")))
            }, Modifier.weight(1f))
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        }

        if (showCashSheet) {
            var amt by remember { mutableStateOf("") }
            com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.pay_via_cash), onDismiss = { showCashSheet = false }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amt,
                        onValueChange = { amt = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.amount_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    com.tenco.ui.components.SheetActions(
                        onCancel = { showCashSheet = false },
                        onSave = {
                            val rupees = amt.toIntOrNull() ?: 0
                            if (rupees >= 1) {
                                viewModel.payCash(Money.rupeesToPaise(rupees.toDouble()))
                                showCashSheet = false
                                Toast.makeText(context, R.string.cash_sent_review, Toast.LENGTH_LONG).show()
                            }
                        },
                        saveEnabled = (amt.toIntOrNull() ?: 0) >= 1,
                        saveText = stringResource(R.string.pay_via_cash),
                    )
                }
            }
        }
    }
    }
}

// ---------------- Pay (UPI) ----------------
@Composable
private fun VendorHeaderBand(
    name: String,
    duesPaise: Long,
    caption: String,
    onSpeak: () -> Unit,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit,
) {
    val white = Color.White
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
            modifier = Modifier.align(Alignment.TopEnd).size(210.dp),
        )
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 10.dp, top = 22.dp, bottom = 30.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${stringResource(R.string.namaste)} 👋", style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f))
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = white, maxLines = 1)
                }
                IconButton(onClick = onSpeak) { Icon(Icons.Rounded.VolumeUp, contentDescription = stringResource(R.string.speak), tint = white) }
                IconButton(onClick = onChangeLanguage) { Icon(Icons.Rounded.Translate, contentDescription = stringResource(R.string.change_language), tint = white) }
                IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = stringResource(R.string.logout), tint = white) }
            }
            Text(stringResource(R.string.pending_dues), style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f), modifier = Modifier.padding(top = 24.dp))
            com.tenco.ui.components.AnimatedRupees(paise = duesPaise, style = MaterialTheme.typography.displaySmall, color = white)
            Text(caption, style = MaterialTheme.typography.bodyMedium, color = white.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun VendorBottomBar(selected: Int, onSelect: (Int) -> Unit, onOrder: () -> Unit) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().height(66.dp).align(Alignment.BottomCenter),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                VNavCell(Icons.Rounded.Home, stringResource(R.string.nav_home), selected == 0, Modifier.weight(1f)) { onSelect(0) }
                VNavCell(Icons.Rounded.CurrencyRupee, stringResource(R.string.pay), selected == 1, Modifier.weight(1f)) { onSelect(1) }
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                VNavCell(Icons.Rounded.History, stringResource(R.string.history), selected == 2, Modifier.weight(1f)) { onSelect(2) }
                VNavCell(Icons.Rounded.Person, stringResource(R.string.menu_profile), selected == 4, Modifier.weight(1f)) { onSelect(4) }
            }
        }
        Column(Modifier.align(Alignment.TopCenter).offset(y = (-14).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 16.dp,
                modifier = Modifier.size(60.dp).clickable { onOrder() },
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.ShoppingCart, contentDescription = stringResource(R.string.place_order), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
                }
            }
            Text(stringResource(R.string.place_order), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun VNavCell(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}

@Composable
fun VendorPayScreen(
    vendorId: String,
    onBack: (() -> Unit)? = null,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Boolean?>(null) }
    val dues = dashboard?.pendingDuesPaise ?: 0L

    TencoScaffold(title = stringResource(R.string.pay_now), onBack = onBack) { padding ->
        // Shared payment launcher used by both "Pay full" and "Pay".
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
        val startPay: (Double) -> Unit = { amt ->
            if (amt > 0) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                amount = String.format("%.2f", amt)
                // Ask the backend for a payment intent (Razorpay order + UPI link); if the
                // backend is unreachable, fall back to a locally-built UPI deep link.
                viewModel.createBackendIntent(Money.rupeesToPaise(amt)) { backendLink ->
                    val launched = if (backendLink != null) {
                        UpiPayment.launchLink(context, backendLink)
                    } else {
                        UpiPayment.launch(
                            context = context,
                            payeeVpa = dashboard?.supplierVpa ?: Demo.SUPPLIER_VPA,
                            payeeName = Demo.SUPPLIER_NAME,
                            amountRupees = amt,
                            note = "TENCO dues",
                        )
                    }
                    if (launched) showConfirm = true
                    else Toast.makeText(context, R.string.no_upi_app, Toast.LENGTH_LONG).show()
                }
            }
        }
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("${stringResource(R.string.remaining_dues)}: ${Money.format(dues)}", style = MaterialTheme.typography.titleMedium)
            run {
                val qrAmt = amount.toDoubleOrNull() ?: (dues / 100.0)
                val upiStr = com.tenco.core.UpiPayment.buildUri(
                    dashboard?.supplierVpa ?: Demo.SUPPLIER_VPA, Demo.SUPPLIER_NAME, qrAmt, "TENCO dues",
                ).toString()
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    com.tenco.ui.components.QrCode(upiStr)
                    Text(stringResource(R.string.scan_to_pay), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            // One tap: fills the full amount AND launches payment.
            OutlinedButton(onClick = { startPay(dues / 100.0) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pay_full))
            }
            Button(
                onClick = { amount.toDoubleOrNull()?.let { startPay(it) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.pay_via_upi))
            }
        }
    }

    if (showConfirm) {
        val amt = amount.toDoubleOrNull() ?: 0.0
        val paise = Money.rupeesToPaise(amt)
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.did_payment_succeed)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.recordPayment(paise, success = true)
                    showConfirm = false
                    result = true
                }) { Text(stringResource(R.string.mark_paid)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.recordPayment(paise, success = false)
                    showConfirm = false
                    result = false
                }) { Text(stringResource(R.string.mark_failed)) }
            },
        )
    }

    result?.let { ok ->
        val paise = Money.rupeesToPaise(amount.toDoubleOrNull() ?: 0.0)
        com.tenco.ui.components.PaymentResultOverlay(
            success = ok,
            amountText = Money.format(paise),
            title = if (ok) stringResource(R.string.payment_recorded) else stringResource(R.string.status_failed),
            onDone = { onBack?.invoke() },
        )
    }
}

// ---------------- Complaint ----------------
@Composable
fun VendorComplaintScreen(
    vendorId: String,
    onBack: (() -> Unit)? = null,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val context = LocalContext.current
    val reasons = listOf(
        R.string.reason_spoiled,
        R.string.reason_damaged,
        R.string.reason_short,
        R.string.reason_other,
    )
    var selected by remember { mutableStateOf(reasons.first()) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var shortQty by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri?.toString()
    }
    val complaints by viewModel.complaints.collectAsStateWithLifecycle()

    TencoScaffold(title = stringResource(R.string.raise_complaint), onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.complaint_reason), style = MaterialTheme.typography.titleMedium)
            reasons.forEach { res ->
                Row(
                    Modifier.fillMaxWidth().selectable(selected = selected == res, onClick = { selected = res }).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == res, onClick = { selected = res })
                    Text(stringResource(res), style = MaterialTheme.typography.bodyLarge)
                }
            }
            OutlinedTextField(
                value = shortQty,
                onValueChange = { shortQty = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.short_quantity)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (photoUri == null) stringResource(R.string.attach_photo) else "✓ " + stringResource(R.string.attach_photo))
            }
            Button(
                onClick = {
                    val reasonKey = when (selected) {
                        R.string.reason_spoiled -> "spoiled"
                        R.string.reason_damaged -> "damaged"
                        R.string.reason_short -> "short"
                        else -> "other"
                    }
                    viewModel.raiseComplaint(reasonKey, photoUri, shortQty.toIntOrNull() ?: 0)
                    Toast.makeText(context, R.string.complaint_submitted, Toast.LENGTH_SHORT).show()
                    onBack?.invoke()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.submit))
            }

            if (complaints.isNotEmpty()) {
                androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 8.dp))
                com.tenco.ui.components.SectionHeader(stringResource(R.string.my_complaints))
                complaints.forEach { c ->
                    com.tenco.ui.components.TencoCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    com.tenco.ui.components.complaintReasonLabel(c.reason) + if (c.shortQuantity > 0) " · ${c.shortQuantity} ${stringResource(R.string.coconuts)}" else "",
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (c.status == com.tenco.domain.ComplaintStatus.RESOLVED && c.adjustmentPaise > 0) {
                                    Text("− ${Money.format(c.adjustmentPaise)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            StatusChip(c.status, complaintStatusLabel(c.status))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- History ----------------
@Composable
fun VendorHistoryScreen(
    vendorId: String,
    onBack: (() -> Unit)? = null,
    viewModel: VendorViewModel = hiltViewModel(),
) {
    LaunchedEffect(vendorId) { viewModel.setVendor(vendorId) }
    val payments by viewModel.payments.collectAsStateWithLifecycle()

    TencoScaffold(title = stringResource(R.string.transaction_history), onBack = onBack) { padding ->
        if (payments.isEmpty()) {
            EmptyState(R.drawable.ic_coconut, stringResource(R.string.empty_transactions))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { p ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(Money.format(p.amountPaise), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(dateFmt.format(Date(p.createdAt)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            StatusChip(p.status, paymentStatusLabel(p.status))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun complaintStatusLabel(status: String): String = when (status) {
    com.tenco.domain.ComplaintStatus.OPEN -> stringResource(R.string.status_open)
    com.tenco.domain.ComplaintStatus.UNDER_REVIEW -> stringResource(R.string.status_under_review)
    com.tenco.domain.ComplaintStatus.RESOLVED -> stringResource(R.string.status_resolved)
    com.tenco.domain.ComplaintStatus.REJECTED -> stringResource(R.string.status_rejected)
    else -> stringResource(R.string.status_open)
}

@Composable
private fun paymentStatusLabel(status: String): String = when (status) {
    PaymentStatus.COMPLETED -> stringResource(R.string.status_completed)
    PaymentStatus.FAILED -> stringResource(R.string.status_failed)
    PaymentStatus.REJECTED -> stringResource(R.string.status_rejected)
    PaymentStatus.PENDING_VERIFICATION -> stringResource(R.string.status_pending_verification)
    else -> stringResource(R.string.status_pending)
}
