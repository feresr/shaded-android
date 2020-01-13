package com.feresr.shaded

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES30
import android.opengl.GLU
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10

internal fun loadProgram(fragmentShader: String, vertexShader: String): Int {

    val iVShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, vertexShader)
    if (iVShader == 0) {
        Log.d("Load Program", "Vertex Shader Failed")
        return 0
    }
    val iFShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader)
    if (iFShader == 0) {
        Log.d("Load Program", "Fragment Shader Failed")
        return 0
    }
    val iProgId: Int = GLES30.glCreateProgram()
    GLES30.glAttachShader(iProgId, iVShader)
    GLES30.glAttachShader(iProgId, iFShader)
    GLES30.glLinkProgram(iProgId)

    val link = IntArray(1)
    GLES30.glGetProgramiv(iProgId, GLES30.GL_LINK_STATUS, link, 0)
    if (link[0] <= 0) {
        val glError = GLES30.glGetError()
        throw RuntimeException(
            "Failed to create program: " +
                    "glGetProgramInfoLog ${GLES30.glGetProgramInfoLog(iProgId)} " +
                    "glError $glError " +
                    "glErrorMessage ${GLU.gluErrorString(glError)}"
        )
    }
    GLES30.glDeleteShader(iVShader)
    GLES30.glDeleteShader(iFShader)

    return iProgId
}

internal fun loadShader(shaderType: Int, source: String): Int {
    val shader = GLES30.glCreateShader(shaderType)
    if (shader != 0) {
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Could not compile shader $shaderType:$info")
        }
    }
    return shader
}

internal fun createVerticesBuffer(vertices: FloatArray): FloatBuffer {
    if (vertices.size != 8) {
        throw RuntimeException("Number of vertices should be four.")
    }
    val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    buffer.put(vertices).position(0)
    return buffer
}

internal fun createTextures(n: Int): IntArray {
    val textures = IntArray(n)
    GLES30.glGenTextures(n, textures, 0)
    val glError = GLES30.glGetError()
    if (glError != 0) throw RuntimeException("OpenGL error: $glError - ${GLU.gluErrorString(glError)}")
    for (texture in textures) {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )
    }
    return textures
}

fun createTexture(): Int {
    val textureHandle = IntArray(1)
    GLES30.glGenTextures(1, textureHandle, 0)

    val glError = GLES30.glGetError()
    if (glError != 0) throw RuntimeException(
        "Cannot create GL texture GLError on " +
                " 'createTexture': ${GLU.gluErrorString(glError)}" +
                " thread: ${Thread.currentThread().name}" +
                " error: $glError"
    )
    val texture = textureHandle[0]

    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
    GLES30.glTexParameteri(
        GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_MAG_FILTER,
        GLES30.GL_LINEAR
    )
    GLES30.glTexParameteri(
        GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_MIN_FILTER,
        GLES30.GL_LINEAR
    )
    GLES30.glTexParameteri(
        GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_WRAP_S,
        GLES30.GL_CLAMP_TO_EDGE
    )
    GLES30.glTexParameteri(
        GLES30.GL_TEXTURE_2D,
        GLES30.GL_TEXTURE_WRAP_T,
        GLES30.GL_CLAMP_TO_EDGE
    )
    return texture
}

internal fun attachTextureToFBO(framebuffer: Int, texture: Int) {
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebuffer)
    // Write to this texture
    GLES30.glFramebufferTexture2D(
        GLES30.GL_FRAMEBUFFER,
        GLES30.GL_COLOR_ATTACHMENT0,
        GL10.GL_TEXTURE_2D,
        texture,
        0
    )
    if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
        val glError = GLES30.glGetError()
        val isOpenGLThread =
            (EGLContext.getEGL() as EGL10).eglGetCurrentContext() != EGL10.EGL_NO_CONTEXT
        throw RuntimeException(
            "Failed to create program (attachTextureToFBO): glCheckFramebufferStatus - ${GLES30.glCheckFramebufferStatus(
                GLES30.GL_FRAMEBUFFER
            )} -glError $glError - glErrorMessage ${GLU.gluErrorString(glError)}, texture: $texture, frameBuffer: $framebuffer, isOpenGLThread: $isOpenGLThread"
        )
    }
    GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
}

fun supportsOpenGLES(context: Context): Boolean {
    val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val configurationInfo = activityManager.deviceConfigurationInfo
    return configurationInfo.reqGlEsVersion >= 0x20000
}
