package io.proximi.demo.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import io.proximi.demo.R
import kotlinx.android.synthetic.main.dialog_not_covered.view.*

class NotCoveredDialogFragment : DialogFragment() {

    private var onAccept: ((customDialogFragmentLocation: NotCoveredDialogFragment) -> Unit)? = null

    companion object {
        @JvmStatic
        fun newInstance(
                onAccept: (customDialogFragmentLocation: NotCoveredDialogFragment) -> Unit
        ) = NotCoveredDialogFragment().apply {
            this.onAccept = onAccept
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_not_covered, container, false)
        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.requestFeature(Window.FEATURE_NO_TITLE)
        }
        view.acceptButton.setOnClickListener {
            dismiss()
            onAccept?.invoke(this)
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
        }
    }

}
