package hu.bagoly.snow.merengoapp.custompreference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.preference.DialogPreference


class LimitedNumberPreference(context: Context?, attrs: AttributeSet) :
    DialogPreference(context, attrs) {

    val minValue: Int = attrs.getAttributeIntValue(null, "minValue", 0)
    val maxValue: Int = attrs.getAttributeIntValue(null, "maxValue", 100)

    var value: Int = minValue
        get() {
            return field
        }
        set(newValue) {
            if (newValue == field) return
            if (newValue < minValue || newValue > maxValue) return  // Ignoring setting to invalid values
            field = newValue
            if (shouldPersist()) {
                persistInt(field)
                summary = "" + field
            }
            notifyChanged()
        }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, minValue)
    }

    override fun onSetInitialValue(@Nullable defaultValue: Any?) {
        value = if (defaultValue != null) defaultValue as Int else getPersistedInt(minValue)
    }

    init {
        value = if (isPersistent) getPersistedInt(minValue) else minValue
        summary = "" + value
    }
}