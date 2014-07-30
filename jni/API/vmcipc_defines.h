#pragma once



#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "vmc", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "vmc", __VA_ARGS__))

