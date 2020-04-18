package ua.turskyi.travelling.extensions

import com.chad.library.adapter.base.entity.node.BaseNode
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.CityNode
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

fun CityNode.mapNodeToModel() = CityModel(id = id, name =  name, parentId = parentId)

fun CountryNode.mapNodeToActual() = Country(id = id, visited = visited, name = title, flag = img, cities = childNode)
fun Country.mapActualToBaseNode(childNodes: MutableList<BaseNode>?) =
    CountryNode(id = id, title = name, img = flag, childNode = childNodes, visited = visited)

fun List<Country>.mapActualListToBaseNodeList() = this.mapTo(mutableListOf(), { country ->
    country.mapActualToBaseNode(mutableListOf())
})

