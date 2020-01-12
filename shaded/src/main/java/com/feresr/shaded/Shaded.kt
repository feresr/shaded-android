package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Handler
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Shaded(
    private val context: Context,
    private val surfaceView: GLSurfaceView,
    private val filters: List<Filter>
) : GLSurfaceView.Renderer {

    private val handler = Handler()
    private var screenRenderer: ScreenRenderer? = null
    private var previewPingPongRenderer: PingPongRenderer? = null
    private var originalTexture: Int = 0
    private var bitmap: Bitmap? = null
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
        surfaceView.queueEvent { previewPingPongRenderer?.render(filters) }
        surfaceView.requestRender()
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        surfaceView.queueEvent { loadBitmap(bitmap) }
        requestPreviewRender()
    }

    fun setMatrix(matrix: Matrix) {
        this.matrix = matrix
        surfaceView.queueEvent { loadMatrix(matrix) }
        surfaceView.requestRender()
    }

    /**
     * Downscaling allows for faster rendering
     */
    fun downScale(downScale: Int) {
        if (this.downScale == downScale) return
        this.downScale = downScale
        surfaceView.queueEvent { loadBitmap(bitmap) }
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
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, originalTexture)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

        val previewWidth = bitmap.width / downScale
        val previewHeight = bitmap.height / downScale
        previewPingPongRenderer?.initTextures(previewWidth, previewHeight)
    }

    private fun loadMatrix(matrix: Matrix?) {
        if (matrix == null) return
        screenRenderer?.setMatrix(matrix)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        filters.forEach { it.init() }
        screenRenderer = ScreenRenderer(context)
        originalTexture = createTexture()
        previewPingPongRenderer = PingPongRenderer(originalTexture)
        loadBitmap(bitmap)
        loadMatrix(matrix)
        requestPreviewRender()
    }

    override fun onDrawFrame(unused: GL10) {
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
        surfaceView.queueEvent {
            val bmp = bitmap
            if (bmp == null) {
                handler.post { callback(null) }
                return@queueEvent
            }

            if (downScale != 1) previewPingPongRenderer?.initTextures(bmp.width, bmp.height)
            val bitmap = previewPingPongRenderer?.renderToBitmap(filters)
            handler.post { callback(bitmap) }
        }
        surfaceView.requestRender()
    }

    fun destroy() {
        filters.forEach { it.delete() }
        previewPingPongRenderer?.delete()
        screenRenderer?.delete()
    }
}
