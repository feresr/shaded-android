package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ShadedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    init {
        check(supportsOpenGLES(context)) { "OpenGL ES 3.0 not supported on this device." }
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        if (BuildConfig.DEBUG) debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS

    }

    override fun setRenderer(renderer: Renderer?) {
        super.setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }



    private fun supportsOpenGLES(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= 0x30000
    }
}
