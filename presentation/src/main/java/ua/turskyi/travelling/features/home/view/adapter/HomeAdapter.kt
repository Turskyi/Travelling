package ua.turskyi.travelling.features.home.view.adapter

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import kotlinx.android.synthetic.main.list_item_country.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.home.view.callback.OnVisitedCountryClickListener
import ua.turskyi.travelling.model.Country

class HomeAdapter : ListAdapter<Country, HomeAdapter.ViewHolder>(CountriesDiffCallback()) {

    companion object {
        private var onClick: OnVisitedCountryClickListener? = null
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Country]
     * has been updated.
     */
    class CountriesDiffCallback : DiffUtil.ItemCallback<Country>() {
        override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
            return oldItem.name == newItem.name && oldItem.flag == newItem.flag
        }
    }

    fun setData(newCountryList: List<Country>?) {
        _visibilityLoader.postValue(VISIBLE)
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                submitList(newCountryList)
                _visibilityLoader.postValue(GONE)
            }
        }
    }

    fun setOnItemClickListener(onClickListener: OnVisitedCountryClickListener) {
        onClick = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater
            .from(parent.context).inflate(R.layout.list_item_country, parent, false)
        return ViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCountry = getItem(position) as Country
        holder.tvCountry.text = currentCountry.name
        showPicturesInSVG(currentCountry, holder)
    }

    private fun showPicturesInSVG(
        country: Country,
        holder: ViewHolder
    ) {
        val uri: Uri = Uri.parse(country.flag)

        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() {
                    showPicturesInWebView(holder, country)
                }

                override fun onResourceReady() {
                    holder.ivFlag.visibility = VISIBLE
                    holder.wvFlag.visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, holder.ivFlag)
    }

    private fun showPicturesInWebView(
        holder: ViewHolder,
        country: Country
    ) {
        holder.ivFlag.visibility = GONE
        holder.wvFlag.webViewClient = WebViewClient()
        holder.wvFlag.visibility = VISIBLE
        holder.wvFlag.setBackgroundColor(Color.TRANSPARENT)
        holder.wvFlag.setInitialScale(8)
        holder.wvFlag.loadUrl(country.flag)
    }

   inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnClickListener,
        OnLongClickListener {
       val tvCountry: TextView = itemView.tvCountry
       val ivFlag: ImageView = itemView.ivFlag
       val wvFlag: WebView = itemView.wvFlag
        init {
            itemView.setOnLongClickListener(this)
            itemView.ivFlag.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onClick?.onItemClick(getItem(layoutPosition))
        }

        override fun onLongClick(v: View): Boolean {
            onClick?.onItemLongClick(getItem(layoutPosition))
            return true
        }
    }
}

