package com.crushtech.mycollegecgpa.adapters

import android.graphics.Color.RED
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crushtech.mycollegecgpa.R
import com.crushtech.mycollegecgpa.adapters.SemesterAdapter.SemesterViewHolder
import com.crushtech.mycollegecgpa.data.local.entities.Semester
import com.crushtech.mycollegecgpa.utils.Constants.getHighestGrade
import kotlinx.android.synthetic.main.semester_items.view.*
import java.util.*
import kotlin.collections.ArrayList

class SemesterAdapter(private val authEmail: String) : Adapter<SemesterViewHolder>(), Filterable {
    private var filteredList = ArrayList<Semester>()
    lateinit var actualList: List<Semester>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        actualList = differ.currentList
        return SemesterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.semester_items, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        val semesters = differ.currentList[position]

        holder.itemView.apply {
            if (!semesters.owners.isNullOrEmpty()) {
                if (semesters.owners[0] == authEmail || semesters.owners == listOf(authEmail)) {
                    materialCardView.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.itemOwned
                        )
                    )
                } else {
                    materialCardView.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.itemNotOwnedColor
                        )
                    )
                }
            }
            semesterName.text = semesters.semesterName
            if (semesters.courses.isNullOrEmpty()) {
                coursesNames.text = context.getString(R.string.empty_list)
            } else {
                coursesNames.text = semesters.getThreeCoursesName()
            }

            gpa.text = semesters.getGPA().toString()
            if (!semesters.isSynced) {
                ivSynced.setImageResource(R.drawable.ic_cross)
                tvSynced1.text = context.getString(R.string.notSynced)
            } else {
                ivSynced.setImageResource(R.drawable.ic_check)
                tvSynced1.text = context.getString(R.string.isSynced)
            }

            circularProgressBar.apply {
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
            if (circularProgressBar.progress > 0 && circularProgressBar.progress < 3) {
                circularProgressBar.progressBarColorEnd = RED
                circularProgressBar.progressBarColorStart = RED
            } else if (circularProgressBar.progress == 0F) {
                //set progress to 4 to clear circular progressBarColorEnd
                // and progressBarColorStart color for that item
                circularProgressBar.progressMax = 4F
            } else {
                circularProgressBar.progressBarColorEnd = parseColor("#64DD17")
                circularProgressBar.progressBarColorStart = parseColor("#64DD17")
            }

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(semesters)

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


    inner class SemesterViewHolder(itemView: View) : ViewHolder(itemView)


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraints: CharSequence?): FilterResults {
                val charSearch = constraints.toString()

                if (charSearch.isEmpty()) {
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

