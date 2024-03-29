package com.feresr.shaded.opengl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_RGBA
import android.opengl.GLES30
import android.opengl.GLES30.GL_RGBA8
import android.opengl.GLES30.GL_TEXTURE_2D
import android.opengl.GLES30.GL_UNSIGNED_BYTE
import android.opengl.GLES30.glBindTexture
import android.opengl.GLES30.glTexImage2D
import android.opengl.GLU
import android.util.Log

class Texture(width: Int? = null, height: Int? = null) {

    val id = initTexture()

    private val internalFormat = GL_RGBA8
    private val dataFormat = GL_RGBA

    private var w: Int? = null
    private var h: Int? = null

    init {
        Log.e("TEXTURE JAVA Created", id.toString())
        // Zero is a reserved texture name and is never returned as a texture name by glGenTextures()
        if (id == 0) throw IllegalStateException("Texture id is 0, maybe you forgot to call eglMakeCurrent?")
        if (width != null && height != null) resize(width, height)
    }

    fun width(): Int {
        return w ?: throw IllegalStateException("Texture width not initialized")
    }

    fun height(): Int {
        return h ?: throw IllegalStateException("Texture height not initialized")
    }

    fun bind() = glBindTexture(GL_TEXTURE_2D, id)

    fun bind(scope: () -> Unit) {
        val previouslyBound = getBoundTexture()
        if (previouslyBound != id) bind()
        scope()
        if (previouslyBound != id) glBindTexture(GL_TEXTURE_2D, previouslyBound)
    }

    fun resize(width: Int, height: Int) {
        if (this.w == width && this.h == height) return
        if (width < 0 || height < 0) throw IllegalArgumentException("Texture width/height can't be less than 0")
        Log.e("shaded", "resizing texture to ${width}x${height}")
        this.w = width
        this.h = height
        bind()
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            internalFormat,
            width,
            height,
            0,
            dataFormat,
            GL_UNSIGNED_BYTE,
            null
        )
        val glError = GLES30.glGetError()
        if (glError != GLES20.GL_NO_ERROR) {
            throw RuntimeException("Failed to resize Texture: ${GLU.gluErrorString(glError)}");
        }
    }

    fun setData(bitmap: Bitmap) {
        Log.i("OpenGl", "setting data texture $id")
        bind()
        val success = storeBitmap(bitmap)
        if (!success) throw IllegalStateException("Could not store bitmap in texture $id")
        this.w = bitmap.width
        this.h = bitmap.height
        Log.i("OpenGl", "data texture $id set")
    }

    fun delete() {
        deleteTexture(id)
    }

    private external fun initTexture(): Int
    private external fun deleteTexture(id: Int)
    private external fun getBoundTexture(): Int
    private external fun storeBitmap(bitmap: Bitmap): Boolean

}
