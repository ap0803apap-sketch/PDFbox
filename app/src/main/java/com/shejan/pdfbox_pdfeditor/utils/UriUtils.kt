package com.shejan.pdfbox_pdfeditor.utils

import android.content.Context
import android.net.Uri

object UriUtils {
    fun isPdf(context: Context, uri: Uri): Boolean {
        val type = context.contentResolver.getType(uri)
        return type != null && type == "application/pdf"
    }
}
