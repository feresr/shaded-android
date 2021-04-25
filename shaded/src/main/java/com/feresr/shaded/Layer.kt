package com.feresr.shaded

import com.feresr.shaded.opengl.VertexArray
import com.feresr.shaded.opengl.VertexBuffer

internal class Layer {

    private val vb = VertexBuffer()
    private val va = VertexArray()

    init {
        vb.bind()
        vb.uploadData(positions + uvs)

        va.bind()
        va.pointer(0, 2, 0, 0) // positions
        va.pointer(1, 2, 0, 8) // uv's

        vb.unbind()
        va.unbind()
    }

    fun bind() = va.bind()

    companion object {
        val positions = floatArrayOf(
            -1.0f, -1.0f,   // bottom left
            -1.0f, 1.0f,    // top left
            1.0f, -1.0f,    // bottom right
            1.0f, 1.0f,     // top right
        )

        val uvs = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
    }

}
