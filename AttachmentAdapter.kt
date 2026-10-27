package eng.nookipet.lab7

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttachmentAdapter(
    private val attachments: MutableList<Attachment>,
    private val onItemClick: (Attachment) -> Unit,
    private val onItemLongClick: (Attachment, Int) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    class AttachmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivIcon)
        val fileName: TextView = view.findViewById(R.id.tvFileName)
        val fileSize: TextView = view.findViewById(R.id.tvFileSize)

        val docType: TextView = view.findViewById(R.id.tvDocType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attachment, parent, false)
        return AttachmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        val attachment = attachments[position]
        holder.fileName.text = attachment.name
        holder.fileSize.text = "Size: ${attachment.sizeKb} KB"
        holder.docType.text = attachment.type.uppercase()

        val iconRes = when {
            attachment.name.endsWith(".pdf", true) -> android.R.drawable.ic_menu_agenda
            attachment.name.endsWith(".jpg", true) ||
                    attachment.name.endsWith(".png", true) -> android.R.drawable.ic_menu_gallery
            else -> android.R.drawable.ic_menu_save
        }
        holder.icon.setImageResource(iconRes)

        holder.itemView.setOnClickListener { onItemClick(attachment) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(attachment, position)
            true
        }
    }

    override fun getItemCount(): Int = attachments.size

    fun updateData(newList: List<Attachment>) {
        attachments.clear()
        attachments.addAll(newList)
        notifyDataSetChanged()
    }
}