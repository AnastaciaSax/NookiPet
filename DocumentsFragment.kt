package eng.nookipet.lab7.ui.documents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import eng.nookipet.lab7.AppDatabase
import eng.nookipet.lab7.Attachment
import eng.nookipet.lab7.AttachmentAdapter
import eng.nookipet.lab7.R
import eng.nookipet.lab7.data.repository.AttachmentRepository
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * Documents screen for one pet.
 * Real file picker via OpenDocument, metadata auto-fill, files copied
 * to app-private storage, tap to open with external viewer.
 */
class DocumentsFragment : Fragment() {

    private val viewModel: DocumentsViewModel by viewModels {
        viewModelFactory {
            initializer {
                val db = AppDatabase.getDatabase(requireContext())
                DocumentsViewModel(
                    petId = requireArguments().getLong(ARG_PET_ID),
                    repo = AttachmentRepository(db.attachmentDao())
                )
            }
        }
    }

    private lateinit var adapter: AttachmentAdapter

    /**
     * System file picker. Any file type. Launches from FAB click.
     */
    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) handlePickedFile(uri)
    }

    companion object {
        private const val ARG_PET_ID = "pet_id"
        private const val ARG_PET_NAME = "pet_name"

        fun newInstance(petId: Long, petName: String): DocumentsFragment {
            val f = DocumentsFragment()
            f.arguments = Bundle().apply {
                putLong(ARG_PET_ID, petId)
                putString(ARG_PET_NAME, petName)
            }
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_documents, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        val tvSub: TextView = view.findViewById(R.id.tvDocsSub)
        val rv: RecyclerView = view.findViewById(R.id.rvDocuments)
        val emptyState: View = view.findViewById(R.id.emptyState)
        val fab: FloatingActionButton = view.findViewById(R.id.fabAddDoc)

        tvSub.text = "For ${requireArguments().getString(ARG_PET_NAME).orEmpty()}"

        adapter = AttachmentAdapter(
            attachments = mutableListOf(),
            onItemClick = { doc -> showDocumentDetails(doc) },
            onItemLongClick = { doc, _ -> confirmDelete(doc) }
        )
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.documents.collect { list ->
                    adapter.updateData(list)
                    emptyState.isVisible = list.isEmpty()
                }
            }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Opens the system file picker — any MIME type ("*/*")
        fab.setOnClickListener { pickFileLauncher.launch(arrayOf("*/*")) }
    }

    // ──────────────────────────────────────────────────────────────
    // File picking & saving
    // ──────────────────────────────────────────────────────────────

    private fun handlePickedFile(uri: Uri) {
        val ctx = requireContext()
        val resolver = ctx.contentResolver

        var displayName = "document"
        var sizeBytes = 0L

        // Read name + size from the content provider
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
                if (sizeIndex >= 0) sizeBytes = cursor.getLong(sizeIndex)
            }
        }

        val sizeKb = (sizeBytes / 1024).toInt()

        // Copy to app's private storage so the file survives
        val destDir = File(ctx.filesDir, "documents")
        if (!destDir.exists()) destDir.mkdirs()
        val destFile = File(destDir, displayName)

        try {
            resolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Failed to read file", Toast.LENGTH_SHORT).show()
            return
        }

        showConfirmFileDialog(displayName, sizeKb, destFile)
    }

    private fun showConfirmFileDialog(name: String, sizeKb: Int, localFile: File) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_document, null)
        val tvName: TextView = dialogView.findViewById(R.id.tvPickedName)
        val tvSize: TextView = dialogView.findViewById(R.id.tvPickedSize)
        val etNotes: TextInputEditText = dialogView.findViewById(R.id.etDocNotes)
        val chipGroup: ChipGroup = dialogView.findViewById(R.id.chipGroupDocType)

        tvName.text = name
        tvSize.text = "$sizeKb KB"

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm document")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val notes = etNotes.text?.toString()?.trim().orEmpty()
                val type = chipGroup.children
                    .filterIsInstance<Chip>()
                    .firstOrNull { it.isChecked }
                    ?.text?.toString() ?: "Other"

                viewModel.addDocument(name, sizeKb, type, notes, localFile.name)
                Toast.makeText(requireContext(), "Document added", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { d, _ ->
                localFile.delete()  // cleanup if user cancelled
                d.dismiss()
            }
            .show()
    }

    // ──────────────────────────────────────────────────────────────
    // View & delete
    // ──────────────────────────────────────────────────────────────

    private fun showDocumentDetails(doc: Attachment) {
        val dateStr = java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(java.util.Date(doc.timestamp))

        val message = buildString {
            appendLine("Type: ${doc.type}")
            appendLine("Size: ${doc.sizeKb} KB")
            appendLine("Date: $dateStr")
            if (doc.notes.isNotBlank()) {
                appendLine()
                appendLine(doc.notes)
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(doc.name)
            .setMessage(message.trim())
            .setPositiveButton("Open") { _, _ -> openFile(doc) }
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .show()
    }

    private fun openFile(doc: Attachment) {
        val file = File(requireContext().filesDir, "documents/${doc.fileName}")
        if (!file.exists()) {
            Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(doc.name))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            else -> "*/*"
        }
    }

    private fun confirmDelete(doc: Attachment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete document?")
            .setMessage("\"${doc.name}\" will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                // Remove physical file too
                val file = File(requireContext().filesDir, "documents/${doc.fileName}")
                if (file.exists()) file.delete()
                viewModel.deleteDocument(doc)
                Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }
}