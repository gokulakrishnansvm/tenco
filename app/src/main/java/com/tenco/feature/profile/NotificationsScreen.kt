package com.tenco.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenco.R
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusPending
import com.tenco.ui.theme.TileBlue

private data class Notif(val icon: ImageVector, val title: String, val body: String, val time: String, val color: Color)

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val items = listOf(
        Notif(Icons.Rounded.CurrencyRupee, "Payment received", "₹1,800 received from Lakshmi Cart", "2h ago", StatusCompleted),
        Notif(Icons.Rounded.LocalShipping, "Delivery confirmed", "Ravi Stall confirmed 50 coconuts", "5h ago", TileBlue),
        Notif(Icons.Rounded.ReportProblem, "New complaint", "Spoiled coconuts reported by Ravi Stall", "1d ago", StatusPending),
    )
    TencoScaffold(title = stringResource(R.string.menu_notifications), onBack = onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items) { n ->
                TencoCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = n.color.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(n.icon, null, tint = n.color) }
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(n.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(n.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
