package ua.turskyi.travelling.features.home.view.adapter.providers

import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import kotlinx.android.synthetic.main.list_item_country.view.*
import ua.turskyi.travelling.R
import ua.turskyi.travelling.models.VisitedCountry

class CountryNodeProvider : BaseNodeProvider() {
    override val itemViewType: Int
        get() = 0

    override val layoutId: Int
        get() = R.layout.list_item_country

    var onImageClickListener: ((data: VisitedCountry) -> Unit)? = null
    var onTextClickListener: ((data: VisitedCountry) -> Unit)? = null
    var onLongLickListener: ((data: VisitedCountry) -> Unit)? = null

    override fun convert(
        helper: BaseViewHolder,
        item: BaseNode
    ) {
        val visitedCountry: VisitedCountry = item as VisitedCountry
        helper.getView<TextView>(R.id.tvCountry).setOnLongClickListener{
            onLongLickListener?.invoke(visitedCountry)
            true
        }
        showPicturesInSVG(item, helper)
        helper.setText(R.id.tvCountry, visitedCountry.title)
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.ivFlag))
        helper.getView<ImageView>(R.id.ivFlag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountry)
        }
        setSelectableBorderLessFor(helper.getView<WebView>(R.id.wvFlag))
        helper.getView<WebView>(R.id.wvFlag).setOnClickListener {
            onImageClickListener?.invoke(visitedCountry)
        }
        setSelectableBackgroundFor(helper.getView<TextView>(R.id.tvCountry))
        helper.getView<TextView>(R.id.tvCountry).setOnClickListener {
            onTextClickListener?.invoke(visitedCountry)
        }
        if (!item.childNode.isNullOrEmpty()) {
            if (visitedCountry.isExpanded) {
                    helper.setImageResource(
                        R.id.ivMore,
                        R.drawable.ic_arrow_expandable_up
                    )
                } else {
                    helper.setImageResource(
                        R.id.ivMore,
                        R.drawable.ic_arrow_expandable_down
                    )
                }
            helper.setVisible(R.id.ivMore, true)
        } else {
            helper.setVisible(R.id.ivMore, false)
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
        val uri: Uri = Uri.parse(visitedCountry.img)
        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() {
                    showPicturesInWebView(holder, visitedCountry)
                }
                override fun onResourceReady() {
                    holder.itemView.ivFlag.visibility = VISIBLE
                    holder.itemView.wvFlag.visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, holder.itemView.ivFlag)
    }
    private fun showPicturesInWebView(
        holder: BaseViewHolder,
        visitedCountry: VisitedCountry
    ) {
        holder.itemView.ivFlag.visibility = GONE
        holder.itemView.wvFlag.webViewClient = WebViewClient()
        holder.itemView.wvFlag.visibility = VISIBLE
        holder.itemView.wvFlag.setBackgroundColor(Color.TRANSPARENT)
        holder.itemView.wvFlag.setInitialScale(8)
        holder.itemView.wvFlag.loadUrl(visitedCountry.img)
    }

    override fun onClick(
        helper: BaseViewHolder,
        view: View,
        data: BaseNode,
        position: Int
    ) {
        setSelectableBorderLessFor(helper.getView<ImageView>(R.id.ivMore))
        getAdapter()?.expandOrCollapse(position)
    }
}