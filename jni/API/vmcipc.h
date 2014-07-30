#pragma once
#if 0
extern "C" {
extern void ConvertYUVToRGB565();
extern unsigned char *GetRGBBuffer();
void SetBufferUpdated(bool b);
bool IsBufferUpdated();
unsigned char *GetYUVBuffer();
void MyStat();
int GetVideoMetrics(int *pWidth,int *pHeight);
bool IsVideoInited();
void lock_disp_buffer();
void unlock_disp_buffer();

}
#endif


