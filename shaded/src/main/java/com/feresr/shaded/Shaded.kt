package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_DEPTH_TEST
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

class Shaded(
    private val context: Context,
    private val surfaceView: GLSurfaceView
) : GLSurfaceView.Renderer {

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
    private var matrix: Matrix? = null
    private var downScale: Int = 1
    private var viewportWidth = 0
    private var viewportHeight = 0
    private val filters = mutableListOf<Filter>()

    fun addFilter(filter: Filter) = filters.add(filter)
    fun removeFilter(filter: Filter) = filters.remove(filter)

    fun setBitmap(bitmap: Bitmap) {
        queue.add {
            originalTexture.setData(bitmap)
            previewPingPongRenderer?.initTextures(
                bitmap.width / downScale,
                bitmap.height / downScale
            )
        }
        refresh()
    }

    fun getBitmap(callback: (Bitmap?) -> Unit) {
        queue.add {
            //rescale the image up
            if (downScale != 1) previewPingPongRenderer?.initTextures(
                originalTexture.width(),
                originalTexture.height()
            )
            val bitmap =
                if (filters.size > 0) previewPingPongRenderer?.renderToBitmap(filters) else null
            handler.post { callback(bitmap) }
        }
        surfaceView.requestRender()
    }

    fun refresh() {
        queue.add { previewPingPongRenderer?.render(filters) }
        surfaceView.requestRender()
    }

    fun setMatrix(matrix: Matrix) {
        this.matrix = matrix
        queue.add { loadMatrix(matrix) }
        surfaceView.requestRender()
    }

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
        refresh()
    }

    private fun supportsOpenGLES(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x30000
    }

    private fun loadMatrix(matrix: Matrix?) {
        if (matrix == null) return
        screenRenderer?.setMatrix(matrix)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glDisable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)

        val vb = VertexBuffer()
        vb.bind()
        vb.uploadData(
            floatArrayOf(
                //position
                -1.0f, -1.0f,   //bottom left
                -1.0f, 1.0f,    //top left
                1.0f, -1.0f,    //bottom right
                1.0f, 1.0f,      //top right

                //uvs
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
        loadMatrix(matrix)
        previewPingPongRenderer?.render(filters)
    }

    override fun onDrawFrame(unused: GL10) {
        while (queue.isNotEmpty()) queue.take().invoke()
        glViewport(0, 0, viewportWidth, viewportHeight)
        val previewOutputTexture = previewPingPongRenderer?.outputTexture ?: originalTexture
        screenRenderer?.render(previewOutputTexture)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        this.viewportWidth = width
        this.viewportHeight = height
    }

    companion object {
        init {
            System.loadLibrary("shaded")
        }
    }
}
