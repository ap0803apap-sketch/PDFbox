package com.shejan.pdfbox_pdfeditor.core

import android.content.Context
import android.graphics.Bitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
                    if (i in 0 until sourceDoc.numberOfPages) {
                        destDoc.addPage(sourceDoc.getPage(i))
                    }
                }
                splitDocs.add(destDoc)
            }
        } finally {
            sourceDoc.close()
        }
        splitDocs
    }

    suspend fun rotatePdf(source: InputStream, dest: OutputStream, degrees: Int) = withContext(Dispatchers.IO) {
        val document = PDDocument.load(source)
        try {
            document.pages.forEach { page ->
                page.rotation = (page.rotation + degrees) % 360
            }
            document.save(dest)
        } finally {
            document.close()
        }
    }

    suspend fun compressPdf(source: InputStream, dest: OutputStream, quality: Float) = withContext(Dispatchers.IO) {
        // Simple compression by re-saving. PDFBox doesn't have a high-level "compress" method like some others,
        // but we can optimize by setting compression on the output stream or reducing image quality if we iterate.
        // For now, we'll do a standard save which often reduces size if the original wasn't optimized.
        val document = PDDocument.load(source)
        try {
            document.save(dest)
        } finally {
            document.close()
        }
    }

    suspend fun protectPdf(source: InputStream, dest: OutputStream, password: String) = withContext(Dispatchers.IO) {
        val document = PDDocument.load(source)
        try {
            val ap = com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission()
            val spp = com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy(password, password, ap)
            spp.encryptionKeyLength = 128
            document.protect(spp)
            document.save(dest)
        } finally {
            document.close()
        }
    }

    suspend fun unprotectPdf(source: InputStream, dest: OutputStream, password: String) = withContext(Dispatchers.IO) {
        val document = PDDocument.load(source, password)
        try {
            document.isAllSecurityToBeRemoved = true
            document.save(dest)
        } finally {
            document.close()
        }
    }

    suspend fun pdfToImages(source: InputStream, outputDir: File, format: String): List<File> = withContext(Dispatchers.IO) {
        val document = PDDocument.load(source)
        val renderer = PDFRenderer(document)
        val imageFiles = mutableListOf<File>()
        
        try {
            if (!outputDir.exists()) outputDir.mkdirs()
            
            for (i in 0 until document.numberOfPages) {
                val bitmap = renderer.renderImageWithDPI(i, 300f) // High quality 300 DPI
                val imageFile = File(outputDir, "page_${i + 1}_${System.currentTimeMillis()}.${format.lowercase()}")
                val out = FileOutputStream(imageFile)
                
                val compressFormat = if (format.equals("PNG", true)) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                
                bitmap.compress(compressFormat, 100, out)
                out.close()
                imageFiles.add(imageFile)
            }
        } finally {
            document.close()
        }
        imageFiles
    }

    suspend fun extractText(source: InputStream): String = withContext(Dispatchers.IO) {
        val document = PDDocument.load(source)
        try {
            val stripper = PDFTextStripper()
            stripper.getText(document)
        } finally {
            document.close()
        }
    }
}
