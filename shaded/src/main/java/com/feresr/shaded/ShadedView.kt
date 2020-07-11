package com.feresr.shaded

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ShadedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    val renderer = Shaded(context, this)

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        setRenderer(renderer)
        if (BuildConfig.DEBUG) debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onDetachedFromWindow() {
        renderer.dispose()
        super.onDetachedFromWindow()
    }
}
