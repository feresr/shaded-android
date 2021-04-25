#include <jni.h>
#include <GLES3/gl3.h>
#include <cstdint>
#include <android/bitmap.h>
#include <cstring>
#include <Logger.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_opengl_Texture_initTexture(JNIEnv *jenv, jobject thiz) {
    LOGI("Texture_initTexture");
    GLuint id;
    glGenTextures(1, &id);
    glBindTexture(GL_TEXTURE_2D, id);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    return (jint) id;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_Texture_deleteTexture(JNIEnv *jenv, jobject thiz, jint texture) {
    LOGI("Texture_deleteTexture");
    auto id = (GLuint) texture;
    glDeleteTextures(1, &id);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_opengl_Texture_getBoundTexture(JNIEnv *jenv, jobject thiz) {
    LOGI("Texture_getBoundTexture");
    GLint id;
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &id);
    return (jint) id;
}

//
//// Loads the bitmap (if set) to the currently bound openGL texture
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_feresr_shaded_Shaded_loadIntoOpenGl(JNIEnv *jenv, jclass thiz, jint texture) {
//    if (current.pixels == NULL) return;
//    glBindTexture(GL_TEXTURE_2D, texture);
//    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, current.width, current.height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
//                 current.pixels);
//}

// Stores the bitmap into native memory to avoid OOMs on the JVM
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_feresr_shaded_opengl_Texture_storeBitmap(JNIEnv *jenv, jobject thiz, jobject src) {
    LOGI("Texture_storeBitmap");
    AndroidBitmapInfo srcInfo;

    int opResult = 0;
    opResult = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return false;

    void *dstPixels;
    opResult = AndroidBitmap_lockPixels(jenv, src, &dstPixels);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return false;

    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_RGBA8,
                 srcInfo.width,
                 srcInfo.height,
                 0,
                 GL_RGBA,
                 GL_UNSIGNED_BYTE,
                 dstPixels);

    AndroidBitmap_unlockPixels(jenv, src);

    return true;
}