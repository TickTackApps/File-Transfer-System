package com.example.localdb1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadedFilesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DownloadedFilesAdapter
    private var fileList: MutableList<File> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloaded_files)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadDownloadedFiles()
    }

    private fun loadDownloadedFiles() {
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "LocalDB1")

        if (!downloadsDir.exists() || downloadsDir.listFiles().isNullOrEmpty()) {
            Toast.makeText(this, "No downloaded files found", Toast.LENGTH_SHORT).show()
            return
        }

        fileList = downloadsDir.listFiles()?.toMutableList() ?: mutableListOf()
        adapter = DownloadedFilesAdapter(this, fileList, ::onFileDeleted, ::onFileOpened)
        recyclerView.adapter = adapter
    }

    private fun onFileDeleted(file: File) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Delete File")
        builder.setMessage("Are you sure you want to delete ${file.name}?")

        builder.setPositiveButton("Yes") { _, _ ->
            if (file.exists() && file.delete()) {
                fileList.remove(file)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "File deleted: ${file.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete ${file.name}", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun onFileOpened(file: File) {
        try {
            val uri = FileProvider.getUriForFile(this, "com.example.localdb1.fileprovider", file)

            val mimeType = contentResolver.getType(uri) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file", Toast.LENGTH_SHORT).show()
            Log.e("FILE_OPEN", "Error opening file: ${e.message}")
        }
    }


}
