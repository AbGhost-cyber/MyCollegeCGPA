package com.crushtech.myccga.adapters

import android.graphics.Color.RED
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crushtech.myccga.R
import com.crushtech.myccga.adapters.SemesterAdapter.SemesterViewHolder
import com.crushtech.myccga.data.local.entities.Semester
import com.crushtech.myccga.databinding.SemesterItemsBinding
import com.crushtech.myccga.utils.Constants.getHighestGrade
import java.util.*
import kotlin.collections.ArrayList

class SemesterAdapter(private val authEmail: String) : Adapter<SemesterViewHolder>(), Filterable {
    private var filteredList = ArrayList<Semester>()
    lateinit var actualList: List<Semester>
    var binding: SemesterItemsBinding? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        actualList = differ.currentList

        binding = SemesterItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return SemesterViewHolder(binding!!)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semesters = differ.currentList[position]

        holder.itemView.apply {
            binding?.let { bind ->
                if (!semesters.owners.isNullOrEmpty()) {
                    if (semesters.owners[0] == authEmail || semesters.owners == listOf(authEmail)) {
                        bind.materialCardView.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.itemOwned
                            )
                        )
                    } else {
                        bind.materialCardView.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.itemNotOwnedColor
                            )
                        )
                    }
                }
                bind.semesterName.text = semesters.semesterName
                if (semesters.courses.isNullOrEmpty()) {
                    bind.coursesNames.text = context.getString(R.string.empty_list)
                } else {
                    bind.coursesNames.text = semesters.getThreeCoursesName()
                }

                bind.gpa.text = semesters.getGPA().toString()
                if (!semesters.isSynced) {
                    bind.ivSynced.setImageResource(R.drawable.ic_cross)
                    bind.tvSynced1.text = context.getString(R.string.notSynced)
                } else {
                    bind.ivSynced.setImageResource(R.drawable.ic_check)
                    bind.tvSynced1.text = context.getString(R.string.isSynced)
                }

                bind.circularProgressBar.apply {
                    semesters.courses.forEach {
                        if (it.gradesPoints.isNotEmpty()) {
                            val grade = it.gradesPoints[0]
                            val max = getHighestGrade(grade)
                            max?.let {
                                progressMax = max
                            }
                        }
                    }

                    progress = semesters.getGPA().toFloat()
                    setProgressWithAnimation(progress, 5000)
                    backgroundProgressBarWidth = 3f
                }
                if (bind.circularProgressBar.progress > 0 && bind.circularProgressBar.progress < 3) {
                    bind.circularProgressBar.progressBarColorEnd = RED
                    bind.circularProgressBar.progressBarColorStart = RED
                } else if (bind.circularProgressBar.progress == 0F) {
                    //set progress to 4 to clear circular progressBarColorEnd
                    // and progressBarColorStart color for that item
                    bind.circularProgressBar.progressMax = 4F
                } else {
                    bind.circularProgressBar.progressBarColorEnd = parseColor("#64DD17")
                    bind.circularProgressBar.progressBarColorStart = parseColor("#64DD17")
                }

                setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(semesters)

                    }
                }
            }

        }
    }

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<Semester>() {
        override fun areItemsTheSame(oldItem: Semester, newItem: Semester): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Semester, newItem: Semester): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }


    private var onItemClickListener: ((Semester) -> Unit)? = null

    fun setOnItemClickListener(listener: (Semester) -> Unit) {
        this.onItemClickListener = listener
    }


    val differ = AsyncListDiffer(this, diffUtilCallBack)


    inner class SemesterViewHolder(itemView: SemesterItemsBinding) : ViewHolder(itemView.root)


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraints: CharSequence?): FilterResults {
                val charSearch = constraints.toString()

                if (charSearch.isEmpty() && this@SemesterAdapter::actualList.isInitialized) {
                    filteredList.addAll(actualList)
                } else {
                    val results = ArrayList<Semester>()
                    actualList.onEach { semester ->
                        if (semester.semesterName.toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT)) ||
                            semester.getThreeCoursesName().toLowerCase(Locale.ROOT)
                                .contains(charSearch.toLowerCase(Locale.ROOT))
                        ) {
                            results.add(semester)
                        }
                    }
                    filteredList = results
                    differ.submitList(results)
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(p0: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    filteredList = it as ArrayList<Semester>
                }
            }

        }
    }

}

