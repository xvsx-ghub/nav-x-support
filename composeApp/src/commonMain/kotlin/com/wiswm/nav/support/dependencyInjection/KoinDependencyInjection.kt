package com.wiswm.nav.support.dependencyInjection

import com.russhwolf.settings.Settings
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.SettingsManager
import com.wiswm.nav.support.data.remote.RepositoryRemote
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.stomp.StompManager
import com.wiswm.nav.support.data.useCase.LoginUseCase
import com.wiswm.nav.support.userInterface.screen.CustomerDetailsScreen
import com.wiswm.nav.support.userInterface.screen.CustomerListScreen
import com.wiswm.nav.support.userInterface.screen.DashboardScreen
import com.wiswm.nav.support.userInterface.screen.LoginScreen
import com.wiswm.nav.support.userInterface.screen.ProfileScreen
import com.wiswm.nav.support.userInterface.screen.ReportScreen
import com.wiswm.nav.support.userInterface.screen.SplashScreen
import com.wiswm.nav.support.userInterface.screen.TaskListScreen
import com.wiswm.nav.support.userInterface.screen.WeighingDetailsScreen
import com.wiswm.nav.support.userInterface.screen.WeighingListScreen
import com.wiswm.nav.support.userInterface.viewModel.CustomerDetailsViewModel
import com.wiswm.nav.support.userInterface.viewModel.CustomerListViewModel
import com.wiswm.nav.support.userInterface.viewModel.DashboardViewModel
import com.wiswm.nav.support.userInterface.viewModel.LoginViewModel
import com.wiswm.nav.support.userInterface.viewModel.ProfileViewModel
import com.wiswm.nav.support.userInterface.viewModel.ReportViewModel
import com.wiswm.nav.support.userInterface.viewModel.SplashViewModel
import com.wiswm.nav.support.userInterface.viewModel.TaskListViewModel
import com.wiswm.nav.support.userInterface.viewModel.WeighingDetailsViewModel
import com.wiswm.nav.support.userInterface.viewModel.WeighingListViewModel
import com.wiswm.nav.support.util.System
import getRoomDatabase
import org.koin.dsl.module

val baseApplicationModule = module {
    single { SplashViewModel(get()) }
    single { LoginViewModel(get(), get(), get()) }
    single {
        DashboardViewModel(
            get(), get(), get(),
            get(), get(), get(),
            get(), get(), get(), get()
        )
    }
    single { TaskListViewModel(get(), get()) }
    single { CustomerListViewModel(get(), get()) }
    single {
        CustomerDetailsViewModel(
            get(), get(), get()
        )
    }
    single { ReportViewModel(get(), get()) }
    single { WeighingListViewModel(get(), get()) }
    single { WeighingDetailsViewModel(get(), get()) }
    single { ProfileViewModel(get()) }

    single { SplashScreen() }
    single { LoginScreen() }
    single { DashboardScreen() }
    single { TaskListScreen() }
    single { CustomerListScreen() }
    single { CustomerDetailsScreen() }
    single { ReportScreen() }
    single { WeighingListScreen() }
    single { WeighingDetailsScreen() }
    single { ProfileScreen() }

    single<Settings> { Settings() }
    single { SettingsManager(get()) }
    single { getRoomDatabase() }
    single { Http(get()) }
    single { StompManager(get(), get()) }

    single { System() }

    single { LoginUseCase(get(), get()) }

    single { RepositoryLocal(get(), get(), get()) }
    single { RepositoryRemote() }
}