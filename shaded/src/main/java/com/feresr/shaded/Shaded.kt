package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.View
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL10.GL_COLOR_BUFFER_BIT

class Shaded(
    private val context: Context,
    private val surfaceView: GLSurfaceView,
    private val filters: List<Filter>
) : GLSurfaceView.Renderer {

    private var screenRenderer: ScreenRenderer? = null
    private var previewPingPongRenderer: PingPongRenderer? = null
    private var outputPingPongRenderer: PingPongRenderer? = null
    private var originalTexture: Int = 0

    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private var viewportWidth = 0
    private var viewportHeight = 0
    private val glEventQueue: Queue<() -> Unit> = ConcurrentLinkedQueue()

    var color: Int = 0
        set(value) {
            field = value
            if (surfaceView.isAttachedToWindow) surfaceView.requestRender()
        }

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 3.0 is not supported on this phone." }
        configureSurfaceView()
    }

    private fun configureSurfaceView() {
        surfaceView.apply {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            holder.setFormat(PixelFormat.RGBA_8888)
            setRenderer(this@Shaded)
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
        glEventQueue.add { previewPingPongRenderer?.render(filters) }
        if (surfaceView.isAttachedToWindow) surfaceView.requestRender()
    }

    fun setBitmap(bitmap: Bitmap) {
        glEventQueue.add {
            screenRenderer = ScreenRenderer(context)
            previewPingPongRenderer?.delete()
            outputPingPongRenderer?.delete()
            originalTexture = createTexture(bitmap)
            bitmapWidth = bitmap.width
            bitmapHeight = bitmap.height
            previewPingPongRenderer =
                PingPongRenderer(originalTexture, viewportWidth / 2, viewportHeight / 2)
            outputPingPongRenderer = PingPongRenderer(originalTexture, bitmapWidth, bitmapHeight)
            updatePreview()
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
    }

    override fun onDrawFrame(unused: GL10) {
        while (glEventQueue.isNotEmpty()) glEventQueue.remove().invoke()
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
        glEventQueue.add {
            val bitmap = outputPingPongRenderer?.renderToBitmap(filters)
            callback(bitmap)
        }
        if (surfaceView.isAttachedToWindow) surfaceView.requestRender()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
    }

    fun setMatrix(matrix: Matrix) {
        glEventQueue.add {
            previewPingPongRenderer?.setMatrix(matrix)
            previewPingPongRenderer?.render(filters)
        }
        if (surfaceView.isAttachedToWindow) surfaceView.requestRender()
    }

    fun destroy() {
        filters.forEach { it.clear() }
        previewPingPongRenderer?.delete()
        outputPingPongRenderer?.delete()
    }
}
