package com.example.utilityapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.utilityapp.databinding.ActivityMainBinding
import com.example.utilityapp.databinding.DialogNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dao by lazy { AppDatabase.get(this).noteDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep content clear of the navigation bar (Android 15+ is edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        val adapter = NoteAdapter(
            onClick = { showNoteDialog(it) },
            onLongClick = { confirmDelete(it) }
        )
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter

        binding.fab.setOnClickListener { showNoteDialog(null) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.observeAll().collect { notes ->
                    adapter.submitList(notes)
                    binding.empty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showNoteDialog(note: Note?) {
        val dialogBinding = DialogNoteBinding.inflate(layoutInflater)
        dialogBinding.titleInput.setText(note?.title.orEmpty())
        dialogBinding.bodyInput.setText(note?.body.orEmpty())

        MaterialAlertDialogBuilder(this)
            .setTitle(if (note == null) "New note" else "Edit note")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val title = dialogBinding.titleInput.text.toString().trim()
                val body = dialogBinding.bodyInput.text.toString().trim()
                if (title.isEmpty() && body.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    dao.upsert(Note(id = note?.id ?: 0, title = title, body = body))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete note?")
            .setMessage(note.title.ifBlank { "This note" })
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { dao.delete(note) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
