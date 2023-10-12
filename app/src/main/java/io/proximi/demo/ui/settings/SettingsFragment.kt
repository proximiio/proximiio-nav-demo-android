package io.proximi.demo.ui.settings

import android.os.Bundle
import android.view.*
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.proximi.demo.ProximiioHelper
import io.proximi.demo.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_close, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return super.onCreateView(inflater, container, savedInstanceState)!!.apply {
            // TODO: 15.1.2021 improve?
            setBackgroundColor(resources.getColor(R.color.primary, requireContext().theme))
        }
    }

    override fun onStop() {
        ProximiioHelper.updateMapboxSettings()
        super.onStop()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
//        findPreference<Preference>("about")!!.setOnPreferenceClickListener {
//            requireView().findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAboutFragment())
//            return@setOnPreferenceClickListener true
//        }
        findPreference<Preference>("policy")!!.setOnPreferenceClickListener {
            requireView().findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToPrivacyPolicyFragment())
            return@setOnPreferenceClickListener true
        }
    }
}