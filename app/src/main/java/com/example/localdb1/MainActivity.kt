package com.example.localdb1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.localdb1.databinding.ActivityMainBinding
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ProgressRequestBody


class MainActivity : AppCompatActivity() {

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedFile(intent)
    }

    private val REQUEST_FILE_PICK = 1
    private var selectedFileUri: Uri? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var ipEditText: EditText
    private lateinit var updateIpButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleSharedFile(intent)

        binding.settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.downloadAct.setOnClickListener {
            val intent = Intent(this, DownloadedFilesActivity::class.java)
            startActivity(intent)
        }

        binding.largeFileActivityBtn.setOnClickListener {

            val intent = Intent(this, LargeFileUploadActivity::class.java)
            startActivity(intent)

        }


        val btnViewFiles: Button = findViewById(R.id.btnViewFiles)
        btnViewFiles.setOnClickListener {
            startActivity(Intent(this, FilesActivity::class.java))
        }


        val selectButton: Button = findViewById(R.id.select)
        val uploadButton: Button = findViewById(R.id.upload)
        ipEditText = findViewById(R.id.ip_edit_text)
        updateIpButton = findViewById(R.id.update_ip_button)
        progressBar = findViewById(R.id.progress_bar)

        val savedIp = getSavedServerIP()
        if (savedIp != null) {
            ipEditText.setText(savedIp)
            RetrofitClient.updateBaseUrl(this,"$savedIp")
        }

        selectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, REQUEST_FILE_PICK)
        }

        uploadButton.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadFile(uri)
            } ?: Toast.makeText(this, "Please select a file first!", Toast.LENGTH_SHORT).show()
        }

        updateIpButton.setOnClickListener {
            val newIp = ipEditText.text.toString().trim()
            if (newIp.isNotEmpty()) {
                RetrofitClient.updateBaseUrl(this, "$newIp")
                Toast.makeText(this, "IP Updated: $newIp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter a valid IP", Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun handleSharedFile(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type != null) {
            val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM)
            uri?.let {
                handleReceivedFile(it)
            }
        } else if (intent?.action == Intent.ACTION_SEND_MULTIPLE && intent.type != null) {
            val uriList: ArrayList<Uri>? = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            uriList?.forEach {
                handleReceivedFile(it)
            }
        }
    }
    private fun getRealPathFromURI(uri: Uri): String? {
        var result: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex("_data")
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
        return result ?: uri.path
    }

    private fun handleReceivedFile(uri: Uri) {
        val filePath = getRealPathFromURI(uri)
        Toast.makeText(this, "Received file: $filePath", Toast.LENGTH_LONG).show()
        Log.d("SHARE_INTENT", "Received file path: $filePath")

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_PICK && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Toast.makeText(this, "File Selected: $selectedFileUri", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@MainActivity, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("UPLOAD", "Upload failed: ${response.errorBody()?.string()}")
                        Toast.makeText(this@MainActivity, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e("UPLOAD", "Error: ${t.message}")
                    Toast.makeText(this@MainActivity, "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Log.e("UPLOAD", "Exception: ${e.message}")
            progressBar.visibility = View.GONE
        }
    }

    private fun getSavedServerIP(): String? {
        return getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).getString("server_ip", null)
    }
}
