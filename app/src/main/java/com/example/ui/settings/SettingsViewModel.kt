package com.example.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.AppSettingsEntity
import com.example.data.repository.InventoryRepository
import com.example.utils.BackupRestoreManager
import com.example.workers.StockCheckWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val repository: InventoryRepository
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettingsEntity> = repository.appSettings
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun updateSettings(newSettings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.saveAppSettings(newSettings)
            if (newSettings.backgroundCheckEnabled) {
                StockCheckWorker.schedule(getApplication())
            } else {
                StockCheckWorker.cancel(getApplication())
            }
            _message.value = "Settings saved successfully"
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val res = BackupRestoreManager.exportBackupJson(getApplication(), uri)
            res.onSuccess { count ->
                _message.value = "Backup exported successfully ($count products)"
            }.onFailure { err ->
                _message.value = "Export failed: ${err.message}"
            }
        }
    }

    fun importBackup(uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            val res = BackupRestoreManager.importBackupJson(getApplication(), uri)
            res.onSuccess { count ->
                _message.value = "Data restored successfully ($count items)"
                onDone()
            }.onFailure { err ->
                _message.value = "Restore failed: ${err.message}"
            }
        }
    }
}
