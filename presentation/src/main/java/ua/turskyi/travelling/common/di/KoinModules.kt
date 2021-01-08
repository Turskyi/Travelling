package ua.turskyi.travelling.common.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.features.flags.view.adapter.FlagsAdapter
import ua.turskyi.travelling.features.flags.viewmodel.FlagsFragmentViewModel
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.AddCityDialogViewModel
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
    factory { FlagsAdapter(get()) }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get(), androidApplication()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsFragmentViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

