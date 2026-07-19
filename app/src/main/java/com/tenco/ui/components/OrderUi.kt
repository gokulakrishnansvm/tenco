package com.tenco.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenco.R
import com.tenco.domain.OrderStatus
import com.tenco.ui.theme.StatusCompleted
import com.tenco.ui.theme.StatusPending

@Composable
fun orderStatusLabel(status: String): String = when (status) {
    OrderStatus.PLACED -> stringResource(R.string.ord_placed)
    OrderStatus.CONFIRMED -> stringResource(R.string.ord_confirmed)
    OrderStatus.IN_PROGRESS -> stringResource(R.string.ord_in_progress)
    OrderStatus.IN_TRANSIT -> stringResource(R.string.ord_in_transit)
    OrderStatus.DELIVERED -> stringResource(R.string.ord_delivered)
    OrderStatus.CANCEL_REQUESTED -> stringResource(R.string.ord_cancel_requested)
    OrderStatus.CANCELLED -> stringResource(R.string.ord_cancelled)
    else -> status
}

@Composable
fun OrderStatusChip(status: String) {
    val color = when (status) {
        OrderStatus.DELIVERED -> StatusCompleted
        OrderStatus.PLACED -> StatusPending
        OrderStatus.CANCEL_REQUESTED -> StatusPending
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Text(
            orderStatusLabel(status),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** Horizontal step timeline showing progress along the order pipeline. */
@Composable
fun OrderTimeline(currentStatus: String, modifier: Modifier = Modifier) {
    val steps = OrderStatus.PIPELINE
    val currentIndex = steps.indexOf(currentStatus).let { if (it < 0) 0 else it }
    Row(modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { i, step ->
            val done = i <= currentIndex
            val dotColor = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(if (i == currentIndex) 16.dp else 12.dp).background(dotColor, CircleShape),
                )
                Text(
                    orderStatusLabel(step),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (i == currentIndex) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
