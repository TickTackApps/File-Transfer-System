package com.example.localdb1

import InputStreamRequestBody
import ProgressRequestBody
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LargeFileUploadActivity : AppCompatActivity() {

    private lateinit var btnSelectFile: Button
    private lateinit var btnUpload: Button
    private lateinit var tvFileName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

    private var selectedFileUri: Uri? = null
    private var selectedFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_large_file_upload)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        tvFileName = findViewById(R.id.tvFileName)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)

        btnSelectFile.setOnClickListener { openFilePicker() }
        btnUpload.setOnClickListener {
            selectedFilePath?.let { filePath ->
                val file = File(filePath)
                val fileUri = Uri.fromFile(file)
                uploadFile(fileUri)
            }
        }

    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            selectedFilePath = FileUtils.getPath(this, selectedFileUri!!)

            if (selectedFilePath != null) {
                tvFileName.text = "Selected: ${File(selectedFilePath!!).name}"
                btnUpload.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Error selecting file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadFile(fileUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(fileUri) ?: return
            val fileName = DocumentFile.fromSingleUri(this, fileUri)?.name ?: "unknown_file"

            // Get file size
            val fileSize = contentResolver.openFileDescriptor(fileUri, "r")?.statSize ?: -1

            if (fileSize <= 0) {
                Toast.makeText(this, "Invalid file size!", Toast.LENGTH_SHORT).show()
                return
            }

            val requestBody = InputStreamRequestBody(
                mediaType = "application/octet-stream".toMediaTypeOrNull(),
                inputStream = inputStream,
                contentLength = fileSize
            ) { progress ->
                runOnUiThread {
                    progressBar.progress = progress
                    tvProgress.text = "Progress: $progress%"
                }
            }

            val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0

            RetrofitClient.api.uploadFile(filePart).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@LargeFileUploadActivity, "Upload successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LargeFileUploadActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LargeFileUploadActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("UPLOAD", "Exception: ${e.message}")
            progressBar.visibility = View.GONE
        }
    }

    companion object {
        private const val FILE_PICK_CODE = 100
    }
}
