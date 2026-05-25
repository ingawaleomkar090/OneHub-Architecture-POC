package com.catalent.auth.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.salesforce.androidsdk.config.BootConfig
import com.salesforce.androidsdk.ui.LoginViewModel

class HiddenLoginViewModel(bootConfig: BootConfig) : LoginViewModel(bootConfig) {

    init {
        showServerPicker.value = false
    }

    override val topAppBar: @Composable () -> Unit = { }
    override val bottomAppBar: @Composable () -> Unit = { }
//    override val loadingIndicator: @Composable () -> Unit = { } // Optionally hide the loading indicator as well

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
	            modelClass: Class<T>,
	            extras: CreationExtras,
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return HiddenLoginViewModel(BootConfig.getBootConfig(application.baseContext)) as T
            }
        }
    }
}