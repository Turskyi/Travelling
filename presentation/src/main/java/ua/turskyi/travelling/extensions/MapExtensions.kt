package ua.turskyi.travelling.extensions

import com.chad.library.adapter.base.entity.node.BaseNode
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.CountryNode

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(mutableListOf(), { model ->
    model.mapModelToActual()
})

fun CountryModel.mapModelToActual() = Country(
    id, name, flag, visited
)

fun Country.mapActualToModel() = CountryModel(
    id, name, flag, visited
)

fun CountryNode.mapNodeToActual() = Country(id, title, img)
fun Country.mapActualToBaseNode(childNodes: MutableList<BaseNode>?) =
    CountryNode(id = id, title = name, img = flag, childNode = childNodes)

fun List<Country>.mapActualListToBaseNodeList() = this.mapTo(mutableListOf(), { country ->
    country.mapActualToBaseNode(mutableListOf())
})

