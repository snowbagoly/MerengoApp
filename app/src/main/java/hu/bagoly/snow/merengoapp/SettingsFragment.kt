package hu.bagoly.snow.merengoapp

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import hu.bagoly.snow.merengoapp.custompreference.LimitedNumberPreference
import hu.bagoly.snow.merengoapp.custompreference.LimitedNumberPreferenceDialog


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag("font_size") != null) {
            return
        }
        if (preference is LimitedNumberPreference) {
            val f: DialogFragment =
                LimitedNumberPreferenceDialog(preference)
            f.setTargetFragment(this, 0)
            f.show(parentFragmentManager, preference.getKey())
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}