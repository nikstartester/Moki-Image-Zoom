@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.xando.moki.image_zoom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.xando.moki.image_zoom.model.ImageZoomScene
import com.xando.moki.image_zoom.model.TouchState
import com.xando.moki.image_zoom.utils.Utils
import com.xando.moki.image_zoom.utils.getRotationAngle
import com.xando.moki.image_zoom.utils.getScale
import com.xando.moki.image_zoom.utils.getValues

/**
 * View for rotation, zoom and movement image
 */
class ImageZoomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(
    context,
    attrs,
    defStyleAttr
), View.OnTouchListener {

    private companion object {
        //region DEFAULT_PARAMS
        const val SUPPORT_ZOOM = true
        const val SUPPORT_TRANSLATE = false
        const val SUPPORT_ROTATE = false

        const val MAX_ZOOM = 3f
        const val MIN_ZOOM = 0.9f

        // TODO: true
        const val SUPPORT_DOUBLE_TAP = false

        // TODO: > 0?
        const val MAX_SCREEN_OFFSET = -1f

        // TODO: true
        const val USE_RETURN_ANIM_FROM_MAX_OR_MIN_ZOOM = false
        //endregion

        const val MIN_DISTANCE_MULTITOUCH = 10f

        const val MATRIX_VALUES_ARRAY_SIZE = 9
    }

    /**
     * Get/set the current image scene
     */
    var actualSceneData: ImageZoomScene
        get() = ImageZoomScene(currMatrix.getValues())
        set(value) {
            if (isDrawn) {
                currMatrix.setValues(value.sceneValues)
                imageMatrix = currMatrix
            } else sceneDataAfterDraw = value
        }

    //region Params
    /**
     * Support zoom flag
     */
    var isSupportZoom = SUPPORT_ZOOM

    /**
     *  Support translate flag
     */
    var isSupportTranslate = SUPPORT_TRANSLATE

    /**
     *  Support rotate flag
     */
    var isSupportRotate = SUPPORT_ROTATE

    /**
     * Maximum supported zoom
     */
    var maxZoom = MAX_ZOOM
        set(value) {
            field = value
            invalidateZoom()
        }

    /**
     * Minimum supported zoom
     */
    var minZoom = MIN_ZOOM
        set(value) {
            field = value
            invalidateZoom()
        }

    /**
     * Support double tap flag
     */
    // TODO: add double tap support
    var supportDoubleTap = SUPPORT_DOUBLE_TAP

    /**
     * The maximum limit for which the image can go beyond the screen.
     * If current offset is greater than value you want set, image will move automatically to fit
     * into your offset.
     * If [maxScreenOffset] < 0 image can go away from the screen.
     */
    // TODO: add max offset limit
    var maxScreenOffset = MAX_SCREEN_OFFSET
        set(value) {
            field = value
            invalidateScreenOffset()
        }

    /**
     * If 'true' when zoom value is greater than [maxZoom] or less than [minZoom] with multi touch,
     * image zoom will go beyond the limits and when point up, zoom return to limits [maxZoom]
     * or [minZoom] with animation
     */
    // TODO: add return zoom anim support
    var useReturnAnimFromMaxOrMinZoom = USE_RETURN_ANIM_FROM_MAX_OR_MIN_ZOOM
    //endregion

    private var currMatrix = Matrix()
    private val savedMatrix = Matrix()

    private var touchMode = TouchState.NONE

    private val startTouchPoint = PointF()
    private val middleTouchPoint = PointF()

    private var oldDistance = 1f

    private var lastAngle = 0f

    private var needSkipRotationEvent = true

    private var initScale = 1f

    private var isDrawn = false

    private var sceneDataAfterDraw: ImageZoomScene? = null

    init {
        setOnTouchListener(this)
        scaleType = ScaleType.MATRIX
        currMatrix = Matrix(imageMatrix)
    }

    /**
     * Reset zoom, rotate and movement to initial image state
     */
    fun resetImage() {
        if (drawable != null) {
            val drawableRect =
                RectF(
                    0f, 0f,
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )

            val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            currMatrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)

            initScale = getActualScale()

            // sceneDataAfterDraw in priority
            sceneDataAfterDraw?.let {
                currMatrix.setValues(it.sceneValues)
            }

            sceneDataAfterDraw = null

            imageMatrix = currMatrix
        }
    }

    /**
     * @return a matrix of all image changes. You can use [getImageMatrix] alternatively
     */
    fun getActualSceneMatrix() = currMatrix

    /**
     * @return the current zoom value
     */
    fun getActualScale(): Float = currMatrix.getScale()

    /**
     * @return the current rotation value in degrees
     */
    fun getActualRotation(): Float = currMatrix.getRotationAngle()

    /**
     * Returns the value of the X and Y offset relative to point (0,0) - upper left corner of the screen.
     *
     * Y - top point of image
     * X - depends on rotation: either min X from top points or min X from bottom points
     */
    fun getPointTranslate(): PointF =
        if (drawable != null) Utils.getPointTranslate(
            getActualRotation(),
            currMatrix,
            Size(drawable.intrinsicWidth, drawable.intrinsicHeight)
        ) else PointF()

    override fun onDraw(canvas: Canvas?) {
        if (isDrawn.not() && drawable != null) {
            resetImage()
            isDrawn = true
        }
        super.onDraw(canvas)
    }

    /**
     * Override to reset image scene for new image from [setImageURI], [setImageBitmap], etc.
     * NOTE: Not apply to the first image.
     */
    override fun unscheduleDrawable(who: Drawable?) {
        super.unscheduleDrawable(who)
        resetImage()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN ->                              onActionDown(event)
            MotionEvent.ACTION_POINTER_DOWN ->                      onActionPointerDown(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> onActionUp()
            MotionEvent.ACTION_MOVE ->                              onActionMove(event)
        }

        saveTouchChanges()

        // TODO: do not return `true` always. For example case with ViewPager
        return true
    }

    private fun invalidateZoom() {
        // TODO: add invalidate zoom
    }

    private fun invalidateScreenOffset() {
        // TODO: add invalidate screen offset
    }

    private fun onActionDown(event: MotionEvent) {
        savedMatrix.set(currMatrix)
        startTouchPoint[event.x] = event.y
        touchMode = TouchState.DRAG
        needSkipRotationEvent = true
    }

    private fun onActionPointerDown(event: MotionEvent) {
        oldDistance = Utils.getSpacing(event)
        if (oldDistance > MIN_DISTANCE_MULTITOUCH) {
            savedMatrix.set(currMatrix)
            setMiddlePoint(event)
            touchMode = TouchState.MULTI_TOUCH
        }
        needSkipRotationEvent = false
        lastAngle = Utils.getAngle(event)
    }

    private fun onActionUp() {
        touchMode = TouchState.NONE
        needSkipRotationEvent = true
    }

    private fun onActionMove(event: MotionEvent) {
        if (touchMode == TouchState.DRAG) {
            currMatrix.set(savedMatrix)

            applyTranslate(event)
        } else if (touchMode == TouchState.MULTI_TOUCH) {
            val newDist = Utils.getSpacing(event)

            if (newDist > MIN_DISTANCE_MULTITOUCH) {
                currMatrix.set(savedMatrix)
                if (isSupportZoom) applyZoom(event)
            }

            if (needSkipRotationEvent.not() && event.pointerCount == 2 || event.pointerCount == 3)
                if (isSupportRotate) applyRotate(event)
        }
    }

    private fun saveTouchChanges() {
        imageMatrix = currMatrix
        invalidate()
    }

    private fun setMiddlePoint(event: MotionEvent) {
        val middlePoint = Utils.getMiddlePoint(event)
        middleTouchPoint[middlePoint.x] = middlePoint.y
    }

    private fun applyTranslate(event: MotionEvent) {
        val dx = event.x - startTouchPoint.x
        val dy = event.y - startTouchPoint.y

        currMatrix.postTranslate(dx, dy)
    }

    private fun applyZoom(event: MotionEvent) {
        val newDistance = Utils.getSpacing(event)

        val currScale = getActualScale()

        // Multiply the minimum and maximum zoom by initScale, because if the image is larger than
        // the screen, zoom is applied < 1
        val minScale =
            (if (currScale > 1E-5) MIN_ZOOM / currScale else MIN_ZOOM) * initScale
        val maxScale =
            (if (currScale > 1E-5) MAX_ZOOM / currScale else MAX_ZOOM) * initScale

        val newScale = newDistance / oldDistance

        val scale = minScale.coerceAtLeast((newScale).coerceAtMost(maxScale))

        currMatrix.postScale(scale, scale, middleTouchPoint.x, middleTouchPoint.y)
    }

    private fun applyRotate(event: MotionEvent) {
        val newAngle = Utils.getAngle(event)
        val rotation = newAngle - lastAngle
        currMatrix.postRotate(rotation, middleTouchPoint.x, middleTouchPoint.y)
    }
}
