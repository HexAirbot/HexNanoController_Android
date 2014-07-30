#pragma once
#include <jni.h>
#include <android/log.h>


struct ANativeWindow;
namespace videostream
{
void SetNativeWindow(ANativeWindow *window);
void ResetRTMPState();
void StartRec(const char *dir,const char *filename);
unsigned char *GetRGBBuffer();
unsigned char *GetYUVBuffer();
void lock_disp_buffer();
void unlock_disp_buffer();
char *GetServerAddr();

void StartRTMPThread();
int GetVideoMetrics(int *pWidth,int *pHeight);
bool IsVideoInited();
void StartSoftDecoder();
void CloseRTMPConnection();

}

