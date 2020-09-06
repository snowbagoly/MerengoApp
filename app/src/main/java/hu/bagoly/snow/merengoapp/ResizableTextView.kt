package hu.bagoly.snow.merengoapp

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.PreferenceManager


class ResizableTextView : androidx.appcompat.widget.AppCompatTextView, SharedPreferences.OnSharedPreferenceChangeListener {
    private val fontSizeDiff: Int

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        fontSizeDiff = attrs?.getAttributeIntValue(null, "fontSizeDiff", 0) ?: 0
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, android.R.attr.textViewStyle)

    constructor(context: Context?) : this(context, null)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "font_size") { // redraw if font size changed
            init(context)
            invalidate()
        }
    }
    private fun init(context: Context?) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this)
        val fontSize = PreferenceManager.getDefaultSharedPreferences(context).getInt("font_size", 12) + fontSizeDiff
        if (!isInEditMode) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        }
    }
}