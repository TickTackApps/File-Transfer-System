package com.example.localdb1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.io.*

class FileAdapter(private val context: Context, private var fileList: MutableList<String>) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {

        val ip = getSavedServerIP(context)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val btnDownload: Button = view.findViewById(R.id.btnDownload)
        val btnOpen: Button = itemView.findViewById(R.id.btnOpen)
        val progressBarDownload: ProgressBar = view.findViewById(R.id.progressBarDownload)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position]
        holder.fileName.text = file
        holder.deleteButton.setOnClickListener {
            deleteFile(file, position)
        }
        holder.btnDownload.setOnClickListener {
            downloadFile(context, file, holder.progressBarDownload)
        }

        holder.btnOpen.setOnClickListener {
            openFileFromServer(file)
        }

    }



    private fun deleteFile(filename: String, position: Int) {
        RetrofitClient.api.deleteFile(filename).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "$filename deleted!", Toast.LENGTH_SHORT).show()

                    if (position in fileList.indices) { // Safe index check
                        fileList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, fileList.size) // Update positions
                    }
                } else {
                    Toast.makeText(context, "Failed to delete $filename", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    override fun getItemCount(): Int = fileList.size

    fun updateData(newFiles: List<String>) {
        fileList.clear() // Clear old data
        fileList.addAll(newFiles) // Add new data
        notifyDataSetChanged()

    }

    private fun downloadFile(context: Context, fileName: String, progressBar: ProgressBar) {
        Toast.makeText(context, "Downloading $fileName...", Toast.LENGTH_SHORT).show()

        progressBar.visibility = View.VISIBLE  // Show progress bar before starting

        RetrofitClient.api.downloadFile(fileName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        saveFileToStorage(context, responseBody, fileName, progressBar)
                    } ?: Log.e("DOWNLOAD", "Response body is null")
                } else {
                    Log.e("DOWNLOAD", "Failed to download: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DOWNLOAD", "Error: ${t.message}")
                Toast.makeText(context, "Download failed: ${t.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE  // Hide progress bar on failure
            }
        })
    }

    private fun openFileFromServer(fileName: String) {



        val fileUrl = "http://$ip:5000/download/$fileName"  // Replace <PC_IP> with your PC's IP

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(fileUrl), "video/*")  // Set MIME type based on file type
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app can open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFileToStorage(context: Context, body: ResponseBody, fileName: String, progressBar: ProgressBar) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create directory in Downloads folder
                val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "LocalDB1")
                if (!downloadsDir.exists()) downloadsDir.mkdirs() // Ensure directory exists

                val file = File(downloadsDir, fileName)

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                val fileSize = body.contentLength()
                var downloadedSize = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead

                    // Update progress bar
                    val progress = ((downloadedSize * 100) / fileSize).toInt()
                    withContext(Dispatchers.Main) { progressBar.progress = progress }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "File saved to /DCIM/LocalDB1: $fileName", Toast.LENGTH_SHORT).show()
                }

                Log.d("DOWNLOAD", "File downloaded: ${file.absolutePath}")
            } catch (e: IOException) {
                Log.e("DOWNLOAD", "Error saving file: ${e.message}")
            }
        }
    }
    private fun getSavedServerIP(context: Context): String? {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString("server_ip", null)
    }

}
