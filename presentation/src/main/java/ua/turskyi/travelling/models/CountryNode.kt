package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class CountryNode(
    var id: Int,
    var img: String,
    var visited: Boolean?,
    override var childNode: MutableList<BaseNode>? = null, val title: String) : BaseExpandNode()
