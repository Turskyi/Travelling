package ua.turskyi.travelling.extensions

import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.CityNode
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.CountryNode

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(mutableListOf(), { model ->
    model.mapModelToActual()
})

fun List<CountryModel>.mapModelListToNodeList() = this.mapTo(mutableListOf(), { model ->
    model.mapModelToNode()
})

fun List<CityModel>.mapModelsToNodeList() = this.mapTo(mutableListOf(), { model ->
    model.mapModelToBaseNode()
})

fun CityModel.mapModelToBaseNode() = CityNode(id = id, name = name, parentId = parentId)

fun CountryModel.mapModelToNode() = CountryNode(
    id = id, title = name, img = flag, visited = visited
)

fun CountryModel.mapModelToActual() = Country(
    id, name, flag, visited
)

fun Country.mapActualToModel() = CountryModel(
    id, name, flag, visited
)

fun CountryNode.mapNodeToModel() = CountryModel(
    id = id, name = title, flag = img, visited = visited
)

fun MutableList<CityNode>.mapNodeListToModelList() = this.mapTo(mutableListOf(), {
    it.mapNodeToModel()
})

fun CityNode.mapNodeToModel() = CityModel(id = id, name =  name, parentId = parentId)

fun CountryNode.mapNodeToActual() = Country(id = id, visited = visited, name = title, flag = img, cities = childNode)
fun Country.mapActualToBaseNode() =
    CountryNode(id = id, title = name, img = flag, childNode = cities, visited = visited)

fun List<Country>.mapActualListToBaseNodeList() = this.mapTo(mutableListOf(), { country ->
    country.mapActualToBaseNode()
})

