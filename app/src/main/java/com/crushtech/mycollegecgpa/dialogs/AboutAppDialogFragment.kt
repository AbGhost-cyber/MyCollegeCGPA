package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.extras_layout.*


class AboutAppDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val aboutAppParent = LayoutInflater.from(requireContext()).inflate(
            R.layout.about_app_layout,
            OthersContainer,
            false
        ) as ConstraintLayout
        val aboutAppDialog = Dialog(requireContext())
        aboutAppDialog.setContentView(aboutAppParent)
        aboutAppParent.findViewById<MaterialButton>(R.id.close_about_app).setOnClickListener {
            dialog?.dismiss()
        }
        aboutAppDialog.create()
        aboutAppDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return aboutAppDialog
    }
}