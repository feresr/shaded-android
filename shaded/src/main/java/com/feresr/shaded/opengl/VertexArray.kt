package com.feresr.shaded.opengl

import android.opengl.GLES30.glBindVertexArray

class VertexArray {

    val id = initVertexArray()

    fun bind() = glBindVertexArray(id)
    fun unbind() = glBindVertexArray(0)

    fun delete() = deleteVertexArray(id)

    fun pointer(index: Int, size: Int, stride: Int, offset: Int) {
        bind()
        setupPointer(index, size, stride, offset)
    }

    private external fun setupPointer(index: Int, size: Int, stride: Int, offset: Int)
    private external fun initVertexArray(): Int
    private external fun deleteVertexArray(id: Int)

}
