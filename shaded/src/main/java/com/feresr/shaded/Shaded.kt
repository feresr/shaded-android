package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Shaded(
    private val context: Context,
    private val surfaceView: GLSurfaceView,
    private val filters: List<Filter>
) : GLSurfaceView.Renderer {

    /**
     * Using own Queue implementation as opposed to [GLSurfaceView.queueEvent].
     * [GLSurfaceView.queueEvent] will invoke the runnable right away, even after the GLThread is
     * paused by and [GLSurfaceView.onPause] the gl context is lost.
     * See [GLSurfaceView] guarded run method.
     */
    private val queue: BlockingQueue<() -> Unit> = LinkedBlockingQueue<() -> Unit>()
    private val handler = Handler()
    private var screenRenderer: ScreenRenderer? = null
    private var previewPingPongRenderer: PingPongRenderer? = null
    private var originalTexture: Int = 0
    private var matrix: Matrix? = null
    private var downScale: Int = 1
    private var viewportWidth = 0
    private var viewportHeight = 0

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 2.0 is not supported on this device." }
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    fun requestPreviewRender() {
        queue.add { previewPingPongRenderer?.render(filters) }
        surfaceView.requestRender()
    }

    fun setBitmap(bitmap: Bitmap) {
        queue.add { loadBitmap(bitmap) }
        requestPreviewRender()
    }

    fun setMatrix(matrix: Matrix) {
        this.matrix = matrix
        queue.add { loadMatrix(matrix) }
        surfaceView.requestRender()
    }

    /**
     * Downscaling allows for faster rendering
     */
    fun downScale(downScale: Int) {
        if (this.downScale == downScale) return
        this.downScale = downScale
        queue.add {
            if (PingPongRenderer.isBitmapStored()) {
                previewPingPongRenderer?.initTextures(
                    PingPongRenderer.getBitmapWidth() / downScale,
                    PingPongRenderer.getBitmapHeight() / downScale
                )
            }
        }
        requestPreviewRender()
    }

    /**
     * Loads the bitmap data into OpenGL texture [originalTexture]
     * Loads the bitmap data into the internal preview [PingPongRenderer]s.
     *
     * @param bitmap the bitmap to be loaded
     * @param downScale downsamples the image size for better performance.
     * ([getBitmap] is not affected by this and will always returns the original bitmap size.
     *
     * This is meant to be called on a thread with an OpenGL context attached.
     */
    private fun loadBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        PingPongRenderer.storeBitmap(bitmap)
        PingPongRenderer.loadIntoOpenGl(originalTexture)
        previewPingPongRenderer?.initTextures(
            bitmap.width / downScale,
            bitmap.height / downScale
        )
    }

    private fun loadMatrix(matrix: Matrix?) {
        if (matrix == null) return
        screenRenderer?.setMatrix(matrix)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)

        originalTexture = createTexture()
        PingPongRenderer.loadIntoOpenGl(originalTexture)

        filters.forEach { it.init() }

        screenRenderer = ScreenRenderer(context)
        previewPingPongRenderer = PingPongRenderer(originalTexture)
        if (PingPongRenderer.isBitmapStored()) {
            previewPingPongRenderer?.initTextures(
                PingPongRenderer.getBitmapWidth(),
                PingPongRenderer.getBitmapHeight()
            )
            loadMatrix(matrix)
            previewPingPongRenderer?.render(filters)
        }

    }

    override fun onDrawFrame(unused: GL10) {
        while (queue.isNotEmpty()) queue.take().invoke()
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight)
        val previewOutputTexture = previewPingPongRenderer?.outputTexture ?: originalTexture
        screenRenderer?.render(previewOutputTexture)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
    }

    /**
     * Renders the current frame into a bitmap.
     * It will re initialize the textures on the ping-pong renderer in order to make it match the
     * dimensions of the original bitmap. (This has no effect if downScale == 1)
     */
    fun getBitmap(callback: (Bitmap?) -> Unit) {
        queue.add {
            if (!PingPongRenderer.isBitmapStored()) {
                handler.post { callback(null) }
                return@add
            }
            if (downScale != 1) previewPingPongRenderer?.initTextures(
                PingPongRenderer.getBitmapWidth(),
                PingPongRenderer.getBitmapHeight()
            )
            val bitmap = previewPingPongRenderer?.renderToBitmap(filters)
            handler.post { callback(bitmap) }
        }
        surfaceView.requestRender()
    }

    fun destroy() {
        PingPongRenderer.freeBitmap()
        queue.clear()
        filters.forEach { it.delete() }
        previewPingPongRenderer?.delete()
        screenRenderer?.delete()
    }
}
