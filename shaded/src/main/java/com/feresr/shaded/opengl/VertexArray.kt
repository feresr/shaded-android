package com.feresr.shaded.opengl

internal class VertexArray {

    val id = initVertexArray()

    fun bind() = bind(id)
    fun unbind() = bind(0)

    fun delete() = deleteVertexArray(id)

    fun pointer(index: Int, size: Int, stride: Int, offset: Int) {
        setupPointer(index, size, stride, offset)
    }

    private external fun setupPointer(index: Int, size: Int, stride: Int, offset: Int)
    private external fun initVertexArray(): Int
    private external fun bind(id: Int)
    private external fun deleteVertexArray(id: Int)

}
