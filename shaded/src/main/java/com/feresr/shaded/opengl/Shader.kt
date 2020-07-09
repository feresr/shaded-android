package com.feresr.shaded.opengl

import android.opengl.GLES20.GL_FALSE
import android.opengl.GLES20.glDetachShader
import android.opengl.GLES20.glUniform3f
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES30.GL_COMPILE_STATUS
import android.opengl.GLES30.GL_FRAGMENT_SHADER
import android.opengl.GLES30.GL_LINK_STATUS
import android.opengl.GLES30.GL_VERTEX_SHADER
import android.opengl.GLES30.glAttachShader
import android.opengl.GLES30.glCompileShader
import android.opengl.GLES30.glCreateProgram
import android.opengl.GLES30.glCreateShader
import android.opengl.GLES30.glDeleteProgram
import android.opengl.GLES30.glDeleteShader
import android.opengl.GLES30.glGetError
import android.opengl.GLES30.glGetProgramInfoLog
import android.opengl.GLES30.glGetProgramiv
import android.opengl.GLES30.glGetShaderInfoLog
import android.opengl.GLES30.glGetShaderiv
import android.opengl.GLES30.glGetUniformLocation
import android.opengl.GLES30.glLinkProgram
import android.opengl.GLES30.glShaderSource
import android.opengl.GLES30.glUniform1f
import android.opengl.GLES30.glUniform1i
import android.opengl.GLES30.glUseProgram
import android.opengl.GLU

class Shader(val name: String, vertexSource: String, fragmentSource: String) {
    private val program = loadProgram(vertexSource, fragmentSource)

    fun bind(block: () -> Unit) {
        glUseProgram(program)
        block()
        glUseProgram(0)
    }

    fun unbind() = glUseProgram(0)

    fun getUniformLocation(name: String): Int = glGetUniformLocation(program, name)
    fun setInt(location: Int, value: Int) = glUniform1i(location, value)
    fun setFloat(location: Int, value: Float) = glUniform1f(location, value)
    fun setSetFloat3(location: Int, r: Float, g: Float, b: Float) = glUniform3f(location, r, g, b)
    fun setSetFloat4(location: Int, r: Float, g: Float, b: Float, w: Float) =
        glUniform4f(location, r, g, b, w)

    //TODO:
    //virtual void setMat4(const std::string &name, const glm::mat4 &matrix);

    fun delete() = glDeleteProgram(program)

    private fun loadProgram(fragmentShader: String, vertexShader: String): Int {
        val iVShader: Int = loadShader(GL_VERTEX_SHADER, vertexShader)
        val iFShader: Int = loadShader(GL_FRAGMENT_SHADER, fragmentShader)

        val programId: Int = glCreateProgram()
        glAttachShader(programId, iVShader)
        glAttachShader(programId, iFShader)
        glLinkProgram(programId)

        val link = IntArray(1)
        glGetProgramiv(programId, GL_LINK_STATUS, link, 0)
        if (link[0] <= GL_FALSE) {
            val glError = glGetError()
            throw RuntimeException(
                "Failed to create program: " +
                        "glGetProgramInfoLog ${glGetProgramInfoLog(programId)} " +
                        "glError $glError " +
                        "glErrorMessage ${GLU.gluErrorString(glError)}"
            )
        }

        glDetachShader(programId, iVShader);
        glDetachShader(programId, iFShader);
        glDeleteShader(iVShader)
        glDeleteShader(iFShader)
        return programId
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        val shader = glCreateShader(shaderType)
        if (shader != GL_FALSE) {
            glShaderSource(shader, source)
            glCompileShader(shader)
            val compiled = IntArray(1)
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == GL_FALSE) {
                val info = glGetShaderInfoLog(shader)
                glDeleteShader(shader)
                throw RuntimeException("Could not compile shader $shaderType:$info")
            }
        }
        return shader
    }
}
