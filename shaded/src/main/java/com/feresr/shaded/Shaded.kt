package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Handler
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT

class Shaded(
    private val context: Context,
    private val surfaceView: GLSurfaceView,
    private val filters: List<Filter>
) : GLSurfaceView.Renderer {

    private val handler = Handler()
    private var screenRenderer: ScreenRenderer? = null
    private var previewPingPongRenderer: PingPongRenderer? = null
    private var outputPingPongRenderer: PingPongRenderer? = null
    private var originalTexture: Int = 0
    private var bitmap: Bitmap? = null
    private var downScale: Int = 1

    private var viewportWidth = 0
    private var viewportHeight = 0

    var color: Int = 0
        set(value) {
            field = value
            surfaceView.requestRender()
        }

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 2.0 is not supported on this phone." }
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        surfaceView.requestRender()
    }

    private fun supportsOpenGLES(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x20000
    }

    fun requestRender() {
        surfaceView.queueEvent { previewPingPongRenderer?.render(filters) }
        surfaceView.requestRender()
    }

    fun setBitmap(bitmap: Bitmap, downScale: Int = 1) {
        this.bitmap = bitmap
        this.downScale = downScale
        surfaceView.queueEvent { loadBitmap(bitmap, downScale) }
        requestRender()
    }

    /**
     * Loads the bitmap data into OpenGL texture [originalTexture]
     * Loads the bitmap data into the internal [PingPongRenderer] textures with new dimensions
     *
     * @param bitmap the bitmap to be loaded
     * @param downScale how much to reduce the size for the preview, this results in better
     * performance. ([getBitmap] always returns the original bitmap size)
     *
     * This is meant to be called on a thread with an OpenGL context attached.
     */
    private fun loadBitmap(bitmap: Bitmap?, downScale: Int = 1) {
        if (bitmap == null) return
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, originalTexture)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

        val previewWidth = bitmap.width / downScale
        val previewHeight = bitmap.height / downScale
        previewPingPongRenderer?.initTextures(previewWidth, previewHeight)
        outputPingPongRenderer?.initTextures(bitmap.width, bitmap.height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        filters.forEach { it.init() }
        screenRenderer = ScreenRenderer(context)
        originalTexture = createTexture()
        previewPingPongRenderer = PingPongRenderer(originalTexture)
        outputPingPongRenderer = PingPongRenderer(originalTexture)
        loadBitmap(bitmap, downScale)
        previewPingPongRenderer?.render(filters)
    }

    override fun onDrawFrame(unused: GL10) {
        GLES30.glClearColor(
            Color.red(color) / 255f,
            Color.green(color) / 255f,
            Color.blue(color) / 255f,
            1f
        )
        GLES30.glClear(GL_COLOR_BUFFER_BIT)
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight)

        val previewOutputTexture = previewPingPongRenderer?.outputTexture ?: return
        screenRenderer?.render(previewOutputTexture)
    }

    fun getBitmap(callback: (Bitmap?) -> Unit) {
        surfaceView.queueEvent {
            val bitmap = outputPingPongRenderer?.renderToBitmap(filters)
            handler.post { callback(bitmap) }
        }
        surfaceView.requestRender()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
    }

    fun setMatrix(matrix: Matrix) {
        surfaceView.queueEvent {
            previewPingPongRenderer?.setMatrix(matrix)
            previewPingPongRenderer?.render(filters)
        }
        if (surfaceView.isAttachedToWindow) surfaceView.requestRender()
    }

    fun destroy() {
        previewPingPongRenderer?.delete()
        outputPingPongRenderer?.delete()
    }
}
