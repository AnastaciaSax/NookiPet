package eng.nookipet.lab7

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar

class AttachmentAddFragment : Fragment() {

    private lateinit var etDate: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_attachment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etFileName: EditText = view.findViewById(R.id.etFileName)
        val etFileSize: EditText = view.findViewById(R.id.etFileSize)
        val btnSave: Button = view.findViewById(R.id.btnSave)

        etDate = view.findViewById(R.id.etDate)
        etDate.setOnClickListener { showDatePicker() }

        btnSave.setOnClickListener {
            val name = etFileName.text.toString().trim()
            val sizeText = etFileSize.text.toString().trim()
            val date = etDate.text.toString().trim()

            if (name.isEmpty() || sizeText.isEmpty() || date.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sizeKb = sizeText.toIntOrNull()
            if (sizeKb == null) {
                Toast.makeText(requireContext(), "Size must be a number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // id = 0 — Room will generate the real one during insert.
            val newAttachment = Attachment(name = name, sizeKb = sizeKb)

            Toast.makeText(
                requireContext(),
                "Saved: $name ($sizeKb KB), $date",
                Toast.LENGTH_SHORT
            ).show()

            (activity as? MainActivity)?.onAttachmentCreated(newAttachment)
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val date = "%02d.%02d.%d".format(d, m + 1, y)
                etDate.setText(date)
            },
            year, month, day
        ).show()
    }
}