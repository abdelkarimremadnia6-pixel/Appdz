package com.example.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.R
import com.example.data.entities.AppSettingsEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLicense: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    var businessName by remember { mutableStateOf(settings.businessName) }
    var currency by remember { mutableStateOf(settings.currency) }
    var defaultMinStock by remember { mutableStateOf(settings.defaultMinimumStock.toString()) }
    var allowNegativeStock by remember { mutableStateOf(settings.allowNegativeStock) }
    var lowStockNotifications by remember { mutableStateOf(settings.lowStockNotificationsEnabled) }
    var remindersEnabled by remember { mutableStateOf(settings.remindersEnabled) }
    var backgroundCheckEnabled by remember { mutableStateOf(settings.backgroundCheckEnabled) }
    var isDarkTheme by remember { mutableStateOf(settings.isDarkTheme) }
    var language by remember { mutableStateOf(settings.language) }

    LaunchedEffect(settings) {
        businessName = settings.businessName
        currency = settings.currency
        defaultMinStock = settings.defaultMinimumStock.toString()
        allowNegativeStock = settings.allowNegativeStock
        lowStockNotifications = settings.lowStockNotificationsEnabled
        remindersEnabled = settings.remindersEnabled
        backgroundCheckEnabled = settings.backgroundCheckEnabled
        isDarkTheme = settings.isDarkTheme
        language = settings.language
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        lowStockNotifications = granted
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (message != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message!!,
                            color = PrimaryBlue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearMessage() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = PrimaryBlue)
                        }
                    }
                }
            }

            // License & Stock Movement Quick Links
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.license_system), fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("View license status, activate key or upgrade tier") },
                        leadingContent = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryBlue) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLicense)
                            .testTag("settings_license_item")
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.stock_history), fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Complete audit log of all inventory changes") },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = SuccessGreen) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable(onClick = onNavigateToHistory)
                            .testTag("settings_history_item")
                    )
                }
            }

            // General Business Settings
            Text("Business & General", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text(stringResource(R.string.business_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("business_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text(stringResource(R.string.currency)) },
                            placeholder = { Text("DA, USD, EUR") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("currency_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = defaultMinStock,
                            onValueChange = { defaultMinStock = it },
                            label = { Text(stringResource(R.string.default_min_stock)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Allow negative stock switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.allow_negative_stock), fontWeight = FontWeight.SemiBold)
                            Text("Allow sales even if quantity reaches zero", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = allowNegativeStock,
                            onCheckedChange = { allowNegativeStock = it },
                            modifier = Modifier.testTag("switch_allow_negative_stock")
                        )
                    }
                }
            }

            // Notifications & Monitoring
            Text("Background Monitoring & Alerts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.low_stock_notifications), fontWeight = FontWeight.SemiBold)
                            Text("Alert when items fall below minimum threshold", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = lowStockNotifications,
                            onCheckedChange = {
                                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        lowStockNotifications = true
                                    }
                                } else {
                                    lowStockNotifications = it
                                }
                            },
                            modifier = Modifier.testTag("switch_low_stock_notifications")
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.background_check), fontWeight = FontWeight.SemiBold)
                            Text("Run periodic checks even when app is closed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = backgroundCheckEnabled,
                            onCheckedChange = { backgroundCheckEnabled = it },
                            modifier = Modifier.testTag("switch_background_check")
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stock Reminders", fontWeight = FontWeight.SemiBold)
                            Text("Enable custom stock alarm notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { remindersEnabled = it }
                        )
                    }
                }
            }

            // Language & Appearance
            Text("Language & Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.dark_theme), fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it },
                            modifier = Modifier.testTag("switch_dark_theme")
                        )
                    }

                    HorizontalDivider()

                    Text(stringResource(R.string.language), fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = language == "en",
                            onClick = { language = "en" },
                            label = { Text(stringResource(R.string.english)) }
                        )
                        FilterChip(
                            selected = language == "ar",
                            onClick = { language = "ar" },
                            label = { Text(stringResource(R.string.arabic)) }
                        )
                        FilterChip(
                            selected = language == "fr",
                            onClick = { language = "fr" },
                            label = { Text(stringResource(R.string.french)) }
                        )
                    }
                }
            }

            // Backup & Restore
            Text(stringResource(R.string.backup_restore), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Export your entire inventory database to a secure JSON file, or restore from a previous backup file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                exportLauncher.launch("inventory_backup_${System.currentTimeMillis()}.json")
                            },
                            modifier = Modifier.weight(1f).testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.export_backup))
                        }
                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            modifier = Modifier.weight(1f).testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.restore_backup))
                        }
                    }
                }
            }

            // Save Settings Button
            Button(
                onClick = {
                    val updated = settings.copy(
                        businessName = businessName.trim().ifBlank { "My Business" },
                        currency = currency.trim().ifBlank { "DA" },
                        defaultMinimumStock = defaultMinStock.toIntOrNull() ?: 5,
                        allowNegativeStock = allowNegativeStock,
                        lowStockNotificationsEnabled = lowStockNotifications,
                        remindersEnabled = remindersEnabled,
                        backgroundCheckEnabled = backgroundCheckEnabled,
                        isDarkTheme = isDarkTheme,
                        language = language
                    )
                    viewModel.updateSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.save_settings), fontWeight = FontWeight.Bold)
            }

            // App Version Info
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "StockFlow Inventory v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
