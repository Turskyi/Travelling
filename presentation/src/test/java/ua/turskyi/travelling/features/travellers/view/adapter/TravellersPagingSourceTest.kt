package ua.turskyi.travelling.features.travellers.view.adapter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import ua.turskyi.domain.interactor.TravellersInteractor
import ua.turskyi.domain.model.TravellerModel
import ua.turskyi.travelling.entities.Traveller

class TravellersPagingSourceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val interactor: TravellersInteractor = mockk()

    @Test
    fun `load requests a positional range and returns page keys`() = runTest {
        val travellerModels = listOf(
            TravellerModel(id = "1", name = "Alex"),
            TravellerModel(id = "2", name = "Sam"),
        )
        every {
            interactor.setTravellersByRange(any(), any(), any(), any())
        } answers {
            thirdArg<(List<TravellerModel>) -> Unit>()(travellerModels)
        }

        val result = TravellersPagingSource(interactor, searchQuery = "").load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 2,
                placeholdersEnabled = false,
            ),
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page<Int, Traveller>
        assertThat(page.data.map { it.name }).containsExactly("Alex", "Sam").inOrder()
        assertThat(page.prevKey).isNull()
        assertThat(page.nextKey).isEqualTo(2)
        verify { interactor.setTravellersByRange(2L, 0, any(), any()) }
    }

    @Test
    fun `load routes nonblank search to the name query`() = runTest {
        val onSuccess = slot<(List<TravellerModel>) -> Unit>()
        every {
            interactor.setTravellersByName("Sam", any(), any(), capture(onSuccess), any())
        } answers {
            onSuccess.captured(listOf(TravellerModel(name = "Sam")))
        }

        val result = TravellersPagingSource(interactor, searchQuery = "Sam").load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false,
            ),
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val page = result as PagingSource.LoadResult.Page<Int, Traveller>
        assertThat(page.data.single().name).isEqualTo("Sam")
        assertThat(page.nextKey).isNull()
        verify { interactor.setTravellersByName("Sam", 10L, 0, any(), any()) }
    }
}
