package com.feresr.shaded

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PointF
import android.opengl.GLES31Ext
import android.opengl.GLES32
import android.opengl.GLU
import android.util.Log
import com.feresr.shaded.R
import com.feresr.shaded.ScreenRenderer
import com.feresr.shaded.attachTextureToFBO
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

internal class BrushRenderer(private val context: Context) {

    private var transformedTextureCords = createVerticesBuffer(TEX_VERTICES)

    private val frameBuffers = IntArray(1)

    var brushTexture = 0
    var outputTexture = 0

    private val screenRenderer = ScreenRenderer(context)
    private var program: Int = 0
    private var posCoordHandle = 0

    private val brushBitmap: Bitmap =
        BitmapFactory.decodeResource(
            context.resources,
            R.drawable.blurdot
        )


    private var capacity = 1
    private var bufferIndex = 0
    private var buffer = ByteBuffer.allocateDirect(capacity * 2 * SIZE_OF_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    //private val pointInterpolator: PointInterpolator = PointInterpolator()

    private val dots: MutableList<PointF> = mutableListOf()

    fun clearPoints() {
        dots.clear()
        render()
    }

    var firstPointAdded = false;
    fun addPoint(x: Float, y: Float) {

        firstPointAdded = true
        val new = PointF((x -.5f) * 2, (y - .5f) * 2f)
        buffer.put(0, new.x)
        buffer.put(1, new.y)

//        val new = PointF((x - .5f) * 2, (y - .5f) * -2)
//        if (dots.size < 4) {
//            dots.add(new)
//            return
//        }
//        val last = dots.last()
//        val distance = (last - new).length()
//        if (distance > .01f) {
//            dots.add(new)
//            pointInterpolator
//                .interpolateSpline(1f, dots.takeLast(4))
//                .forEach {
//                    buffer.put(bufferIndex * 2, it.x)
//                    buffer.put((bufferIndex * 2) + 1, it.y)
//                    bufferIndex += 1
//                }
//        }

        if (bufferIndex * 2 >= capacity) {
            capacity *= 2

//            val arr = buffer.array()
//            ByteArrayOutputStream(capacity * 2 * SIZEOFFLOAT).write
//            buffer = ByteBuffer.allocateDirect(capacity * 2 * SIZEOFFLOAT)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer()
//            buffer.put(arr)
        }
    }

    fun init() {
        brushTexture = com.feresr.shaded.createTexture()
        program = loadProgram(
            context.resources.openRawResource(R.raw.brush).reader().readText(),
            context.resources.openRawResource(R.raw.vertex_brush).reader().readText(),
            context.resources.openRawResource(R.raw.geometry).reader().readText()
        )
        posCoordHandle = GLES32.glGetAttribLocation(program, "a_position")

        val buff = IntBuffer.allocate(brushBitmap.width * brushBitmap.height)
        brushBitmap.copyPixelsToBuffer(buff)
        buff.position(0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, brushTexture)
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D,
            0,
            GLES32.GL_RGBA,
            brushBitmap.width,
            brushBitmap.height,
            0,
            GLES32.GL_RGBA,
            GLES32.GL_UNSIGNED_BYTE,
            buff
        )


        outputTexture = com.feresr.shaded.createTexture()

        GLES32.glGenFramebuffers(1, frameBuffers, 0)


        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, outputTexture)
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D,
            0,
            GLES32.GL_RGBA,
            1080,
            1920,
            0,
            GLES32.GL_RGBA,
            GLES32.GL_UNSIGNED_BYTE,
            null
        )

        attachTextureToFBO(frameBuffers[0], outputTexture)
    }


    fun render() {
        if (program == 0) {
            init()
        }

        if (!firstPointAdded) return
        GLES32.glUseProgram(program)

        // Read from
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, brushTexture)

        // Draw to the this (FBO)
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, frameBuffers[0])

        buffer.position(0)
        GLES32.glVertexAttribPointer(
            posCoordHandle,
            2,
            GLES32.GL_FLOAT,
            true,
            0,
            buffer
        )



        GLES32.glEnableVertexAttribArray(posCoordHandle)
        GLES32.glDrawArrays(GLES32.GL_POINTS, 0, 1)


        screenRenderer.render(outputTexture)

        GLES32.glDisableVertexAttribArray(posCoordHandle)
    }


//    private fun interplate(dots: List<PointF>): List<PointF> {
//        return when (dots.size) {
//            0 -> emptyList()
//            1 -> dots
//            2 -> pointInterpolator.interpolateLinearly(.1f, dots)
//            //3 -> pointInterpolator.interpolateQuadratic(.5f, dots)
//            //4 -> pointInterpolator.interpolateQubic(.25f, dots)
//            else -> pointInterpolator.interpolateLinearly(
//                .01f,
//                dots
//            ) + interplate(dots.takeLast(dots.size - 1))
//        }
//    }


    fun setMatrix(matrix: Matrix) {
        val array = FloatArray(8)
        matrix.mapPoints(array, TEX_VERTICES)
        transformedTextureCords = createVerticesBuffer(array)
    }

    fun delete() {
        GLES32.glDeleteProgram(program)
        //transformedPosVertices.clear()
        transformedTextureCords.clear()
        program = 0
    }

    companion object {
        const val SIZE_OF_FLOAT = 4
        private val TEX_VERTICES = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )

    }
}

operator fun PointF.times(t: Float): PointF {
    return PointF(this.x * t, this.y * t)
}

internal fun loadProgram(
    fragmentShader: String,
    vertexShader: String,
    geometryShader: String? = null
): Int {

    val iVShader: Int = loadShader(GLES32.GL_VERTEX_SHADER, vertexShader)
    if (iVShader == 0) {
        Log.d("Load Program", "Vertex Shader Failed")
        return 0
    }
    val iFShader: Int = loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShader)
    if (iFShader == 0) {
        Log.d("Load Program", "Fragment Shader Failed")
        return 0
    }

    var iGShader = 0
    if (geometryShader != null) {
        iGShader = loadShader(
            GLES31Ext.GL_GEOMETRY_SHADER_EXT,
            geometryShader
        )

        if (iGShader == 0) {
            Log.d("Load Program", "Geometry Shader Failed")
            return 0
        }
    }


    val iProgId: Int = GLES32.glCreateProgram()
    GLES32.glAttachShader(iProgId, iVShader)
    GLES32.glAttachShader(iProgId, iFShader)
    if (iGShader != 0) GLES32.glAttachShader(iProgId, iGShader)
    GLES32.glLinkProgram(iProgId)

    val link = IntArray(1)
    GLES32.glGetProgramiv(iProgId, GLES32.GL_LINK_STATUS, link, 0)
    if (link[0] <= 0) {
        val glError = GLES32.glGetError()
        throw RuntimeException(
            "Failed to create program: " +
                    "glGetProgramInfoLog ${GLES32.glGetProgramInfoLog(iProgId)} " +
                    "glError $glError " +
                    "glErrorMessage ${GLU.gluErrorString(glError)}"
        )
    }
    GLES32.glDeleteShader(iVShader)
    GLES32.glDeleteShader(iFShader)
    if (iGShader != 0) GLES32.glDeleteShader(iGShader)

    return iProgId
}
