package ua.turskyi.travelling.common.di

import org.koin.dsl.module
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.features.flags.view.adapter.ScreenSlidePagerAdapter
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel
import ua.turskyi.travelling.features.home.view.adapter.HomeAdapter
import ua.turskyi.travelling.features.home.viewmodels.AddCityDialogViewModel
import ua.turskyi.travelling.features.home.viewmodels.HomeActivityViewModel

val adaptersModule = module {
    factory { HomeAdapter() }
    factory { AllCountriesAdapter() }
    factory { ScreenSlidePagerAdapter(get()) }
}

val viewModelsModule = module {
    factory { HomeActivityViewModel(get()) }
    factory { AllCountriesActivityViewModel(get()) }
    factory { FlagsActivityViewModel(get()) }
    factory { AddCityDialogViewModel(get()) }
}

val interactorsModule = module {
    factory { CountriesInteractor() }
}

