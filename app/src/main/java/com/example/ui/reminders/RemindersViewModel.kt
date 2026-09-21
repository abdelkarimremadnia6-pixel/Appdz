package com.example.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entities.ReminderEntity
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(private val repository: InventoryRepository) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveReminder(reminder: ReminderEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveReminder(reminder)
            onDone()
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.saveReminder(reminder.copy(isEnabled = !reminder.isEnabled))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }
}
