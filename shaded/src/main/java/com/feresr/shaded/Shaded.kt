package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.glDisable
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors.*
import kotlin.coroutines.CoroutineContext

class Shaded(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() +
            newSingleThreadExecutor().asCoroutineDispatcher() + // OpenGL thread
            CoroutineExceptionHandler { _, e -> Log.e(Shaded::class.java.simpleName, e.toString()) }

    private lateinit var previewPingPongRenderer: PingPongRenderer

    init {
        launch {
            com.feresr.shaded.opengl.Context.init()
            glDisable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            Layer().bind()
            previewPingPongRenderer = PingPongRenderer(Filter(context))
        }
    }

    suspend fun upload(bitmap: Bitmap) = withContext(coroutineContext) {
        previewPingPongRenderer.setData(bitmap)
    }

    suspend fun render(target: Bitmap, filters: () -> List<Filter>) = withContext(coroutineContext) {
        previewPingPongRenderer.render(target, filters())
    }

    fun dispose() {
        // launch a coroutine independently of the parent scope (to avoid it being cancelled)
        launch {
            previewPingPongRenderer.delete()
            com.feresr.shaded.opengl.Context.tearDown()
            this@Shaded.cancel()
        }
    }

    companion object {
        init {
            System.loadLibrary("shaded")
        }
    }
}
