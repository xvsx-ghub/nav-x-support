package com.wiswm.nav.support.userInterface.viewModel

import androidx.lifecycle.ViewModel
import com.wiswm.nav.support.data.local.RepositoryLocal

class SplashViewModel(
    val repositoryLocal: RepositoryLocal
) : ViewModel() {
    companion object {
        const val TAG = "SplashViewModel"
    }

    fun isAuthorizationRequired(): Boolean{
        return repositoryLocal.getSessionKey().isEmpty()
    }
}