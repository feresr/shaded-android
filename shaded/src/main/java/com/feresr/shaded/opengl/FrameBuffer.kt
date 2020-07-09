package com.feresr.shaded.opengl

import android.graphics.Bitmap
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES30.GL_COLOR_ATTACHMENT0
import android.opengl.GLES30.GL_FRAMEBUFFER_COMPLETE
import android.opengl.GLES30.glBindFramebuffer
import android.opengl.GLES30.glCheckFramebufferStatus
import android.opengl.GLES30.glFramebufferTexture2D
import javax.microedition.khronos.opengles.GL10

class FrameBuffer(private val isScreenBuffer: Boolean = false) {

    private val id: Int = if (isScreenBuffer) 0 else initFrameBuffer()
    private var colorAttachment: Texture? = null

    fun bind() = glBindFramebuffer(GL_FRAMEBUFFER, id)
    fun unbind() = glBindFramebuffer(GL_FRAMEBUFFER, 0)

    fun setColorAttachment(texture: Texture, index: Int = 0) {
        if (isScreenBuffer) throw java.lang.IllegalStateException("Can't update color attachment of the default screen buffer")
        bind()
        glFramebufferTexture2D(
            GL_FRAMEBUFFER,
            GL_COLOR_ATTACHMENT0 + index,
            GL10.GL_TEXTURE_2D,
            texture.id,
            0
        )
        if (!isComplete()) throw IllegalStateException("Could not attach color texture for framebuffer $id")
        this.colorAttachment = texture
    }

    fun isComplete(): Boolean {
        bind()
        return glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE
    }

    fun getBitmap(): Bitmap {
        val color = colorAttachment
            ?: throw IllegalStateException("Could get bitmap, framebuffer $id has no color attachment")
        val bitmap = Bitmap.createBitmap(color.width(), color.height(), Bitmap.Config.ARGB_8888)
        bind()
        val success = getBitmap(bitmap)
        if (!success) throw IllegalStateException("Could not retrieve bitmap from framebuffer $id")
        unbind()
        return bitmap
    }

    fun delete() {
        if (!isScreenBuffer) deleteFrameBuffer(id)
    }

    private external fun initFrameBuffer(): Int
    private external fun getBitmap(bitmap: Bitmap): Boolean
    private external fun deleteFrameBuffer(id: Int)
}
