package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GlobalSummary
import com.example.data.model.Recitation
import com.example.data.repository.DuroodRepository
import com.example.ui.model.DUROOD_PRESETS
import com.example.ui.model.DuroodInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class DuroodViewModel(
    private val repository: DuroodRepository,
    val currentUserId: String,
    val defaultUserName: String = ""
) : ViewModel() {

    private companion object {
        const val TAG = "DuroodViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }

    val publicRecitations: StateFlow<UiState<List<Recitation>>> = repository.observePublicRecitations()
        .map<List<Recitation>, UiState<List<Recitation>>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Error observing public recitations", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to observe recitations"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val userRecitations: StateFlow<UiState<List<Recitation>>> = repository.observeUserRecitations(currentUserId)
        .map<List<Recitation>, UiState<List<Recitation>>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Error observing user recitations", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to observe user recitations"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val globalSummary: StateFlow<UiState<GlobalSummary?>> = repository.observeGlobalSummary()
        .map<GlobalSummary?, UiState<GlobalSummary?>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Error observing global summary", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to observe summary"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Interactive tasbeeh state
    private val _sessionCount = MutableStateFlow(0L)
    val sessionCount: StateFlow<Long> = _sessionCount.asStateFlow()

    private val _targetGoal = MutableStateFlow(33L)
    val targetGoal: StateFlow<Long> = _targetGoal.asStateFlow()

    private val _selectedDurood = MutableStateFlow(DUROOD_PRESETS[0])
    val selectedDurood: StateFlow<DuroodInfo> = _selectedDurood.asStateFlow()

    val userNameInput = MutableStateFlow(defaultUserName.ifBlank { "Devotee" })
    val dedicationInput = MutableStateFlow("")

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Admin Panel state (Password: 123450)
    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _adminPasswordError = MutableStateFlow<String?>(null)
    val adminPasswordError: StateFlow<String?> = _adminPasswordError.asStateFlow()

    fun incrementSession(by: Long = 1L) {
        _sessionCount.value += by
    }

    fun decrementSession(by: Long = 1L) {
        if (_sessionCount.value > 0L) {
            _sessionCount.value = maxOf(0L, _sessionCount.value - by)
        }
    }

    fun setSessionCount(count: Long) {
        _sessionCount.value = maxOf(0L, count)
    }

    fun resetSession() {
        _sessionCount.value = 0L
    }

    fun setTargetGoal(goal: Long) {
        _targetGoal.value = goal
    }

    fun selectDurood(durood: DuroodInfo) {
        _selectedDurood.value = durood
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun submitCurrentSession(onComplete: (Boolean, String?) -> Unit) {
        val count = _sessionCount.value
        if (count <= 0L) {
            onComplete(false, "Please recite at least 1 Durood Shareef before submitting")
            return
        }

        val name = userNameInput.value.trim()
        if (name.isBlank()) {
            onComplete(false, "Please enter your name")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.addRecitation(
                userId = currentUserId,
                userName = name,
                count = count,
                duroodType = _selectedDurood.value.title,
                note = dedicationInput.value.trim()
            )
            _isSubmitting.value = false

            if (result.isSuccess) {
                _sessionCount.value = 0L
                _statusMessage.value = "Alhamdulillah! $count recitations submitted and synced."
                onComplete(true, null)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to sync recitation"
                onComplete(false, err)
            }
        }
    }

    fun verifyAdminPassword(password: String): Boolean {
        if (password.trim() == "123450") {
            _isAdminUnlocked.value = true
            _adminPasswordError.value = null
            return true
        } else {
            _adminPasswordError.value = "Invalid password. The admin password is 123450."
            return false
        }
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
        _adminPasswordError.value = null
    }

    fun deleteRecitationAsAdmin(recitation: Recitation, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteRecitation(
                recitationId = recitation.id,
                countToDeduct = recitation.count
            )
            onDone(result.isSuccess)
        }
    }
}
