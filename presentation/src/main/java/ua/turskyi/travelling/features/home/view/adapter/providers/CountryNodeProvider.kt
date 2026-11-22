package ua.turskyi.travelling.features.home.view.adapter.providers

import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import ua.turskyi.travelling.R
import ua.turskyi.travelling.models.VisitedCountry
import ua.turskyi.travelling.utils.extensions.isHttpUrl
import ua.turskyi.travelling.utils.extensions.loadFlagFallback
import ua.turskyi.travelling.utils.extensions.showFlagText
import ua.turskyi.travelling.utils.getDisplayName

class CountryNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = 0

    override val layoutId: Int
        get() = R.layout.item_list_country

    var onImageClickListener: ((data: VisitedCountry) -> Unit)? = null
    var onTextClickListener: ((data: VisitedCountry) -> Unit)? = null
    var onLongLickListener: ((data: VisitedCountry) -> Unit)? = null

    override fun convert(
        helper: BaseViewHolder,
        item: BaseNode
    ) {
        val visitedCountry: VisitedCountry = item as VisitedCountry
        helper.getView<TextView>(R.id.tv_country).setOnLongClickListener{
            onLongLickListener?.invoke(visitedCountry)
            true
        }
        showPicturesInSVG(item, helper)
        helper.setText(R.id.tv_country, visitedCountry.getDisplayName(helper.itemView.context))
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.iv_flag))
        helper.getView<ImageView>(R.id.iv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountry)
        }
        setSelectableBorderLessFor(helper.getView<WebView>(R.id.wv_flag))
        helper.getView<WebView>(R.id.wv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountry)
        }
        setSelectableBackgroundFor(helper.getView<TextView>(R.id.tv_country))
        helper.getView<TextView>(R.id.tv_country).setOnClickListener {
            onTextClickListener?.invoke(visitedCountry)
        }
        if (item.childNode.isNotEmpty()) {
            if (visitedCountry.isExpanded) {
                    helper.setImageResource(
                        R.id.iv_more,
                        R.drawable.ic_arrow_expandable_up
                    )
                } else {
                    helper.setImageResource(
                        R.id.iv_more,
                        R.drawable.ic_arrow_expandable_down
                    )
                }
            helper.setVisible(R.id.iv_more, true)
        } else {
            helper.setVisible(R.id.iv_more, false)
        }
    }

    private fun setSelectableBackgroundFor(it: View) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
        it.setBackgroundResource(outValue.resourceId)
    }

    private fun setSelectableBorderLessFor(view: View) {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            R.attr.selectableItemBackgroundBorderless, outValue,
            true
        )
        view.setBackgroundResource(outValue.resourceId)
    }

    private fun showPicturesInSVG(
        visitedCountry: VisitedCountry,
        holder: BaseViewHolder
    ) {
        val flagFallback = holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback)
        if (!visitedCountry.img.isHttpUrl()) {
            holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = GONE
            holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
            flagFallback.apply {
                showFlagText(visitedCountry.img)
                setOnClickListener { onImageClickListener?.invoke(visitedCountry) }
            }
            return
        }
        flagFallback.visibility = GONE
        holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = VISIBLE
        holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
        val uri: Uri = visitedCountry.img.toUri()
        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() {
                    showPicturesInWebView(holder, visitedCountry)
                }
                override fun onResourceReady() {
                    holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = VISIBLE
                    holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, holder.itemView.findViewById(R.id.iv_flag))
    }
    private fun showPicturesInWebView(
        holder: BaseViewHolder,
        visitedCountry: VisitedCountry
    ) {
        holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = GONE
        holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback).visibility = GONE
        val wvFlag = holder.itemView.findViewById<WebView>(R.id.wv_flag)
        wvFlag.loadFlagFallback(visitedCountry.img)
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.iv_more))
        getAdapter()?.expandOrCollapse(position)
    }
}