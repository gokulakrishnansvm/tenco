package com.tenco.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R
import com.tenco.core.prefs.AppPreferences
import com.tenco.ui.components.TencoCard
import com.tenco.ui.components.TencoScaffold
import com.tenco.ui.theme.StatusFailed

@Composable
fun ProfileScreen(
    onBack: (() -> Unit)? = null,
    onChangeLanguage: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    TencoScaffold(title = stringResource(R.string.menu_profile), onBack = onBack) { padding ->
        var showEdit by remember { mutableStateOf(false) }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Identity card
            TencoCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(viewModel.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(viewModel.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("+91 ${viewModel.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.padding(top = 6.dp)) {
                            Text(viewModel.role, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                        }
                    }
                }
            }

            if (viewModel.isSupplier) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showEdit = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null)
                    Text("  " + stringResource(R.string.your_details), fontWeight = FontWeight.SemiBold)
                }
            }

            // Settings list
            TencoCard(Modifier.fillMaxWidth()) {
                Column {
                    SettingRow(Icons.Rounded.Language, stringResource(R.string.change_language), viewModel.language.nativeName, onChangeLanguage)
                    SettingRow(Icons.Rounded.Notifications, stringResource(R.string.menu_notifications), null, onNotifications)
                    SettingRow(Icons.Rounded.Shield, stringResource(R.string.about), "v0.1.0", {})
                }
            }

            // Appearance / theme
            val themeVm: com.tenco.navigation.ThemeViewModel = hiltViewModel()
            val themeMode by themeVm.themeMode.collectAsStateWithLifecycle()
            TencoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip(Icons.Rounded.BrightnessAuto, stringResource(R.string.theme_system), themeMode == AppPreferences.THEME_SYSTEM, Modifier.weight(1f)) { themeVm.setThemeMode(AppPreferences.THEME_SYSTEM) }
                        ThemeChip(Icons.Rounded.LightMode, stringResource(R.string.theme_light), themeMode == AppPreferences.THEME_LIGHT, Modifier.weight(1f)) { themeVm.setThemeMode(AppPreferences.THEME_LIGHT) }
                        ThemeChip(Icons.Rounded.DarkMode, stringResource(R.string.theme_dark), themeMode == AppPreferences.THEME_DARK, Modifier.weight(1f)) { themeVm.setThemeMode(AppPreferences.THEME_DARK) }
                    }
                }
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusFailed.copy(alpha = 0.12f), contentColor = StatusFailed),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Text("  " + stringResource(R.string.logout), fontWeight = FontWeight.SemiBold)
            }
        }

        if (showEdit) {
            var editPhone by remember { mutableStateOf(viewModel.phone) }
            var editUpi by remember { mutableStateOf(viewModel.upi) }
            val ctx = androidx.compose.ui.platform.LocalContext.current
            com.tenco.ui.components.TencoBottomSheet(title = stringResource(R.string.your_details), onDismiss = { showEdit = false }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it.filter(Char::isDigit) },
                        label = { Text(stringResource(R.string.phone)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editUpi,
                        onValueChange = { editUpi = it },
                        label = { Text(stringResource(R.string.upi_id)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    com.tenco.ui.components.SheetActions(
                        onCancel = { showEdit = false },
                        onSave = {
                            viewModel.saveContact(editPhone.trim(), editUpi.trim())
                            showEdit = false
                            android.widget.Toast.makeText(ctx, R.string.details_saved, android.widget.Toast.LENGTH_SHORT).show()
                        },
                        saveText = stringResource(R.string.save),
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = MaterialTheme.shapes.medium, color = bg, modifier = modifier.clickable { onClick() }) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = label, tint = fg)
            Text(label, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 14.dp).weight(1f))
        if (value != null) Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
