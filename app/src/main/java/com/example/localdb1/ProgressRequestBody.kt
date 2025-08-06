import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ProgressRequestBody(
    private val data: Any,
    private val contentType: String,
    private val onProgressUpdate: (Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long {
        return when (data) {
            is File -> data.length()
            is ByteArray -> data.size.toLong()
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    override fun writeTo(sink: BufferedSink) {
        when (data) {
            is File -> writeFileToSink(data, sink)
            is ByteArray -> writeByteArrayToSink(data, sink)
            else -> throw IllegalArgumentException("Unsupported data type")
        }
    }

    private fun writeFileToSink(file: File, sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileSize = file.length()
        var uploaded = 0L

        FileInputStream(file).use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                uploaded += read
                sink.write(buffer, 0, read)


                val progress = (uploaded * 100 / fileSize).toInt()
                onProgressUpdate(progress)
            }
        }
    }

    private fun writeByteArrayToSink(byteArray: ByteArray, sink: BufferedSink) {
        val totalSize = byteArray.size
        var uploaded = 0

        for (i in byteArray.indices step DEFAULT_BUFFER_SIZE) {
            val chunkSize = minOf(DEFAULT_BUFFER_SIZE, totalSize - i)
            sink.write(byteArray, i, chunkSize)
            uploaded += chunkSize

            // Update progress
            val progress = (uploaded * 100 / totalSize).toInt()
            onProgressUpdate(progress)
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 4096
    }
}
