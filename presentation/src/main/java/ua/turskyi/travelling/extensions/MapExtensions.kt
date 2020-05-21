package ua.turskyi.travelling.extensions

import android.content.Context
import android.graphics.Bitmap
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry
import java.io.File
import java.io.FileOutputStream

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(
    mutableListOf(),
    { model ->
    model.mapModelToActual()
})

fun List<CountryModel>.mapModelListToNodeList() = this.mapTo(
    mutableListOf()
    , { model ->
    model.mapModelToNode()
})

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId)
fun CountryModel.mapModelToNode() = VisitedCountry(
    id = id, title = name, img = flag, visited = visited, selfie = selfie
)

fun CountryModel.mapModelToActual() = Country(id, name, flag, visited, selfie)
fun Country.mapActualToModel() = CountryModel(id, name, flag, visited, selfie)
fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId)
fun VisitedCountry.mapNodeToActual() = Country(
    id = id, visited = visited, name = title,
    flag = img, selfie = selfie
)

fun Bitmap.mapBitmapToFile(context: Context, fileName: String): File {
    val dirPath = context.externalCacheDir?.absolutePath + "/Screenshots"
    val dir = File(dirPath)
    if (!dir.exists()) dir.mkdirs()
    val file = File(dirPath, fileName)
    try {
        val fileOutputStream = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return file
}