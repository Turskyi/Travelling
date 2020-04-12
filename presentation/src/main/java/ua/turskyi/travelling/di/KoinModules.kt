package ua.turskyi.travelling.di

import org.koin.dsl.module
import ua.turskyi.domain.interactors.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodel.HomeActivityViewModel
import ua.turskyi.travelling.features.selfie.viewmodel.SelfieActivityViewModel

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { SelfieActivityViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

