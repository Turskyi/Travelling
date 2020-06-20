package ua.turskyi.travelling.extensions

import androidx.appcompat.app.AppCompatActivity
import ua.turskyi.travelling.common.view.InfoDialog

fun AppCompatActivity.openInfoDialog(info: String) {
    val infoDialog = InfoDialog.newInstance(info)
    infoDialog.show(this.supportFragmentManager, "info dialog")
}