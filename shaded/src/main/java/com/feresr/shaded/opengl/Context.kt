package com.feresr.shaded.opengl

import android.opengl.EGL14
import android.opengl.EGL14.EGL_NO_CONTEXT
import android.opengl.EGL14.EGL_NO_DISPLAY
import android.opengl.EGL14.EGL_NO_SURFACE
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import javax.microedition.khronos.egl.EGL10


class Context {

    data class EGL(
        val display: EGLDisplay,
        val surface: EGLSurface,
        val context: EGLContext
    )

    companion object {
        private var egl: EGL? = null

        fun init() {
            // Init display
            // A Display is just a connection to the native windowing system running on your computer
            val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL_NO_DISPLAY) {
                throw RuntimeException("Unable to open connection to local windowing system")
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                throw RuntimeException("Unable to initialize EGL")
            }

            // choose config
            val configAttr = intArrayOf(
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
            )

            val configs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfig = IntArray(1)
            EGL14.eglChooseConfig(
                display, configAttr, 0,
                configs, 0, 1, numConfig, 0
            )
            if (numConfig[0] == 0) throw RuntimeException("EGL Config not found!")
            val config: EGLConfig? = configs[0]

            // create a rendering surface (created just because it's required, not actually used)
            // You can guess that Surface‘s purpose is storing output of rendering.
            // Indeed, a Surface extends a native window or pixmap with additional auxiliary buffers.
            // These buffers include a color buffer, a depth buffer, and a stencil buffer.
            val surfAttr = intArrayOf(
                EGL14.EGL_WIDTH, 8,
                EGL14.EGL_HEIGHT, 8,
                EGL14.EGL_NONE
            )
            val surf: EGLSurface = EGL14.eglCreatePbufferSurface(display, config, surfAttr, 0)

            // create context
            // Context is nothing but a container that contains two things:
            //  - Internal state machine (view port, depth range, clear color, textures, VBO, FBO
            //  - A command buffer to hold GL commands that have been called in this context.
            val ctxAttrib = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val ctx: EGLContext =
                EGL14.eglCreateContext(display, config, EGL_NO_CONTEXT, ctxAttrib, 0)

            // make context current
            // eglMakeCurrent binds context to the current rendering thread and to the draw and read surfaces.
            EGL14.eglMakeCurrent(display, surf, surf, ctx)

            // you can make OpenGL calls now
            egl = EGL(display, surf, ctx)
        }


        fun tearDown() {
            val egl = this.egl ?: return

            EGL14.eglMakeCurrent(egl.display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(egl.display, egl.surface)
            EGL14.eglDestroyContext(egl.display, egl.context)
            EGL14.eglTerminate(egl.display)

            this.egl = null
        }
    }

}