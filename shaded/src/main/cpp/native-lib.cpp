#include <jni.h>
#include <android/bitmap.h>
#include <cstring>
#include <cstdint>
#include <GLES2/gl2.h>

struct Bitmap {
    uint32_t *pixels = NULL;
    int w = 0;
    int h = 0;
};

Bitmap current;

// Reads pixels from the bounded openGL frame buffer object directly into the bitmap passed in as a
// parameter
extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_PingPongRenderer_glReadPixelsInto(JNIEnv *jenv, jclass thiz, jobject src) {
    AndroidBitmapInfo srcInfo;

    int opResult = 0;
    opResult = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    uint32_t *srcByteBuffer;
    opResult = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    glReadPixels(0, 0, srcInfo.width, srcInfo.height, GL_RGBA, GL_UNSIGNED_BYTE, srcByteBuffer);

    AndroidBitmap_unlockPixels(jenv, src);
}

// Loads the bitmap (if set) to the currently bound openGL texture
extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_PingPongRenderer_loadIntoOpenGl(JNIEnv *jenv, jclass thiz, jint texture) {
    if (current.pixels == NULL) return;
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, current.w, current.h, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 current.pixels);
}

// Stores the bitmap into native memory to avoid OOMs on the JVM
extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_PingPongRenderer_storeBitmap(JNIEnv *jenv, jclass thiz, jobject src) {

    AndroidBitmapInfo srcInfo;

    int opResult = 0;
    opResult = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    uint32_t *srcByteBuffer;
    opResult = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    delete[] current.pixels;
    current.w = srcInfo.width;
    current.h = srcInfo.height;
    int pixelsCount = current.w * current.h;
    current.pixels = new uint32_t[pixelsCount];

    memcpy(current.pixels, srcByteBuffer, sizeof(uint32_t) * pixelsCount);
    AndroidBitmap_unlockPixels(jenv, src);
}

// clears the stored bitmap from native memory (does not clear it from openGL)
extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_PingPongRenderer_freeBitmap(JNIEnv *jenv, jclass thiz) {
    delete[] current.pixels;
    current.pixels = NULL;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_feresr_shaded_PingPongRenderer_isBitmapStored(JNIEnv *jenv, jclass thiz) {
    return current.pixels != NULL;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_PingPongRenderer_getBitmapWidth(JNIEnv *jenv, jclass thiz) {
    return current.w;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_feresr_shaded_PingPongRenderer_getBitmapHeight(JNIEnv *jenv, jclass thiz) {
    return current.h;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feresr_shaded_PingPongRenderer_genVertexBuffer(JNIEnv *jenv, jclass thiz) {

    unsigned int vbo;
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);

    float pos[] = {
            //position
            -1.0f, -1.0f,   //bottom left
            -1.0f, 1.0f,    //top left
            1.0f, -1.0f,    //bottom right
            1.0f, 1.0f,      //top right

            //uvs
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };
    glBufferData(GL_ARRAY_BUFFER, 16 * sizeof(float), pos, GL_STATIC_DRAW);
}
