import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.InputStream

class InputStreamRequestBody(
    private val mediaType: MediaType?,
    private val inputStream: InputStream,
    private val contentLength: Long,
    private val progressCallback: (Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = contentLength

    override fun writeTo(sink: BufferedSink) {
        val source = inputStream.source()
        val totalBytes = contentLength
        var uploadedBytes = 0L
        val buffer = Buffer()
        var read: Long

        while (source.read(buffer, 8192).also { read = it } != -1L) {
            sink.write(buffer, read)
            uploadedBytes += read
            progressCallback((uploadedBytes * 100 / totalBytes).toInt())
        }
        inputStream.close()
    }
}
