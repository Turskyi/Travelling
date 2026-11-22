package ua.turskyi.travelling.features.travellers.view.adapter

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ua.turskyi.travelling.databinding.ItemTravellerBinding
import ua.turskyi.travelling.entities.Traveller

class TravellersAdapter : PagingDataAdapter<Traveller, TravellersAdapter.TravellerViewHolder>(
    COUNTRIES_DIFF_CALLBACK
) {

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Traveller]
     * has been updated.
     */
    companion object {
        val COUNTRIES_DIFF_CALLBACK: DiffUtil.ItemCallback<Traveller> =
            object : DiffUtil.ItemCallback<Traveller>() {
                override fun areItemsTheSame(
                    oldItem: Traveller,
                    newItem: Traveller,
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: Traveller,
                    newItem: Traveller
                ): Boolean = oldItem.name == newItem.name &&
                        oldItem.avatar == newItem.avatar &&
                        oldItem.id == newItem.id
            }
    }

    var onTravellerClickListener: ((traveller: Traveller) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravellerViewHolder {
        val binding: ItemTravellerBinding = ItemTravellerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TravellerViewHolder(binding)
    }

    override fun onBindViewHolder(holderTraveller: TravellerViewHolder, position: Int) {
        val currentItem: Traveller? = getItem(position)
        if (currentItem != null) {
            holderTraveller.bind(currentItem)
        }
    }

    inner class TravellerViewHolder(private val binding: ItemTravellerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cvTraveller.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_ID.toInt()) {
                    getItem(position)?.let { traveller ->
                        onTravellerClickListener?.invoke(traveller)
                    }
                }
            }
        }

        fun bind(traveller: Traveller) {
            setSelectableItemBackground(this)
            binding.apply {
                Glide.with(itemView).load(traveller.avatar).into(ivAvatar)
                if (!traveller.isVisible) {
                    val tint: ColorStateList = ColorStateList.valueOf(
                        itemView.context.resources.getColor(
                            android.R.color.background_dark,
                            itemView.context.theme,
                        )
                    ).withAlpha(100)
                    @Suppress("UsePropertyAccessSyntax")
                    cvTraveller.setCardBackgroundColor(tint)
                    ivAvatar.alpha = 0.4f
                }
                tvName.text = traveller.name
            }
        }

        private fun setSelectableItemBackground(holderTraveller: TravellerViewHolder) {
            holderTraveller.itemView.context.theme.resolveAttribute(
                ua.turskyi.travelling.R.attr.selectableItemBackground,
                TypedValue(),
                true
            )
        }
    }
}
