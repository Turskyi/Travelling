package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseNode

class City(var id: Int, var name: String, var parentId: Int, var month: String) : BaseNode() {
    constructor(name: String, parentId: Int, month: String) : this(
        id = 0,
        name = name,
        parentId = parentId,
        month = month,
    )

    constructor(name: String, parentId: Int) : this(
        id = 0,
        name = name,
        parentId = parentId,
        month = "",
    )

    override val childNode: MutableList<BaseNode>?
        get() = null
}