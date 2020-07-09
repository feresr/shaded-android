package com.feresr.shaded.opengl

import android.opengl.GLES20.GL_ARRAY_BUFFER
import android.opengl.GLES20.glBindBuffer


class VertexBuffer {
    private val id = initVertexBuffer();

    fun bind() = glBindBuffer(GL_ARRAY_BUFFER, id)
    fun unbind() = glBindBuffer(GL_ARRAY_BUFFER, 0)


    fun uploadData(data: FloatArray) {
        bind()
        setData(data)
    }

    private external fun initVertexBuffer(): Int
    private external fun setData(data: FloatArray)
}
