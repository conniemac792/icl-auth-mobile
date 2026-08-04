package icl.ohs.libs.auth.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileViewModel {
    var uiState by mutableStateOf(ProfileRepository.getProfile())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun refresh() {
        if (isRefreshing) return

        isRefreshing = true
        errorMessage = null
        viewModelScope.launch {
            val result = ProfileRepository.refreshProfile()
            result.fold(
                onSuccess = { uiState = it },
                onFailure = { throwable ->
                    errorMessage = throwable.message
                        ?: "Unable to refresh profile. Check your internet connection and try again."
                },
            )
            isRefreshing = false
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // Simulate clearing cache
            successMessage = "Cache cleared successfully!"
            delay(2.seconds)
            successMessage = null
        }
    }

    fun clearAppData() {
        viewModelScope.launch {
            // Simulate clearing app data
            ProfileRepository.clearProfile()
            successMessage = "App data removed. Please log in again."
            delay(2.seconds)
            successMessage = null
        }
    }

    fun logout(onLogout: () -> Unit) {
        ProfileRepository.clearProfile()
        onLogout()
    }

    fun dismissMessages() {
        errorMessage = null
        successMessage = null
    }

    fun clear() {
        viewModelScope.cancel()
    }
}
