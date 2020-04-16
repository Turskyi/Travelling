package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseNode

class CityNode(var name: String) : BaseNode() {
    override val childNode: MutableList<BaseNode>?
        get() = null
}