package ua.turskyi.travelling.extensions

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ua.turskyi.travelling.R

/**
 * @param container fragments container layout id
 * @param backStackTag if null, then fragment does not adds to backstack
 */
fun Fragment.navigateTo(
    @IdRes container: Int,
    destination: Fragment,
    args: Bundle? = null,
    backStackTag: String? = null,
    sharedElements: List<Pair<View, String>>? = null,
    byFade: Boolean = false,
    slideBack: Boolean = false,
    clearStack: Boolean = false
) {
    (activity as? AppCompatActivity)?.navigateTo(
        container,
        destination,
        args,
        backStackTag,
        sharedElements,
        byFade,
        slideBack,
        clearStack
    )
}

fun AppCompatActivity.navigateTo(
    @IdRes container: Int,
    destination: Fragment,
    args: Bundle? = null,
    backStackTag: String? = null,
    sharedElements: List<Pair<View, String>>? = null,
    byFade: Boolean = false,
    slideBack: Boolean = false,
    clearStack: Boolean = false
) {
    if (clearStack) {
        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }
    destination.arguments = args
    supportFragmentManager
        .beginTransaction()
        .apply {
            if (byFade) {
                setCustomAnimations(
                    R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out
                )
            } else {
                if (slideBack) {
                    setCustomAnimations(
                        R.anim.enter_from_left,
                        R.anim.exit_to_right,
                        R.anim.enter_from_right,
                        R.anim.exit_to_left
                    )
                } else {
                    setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_to_left,
                        R.anim.enter_from_left,
                        R.anim.exit_to_right
                    )
                }
            }
            sharedElements?.forEach {
                addSharedElement(it.first, it.second)
            }
            backStackTag?.let {
                addToBackStack(it)
            }
        }
        .replace(container, destination)
        .commit()
}