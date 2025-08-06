package com.example.localdb1

import ProgressRequestBody
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        progressBar = findViewById(R.id.progress_bar)
        progressBar.isIndeterminate = false
        progressBar.progress = 0
        handleSharedFile(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedFile(intent)




    }

    private fun handleSharedFile(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type != null) {
            val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            uri?.let {
                uploadFile(it)
            }
        } else if (intent?.action == Intent.ACTION_SEND_MULTIPLE && intent.type != null) {
            val uriList: ArrayList<Uri>? = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            uriList?.forEach {
                uploadFile(it)
            }
        }
    }

    private fun uploadFile(fileUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(fileUri)
            if (inputStream == null) {
                Log.e("UPLOAD", "Failed to open input stream")
                return
            }

            val fileBytes = inputStream.readBytes()
            inputStream.close()

            val fileName = DocumentFile.fromSingleUri(this, fileUri)?.name ?: "unknown_file"

            val requestBody = ProgressRequestBody(fileBytes, "multipart/form-data") { progress ->
                runOnUiThread {
                    progressBar.progress = progress
                }
            }

            val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0

            RetrofitClient.api.uploadFile(filePart).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Log.d("UPLOAD", "File uploaded successfully!")
                        Toast.makeText(this@ShareActivity, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("UPLOAD", "Upload failed: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ShareActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e("UPLOAD", "Error: ${t.message}")
                    Toast.makeText(this@ShareActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("UPLOAD", "Exception: ${e.message}")
            progressBar.visibility = View.GONE
        }
    }




}
