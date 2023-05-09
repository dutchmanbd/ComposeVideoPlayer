package com.dutch.composevideoplayer.util

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import com.dutch.composevideoplayer.model.MetaData

class MetaDataReaderImpl(
    private val app: Application
) : MetaDataReader {
    override fun getMetaDataFromUri(contentUri: Uri): MetaData? {
        if (contentUri.scheme != "content") {
            return null
        }
        val fileName = app.contentResolver.query(
            contentUri,
            arrayOf(MediaStore.Video.VideoColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(index)
        }

        return fileName?.let { name ->
            MetaData(
                fileName = Uri.parse(name).lastPathSegment ?: return null
            )
        }
    }
}