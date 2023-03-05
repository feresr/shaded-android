package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.EGLSurface
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glDisable
import android.opengl.GLES30
import android.opengl.GLU
import android.util.Log
import android.view.SurfaceView
import com.feresr.shaded.opengl.EglCore
import com.feresr.shaded.opengl.EglCore.FLAG_TRY_GLES3
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.coroutines.CoroutineContext

/* This class defines its own CoroutineContext with a SupervisorJob() and a single threaded
 * dispatcher. This allows the class to be used independently of the caller scope.
 * Disposing the job [dispose] will take care of cancelling any ongoing GL tasks
 */
class Shaded(private val context: Context) {

    private lateinit var previewPingPongRenderer: PingPongRenderer
    private val dispatcher = GLDispatcher.instance
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.e(Shaded::class.java.simpleName, e.toString())
    }

    private val scope = object : CoroutineScope {
        override val coroutineContext = SupervisorJob() + dispatcher + exceptionHandler
    }

    lateinit var core: EglCore

    init {
        scope.launch(scope.coroutineContext) {
            Log.e("Shaded", "Init called...")
            core = EglCore(null, FLAG_TRY_GLES3);
            //GLContext.init(core.mEGLContext)
            glDisable(GL_BLEND) //blend is disabled
            glDisable(GL_DEPTH_TEST)
            //glEnable(GL_SCISSOR_TEST);
            var glError = GLES30.glGetError()
            if (glError != 0) {
                throw RuntimeException(
                    "Failed to create initialise GL: " +
                            "glError $glError " +
                            "glErrorMessage ${GLU.gluErrorString(glError)}"
                )
            }
            previewPingPongRenderer = PingPongRenderer(Filter(context))
            glError = GLES30.glGetError()
            if (glError != 0) {
                throw RuntimeException(
                    "Failed to create Ping pong renderer GL: " +
                            "glError $glError " +
                            "glErrorMessage ${GLU.gluErrorString(glError)}"
                )
            }

            try {
                Log.e("Shaded", "Initialised successfully...")
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    Log.d("Shaded", "GL context tear down")
                    previewPingPongRenderer.delete()
                    surfaceMap.forEach { core.releaseSurface(it.value) }
                    surfaceMap.clear()
                    core.release()
                }
            }
        }
    }

    private var bmp: Bitmap? = null
    fun upload(bitmap: Bitmap) = runBlocking(scope.coroutineContext) {
        // todo: uploading in render this fixes it
        previewPingPongRenderer.setData(bitmap)
        bmp = bitmap

    }

    private val surfaceMap = mutableMapOf<SurfaceView, EGLSurface>()
    private fun setTarget(target: SurfaceView) {
        // todo?
        val surface = surfaceMap.getOrPut(target) { core.createWindowSurface(target.holder.surface) }
        core.makeCurrent(surface)
    }

    fun render(target: SurfaceView, filters: Collection<Filter>, rect: Rect? = null) =
        runBlocking(scope.coroutineContext) {
            setTarget(target)
            previewPingPongRenderer.render(target, filters, rect)
            core.swapBuffers(surfaceMap[target])
        }

    fun dispose() {
        scope.cancel("Dispose called")
    }

    companion object {
        init {
            System.loadLibrary("shaded")
        }
    }
}

class GLDispatcher : CoroutineDispatcher() {
    private val executor = newSingleThreadExecutor()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.submit(block)
    }

    companion object {
        val instance: GLDispatcher by lazy { GLDispatcher() }
    }
}
