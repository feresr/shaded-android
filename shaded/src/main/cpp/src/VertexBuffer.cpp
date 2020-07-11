#include "jni.h"
#include <GLES3/gl3.h>
#include <Logger.h>
#include <cstring>

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_opengl_VertexBuffer_initVertexBuffer(JNIEnv *jenv, jobject thiz) {
    LOGI("VertexBuffer_initVertexArray");
    GLuint id;
    glGenBuffers(1, &id);
    return (jint) id;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_VertexBuffer_setData(JNIEnv *jenv, jobject thiz, jfloatArray floats) {
    LOGI("VertexBuffer_setData");
    jsize length = jenv->GetArrayLength(floats);
    auto data = jenv->GetFloatArrayElements(floats, nullptr);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), data, GL_STATIC_DRAW);
    jenv->ReleaseFloatArrayElements(floats, data, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_VertexBuffer_bind(JNIEnv *jenv, jobject thiz, jint id) {
    glBindBuffer(GL_ARRAY_BUFFER, (GLuint) id);
}
