package com.dutch.composevideoplayer.util

import android.net.Uri
import com.dutch.composevideoplayer.model.MetaData

interface MetaDataReader {
    fun getMetaDataFromUri(contentUri: Uri): MetaData?
}