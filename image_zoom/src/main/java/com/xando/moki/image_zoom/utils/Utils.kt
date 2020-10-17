package com.xando.moki.image_zoom.utils

import android.graphics.Matrix
import android.graphics.Matrix.MSCALE_X
import android.graphics.Matrix.MSKEW_X
import android.graphics.PointF
import android.util.Size
import android.view.MotionEvent
import com.xando.moki.image_zoom.utils.Utils.MATRIX_VALUES_ARRAY_SIZE
import kotlin.math.*

internal object Utils {

    const val MATRIX_VALUES_ARRAY_SIZE = 9

    private const val POINT_LEFT_TOP_X = 0
    private const val POINT_LEFT_TOP_Y = 1
    private const val POINT_RIGHT_TOP_X = 2
    private const val POINT_RIGHT_TOP_Y = 3
    private const val POINT_RIGHT_BOTTOM_X_ = 4
    private const val POINT_RIGHT_BOTTOM_Y = 5
    private const val POINT_LEFT_BOTTOM_X = 6
    private const val POINT_LEFT_BOTTOM_Y = 7

    /**
     * Returns the value of the X and Y offset relative to point (0,0) - upper left corner of the screen.
     *
     * Y - top point of image
     * X - depends on rotation: either min X from top points or min X from bottom points
     */
    fun getPointTranslate(rotationAngle: Float, currMatrix: Matrix, imageSize: Size): PointF {

        val matrix = Matrix(currMatrix)

        val points = floatArrayOf(
            0f, 0f,                                                // left, top
            imageSize.width.toFloat(), 0f,                         // right, top
            imageSize.width.toFloat(), imageSize.height.toFloat(), // right, bottom
            0f, imageSize.height.toFloat()                         // left, bottom
        )

        matrix.mapPoints(points)

        val highestY = getMinYOfTopPoints(points, rotationAngle)

        // The points selection logic is different for 1, 3 and 2, 4 quarters of the Cartesian coordinate system
        return if ((rotationAngle in 0.0..90.0) || (rotationAngle < -90 && rotationAngle > -180))
            PointF(getMinXOfBottomPoints(points, rotationAngle), highestY)
        else PointF(getMinXOfTopPoints(points, rotationAngle), highestY)
    }

    /**
     * Returns the spacing of two pointers from [event]
     */
    fun getSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    /**
     * Returns the middle point of two pointers from [event]
     */
    fun getMiddlePoint(event: MotionEvent): PointF {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        return PointF(x / 2, y / 2)
    }

    /**
     * Returns the angle from two pointers from [event]
     */
    fun getAngle(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    /**
     * Y - choose from the top points
     */
    private fun getMinYOfTopPoints(points: FloatArray, rotationAngle: Float): Float {
        val y1 =
            if (abs(rotationAngle) <= 90) points[POINT_LEFT_TOP_Y] else points[POINT_RIGHT_BOTTOM_Y]
        val y2 =
            if (abs(rotationAngle) <= 90) points[POINT_RIGHT_TOP_Y] else points[POINT_LEFT_BOTTOM_Y]

        return min(y1, y2)
    }

    /**
     * X - choose from the top points
     */
    private fun getMinXOfTopPoints(points: FloatArray, rotationAngle: Float): Float {
        val x1 =
            if (abs(rotationAngle) <= 90) points[POINT_LEFT_TOP_X] else points[POINT_RIGHT_BOTTOM_X_]
        val x2 =
            if (abs(rotationAngle) <= 90) points[POINT_RIGHT_TOP_X] else points[POINT_LEFT_BOTTOM_X]

        return min(x1, x2)
    }

    /**
     * X - choose from the bottom points
     */
    private fun getMinXOfBottomPoints(points: FloatArray, rotationAngle: Float): Float {
        val x1 =
            if (abs(rotationAngle) <= 90) points[POINT_RIGHT_BOTTOM_X_] else points[POINT_LEFT_TOP_X]
        val x2 =
            if (abs(rotationAngle) <= 90) points[POINT_LEFT_BOTTOM_X] else points[POINT_RIGHT_TOP_X]

        return min(x1, x2)
    }
}

/**
 * @return scale value
 */
internal fun Matrix.getScale(): Float =
    getValues().let { values ->
        sqrt(values[MSCALE_X] * values[MSCALE_X] + values[MSKEW_X] * values[MSKEW_X])
    }

/**
 * @return rotation angle value
 */
internal fun Matrix.getRotationAngle(): Float =
    getValues().let {
        -round(Math.toDegrees(atan2(it[MSKEW_X], it[MSCALE_X]).toDouble())).toFloat()
    }

/**
 * @return matrix values
 */
internal fun Matrix.getValues(): FloatArray =
    FloatArray(MATRIX_VALUES_ARRAY_SIZE).apply { getValues(this) }