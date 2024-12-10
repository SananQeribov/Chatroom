package com.legalist.newchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.legalist.newchat.databinding.RecyclerListRowBinding

class RecyclerViewAdapter(private val chatMessages: List<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerListRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        holder.bind(chatMessage)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class MyViewHolder(private val binding: RecyclerListRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: String) {
            binding.recyclerViewTextView.text = chatMessage
        }
    }
}
