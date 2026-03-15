package com.shejan.pdfbox_pdfeditor.ui.merge

import android.net.Uri

data class SelectedFile(
    val uri: Uri,
    val name: String,
    val size: String
)
