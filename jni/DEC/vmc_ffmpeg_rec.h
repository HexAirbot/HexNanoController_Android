#ifndef VMC_FFMPEG_REC_H

#define VMC_FFMPEG_REC_H

#ifdef __cplusplus
extern "C" {
#endif


//#include <video_encapsulation.h>
#include "include/libavcodec//avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavutil/avutil.h"

#ifdef __cplusplus
}
#endif


typedef struct VmcRecContext_{

	AVOutputFormat *OutForamt;
	AVFormatContext *FormatContext;
	AVStream * VideoSt;
	int FrameRate;
	int widht;
	int height;
	char *pExtradata;
	int ExdataSize;
	const char *filename;
	
}VmcRecContext;



VmcRecContext * VmcRecInit(const char*filename, int w, int h, int fps, char *extradata, int exdatasize);
int VmcWiteVideoFrame( VmcRecContext *pContext, char *input, int size, uint64_t pts);
int VmcRecClose(VmcRecContext * pContext);





#endif //VMC_FFMPEG_REC_H

