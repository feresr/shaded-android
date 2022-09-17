package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glDisable
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors.newSingleThreadExecutor
import kotlin.coroutines.CoroutineContext

/* This class defines its own CoroutineContext with a SupervisorJob() and a single threaded
 * dispatcher. This allows the class to be used independently of the caller scope.
 * Disposing the job [dispose] will take care of cancelling any ongoing GL tasks
 */
// todo: Add exception handler here?

suspend fun render(context: Context, block: suspend Renderer.() -> Unit) {
    val shaded = Shaded(context)
    shaded.init()
    try {
        shaded.block()
    } finally {
        shaded.dispose()
    }
}

interface Renderer {
    suspend fun upload(bitmap: Bitmap)
    suspend fun render(target: Bitmap, filters: List<Filter>)
}

private class Shaded(private val context: Context) : Renderer {

    private lateinit var previewPingPongRenderer: PingPongRenderer
    private val coroutineContext: CoroutineContext =
        NonCancellable + newSingleThreadExecutor().asCoroutineDispatcher() + // OpenGL thread
                CoroutineExceptionHandler { _, e ->
                    Log.e(
                        Shaded::class.java.simpleName,
                        e.toString()
                    )
                }

    suspend fun init() = withContext(coroutineContext) {
        com.feresr.shaded.opengl.Context.init()
        glDisable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        Layer().bind()
        previewPingPongRenderer = PingPongRenderer(Filter(context))
    }

    override suspend fun upload(bitmap: Bitmap) = withContext(coroutineContext) {
        previewPingPongRenderer.setData(bitmap)
    }

    override suspend fun render(target: Bitmap, filters: List<Filter>): Unit =
        withContext(coroutineContext) {
            previewPingPongRenderer.render(target, filters)
        }

    suspend fun dispose() = withContext(coroutineContext) {
        previewPingPongRenderer.delete()
        com.feresr.shaded.opengl.Context.tearDown()
    }

    companion object {
        init {
            System.loadLibrary("shaded")
        }
    }
}

class GLDispatcher : CoroutineDispatcher() {
    private val executor = newSingleThreadExecutor()

    init {
        executor.submit {
            com.feresr.shaded.opengl.Context.init()
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        executor.submit(block)
    }

    companion object {
        val instance: GLDispatcher by lazy { GLDispatcher() }
    }
}
