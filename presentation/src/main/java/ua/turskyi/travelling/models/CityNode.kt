package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseNode

class CityNode(var id: Int?, var name: String, var parentId: Int) : BaseNode() {
    constructor(name: String, parentId: Int) : this(null, name = name, parentId = parentId)
    override val childNode: MutableList<BaseNode>?
        get() = null
}