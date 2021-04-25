package com.feresr.shaded.opengl

internal class VertexBuffer {
    private val id = initVertexBuffer()

    fun bind() = bind(id)
    fun unbind() = bind(0)

    fun uploadData(data: FloatArray) {
        setData(data)
    }

    private external fun initVertexBuffer(): Int
    private external fun setData(data: FloatArray)
    private external fun bind(id: Int)
}
