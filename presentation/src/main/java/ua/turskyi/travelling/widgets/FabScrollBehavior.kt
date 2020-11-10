package ua.turskyi.travelling.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ua.turskyi.travelling.utils.ContextUtil

class FabScrollBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<FloatingActionButton?>(context, attrs) {
    private val toolbarHeight: Int = ContextUtil.getToolbarHeight(context)
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        fab: FloatingActionButton,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        fab: FloatingActionButton,
        dependency: View
    ): Boolean {
        if (dependency is AppBarLayout) {
            val layoutParams: CoordinatorLayout.LayoutParams =
                fab.layoutParams as CoordinatorLayout.LayoutParams
            val fabBottomMargin: Int = layoutParams.bottomMargin
            val distanceToScroll = fab.height + fabBottomMargin
            val ratio = dependency.getY() / toolbarHeight.toFloat()
            /* change the following data depending on the result is needed */
            fab.translationY = -distanceToScroll * 3 * ratio
        }
        return true
    }
}