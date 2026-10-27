package eng.nookipet.lab7

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import eng.nookipet.lab7.data.repository.AttachmentRepository

class AttachmentListFragment : Fragment() {

    // viewModels() — KTX-extension, creates ViewModel lazily and caches it.
    // On rotation the ViewModel is NOT recreated — data survives.
    private val viewModel: AttachmentListViewModel by viewModels {
        AttachmentListViewModelFactory(
            AttachmentRepository(AppDatabase.getDatabase(requireContext()).attachmentDao())
        )
    }

    private lateinit var adapter: AttachmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attachment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MVVM", "AttachmentListFragment.onViewCreated — new subscription about to start")

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val btnAdd: Button = view.findViewById(R.id.btnAdd)

        adapter = AttachmentAdapter(
            mutableListOf(),
            onItemClick = { attachment ->
                Toast.makeText(requireContext(), "Selected: ${attachment.name}", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { attachment, _ ->
                showDeleteDialog(attachment)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        // repeatOnLifecycle(STARTED) — subscription active only while the fragment is visible.
        // On rotation the subscription is recreated, but ViewModel keeps its data.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.attachments.collect { list ->
                    Log.d("MVVM", "AttachmentListFragment: state emitted, ${list.size} items")
                    adapter.updateData(list)
                }
            }
        }

        btnAdd.setOnClickListener {
            (activity as? MainActivity)?.navigateToAddFragment()
        }
    }

    fun addAttachment(attachment: Attachment) {
        viewModel.addAttachment(attachment)
    }

    private fun showDeleteDialog(attachment: Attachment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete attachment?")
            .setMessage("Are you sure you want to delete \"${attachment.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAttachment(attachment)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}