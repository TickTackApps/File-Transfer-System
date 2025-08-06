package com.example.localdb1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class DownloadedFilesAdapter(
    private val context: Context,
    private val fileList: MutableList<File>,
    private val onDelete: (File) -> Unit,
    private val onOpen: (File) -> Unit
) : RecyclerView.Adapter<DownloadedFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.txtFileName)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
        val btnOpen: Button = view.findViewById(R.id.btnOpen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_downloaded_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = fileList[position]
        holder.fileName.text = file.name

        holder.btnDelete.setOnClickListener { onDelete(file) }
        holder.btnOpen.setOnClickListener { onOpen(file) }
    }

    override fun getItemCount(): Int = fileList.size
}
