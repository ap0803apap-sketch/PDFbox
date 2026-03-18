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
        val merger = com.tom_roush.pdfbox.multipdf.PDFMergerUtility()
        merger.destinationStream = dest
        sources.forEach { merger.addSource(it) }
        merger.mergeDocuments(com.tom_roush.pdfbox.io.MemoryUsageSetting.setupMainMemoryOnly())
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
        val document = PDDocument.load(source)
        try {
            for (page in document.pages) {
                val resources = page.resources
                resources?.xObjectNames?.forEach { name ->
                    if (resources.isImageXObject(name)) {
                        val xobject = resources.getXObject(name)
                        if (xobject is com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                            val bitmap = xobject.image
                            val baos = java.io.ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, (quality * 100).toInt(), baos)
                            val compressedXObject = com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory.createFromStream(document, baos.toByteArray().inputStream())
                            resources.put(name, compressedXObject)
                        }
                    }
                }
            }
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
