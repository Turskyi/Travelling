package ua.turskyi.travelling.features.allcountries.view.adapter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import ua.turskyi.domain.interactor.CountriesInteractor
import ua.turskyi.domain.model.CountryModel

@ExperimentalCoroutinesApi
class CountriesPagingSourceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val interactor: CountriesInteractor = mockk()

    @Test
    fun `load returns page when refresh is successful with blank query`() = runTest {
        val countryModels = listOf(
            CountryModel(id = 1, name = "Ukraine", flag = "", isVisited = true, selfie = "")
        )
        coEvery { interactor.setCountriesByRange(any(), any()) } returns Result.success(countryModels)

        val pagingSource = CountriesPagingSource(interactor, searchQuery = "")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isNotEmpty()
        assertThat(page.data[0].name).isEqualTo("Ukraine")
    }

    @Test
    fun `load returns page when refresh is successful with search query`() = runTest {
        val countryModels = listOf(
            CountryModel(id = 2, name = "Poland", flag = "", isVisited = false, selfie = "")
        )
        coEvery { interactor.loadCountriesByNameAndRange("Poland", any(), any(), any()) } returns Result.success(countryModels)

        val pagingSource = CountriesPagingSource(interactor, searchQuery = "Poland")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page
        assertThat(page.data).isNotEmpty()
        assertThat(page.data[0].name).isEqualTo("Poland")
    }

    @Test
    fun `load returns error when interactor fails`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { interactor.setCountriesByRange(any(), any()) } returns Result.failure(exception)

        val pagingSource = CountriesPagingSource(interactor, searchQuery = "")
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        assertThat((result as PagingSource.LoadResult.Error).throwable).isEqualTo(exception)
    }
}
