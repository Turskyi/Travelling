package ua.turskyi.travelling.features.home.view.adapter

import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import ua.turskyi.travelling.features.home.view.adapter.providers.CityNodeProvider
import ua.turskyi.travelling.features.home.view.adapter.providers.CountryNodeProvider
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.VisitedCountry

class HomeAdapter : BaseNodeAdapter() {

    private var provider = CountryNodeProvider()

    init {
        addFullSpanNodeProvider(provider)
        addNodeProvider(CityNodeProvider())
    }

    var onImageClickListener: ((data: VisitedCountry) -> Unit)? = null
        set(value) {
            provider.onImageClickListener = value
            field = value
        }

    var onTextClickListener: ((data: VisitedCountry) -> Unit)? = null
        set(value) {
            provider.onTextClickListener = value
            field = value
        }

    var onLongClickListener: ((data: VisitedCountry) -> Unit)? = null
        set(value) {
            provider.onLongLickListener = value
            field = value
        }

    override fun getItemType(
        data: List<BaseNode>,
        position: Int
    ): Int {
        return when (data[position]) {
            is VisitedCountry -> 0
            is City -> 1
            else -> -1
        }
    }
}