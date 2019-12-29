package com.feresr.filters

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT

class FilterRenderer(
    val context: Context,
    private val surfaceView: GLSurfaceView,
    private val filters: List<Filter>
) : GLSurfaceView.Renderer {

    private var program: MainProgram? = null
    private var previewRenderer: Renderer? = null
    private var outputRenderer: Renderer? = null
    private var originalTexture: Int = 0

    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private var viewportWidth = 0
    private var viewportHeight = 0
    private val pendingRunnable: Queue<() -> Unit> = LinkedList()

    var color: Int = 0
        set(value) {
            field = value
            surfaceView.requestRender()
        }

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 3.0 is not supported on this phone." }
        surfaceView.apply {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            holder.setFormat(PixelFormat.RGBA_8888)
            setRenderer(this@FilterRenderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
    }

    private fun supportsOpenGLES(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x30000
    }

    fun updatePreview() {
        surfaceView.runOnOpenGLThread {
            previewRenderer?.render(filters)
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        surfaceView.runOnOpenGLThread {
            previewRenderer?.delete()
            outputRenderer?.delete()
            originalTexture = createTexture(bitmap)
            bitmapWidth = bitmap.width
            bitmapHeight = bitmap.height
            previewRenderer = Renderer(originalTexture, viewportWidth / 2, viewportHeight / 2)
            outputRenderer = Renderer(originalTexture, bitmapWidth, bitmapHeight)
            updatePreview()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        program = MainProgram(context)
        while (pendingRunnable.isNotEmpty()) {
            pendingRunnable.remove().invoke()
        }
    }

    override fun onDrawFrame(unused: GL10) {
        GLES30.glClearColor(
            Color.red(color) / 255f,
            Color.green(color) / 255f,
            Color.blue(color) / 255f,
            1f
        )
        GLES30.glClear(GL_COLOR_BUFFER_BIT)
        previewRenderer?.let {
            GLES30.glViewport(0, 0, viewportWidth, viewportHeight)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
            program?.render(it.outputTexture)
        }
        if (BuildConfig.DEBUG) Log.e("GL Error", GLES30.glGetError().toString())
    }

    fun getBitmap(callback: (Bitmap?) -> Unit) {
        surfaceView.runOnOpenGLThread {
            val bitmap = outputRenderer?.renderToBitmap(filters)
            callback(bitmap)
        }
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
    }

    fun setMatrix(matrix: Matrix) {
        surfaceView.runOnOpenGLThread {
            previewRenderer?.setMatrix(matrix)
            previewRenderer?.render(filters)
        }
    }

    fun destroy() {
        filters.forEach { it.clear() }
        previewRenderer?.delete()
        outputRenderer?.delete()
    }

    private fun GLSurfaceView.runOnOpenGLThread(block: () -> Unit) {
        if (program == null) {
            pendingRunnable.add(block)
        } else {
            this.queueEvent(block)
            surfaceView.requestRender()
        }
    }
}
