package ua.turskyi.travelling.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.hideKeyboard() {
    val imm: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Finds the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    /* If no view currently does not have a focus, create a new one, just so we can grab a window
     token from it */
    if(view == null){
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}

fun AppCompatActivity.showKeyboard() {
    val inputMethodManager: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Finds the currently focused view, so we can grab the correct window token from it.
    var view: View? = currentFocus
    /* If no view currently does not have a focus, create a new one, just so we can grab a window
     token from it */
    if(view == null){
        view = View(this)
    }
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}