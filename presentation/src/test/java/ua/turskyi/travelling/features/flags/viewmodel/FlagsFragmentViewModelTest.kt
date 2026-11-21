package ua.turskyi.travelling.features.flags.viewmodel

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.CoroutineTestRule

@ExperimentalCoroutinesApi
class FlagsFragmentViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val interactor: CountriesInteractor = mockk(relaxed = true)
    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)

    @Test
    fun `onStateChanged with ON_CREATE loads visited countries`() = runTest {
        val countries = listOf(
            CountryModel(id = 1, name = "Canada", flag = "flag_url", isVisited = true, selfie = ""),
        )
        coEvery { interactor.setVisitedCountries() } returns Result.success(countries)

        val viewModel = FlagsFragmentViewModel(interactor)
        viewModel.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_CREATE)

        assertThat(viewModel.visitedCountries.value?.size).isEqualTo(1)
        assertThat(viewModel.visitedCountries.value?.first()?.name).isEqualTo("Canada")
        assertThat(viewModel.visibilityLoader.value).isEqualTo(View.GONE)
    }

    @Test
    fun `setVisitedCountries preserves remote https selfie URLs`() = runTest {
        val remoteSelfieUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/selfies%2Fphoto.jpg"
        val countries = listOf(
            CountryModel(id = 1, name = "Canada", flag = "flag_url", isVisited = true, selfie = remoteSelfieUrl),
        )
        coEvery { interactor.setVisitedCountries() } returns Result.success(countries)

        val viewModel = FlagsFragmentViewModel(interactor)
        viewModel.onStateChanged(lifecycleOwner, Lifecycle.Event.ON_CREATE)

        assertThat(viewModel.visitedCountries.value?.first()?.filePath).isEqualTo(remoteSelfieUrl)
        assertThat(viewModel.visibilityLoader.value).isEqualTo(View.GONE)
    }

    @Test
    fun `updateSelfie with non-empty filePath updates countries`() = runTest {
        val countries = listOf(
            CountryModel(id = 1, name = "Canada", flag = "flag_url", isVisited = true, selfie = "/path/to/selfie.jpg"),
        )
        coEvery { interactor.updateSelfie(1, "/path/to/selfie.jpg") } returns Result.success(countries)

        val viewModel = FlagsFragmentViewModel(interactor)
        viewModel.updateSelfie(1, "/path/to/selfie.jpg")

        assertThat(viewModel.visitedCountries.value?.first()?.filePath).isEqualTo("/path/to/selfie.jpg")
        assertThat(viewModel.visibilityLoader.value).isEqualTo(View.GONE)
    }

    @Test
    fun `updateSelfie with empty filePath does not show loader and clears selfie`() = runTest {
        val countries = listOf(
            CountryModel(id = 1, name = "Canada", flag = "flag_url", isVisited = true, selfie = ""),
        )
        coEvery { interactor.updateSelfie(1, "") } returns Result.success(countries)

        val viewModel = FlagsFragmentViewModel(interactor)
        viewModel.updateSelfie(1, "")

        assertThat(viewModel.visitedCountries.value?.first()?.filePath).isEmpty()
        // Visibility loader was never set to VISIBLE
        assertThat(viewModel.visibilityLoader.value).isEqualTo(View.GONE)
    }
}
