package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ShadedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    private val renderer = Shaded(context)

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 3.0 not supported on this device." }
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        setRenderer(renderer)
        if (BuildConfig.DEBUG) debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setBitmap(bitmap: Bitmap) {
        renderer.setBitmap(bitmap)
        requestRender()
    }

    fun getBitmap(callback: (Bitmap?) -> Unit) {
        renderer.getBitmap(callback)
        requestRender()
    }

    fun addFilter(filter: Filter) = renderer.addFilter(filter)
    fun removeFilter(filter: Filter) = renderer.removeFilter(filter)

    fun refresh() {
        renderer.render()
        requestRender()
    }

    fun scale(factor: Int) {
        renderer.downScale(factor)
        requestRender()
    }

    override fun onDetachedFromWindow() {
        renderer.dispose()
        super.onDetachedFromWindow()
    }

    fun setZoom(z : Float) {
        renderer.zoom *= z
        renderer.zoom = renderer.zoom.coerceIn(.25f, 4f)
        requestRender()
    }

    private fun supportsOpenGLES(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x30000
    }
}
