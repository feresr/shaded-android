package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView
import android.os.Handler
import com.feresr.shaded.opengl.Texture
import com.feresr.shaded.opengl.VertexArray
import com.feresr.shaded.opengl.VertexBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.atan

class Shaded(private val context: Context) : GLSurfaceView.Renderer {

    /**
     * Using own Queue implementation as opposed to [GLSurfaceView.queueEvent].
     * [GLSurfaceView.queueEvent] will invoke the runnable right away, even after the GLThread is
     * paused by and [GLSurfaceView.onPause] the gl context is lost.
     * See [GLSurfaceView] guarded run method.
     * Also, [GLSurfaceView.queueEvent] says "Queue a runnable to be run on the GL rendering thread"
     * that proved to be a lie:
     * Also: https://stackoverflow.com/questions/18827048/glsurfaceview-queueevent-does-not-execute-in-the-gl-thread
     */
    private val queue: BlockingQueue<() -> Unit> = LinkedBlockingQueue<() -> Unit>()
    private val handler = Handler()
    private var screenRenderer: ScreenRenderer? = null
    private var previewPingPongRenderer: PingPongRenderer? = null
    private val originalTexture: Texture by lazy { Texture() }
    private var downScale: Int = 1
    private var viewportWidth = 0
    private var viewportHeight = 0
    private val filters = mutableListOf<Filter>()

    private var cameraX = 0.0f
    private var cameraY = 0.0f

    private var zoom = 1f
    private val snapYTo = atan(HALF_QUAD / CAMERAZ) * (180f / PI.toFloat()) * 2
    private var fov = snapYTo

    fun changeZoomBy(z: Float) {
        zoom *= z
        zoom = zoom.coerceIn(.5f, 4f)
        fov = snapYTo / zoom
        checkBounds()
    }

    fun moveCameraBy(x: Float, y: Float) {
        cameraX += (x * QUAD_SIZE / viewportHeight) / zoom
        cameraY += (y * QUAD_SIZE / viewportHeight) / zoom
        checkBounds()
    }

    private fun checkBounds() {
        if (fov in (snapYTo - SNAP_SENSITIVITY)..(snapYTo + SNAP_SENSITIVITY)) fov = snapYTo

        cameraX = cameraX.coerceIn(-HALF_QUAD, HALF_QUAD)
        cameraY = cameraY.coerceIn(-HALF_QUAD, HALF_QUAD)

        if (cameraX in (0 - .01f)..(0 + .01f)) cameraX = 0f
        if (cameraY in (0 - .01f)..(0 + .01f)) cameraY = 0f
    }

    fun addFilter(filter: Filter) = filters.add(filter)
    fun removeFilter(filter: Filter) = filters.remove(filter)

    fun setBitmap(bitmap: Bitmap, recycle: Boolean) {
        queue.add {
            originalTexture.setData(bitmap)
            previewPingPongRenderer?.initTextures(
                bitmap.width / downScale,
                bitmap.height / downScale
            )
            if (recycle) bitmap.recycle()
        }
        render()
    }

    fun getBitmap(withFilters: List<Filter> = filters, callback: (Bitmap?) -> Unit) {
        queue.add {
            //rescale the image up
            if (downScale != 1) previewPingPongRenderer?.initTextures(
                originalTexture.width(),
                originalTexture.height()
            )
            val bitmap = if (filters.size > 0) previewPingPongRenderer?.renderToBitmap(withFilters) else null
            handler.post { callback(bitmap) }
        }
    }

    fun render() = queue.add { previewPingPongRenderer?.render(filters) }

    fun dispose() {
        queue.clear()
        filters.forEach { it.delete() }
        previewPingPongRenderer?.delete()
        screenRenderer?.delete()
        originalTexture.delete()
    }

    /**
     * Downscaling allows for faster rendering
     */
    fun downScale(downScaleFactor: Int) {
        if (this.downScale == downScaleFactor) return
        this.downScale = downScaleFactor
        queue.add {
            previewPingPongRenderer?.initTextures(
                originalTexture.width() / downScaleFactor,
                originalTexture.height() / downScaleFactor
            )
        }
        render()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glDisable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)

        val vb = VertexBuffer()
        vb.bind()
        vb.uploadData(
            floatArrayOf(
                // position
                -1.0f, -1.0f,   //bottom left
                -1.0f, 1.0f,    //top left
                1.0f, -1.0f,    //bottom right
                1.0f, 1.0f,     //top right

                // UV's
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
            )
        )

        val va = VertexArray()
        va.bind()
        va.pointer(0, 2, 0, 0) // positions
        va.pointer(1, 2, 0, 8) // uv's

        while (queue.isNotEmpty()) queue.take().invoke()

        screenRenderer = ScreenRenderer(context)
        previewPingPongRenderer = PingPongRenderer(originalTexture)
        previewPingPongRenderer?.initTextures(
            originalTexture.width(),
            originalTexture.height()
        )
        previewPingPongRenderer?.render(filters)
    }

    override fun onDrawFrame(unused: GL10) {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        while (queue.isNotEmpty()) queue.take().invoke()
        previewPingPongRenderer?.let {
            glViewport(0, 0, viewportWidth, viewportHeight)
            screenRenderer?.render(
                it.getOutputTexture(),
                viewportWidth.toFloat() / viewportHeight.toFloat(),
                it.width.toFloat() / it.height.toFloat(),
                fov,
                cameraX,
                cameraY,
                CAMERAZ
            )
        }

    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        this.viewportWidth = width
        this.viewportHeight = height
    }

    fun clearFilters() {
        filters.clear()
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
