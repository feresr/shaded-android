#pragma once

#include <android/log.h>
#define DEBUG
#ifdef DEBUG
#define  LOG_TAG    "shaded native"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#define  LOG_TAG    "shaded native"
#define  LOGI(...)
#define  LOGE(...)
#endif
