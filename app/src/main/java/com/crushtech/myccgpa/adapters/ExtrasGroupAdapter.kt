package com.crushtech.myccgpa.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.crushtech.myccgpa.databinding.ExtrasGroupItemsBinding
import com.crushtech.myccgpa.ui.fragments.extras.OthersFragment


data class Group(var groupTitle: String)
class GroupAdapter(val extraFragment: OthersFragment) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {
    var binding: ExtrasGroupItemsBinding? = null
    var accountingItemAdapter: ExtrasAdapter = ExtrasAdapter()
    var supportItemAdapter: ExtrasAdapter = ExtrasAdapter()
    var legalItemAdapter: ExtrasAdapter = ExtrasAdapter()

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
        }

    }

    private fun setAccountList(recyclerView: RecyclerView) {
        accountingItemAdapter.setOnItemClickListener {

        }
        recyclerViewProps(recyclerView)
    }

    private fun setLegalList(recyclerView: RecyclerView) {
        legalItemAdapter.setOnItemClickListener {

        }
        recyclerViewProps(recyclerView)
    }

    private fun setSupportList(recyclerView: RecyclerView) {
        supportItemAdapter.setOnItemClickListener {

        }
        recyclerViewProps(recyclerView)
    }

    private fun recyclerViewProps(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = true
            isSaveEnabled = true
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
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