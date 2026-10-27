package eng.nookipet.lab7

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val tvTitle: TextView = findViewById(R.id.tvDetailTitle)
        val tvSize: TextView = findViewById(R.id.tvDetailSize)
        val btnBack: Button = findViewById(R.id.btnBack)

        val fileName = intent.getStringExtra("file_name") ?: "Unknown file"
        val fileSize = intent.getIntExtra("file_size", 0)

        tvTitle.text = fileName
        tvSize.text = "Size: $fileSize KB"

        btnBack.setOnClickListener { finish() }
    }
}