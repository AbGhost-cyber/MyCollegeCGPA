package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.others_layout.*


class AboutAppDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val aboutAppParent = LayoutInflater.from(requireContext()).inflate(
            R.layout.about_app_layout,
            OthersContainer,
            false
        ) as ConstraintLayout

        aboutAppParent.findViewById<MaterialButton>(R.id.close_about_app).setOnClickListener {
            dialog?.dismiss()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About My College CGPA / How to use")
            .setMessage("LaraPass is a self-hosted personal password manager build with highest level of security, ease-of-use and data ownership in mind. LaraPass comes with a plethora of features listed below and allows you to securely store your sensitive account login information (like username, password, etc) for services such as Banks, Emails, Social Media, etc in an encrypted form on your own server. ")
            .setView(aboutAppParent)
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )

            .create()
    }
}