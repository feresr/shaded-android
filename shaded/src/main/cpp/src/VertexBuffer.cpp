#include "jni.h"
#include <GLES3/gl3.h>
#include <Logger.h>

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
    auto* data = new jfloat[length];
    jenv->GetFloatArrayRegion(floats, 0, length, data);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), data, GL_STATIC_DRAW);
    jenv->ReleaseFloatArrayElements(floats, data, 0);
    delete[] data;
}

