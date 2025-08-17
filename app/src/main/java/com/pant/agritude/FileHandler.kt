package com.pant.agritude

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

/**
 * Handles file-related operations, particularly converting a Uri to a Base64 string.
 */
class FileHandler(private val context: Context) {

    /**
     * Converts a Uri to a Base64 encoded string.
     * This is useful for sending image data to APIs that require Base64 format.
     *
     * @param uri The Uri of the image to convert.
     * @return A Base64 string of the image, or null if conversion fails.
     */
    fun uriToBase64(uri: Uri): String? {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        return bitmap?.let {
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        }
    }
}
