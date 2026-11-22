package ua.turskyi.travelling.common.view.providers

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
import ua.turskyi.travelling.entities.VisitedCountryNode
import ua.turskyi.travelling.utils.extensions.isHttpUrl
import ua.turskyi.travelling.utils.extensions.loadFlagFallback
import ua.turskyi.travelling.utils.extensions.showFlagText
import ua.turskyi.travelling.utils.getDisplayName

class CountryNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = 0

    override val layoutId: Int
        get() = R.layout.item_list_country

    var onImageClickListener: ((data: VisitedCountryNode) -> Unit)? = null
    var onTextClickListener: ((data: VisitedCountryNode) -> Unit)? = null
    var onLongLickListener: ((data: VisitedCountryNode) -> Unit)? = null

    override fun convert(
        helper: BaseViewHolder,
        item: BaseNode
    ) {
        val visitedCountryNode: VisitedCountryNode = item as VisitedCountryNode
        helper.getView<TextView>(R.id.tv_country).setOnLongClickListener {
            onLongLickListener?.invoke(visitedCountryNode)
            true
        }
        showPicturesInSVG(item, helper)
        helper.setText(R.id.tv_country, visitedCountryNode.getDisplayName(context))
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.iv_flag))
        helper.getView<ImageView>(R.id.iv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountryNode)
        }
        setSelectableBorderLessFor(helper.getView<WebView>(R.id.wv_flag))
        helper.getView<WebView>(R.id.wv_flag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountryNode)
        }
        setSelectableBackgroundFor(helper.getView<TextView>(R.id.tv_country))
        helper.getView<TextView>(R.id.tv_country).setOnClickListener {
            onTextClickListener?.invoke(visitedCountryNode)
        }
        if (item.childNode.isEmpty()) {
            helper.setVisible(R.id.iv_more, false)
        } else {
            if (visitedCountryNode.isExpanded) {
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
            R.attr.selectableItemBackgroundBorderless,
            outValue,
            true,
        )
        view.setBackgroundResource(outValue.resourceId)
    }

    private fun showPicturesInSVG(
        visitedCountryNode: VisitedCountryNode,
        holder: BaseViewHolder
    ) {
        val flagFallback = holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback)
        if (!visitedCountryNode.flag.isHttpUrl()) {
            holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = GONE
            holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
            flagFallback.apply {
                showFlagText(visitedCountryNode.flag)
                setOnClickListener { onImageClickListener?.invoke(visitedCountryNode) }
            }
            return
        }
        flagFallback.visibility = GONE
        holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = VISIBLE
        holder.itemView.findViewById<WebView>(R.id.wv_flag).visibility = GONE
        val uri: Uri = visitedCountryNode.flag.toUri()
        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() {
                    showPicturesInWebView(holder, visitedCountryNode)
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
        visitedCountryNode: VisitedCountryNode
    ) {
        holder.itemView.findViewById<ImageView>(R.id.iv_flag).visibility = GONE
        val wvFlag = holder.itemView.findViewById<WebView>(R.id.wv_flag)
        holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback).visibility = GONE
        wvFlag.loadFlagFallback(visitedCountryNode.flag)
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
