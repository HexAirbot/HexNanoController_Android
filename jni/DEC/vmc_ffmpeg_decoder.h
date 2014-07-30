#ifndef VMC_FFMPEG_DECODER_H

#define VMC_FFMPEG_DECODER_H

#ifdef __cplusplus
extern "C" {
#endif


//#include <video_encapsulation.h>
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavutil/avutil.h"

#ifdef __cplusplus
}
#endif


#define FFMPEG_CHECK_AND_FREE(pointer, freeFunc)        \
  do                                                    \
    {                                                   \
      if (NULL != pointer)                              \
        {                                               \
          freeFunc (pointer);                           \
          pointer = NULL;                               \
        }                                               \
    } while (0)

#define FFMPEG_CHECK_AND_FREE_WITH_CALL(pointer, func, freeFunc)        \
  do                                                                    \
    {                                                                   \
      if (NULL != pointer)                                              \
        {                                                               \
          func (pointer);                                               \
          freeFunc (pointer);                                           \
          pointer = NULL;                                               \
        }                                                               \
    } while (0)




typedef struct VmcDecoderContext_{

	AVCodecContext *pCodecCtxH264;	
	AVCodec *pCodecH264;
	AVFrame *pFrameOutput;
	int FrameRate;
	int widht;
	int height;
	char *pExtradata;
	int ExdataSize;
	unsigned int num_picture_decoded;
	
}VmcDecoderContext;



VmcDecoderContext * VmcDecoderInit(int w, int h, int fps, char *extradata, int exdatasize);
int VmcDecoderProc( VmcDecoderContext *pContext, char *input, int size, AVFrame  * *out, int *iBytesUsed);
int VmcDecoderRelease(VmcDecoderContext * pContext);





#endif //VMC_FFMPEG_DECODER_H

