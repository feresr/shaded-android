package com.feresr.shaded

import android.content.Context
import android.graphics.Matrix
import android.opengl.GLES10.glDrawArrays
import android.opengl.GLES30.GL_TRIANGLE_STRIP
import com.feresr.shaded.opengl.FrameBuffer
import com.feresr.shaded.opengl.Shader
import com.feresr.shaded.opengl.Texture

internal class ScreenRenderer(context: Context) {

    private val screenFrameBuffer = FrameBuffer(true)
    private var shader = Shader(
        "",
        context.resources.openRawResource(R.raw.fragment).reader().readText(),
        context.resources.openRawResource(R.raw.vertexscreen).reader().readText()
    )

    fun render(readFromTexture: Texture) {
        shader.bind {
            readFromTexture.bind()
            screenFrameBuffer.bind()
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
            screenFrameBuffer.unbind()
        }
    }

    fun setMatrix(matrix: Matrix) {
        val array = FloatArray(8)
        //TODO: matrix.mapPoints(array, TEX_VERTICES)
    }

    fun delete() {
        shader.delete()
        screenFrameBuffer.delete()
    }
}
