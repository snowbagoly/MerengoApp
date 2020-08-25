package hu.bagoly.snow.merengoapp

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.PreferenceManager


class ResizableTextView : androidx.appcompat.widget.AppCompatTextView, SharedPreferences.OnSharedPreferenceChangeListener {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?) : super(context) {
        init(context)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "font_size") { // redraw if font size changed
            init(context)
            invalidate()
        }
    }
    private fun init(context: Context?) {
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this)
        val fontSize = PreferenceManager.getDefaultSharedPreferences(context).getInt("font_size", 12)
        if (!isInEditMode) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        }
    }
}