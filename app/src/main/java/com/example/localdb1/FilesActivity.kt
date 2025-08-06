package com.example.localdb1

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FilesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRefresh: Button
    private lateinit var adapter: FileAdapter
    private lateinit var serverIP: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        btnRefresh = findViewById(R.id.btnRefresh)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FileAdapter(this, fileList().toMutableList())
        recyclerView.adapter = adapter

        // Get saved server IP
        serverIP = getSavedServerIP(this) ?: "192.168.1.100"  // Default IP

        btnRefresh.setOnClickListener {
            fetchFiles()
        }

        fetchFiles()
    }

    private fun fetchFiles() {
        progressBar.visibility = View.VISIBLE  // Show progress bar before request starts

        RetrofitClient.api.getFiles().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                progressBar.visibility = View.GONE  // Hide progress bar after response

                if (response.isSuccessful) {
                    val files = response.body()
                    Log.d("FILES", "Received files: $files")

                    if (!files.isNullOrEmpty()) {
                        adapter.updateData(files)
                    } else {
                        Toast.makeText(this@FilesActivity, "No files found.", Toast.LENGTH_SHORT).show()
                        Log.e("FILES", "Response body is empty or null")
                    }
                } else {
                    Toast.makeText(this@FilesActivity, "Failed to load files.", Toast.LENGTH_SHORT).show()
                    Log.e("FILES", "Failed to fetch files: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                progressBar.visibility = View.GONE  // Hide progress bar on failure
                Toast.makeText(this@FilesActivity, "Error fetching files: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("FILES", "Error fetching files: ${t.message}")
            }
        })
    }


    private fun getSavedServerIP(context: Context): String? {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString("server_ip", null)
    }
}
