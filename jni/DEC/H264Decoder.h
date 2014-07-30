#pragma once
#include <stdint.h>


//using namespace android;
#define DATA_TYPE_H264_NORMAL 0
#define DATA_TYPE_H264_SPEC 1

extern "C"
{
typedef int (*H264_READ_CALLBACK)(unsigned char *p,int *pLen,int64_t *timeStampUs,int err,int dataType,void *pData);
typedef void (*H264_DECODE_CALLBACK)(unsigned char *p,int len,void *pData);
int H264DecoderInit();
struct ANativeWindow;
//启动解码器线程
int H264DecoderStart(ANativeWindow *window,H264_READ_CALLBACK cbRead,H264_DECODE_CALLBACK cbDecode,void *pData);
bool H264DecoderIsRunning();
void H264DecoderClose();



}

