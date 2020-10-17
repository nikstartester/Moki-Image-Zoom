package com.xando.moki.image_zoom.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * ImageZoom data to save/restore image scene with [sceneValues] of image matrix
 */
@Parcelize
data class ImageZoomScene(val sceneValues: FloatArray) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageZoomScene

        return sceneValues.contentEquals(other.sceneValues)
    }

    override fun hashCode(): Int = sceneValues.contentHashCode()
}