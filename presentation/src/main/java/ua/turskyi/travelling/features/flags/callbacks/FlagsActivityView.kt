package ua.turskyi.travelling.features.flags.callbacks

interface FlagsActivityView {
    fun getItemCount(): Int
    fun setLoaderVisibility(currentVisibility: Int)
}