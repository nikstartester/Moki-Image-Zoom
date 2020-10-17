package com.xando.moki.imagezoom.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri

/**
 * Some file utils
 */
object FileUriUtil {

    fun getPathFromURI(context: Context, uri: Uri): String? =
        when {
            "content".equals(uri.scheme, ignoreCase = true) -> {
                getDataColumn(context, uri)
            }
            "file".equals(uri.scheme, ignoreCase = true) -> {
                uri.path
            }
            else -> null
        }

    private fun getDataColumn(
        context: Context, uri: Uri
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(column),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}