package com.example.utilityapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.utilityapp.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.VH>(Diff) {

    class VH(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = getItem(position)
        holder.binding.title.text = note.title.ifBlank { "(untitled)" }
        holder.binding.body.text = note.body
        holder.binding.root.setOnClickListener { onClick(note) }
        holder.binding.root.setOnLongClickListener {
            onLongClick(note)
            true
        }
    }

    object Diff : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(a: Note, b: Note) = a.id == b.id
        override fun areContentsTheSame(a: Note, b: Note) = a == b
    }
}
