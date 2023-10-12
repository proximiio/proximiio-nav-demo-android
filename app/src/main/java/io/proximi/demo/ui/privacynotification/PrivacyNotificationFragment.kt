package io.proximi.demo.ui.privacynotification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import io.proximi.demo.R
import kotlinx.android.synthetic.main.fragment_privacy_notification.view.*

class PrivacyNotificationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_privacy_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().nextButton.setOnClickListener {
            requireView().findNavController().navigate(PrivacyNotificationFragmentDirections.actionPrivacyNotificationFragmentToPrivacyPolicyFragment())
        }
    }
}