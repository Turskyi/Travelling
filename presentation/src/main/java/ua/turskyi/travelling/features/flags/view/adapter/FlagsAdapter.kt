package ua.turskyi.travelling.features.flags.view.adapter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import ua.turskyi.travelling.features.flags.callbacks.FlagsActivityView
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.flags.view.fragment.FlagFragment
import ua.turskyi.travelling.utils.extensions.showReportDialog

class FlagsAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity),
    LifecycleEventObserver {
    private var flagsActivityViewListener: FlagsActivityView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context: Context = recyclerView.context
        if (context is FlagsActivityView) {
            flagsActivityViewListener = context
        } else {
            context.showReportDialog()
        }
    }

    override fun getItemCount(): Int = flagsActivityViewListener?.getItemCount() ?: 0

    override fun createFragment(position: Int): Fragment = FlagFragment().apply {
        arguments = bundleOf(EXTRA_POSITION to position)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            flagsActivityViewListener = null
        }
    }
}