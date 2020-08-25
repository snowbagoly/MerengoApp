package hu.bagoly.snow.merengoapp.custompreference

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.preference.PreferenceDialogFragmentCompat

class LimitedNumberPreferenceDialog(private val preference: LimitedNumberPreference) :
    PreferenceDialogFragmentCompat() {
    private var picker: NumberPicker? = null
    override fun onPause() {
        super.onPause()
        dismiss()
    }

    override fun onCreateDialogView(context: Context?): View {
        if (picker != null) return picker!!

        picker = NumberPicker(context)
        picker!!.minValue = preference.minValue
        picker!!.maxValue = preference.maxValue
        picker!!.value = preference.value
        return picker!!
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && picker != null) {
            if (preference.callChangeListener(picker!!.value)) {
                preference.value = picker!!.value
            }
        }
    }

    init {
        val b = Bundle()
        b.putString(ARG_KEY, preference.key)
        arguments = b
    }
}