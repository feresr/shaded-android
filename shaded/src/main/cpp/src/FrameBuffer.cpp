#include <jni.h>
#include <GLES3/gl3.h>
#include <android/bitmap.h>
#include <Logger.h>

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_opengl_FrameBuffer_initFrameBuffer(JNIEnv *jenv, jobject thiz) {
    LOGI("FrameBuffer_initFrameBuffer");
    GLuint id;
    glGenFramebuffers(1, &id);
    return (jint) id;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_opengl_FrameBuffer_deleteFrameBuffer(JNIEnv *jenv, jobject thiz, jint id) {
    LOGI("FrameBuffer_deleteFrameBuffer");
    auto fb = (GLuint) id;
    glDeleteFramebuffers(1, &fb);
}


// Reads pixels from the bounded openGL frame buffer object directly into the bitmap passed in as a
// parameter
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_feresr_shaded_opengl_FrameBuffer_getBitmap(JNIEnv *jenv, jobject thiz, jobject src) {
    AndroidBitmapInfo srcInfo;
    int opResult = 0;
    opResult = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return false;

    uint32_t *srcByteBuffer;
    opResult = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return false;

    glReadPixels(0, 0, srcInfo.width, srcInfo.height, GL_RGBA, GL_UNSIGNED_BYTE, srcByteBuffer);

    AndroidBitmap_unlockPixels(jenv, src);
    return true;
}
