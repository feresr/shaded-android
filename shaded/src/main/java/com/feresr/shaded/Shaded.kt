package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glDisable
import android.util.Log
import com.feresr.shaded.opengl.Texture
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.PI
import kotlin.math.atan

class Shaded(private val context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        newSingleThreadContext("OpenGLDispatcher") + CoroutineExceptionHandler { _, e ->
            Log.e(Shaded::class::simpleName.toString(), e.toString())
        } + SupervisorJob()

    init {
        launch {
            com.feresr.shaded.opengl.Context.init()
            glDisable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            Layer().bind()
        }
    }

    private val previewPingPongRenderer: PingPongRenderer by lazy {
        PingPongRenderer(
            Filter(context),
            originalTexture
        )
    }

    private val originalTexture: Texture by lazy { Texture() }
    private val filters = mutableListOf<Filter>()

    private var cameraX = 0.0f
    private var cameraY = 0.0f
    private var downScale: Int = 1
    private var zoom = 1f

    private val snapYTo = atan(HALF_QUAD / CAMERAZ) * (180f / PI.toFloat()) * 2
    private var fov = snapYTo


    suspend fun addFilter(filter: Filter) = withContext(coroutineContext) {
        filters.add(filter)
    }

    suspend fun removeFilter(filter: Filter) = withContext(coroutineContext) {
        filters.remove(filter)
    }

    suspend fun setBitmap(bitmap: Bitmap, recycle: Boolean) = withContext(coroutineContext) {
        originalTexture.setData(bitmap)
        previewPingPongRenderer.resize(
            bitmap.width / downScale,
            bitmap.height / downScale
        )
        if (recycle) bitmap.recycle()
    }

    suspend fun getBitmap(withFilters: List<Filter> = filters): Bitmap {
        return withContext(coroutineContext) {
            previewPingPongRenderer.renderToBitmap(withFilters)
        }
    }

    fun dispose() {
        // launch independently of the parent scope and finish
        launch {
            filters.forEach { it.delete() }
            previewPingPongRenderer.delete()
            originalTexture.delete()
            com.feresr.shaded.opengl.Context.tearDown()
            this@Shaded.cancel()
        }
    }

    /**
     * Downscaling allows for faster rendering
     */
    suspend fun downScale(factor: Int) {
        withContext(coroutineContext) {
            if (downScale == factor) return@withContext
            downScale = factor
            previewPingPongRenderer.resize(
                originalTexture.width() / factor,
                originalTexture.height() / factor
            )
        }
    }

    suspend fun clearFilters() = withContext(coroutineContext) {
        filters.clear()
    }

    fun changeZoomBy(z: Float) {
        zoom *= z
        zoom = zoom.coerceIn(.5f, 4f)
        fov = snapYTo / zoom
        checkBounds()
    }

    fun moveCameraBy(x: Float, y: Float) {
        cameraX += (x * QUAD_SIZE / 200.0f) / zoom
        cameraY += (y * QUAD_SIZE / 200.0f) / zoom
        checkBounds()
    }

    private fun checkBounds() {
        if (fov in (snapYTo - SNAP_SENSITIVITY)..(snapYTo + SNAP_SENSITIVITY)) fov = snapYTo

        cameraX = cameraX.coerceIn(-HALF_QUAD, HALF_QUAD)
        cameraY = cameraY.coerceIn(-HALF_QUAD, HALF_QUAD)

        if (cameraX in (0 - .01f)..(0 + .01f)) cameraX = 0f
        if (cameraY in (0 - .01f)..(0 + .01f)) cameraY = 0f
    }

    companion object {
        init {
            System.loadLibrary("shaded")
        }

        const val CAMERAZ = 2f
        const val QUAD_SIZE = 2f
        const val HALF_QUAD = QUAD_SIZE / 2
        const val SNAP_SENSITIVITY = 4f
    }
}
