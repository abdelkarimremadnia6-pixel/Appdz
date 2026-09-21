package com.example.ui.license

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entities.LicenseRecordEntity
import com.example.domain.license.LicenseStatus
import com.example.ui.theme.*
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    viewModel: LicenseViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var selectedRecordForEdit by remember { mutableStateOf<LicenseRecordEntity?>(null) }
    var newLimitInput by remember { mutableStateOf("2") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Activate Hanouti 40 PRO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isLicenseActive) "PRO Edition Activated" else "Standard / Trial Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isLicenseActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (state.isAdminMode) {
                                viewModel.toggleAdminMode(false)
                            } else {
                                adminPinInput = ""
                                pinError = false
                                showAdminPinDialog = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.LockPerson,
                            contentDescription = "Admin License Management",
                            tint = if (state.isAdminMode) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        if (state.isAdminMode) {
            // Admin License Management View
            AdminLicenseManagementView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = state,
                onFilterChanged = { viewModel.setAdminFilter(it) },
                onRevoke = { viewModel.revokeLicense(it) },
                onSuspend = { viewModel.suspendLicense(it) },
                onReactivate = { viewModel.reactivateLicense(it) },
                onResetDevices = { viewModel.resetDevices(it) },
                onEditLimit = { record ->
                    selectedRecordForEdit = record
                    newLimitInput = record.max_devices.toString()
                },
                onExitAdmin = { viewModel.toggleAdminMode(false) }
            )
        } else {
            // User License Activation View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // License Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.isLicenseActive) SuccessGreen.copy(alpha = 0.15f)
                                            else WarningAmber.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (state.isLicenseActive) Icons.Default.Verified else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (state.isLicenseActive) SuccessGreen else WarningAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (state.isLicenseActive) "Hanouti 40 PRO Activated" else "License Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (state.isLicenseActive) "Full Enterprise Access Unlocked" else "Free / Unlicensed Tier",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            val (statusText, statusBg, statusFg) = if (state.isLicenseActive) {
                                Triple("ACTIVE", SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
                            } else {
                                Triple("AVAILABLE", WarningAmber.copy(alpha = 0.15f), WarningAmber)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    color = statusFg,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        val lic = state.license
                        if (lic != null && lic.status == "ACTIVE") {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Masked Key:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (lic.maskedKey.isNotBlank()) lic.maskedKey else lic.licenseKey,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Device Authorization:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Authorized (1 / 2 devices)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SuccessGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Activated On:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatDate(lic.activatedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Validity:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lifetime PRO License",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Activation Card: "Activate Hanouti 40 PRO"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    val focusManager = LocalFocusManager.current

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Activate Hanouti 40 PRO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Enter your 20-character license key. Case-insensitive and spaces will be automatically normalized.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = state.inputKey,
                            onValueChange = { viewModel.setKey(it) },
                            label = { Text("Enter License Key") },
                            placeholder = { Text("H40-PRO-XXXX-XXXX-XXXX") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            trailingIcon = {
                                if (state.inputKey.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setKey("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("license_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.activateLicense()
                            })
                        )

                        if (state.message != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (state.isError) ErrorRed.copy(alpha = 0.1f)
                                        else SuccessGreen.copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (state.isError) ErrorRed else SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.message!!,
                                    color = if (state.isError) ErrorRed else SuccessGreen,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.activateLicense()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("activate_license_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Verified, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVATE PRO",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                // Unlocked PRO Features Showcase Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isLicenseActive) PrimaryBlue.copy(alpha = 0.06f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hanouti 40 PRO Features",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isLicenseActive) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                            )
                            if (state.isLicenseActive) {
                                Badge(containerColor = SuccessGreen) {
                                    Text("UNLOCKED", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        ProFeatureItem(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Advanced reports & PDF export",
                            description = "Generate invoices, sales reports, and print-ready PDF summaries."
                        )
                        ProFeatureItem(
                            icon = Icons.Default.CalendarMonth,
                            title = "Advanced financial calendar",
                            description = "Track payments, customer debt deadlines, and monthly cashflow."
                        )
                        ProFeatureItem(
                            icon = Icons.Default.Insights,
                            title = "Advanced analytics & charts",
                            description = "Margin calculations, category distribution, and top profit drivers."
                        )
                        ProFeatureItem(
                            icon = Icons.Default.CloudSync,
                            title = "Cloud synchronization",
                            description = "Encrypted cloud backup across Android and Windows devices."
                        )
                        ProFeatureItem(
                            icon = Icons.Default.QrCodeScanner,
                            title = "Advanced barcode scanner",
                            description = "Continuous batch scanning mode, CSV import/export, and sound alerts."
                        )
                        ProFeatureItem(
                            icon = Icons.Default.Group,
                            title = "Advanced customer/supplier management",
                            description = "Multi-tier pricing, ledger statements, and debt settlement tracking."
                        )
                    }
                }

                // Enterprise Offline Security Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cryptographic Architecture & Security",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "• Public-key cryptographic signature verification (ECDSA/secp256r1).\n" +
                                    "• Up to 2 authorized devices per license with hardware fingerprinting.\n" +
                                    "• 100% offline capability: active tokens persist securely locally.\n" +
                                    "• Private signing keys remain securely offline and are never in the APK.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Admin PIN Authentication Dialog
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Administrator Access")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter Administrator PIN to manage license records and device allocations (Default PIN: 4040):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            pinError = false
                        },
                        label = { Text("Admin PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError) {
                        Text("Incorrect PIN. Please try again.", color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyAdminPin(adminPinInput)) {
                            showAdminPinDialog = false
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Device Limit Dialog
    if (selectedRecordForEdit != null) {
        val record = selectedRecordForEdit!!
        AlertDialog(
            onDismissRequest = { selectedRecordForEdit = null },
            title = { Text("Configure License ${record.license_id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current Status: ${record.status}", fontWeight = FontWeight.Bold)
                    Text("Activated Devices: ${record.activated_devices} / ${record.max_devices}")
                    OutlinedTextField(
                        value = newLimitInput,
                        onValueChange = { newLimitInput = it },
                        label = { Text("Device Limit (Max Devices)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newLimitInput.toIntOrNull() ?: 2
                        viewModel.setDeviceLimit(record.license_id, limit)
                        selectedRecordForEdit = null
                    }
                ) {
                    Text("Save Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecordForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminLicenseManagementView(
    modifier: Modifier = Modifier,
    state: LicenseUiState,
    onFilterChanged: (String) -> Unit,
    onRevoke: (String) -> Unit,
    onSuspend: (String) -> Unit,
    onReactivate: (String) -> Unit,
    onResetDevices: (String) -> Unit,
    onEditLimit: (LicenseRecordEntity) -> Unit,
    onExitAdmin: () -> Unit
) {
    val filter = state.adminFilter
    val records = state.licenseRecords.filter {
        if (filter == "ALL") true else it.status.equals(filter, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admin License Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${records.size} of 20 Licenses in System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onExitAdmin) {
                Text("Exit Admin")
            }
        }

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "ACTIVE", "AVAILABLE", "REVOKED", "SUSPENDED").forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { onFilterChanged(f) },
                    label = { Text(f, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(records, key = { it.license_id }) { record ->
                AdminLicenseRecordCard(
                    record = record,
                    onRevoke = { onRevoke(record.license_id) },
                    onSuspend = { onSuspend(record.license_id) },
                    onReactivate = { onReactivate(record.license_id) },
                    onResetDevices = { onResetDevices(record.license_id) },
                    onEditLimit = { onEditLimit(record) }
                )
            }
        }
    }
}

@Composable
private fun AdminLicenseRecordCard(
    record: LicenseRecordEntity,
    onRevoke: () -> Unit,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit,
    onResetDevices: () -> Unit,
    onEditLimit: () -> Unit
) {
    val (statusBg, statusFg) = when (record.status) {
        LicenseStatus.ACTIVE.name -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        LicenseStatus.AVAILABLE.name -> Pair(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue)
        LicenseStatus.REVOKED.name -> Pair(ErrorRed.copy(alpha = 0.15f), ErrorRed)
        LicenseStatus.SUSPENDED.name -> Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.license_id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.status,
                        color = statusFg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Text(
                text = "Key Hash ID: ${record.license_key_id.take(16)}…",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Devices: ${record.activated_devices} / ${record.max_devices}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Last seen: ${FormatUtils.formatDate(record.last_seen)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record.device_ids.isNotBlank()) {
                Text(
                    text = "Registered IDs: ${record.device_ids}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Admin Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditLimit) {
                    Text("Limit")
                }
                TextButton(onClick = onResetDevices) {
                    Text("Reset")
                }
                if (record.status != LicenseStatus.REVOKED.name) {
                    TextButton(onClick = onRevoke) {
                        Text("Revoke", color = ErrorRed)
                    }
                }
                if (record.status == LicenseStatus.ACTIVE.name) {
                    TextButton(onClick = onSuspend) {
                        Text("Suspend", color = WarningAmber)
                    }
                } else if (record.status == LicenseStatus.REVOKED.name || record.status == LicenseStatus.SUSPENDED.name) {
                    TextButton(onClick = onReactivate) {
                        Text("Reactivate", color = SuccessGreen)
                    }
                }
            }
        }
    }
}
