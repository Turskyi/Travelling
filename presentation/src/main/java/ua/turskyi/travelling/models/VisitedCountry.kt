package ua.turskyi.travelling.models

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

class VisitedCountry(
    var id: Int,
    val title: String,
    var img: String,
    var isVisited: Boolean,
    var selfie: String,
    override var childNode: MutableList<BaseNode>,
) : BaseExpandNode() {
    constructor(
        id: Int,
        title: String,
        img: String,
        isVisited: Boolean,
        selfie: String
    ) : this(
        id = id,
        title = title,
        img = img,
        isVisited = isVisited,
        selfie = selfie,
        childNode = mutableListOf()
    )
}
