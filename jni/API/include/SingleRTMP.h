#pragma once

typedef void (*H264_DATA_CALLBACK)(void *,int,unsigned int);
int StartRTMPClient(const char *url,H264_DATA_CALLBACK callback);
int ParseMetricsFromSPS(unsigned char *pSPS,int lenSPS,int *,int *);
void CloseRTMPClientAsyn();
int GetAdjustedHeight(int h);



