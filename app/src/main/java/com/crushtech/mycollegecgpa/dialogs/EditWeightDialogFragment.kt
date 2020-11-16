package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.data.local.entities.GradeSimplified
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.course_list_layout.*

@AndroidEntryPoint
class EditWeightDialogFragment : DialogFragment() {
    private var positiveListener: ((GradeSimplified) -> Unit)? = null

    fun setPositiveListener(listener: (GradeSimplified) -> Unit) {
        positiveListener = listener

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editWeightLayout = LayoutInflater.from(requireContext()).inflate(
            R.layout.edit_weight_dialog,
            courseContainer,
            false
        ) as ConstraintLayout
        val editWeightDialog = Dialog(requireContext())
        editWeightDialog.setContentView(editWeightLayout)
        val simplifiedGrade = arguments?.getSerializable("GradeSimplified") as GradeSimplified?

        simplifiedGrade?.let { gs ->
            val currentWeightText = "Current Weight for ${gs.name} is ${gs.gradePoint}"
            val currentWeightTv = editWeightDialog.findViewById<TextView>(R.id.currentWeightTv)
            currentWeightTv.text = currentWeightText

            val newWeight = editWeightDialog.findViewById<TextInputEditText>(R.id.newWeightEditText)
            val updateWeightBtn =
                editWeightDialog.findViewById<MaterialButton>(R.id.updateWeightBtn)
            val cancelWeightBtn =
                editWeightDialog.findViewById<MaterialButton>(R.id.cancelWeightBtn)
            updateWeightBtn.setOnClickListener {
                if (TextUtils.isEmpty(newWeight.text.toString())) {
                    return@setOnClickListener
                }
                positiveListener?.let { updateWeight ->
                    val newGrade = GradeSimplified(gs.name, newWeight.text.toString().toFloat())
                    updateWeight(newGrade)
                    editWeightDialog.cancel()
                }
            }

            cancelWeightBtn.setOnClickListener {
                editWeightDialog.cancel()
            }
        }

        editWeightDialog.create()
        editWeightDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return editWeightDialog
    }

}