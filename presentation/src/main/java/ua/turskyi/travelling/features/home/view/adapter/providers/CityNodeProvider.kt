package ua.turskyi.travelling.features.home.view.adapter.providers

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import splitties.toast.toast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.models.CityNode

class CityNodeProvider : BaseNodeProvider() {

    override val itemViewType: Int
        get() = 1

    override val layoutId: Int
        get() = R.layout.item_city_content

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val entity: CityNode = item as CityNode
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
}