package io.proximi.demo.ui.privacypolicy

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import io.proximi.demo.MainActivity
import io.proximi.demo.R
import io.proximi.demo.ui.MainViewModel
import io.proximi.demo.ui.Preferences
import kotlinx.android.synthetic.main.fragment_privacy_policy.view.*

class PrivacyPolicyFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = Preferences(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        view.policy_2_body.text = getHtmlString(R.string.privacy_policy_2_body)
        view.privacyPolicyWebView.loadStringResource(R.string.privacy_policy_html)

        val policyAccepted = preferences.getPrivacyPolicyAccepted()
        if (policyAccepted) {
            requireView().policyButtonGroup.visibility = View.GONE
            requireView().visitorIdTextView.visibility = View.VISIBLE
            viewModel.visitorId.observe(viewLifecycleOwner) { visitorId ->
                requireView().visitorIdTextView.text = getString(R.string.privacy_policy_visitor_id, visitorId)
            }
        } else {
            requireView().policyButtonGroup.visibility = View.VISIBLE
            requireView().visitorIdTextView.visibility = View.GONE
        }
        requireView().acceptPolicyButton.setOnClickListener {
            preferences.setPrivacyPolicyAccepted(true)
            (requireActivity() as MainActivity).onPrivacyPolicyAccepted()
            requireView().findNavController().navigate(PrivacyPolicyFragmentDirections.actionPrivacyPolicyFragmentToMainFragment())
        }
        requireView().declinePolicyButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun getHtmlString(id: Int): Spanned {
        val html = getString(id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html, null, LiTagHandler())
        }
    }
}