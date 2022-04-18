@file:Suppress("unused")

package ua.turskyi.travelling.widgets

import android.content.Context
import android.graphics.PorterDuff
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.Transition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import ua.turskyi.travelling.R

/**
 * An expandable and customizable widget that provides a user interface for the user to enter a search query.
 */
class ExpandableSearchBar(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    companion object {
        const val BUTTON_SEARCH = 1
        const val BUTTON_BACK = 2
        const val BUTTON_CLEAR = 3
    }

    /**
     * The drawable resource id for the search icon.
     */
    private var searchIcon: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_search_icon).setImageResource(field)
        }

    /**
     * The drawable resource id for the back icon.
     */
    private var backIcon: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_back_icon).setImageResource(field)
        }

    /**
     * The drawable resource id for the clear icon.
     */
    private var clearIcon: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_clear_icon).setImageResource(field)
        }

    /**
     * Indicates whether the shape of the [ExpandableSearchBar] is rounded or not.
     */
    private var isRounded: Boolean = false
        /**
         * Set as capsule-shaped search bar when [isRounded] is true.
         * Only works on SDK V21+ due to odd behavior on lower
         */
        set(value) {
            field = value
            if (isRounded)
                findViewById<CardView>(R.id.search_bar_card_container).radius =
                    resources.getDimension(R.dimen.corner_radius_rounded)
            else
                findViewById<CardView>(R.id.search_bar_card_container).radius =
                    resources.getDimension(R.dimen.corner_radius_default)
        }

    /**
     * The background color of the [ExpandableSearchBar].
     */
    private var searchBarBackgroundColor: Int = 0
        set(value) {
            field = value
            findViewById<CardView>(R.id.search_bar_card_container).setCardBackgroundColor(field)
        }

    /**
     * The background color of the [ExpandableSearchBar] when is focused.
     */
    private var searchBarBackgroundColorFocused: Int

    /**
     * The input text of the [ExpandableSearchBar].
     */
    var text: String? = null
        set(value) {
            field = value
            findViewById<EditText>(R.id.search_bar_input_text).setText(field)
        }
        get() {
            field = findViewById<EditText>(R.id.search_bar_input_text).text.toString()
            return field
        }

    /**
     * Hint text to display when the input text is empty.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var hint: String? = null
        set(value) {
            field = value
            findViewById<EditText>(R.id.search_bar_input_text).hint = field
        }

    /**
     * The color for the input text.
     */
    private var textColor: Int = 0
        set(value) {
            field = value
            findViewById<EditText>(R.id.search_bar_input_text).setTextColor(field)
        }

    /**
     * The color for the hint text.
     */
    private var hintColor: Int = 0
        set(value) {
            field = value
            findViewById<EditText>(R.id.search_bar_input_text).setHintTextColor(field)
        }

    /**
     * The tinting color for the search icon.
     */
    private var searchIconTint: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_search_icon).setColorFilter(
                value,
                PorterDuff.Mode.SRC_IN
            )
        }

    /**
     * The tinting color for the back icon.
     */
    private var backIconTint: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_back_icon).setColorFilter(
                value,
                PorterDuff.Mode.SRC_IN
            )
        }

    /**
     * The tinting color for the clear icon.
     */
    private var clearIconTint: Int = 0
        set(value) {
            field = value
            findViewById<ImageView>(R.id.search_bar_clear_icon).setColorFilter(
                value,
                PorterDuff.Mode.SRC_IN
            )
        }

    /**
     * The listener for search actions.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var onSearchActionListener: OnSearchActionListener? = null


    /**
     * The transition for expand/collapse animation.
     */
    private var transition: Transition = ChangeBounds().apply {
        duration = 300
        addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                if (isExpanded) {
                    findViewById<EditText>(R.id.search_bar_input_text).requestFocus()
                    showKeyboard(findViewById<EditText>(R.id.search_bar_input_text))
                } else if (!isAutoCollapse || hasManuallyCollapsed) {
                    hideKeyboard(findViewById<EditText>(R.id.search_bar_input_text))
                }
            }

            // Unused functions.
            override fun onTransitionResume(transition: Transition?): Unit = Unit

            override fun onTransitionPause(transition: Transition?): Unit = Unit
            override fun onTransitionCancel(transition: Transition?): Unit = Unit
            override fun onTransitionStart(transition: Transition?): Unit = Unit
        })
    }

    private var isAutoCollapse: Boolean

    private var hasManuallyCollapsed: Boolean = false

    /**
     * Indicates whether the [ExpandableSearchBar] is expanded or not. It is used to switch search
     * bar states.
     */
    var isExpanded: Boolean = false
        set(value) {
            field = value
            if (field) {
                expand()
                onSearchActionListener?.onSearchStateChanged(true)
            } else {
                collapse()
                onSearchActionListener?.onSearchStateChanged(false)
            }
        }

    private fun collapse() {
        TransitionManager.beginDelayedTransition(
            findViewById<CardView>(R.id.search_bar_card_container),
            transition
        )
        findViewById<EditText>(R.id.search_bar_input_text).text.clear()
        findViewById<CardView>(R.id.search_bar_card_container).layoutParams.width =
            LayoutParams.WRAP_CONTENT
        findViewById<CardView>(R.id.search_bar_card_container).setCardBackgroundColor(
            searchBarBackgroundColor
        )
        findViewById<ImageView>(R.id.search_bar_search_icon).visibility = View.VISIBLE
        findViewById<ConstraintLayout>(R.id.search_bar_input_container).visibility = View.GONE
    }

    private fun expand() {
        TransitionManager.beginDelayedTransition(
            findViewById<CardView>(R.id.search_bar_card_container),
            transition
        )
        findViewById<CardView>(R.id.search_bar_card_container).layoutParams.width = 0
        findViewById<CardView>(R.id.search_bar_card_container).setCardBackgroundColor(
            searchBarBackgroundColorFocused
        )
        findViewById<ImageView>(R.id.search_bar_search_icon).visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.search_bar_input_container).visibility = View.VISIBLE
    }

    /**
     * The elevation of the [ExpandableSearchBar].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var searchBarElevation: Float = 0f
        set(value) {
            field = value
            findViewById<CardView>(R.id.search_bar_card_container).cardElevation = field
        }

    /**
     *
     * The corner radius of the [ExpandableSearchBar].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var searchBarCornerRadius: Float = context.resources.getDimension(R.dimen.corner_radius_default)
        set(value) {
            field = value
            if (!isRounded)
                findViewById<CardView>(R.id.search_bar_card_container).radius = field
        }

    init {
        // Inflate the search bar layout.
        inflate(context, R.layout.search_bar, this)

        // Get the array of the attributes.
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableSearchBar)

        // Get base attributes from array.
        isRounded = typedArray.getBoolean(R.styleable.ExpandableSearchBar_isRounded, false)
        isAutoCollapse = typedArray.getBoolean(R.styleable.ExpandableSearchBar_autoCollapse, false)
        searchBarCornerRadius = typedArray.getDimension(
            R.styleable.ExpandableSearchBar_searchBarCornerRadius,
            context.resources.getDimension(R.dimen.corner_radius_default)
        )
        searchBarBackgroundColor = typedArray.getColor(
            R.styleable.ExpandableSearchBar_searchBarBackgroundColor,
            ContextCompat.getColor(context, android.R.color.white)
        )
        searchBarBackgroundColorFocused = typedArray.getColor(
            R.styleable.ExpandableSearchBar_searchBarBackgroundColorFocused,
            searchBarBackgroundColor
        )
        searchBarElevation =
            typedArray.getDimension(R.styleable.ExpandableSearchBar_searchBarElevation, 0f)

        // Get icon related attributes from array.
        searchIcon = typedArray.getResourceId(
            R.styleable.ExpandableSearchBar_searchIconDrawable,
            R.drawable.ic_search
        )
        backIcon = typedArray.getResourceId(
            R.styleable.ExpandableSearchBar_backIconDrawable,
            R.drawable.ic_search
        )
        clearIcon = typedArray.getResourceId(
            R.styleable.ExpandableSearchBar_clearIconDrawable,
            R.drawable.ic_close
        )
        searchIconTint = typedArray.getColor(
            R.styleable.ExpandableSearchBar_searchIconTint,
            ContextCompat.getColor(context, R.color.searchBarSearchIconTintColor)
        )
        backIconTint = typedArray.getColor(
            R.styleable.ExpandableSearchBar_backIconTint,
            ContextCompat.getColor(context, R.color.searchBarBackIconTintColor)
        )
        clearIconTint = typedArray.getColor(
            R.styleable.ExpandableSearchBar_clearIconTint,
            ContextCompat.getColor(context, R.color.searchBarClearIconTintColor)
        )

        // Get text related attributes from array.
        hint = typedArray.getString(R.styleable.ExpandableSearchBar_hint)
        textColor = typedArray.getColor(
            R.styleable.ExpandableSearchBar_textColor,
            ContextCompat.getColor(context, R.color.searchBarTextColor)
        )
        hintColor = typedArray.getColor(
            R.styleable.ExpandableSearchBar_hintColor,
            ContextCompat.getColor(context, R.color.searchBarHintColor)
        )

        // Recycle array to be re-used later or not.
        typedArray.recycle()

        setupListeners()

    }

    /**
     * Set all listeners for [ExpandableSearchBar] views.
     */
    private fun setupListeners() {
        findViewById<CardView>(R.id.search_bar_card_container).setOnClickListener(this)
        findViewById<ImageView>(R.id.search_bar_clear_icon).setOnClickListener(this)
        findViewById<ImageView>(R.id.search_bar_back_icon).setOnClickListener(this)
        findViewById<ImageView>(R.id.search_bar_search_icon).setOnClickListener(this)
        findViewById<EditText>(R.id.search_bar_input_text).setOnKeyListener(this)
        findViewById<EditText>(R.id.search_bar_input_text).onFocusChangeListener = this
    }

    /**
     * Add text watcher to search_bar's EditText
     *
     * @param textWatcher textWatcher to add
     */
    fun addTextChangeListener(textWatcher: TextWatcher) {
        findViewById<EditText>(R.id.search_bar_input_text).addTextChangedListener(textWatcher)
    }

    override fun onClick(view: View) {
        when (view) {
            findViewById<ImageView>(R.id.search_bar_search_icon) -> {
                if (!isExpanded) {
                    isExpanded = true
                    hasManuallyCollapsed = false
                }
                onSearchActionListener?.onButtonClicked(BUTTON_SEARCH)
            }
            findViewById<ImageView>(R.id.search_bar_back_icon) -> {
                isExpanded = false
                hasManuallyCollapsed = true
                onSearchActionListener?.onButtonClicked(BUTTON_BACK)
            }
            findViewById<ImageView>(R.id.search_bar_clear_icon) -> {
                findViewById<EditText>(R.id.search_bar_input_text).text.clear()
                onSearchActionListener?.onButtonClicked(BUTTON_CLEAR)
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            onSearchActionListener?.onSearchConfirmed(text)
            return true
        }
        return false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (!hasFocus && isAutoCollapse)
            isExpanded = false
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && isExpanded) {
            isExpanded = false
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Interface definition for ExpandableSearchBar callbacks.
     */
    interface OnSearchActionListener {
        /**
         * Invoked when SearchBar opened or closed
         *
         * @param enabled state
         */
        fun onSearchStateChanged(enabled: Boolean)

        /**
         * Invoked when search confirmed and "search" button is clicked on the soft keyboard
         *
         * @param text search input
         */
        fun onSearchConfirmed(text: String?)

        /**
         * Invoked when "speech" or "navigation" buttons clicked.
         *
         * @param buttonCode [.BUTTON_NAVIGATION], [.BUTTON_SPEECH] or [.BUTTON_BACK] will be passed
         */
        fun onButtonClicked(buttonCode: Int)
    }
}
