package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class CountryNode(
    var id: Int,
    var img: String,
    override val childNode: MutableList<BaseNode>?, val title: String) : BaseExpandNode()
