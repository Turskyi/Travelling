package ua.turskyi.travelling.features.home.view.adapter.providers

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import splitties.toast.toast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.models.City

class CityProvider : BaseNodeProvider() {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_city_content

    var onCityLongClickListener: ((data: City) -> Unit)? = null

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val entity: City = item as City
        helper.setText(R.id.tv, entity.name)
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
//        TODO: implement... sending to map for example
        toast("section $position")
    }

    override fun onLongClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ): Boolean {
        val entity: City = data as City
        onCityLongClickListener?.invoke(entity)
        return true
    }
}