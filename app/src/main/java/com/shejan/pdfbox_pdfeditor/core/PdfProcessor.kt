package com.shejan.pdfbox_pdfeditor.core

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

object PdfProcessor {
    
    fun init(context: Context) {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun mergePdfs(sources: List<InputStream>, dest: OutputStream) = withContext(Dispatchers.IO) {
        val destDoc = PDDocument()
        try {
            sources.forEach { stream ->
                val sourceDoc = PDDocument.load(stream)
                sourceDoc.pages.forEach { page ->
                    destDoc.addPage(page)
                }
                sourceDoc.close()
            }
            destDoc.save(dest)
        } finally {
            destDoc.close()
        }
    }

    suspend fun splitPdf(source: InputStream, pageRanges: List<IntRange>): List<PDDocument> = withContext(Dispatchers.IO) {
        val sourceDoc = PDDocument.load(source)
        val splitDocs = mutableListOf<PDDocument>()
        try {
            pageRanges.forEach { range ->
                val destDoc = PDDocument()
                for (i in range) {
                    destDoc.addPage(sourceDoc.getPage(i))
                }
                splitDocs.add(destDoc)
            }
        } finally {
            sourceDoc.close()
        }
        splitDocs
    }
}
