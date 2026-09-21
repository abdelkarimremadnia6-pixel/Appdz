package com.example.ui.license

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.entities.LicenseEntity
import com.example.data.entities.LicenseRecordEntity
import com.example.data.repository.InventoryRepository
import com.example.domain.license.LicenseManager
import com.example.domain.license.LicenseStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LicenseUiState(
    val license: LicenseEntity? = null,
    val isLicenseActive: Boolean = false,
    val inputKey: String = "",
    val message: String? = null,
    val isError: Boolean = false,
    val isDebug: Boolean = BuildConfig.DEBUG,
    val licenseRecords: List<LicenseRecordEntity> = emptyList(),
    val isAdminMode: Boolean = false,
    val adminFilter: String = "ALL",
    val isLoading: Boolean = false
)

class LicenseViewModel(
    application: Application,
    private val repository: InventoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    init {
        // Ensure catalog seeded
        viewModelScope.launch {
            LicenseManager.ensureCatalogSeeded(repository)
        }

        // Monitor active license
        repository.license.onEach { lic ->
            val isActive = LicenseManager.isLicenseActive(lic)
            _uiState.value = _uiState.value.copy(
                license = lic,
                isLicenseActive = isActive
            )
        }.launchIn(viewModelScope)

        // Monitor production license records
        repository.allLicenseRecords.onEach { records ->
            _uiState.value = _uiState.value.copy(
                licenseRecords = records
            )
        }.launchIn(viewModelScope)
    }

    fun setKey(key: String) {
        _uiState.value = _uiState.value.copy(inputKey = key, message = null)
    }

    fun activateLicense(key: String? = null) {
        val rawInput = key ?: _uiState.value.inputKey
        if (rawInput.isBlank()) {
            _uiState.value = _uiState.value.copy(
                message = "Invalid license key",
                isError = true
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = LicenseManager.activateLicense(
                rawKey = rawInput,
                context = getApplication(),
                repository = repository
            )

            if (result.isValid) {
                _uiState.value = _uiState.value.copy(
                    message = "Hanouti 40 PRO Activated",
                    isError = false,
                    isLicenseActive = true,
                    inputKey = "", // Never display complete key after activation
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    message = result.errorMessage ?: "Invalid license key",
                    isError = true,
                    isLoading = false
                )
            }
        }
    }

    // Admin authentication & management
    fun toggleAdminMode(enable: Boolean) {
        _uiState.value = _uiState.value.copy(isAdminMode = enable)
    }

    fun verifyAdminPin(pin: String): Boolean {
        return if (pin.trim() == "4040" || pin.trim() == "admin") {
            _uiState.value = _uiState.value.copy(isAdminMode = true)
            true
        } else {
            false
        }
    }

    fun setAdminFilter(filter: String) {
        _uiState.value = _uiState.value.copy(adminFilter = filter)
    }

    fun revokeLicense(licenseId: String) {
        viewModelScope.launch {
            LicenseManager.revokeLicense(licenseId, repository)
        }
    }

    fun suspendLicense(licenseId: String) {
        viewModelScope.launch {
            LicenseManager.suspendLicense(licenseId, repository)
        }
    }

    fun reactivateLicense(licenseId: String) {
        viewModelScope.launch {
            LicenseManager.reactivateLicense(licenseId, repository)
        }
    }

    fun setDeviceLimit(licenseId: String, maxDevices: Int) {
        viewModelScope.launch {
            LicenseManager.setDeviceLimit(licenseId, maxDevices, repository)
        }
    }

    fun resetDevices(licenseId: String) {
        viewModelScope.launch {
            LicenseManager.resetDevices(licenseId, repository)
        }
    }
}
