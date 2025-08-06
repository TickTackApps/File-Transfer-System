package com.example.localdb1

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("/upload")
    fun uploadLargeFile(@Part file: MultipartBody.Part): Call<Void>

    @GET("/files")
    fun getFiles(): Call<List<String>>

    @DELETE("delete/{filename}")
    fun deleteFile(@Path("filename") filename: String): Call<Void>

    @Multipart
    @POST("/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<ResponseBody>

    @GET("/download/{filename}")
    @Streaming
    fun downloadFile(@Path("filename") fileName: String): Call<ResponseBody>

}
