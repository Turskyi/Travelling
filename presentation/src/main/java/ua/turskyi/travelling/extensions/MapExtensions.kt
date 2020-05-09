package ua.turskyi.travelling.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import ua.turskyi.domain.model.CityModel
import ua.turskyi.domain.model.CountryModel
import ua.turskyi.travelling.models.City
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.models.VisitedCountry

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

fun Uri.mapUriToBitMap(context: Context): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    }

/**
 * @return bitmap (from given string)
 */
fun String.mapStringToBitMap(): Bitmap? {
    return try {
        val encodeByte = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
        Log.d("LOGS==>","error to bitmap ${e.message}")
        e.message
        null
    }
}