package ua.turskyi.travelling.features.allcountries.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.travelling.CoroutineTestRule

@ExperimentalCoroutinesApi
class AllCountriesActivityViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val interactor: CountriesInteractor = mockk(relaxed = true)

    @Test
    fun `init loads not visited countries number successfully`() = runTest {
        coEvery { interactor.setNotVisitedCountriesNum() } returns Result.success(5)

        val viewModel = AllCountriesActivityViewModel(interactor)

        assertThat(viewModel.notVisitedCountriesNumLiveData.value).isEqualTo(5)
    }

    @Test
    fun `searchQuery setter updates property`() = runTest {
        coEvery { interactor.setNotVisitedCountriesNum() } returns Result.success(10)

        val viewModel = AllCountriesActivityViewModel(interactor)
        viewModel.searchQuery = "Ukraine"

        assertThat(viewModel.searchQuery).isEqualTo("Ukraine")
    }

    @Test
    fun `pagedCountries flow is initialized`() = runTest {
        coEvery { interactor.setNotVisitedCountriesNum() } returns Result.success(5)

        val viewModel = AllCountriesActivityViewModel(interactor)

        assertThat(viewModel.pagedCountries).isNotNull()
    }
}
