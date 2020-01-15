#include <jni.h>
#include <android/bitmap.h>
#include <GLES2/gl2.h>

extern "C"
JNIEXPORT void JNICALL Java_com_feresr_shaded_PingPongRenderer_glReadPixelsInto(JNIEnv *jenv, jclass thiz,jobject src) {
    AndroidBitmapInfo srcInfo;

    int opResult = 0;
    opResult = AndroidBitmap_getInfo(jenv, src, &srcInfo);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    unsigned char *srcByteBuffer;
    opResult = AndroidBitmap_lockPixels(jenv, src, (void **) &srcByteBuffer);
    if (opResult != ANDROID_BITMAP_RESULT_SUCCESS) return;

    glReadPixels(0, 0, srcInfo.width, srcInfo.height, GL_RGBA, GL_UNSIGNED_BYTE, srcByteBuffer);

    AndroidBitmap_unlockPixels(jenv, src);
}
