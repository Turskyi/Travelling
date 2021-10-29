package ua.turskyi.travelling.features.flags.view.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import ua.turskyi.travelling.features.flags.callbacks.FlagsActivityView
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.flags.view.fragment.FlagFragment
import ua.turskyi.travelling.utils.extensions.toastLong

class FlagsAdapter(private val activity: AppCompatActivity) : FragmentStateAdapter(activity),
    LifecycleEventObserver {
    private var flagsActivityViewListener: FlagsActivityView? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        try {
            flagsActivityViewListener = recyclerView.context as FlagsActivityView?
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            activity.toastLong(
                castException.localizedMessage ?: castException.stackTraceToString(),
            )
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