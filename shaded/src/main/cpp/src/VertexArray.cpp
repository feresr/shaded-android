#include "jni.h"
#include <GLES3/gl3.h>
#include <Logger.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_opengl_VertexArray_initVertexArray(JNIEnv *jenv, jobject thiz) {
    LOGI("VertexArray_initVertexArray");
    GLuint id;
    glGenVertexArrays(1, &id);
    return (jint) id;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_VertexArray_deleteVertexArray(JNIEnv *jenv, jobject thiz, jint id) {
    LOGI("VertexArray_deleteVertexArray");
    auto va = (GLuint) id;
    glDeleteVertexArrays(1, &va);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_VertexArray_setupPointer(
        JNIEnv *jenv,
        jobject thiz,
        jint index,
        jint size,
        jint stride,
        jint offset) {

    LOGI("VertexArray_setupPointer");
    glEnableVertexAttribArray((GLuint) index);
    glVertexAttribPointer(
            (GLuint) index,
            size,
            GL_FLOAT,
            GL_FALSE,
            stride,
            (void *) (offset * sizeof(float))
    );
}

