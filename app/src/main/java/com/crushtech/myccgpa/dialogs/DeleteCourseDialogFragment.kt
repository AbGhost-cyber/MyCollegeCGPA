package com.crushtech.myccgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.data.local.entities.Courses
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class DeleteCourseDialogFragment : DialogFragment() {
    private var positiveListener: ((Boolean) -> Unit)? = null

    fun setPositiveListener(listener: (Boolean) -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val courses = arguments?.getSerializable("courses") as Courses?
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Course?")
            .setMessage("Do you wish to delete ${courses?.courseName}")
            .setBackground(
                ResourcesCompat.getDrawable(
                    resources, R.drawable.dialog_bg, null
                )
            )
            .setPositiveButton("Yes") { _, _ ->
                positiveListener?.let { delete ->
                    delete(true)
                }
            }

            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}