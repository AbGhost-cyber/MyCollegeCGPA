package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.crushtech.myccgpa.R
import com.crushtech.myccgpa.databinding.ExtrasGroupItemsBinding


data class Group(var groupTitle: String)
class GroupAdapter :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    var binding: ExtrasGroupItemsBinding? = null
    var accountItemAdapter: ExtrasAdapter = ExtrasAdapter()
    var supportItemAdapter: ExtrasAdapter = ExtrasAdapter()
    var legalItemAdapter: ExtrasAdapter = ExtrasAdapter()
    var logoutItemAdapter: ExtrasAdapter = ExtrasAdapter()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        binding = ExtrasGroupItemsBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return GroupViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = differ.currentList[position]
        holder.itemView.apply {
            binding!!.apply {
                groupTitle.text = group.groupTitle
                setList(groupRecyclerView, position)
            }

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun setList(recyclerView: RecyclerView, position: Int) {
        when (position) {
            0 -> setAccountList(recyclerView)
            1 -> setSupportList(recyclerView)
            2 -> setLegalList(recyclerView)
            3 -> setLogoutItemList(recyclerView)
        }
    }

    private fun setAccountList(recyclerView: RecyclerView) {
        setRecyclerViewProps(recyclerView, accountItemAdapter)
    }

    private fun setLegalList(recyclerView: RecyclerView) {
        setRecyclerViewProps(recyclerView, legalItemAdapter)
    }

    private fun setSupportList(recyclerView: RecyclerView) {
        setRecyclerViewProps(recyclerView, supportItemAdapter)
    }

    private fun setLogoutItemList(recyclerView: RecyclerView) {
        setRecyclerViewProps(recyclerView, logoutItemAdapter)
    }

    fun setRecyclerViewProps(
        recyclerView: RecyclerView,
        customAdapter: ExtrasAdapter
    ) {
        recyclerView.apply {
            adapter = customAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
            isSaveEnabled = true
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    DividerItemDecoration.VERTICAL
                ).also {
                    val divider = ContextCompat.getDrawable(
                        recyclerView.context, R.drawable.extra_items_divider
                    )
                    divider?.let { drawable ->
                        it.setDrawable(drawable)
                    }
                }
            )
        }
    }

    private val diffUtilCallback = object : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.groupTitle == newItem.groupTitle
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffUtilCallback)

    inner class GroupViewHolder(itemView: ExtrasGroupItemsBinding) :
        RecyclerView.ViewHolder(itemView.root)
}

