package com.tenco.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenco.R
import com.tenco.ui.theme.Gradients
import com.tenco.ui.theme.TileBlue
import com.tenco.ui.theme.TileGreen

@Composable
fun RoleSelectScreen(
    onSupplier: () -> Unit,
    onVendor: () -> Unit,
    onChangeLanguage: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.select_role), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("Choose how you'll use TENCO", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 28.dp))

        RoleCard(stringResource(R.string.role_supplier), stringResource(R.string.role_supplier_desc), Icons.Rounded.Inventory2, TileGreen, onSupplier)
        Spacer(Modifier.height(16.dp))
        RoleCard(stringResource(R.string.role_vendor), stringResource(R.string.role_vendor_desc), Icons.Rounded.Storefront, TileBlue, onVendor)

        Spacer(Modifier.height(28.dp))
        TextButton(onClick = onChangeLanguage, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Translate, contentDescription = null)
            Text("  " + stringResource(R.string.change_language))
        }
    }
}

@Composable
private fun RoleCard(title: String, desc: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(Gradients.tile(accent)).clickable { onClick() }.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(16.dp), color = accent, modifier = Modifier.size(56.dp)) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
        Column(Modifier.padding(start = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
