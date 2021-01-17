package com.crushtech.mycollegecgpa.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.crushtech.mycollegecgpa.data.local.entities.GradeSimplified
import com.crushtech.mycollegecgpa.databinding.EditWeightDialogBinding
import com.crushtech.mycollegecgpa.utils.Constants.WEIGHT_MAX
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditWeightDialogFragment : DialogFragment() {
    private var positiveListener: ((Boolean, GradeSimplified) -> Unit)? = null
    private lateinit var binding: EditWeightDialogBinding

    fun setPositiveListener(listener: (Boolean, GradeSimplified) -> Unit) {
        positiveListener = listener

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditWeightDialogBinding.inflate(
            LayoutInflater.from(context), null,
            false
        )

        val editWeightDialog = Dialog(requireContext())
        editWeightDialog.setContentView(binding.root)
        val simplifiedGrade = arguments?.getSerializable("GradeSimplified") as GradeSimplified?
        binding.apply {
            simplifiedGrade?.let { gs ->
                val currentWeightText = "Current Weight for ${gs.name} is ${gs.gradePoint}"
                currentWeightTv.text = currentWeightText

                val newWeight = newWeightEditText

                updateWeightBtn.setOnClickListener {
                    if (TextUtils.isEmpty(newWeight.text.toString())) {
                        return@setOnClickListener
                    }

                    positiveListener?.let { updateWeightWithError ->
                        val newGrade = GradeSimplified(gs.name, newWeight.text.toString().toFloat())
                        if (newGrade.gradePoint > WEIGHT_MAX) {
                            updateWeightWithError(true, newGrade)
                        } else {
                            updateWeightWithError(false, newGrade)
                        }
                        editWeightDialog.cancel()
                    }
                }

                cancelWeightBtn.setOnClickListener {
                    editWeightDialog.cancel()
                }
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