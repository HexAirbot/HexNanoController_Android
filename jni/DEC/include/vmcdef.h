
#pragma once

namespace vmc
{
#define COLOR_NONE "\033[m"
#define COLOR_RED "\033[0;32;31m"
#define COLOR_LIGHT_RED "\033[1;31m"
#define COLOR_GREEN "\033[0;32;32m"
#define COLOR_LIGHT_GREEN "\033[1;32m"
#define COLOR_BLUE "\033[0;32;34m"
#define COLOR_LIGHT_BLUE "\033[1;34m"
#define COLOR_DARY_GRAY "\033[1;30m"
#define COLOR_CYAN "\033[0;36m"
#define COLOR_LIGHT_CYAN "\033[1;36m"
#define COLOR_PURPLE "\033[0;35m"
#define COLOR_LIGHT_PURPLE "\033[1;35m"
#define COLOR_BROWN "\033[0;33m"
#define COLOR_YELLOW "\033[1;33m"
#define COLOR_LIGHT_GRAY "\033[0;37m"
#define COLOR_WHITE "\033[1;37m"

typedef unsigned char BYTE;
typedef unsigned int UINT32;
typedef unsigned short UINT16;
typedef unsigned long long UINT64;


#define V_REPORT_ERROR \
	if(ret) \
	{ \
		printf("%s err:%d\n",__PRETTY_FUNCTION__,ret); \
	}


#define SAFE_DELETE(p) if((p)){delete (p);(p) = NULL;}

#define SAFE_FREE(p) if((p)){free(p);(p) = NULL;}


enum STREAM_TYPE
{
	STREAM_TYPE_NONE 	= -1,
	STREAM_TYPE_H264 	= 0,
	STREAM_TYPE_AAC 	= 1,

};

enum DATA_TYPE
{
	DATA_TYPE_NONE = -1,
	DATA_TYPE_H264_SPS = 0,
	DATA_TYPE_H264_PPS = 1,
	DATA_TYPE_H264_RAW = 2,
	DATA_TYPE_AAC_HEAER = 3,
	DATA_TYPE_AAC_RAW = 4
	
};

// NALUµ¥Ôª  
struct NaluUnit  
{  
    int type;  
    int size;  
    unsigned char *data;  
};  
  
struct RTMPMetadata  
{  
    // video, must be h264 type  
    unsigned int    nWidth;  
    unsigned int    nHeight;  
    unsigned int    nFrameRate;     // fps  
    unsigned int    nVideoDataRate; // bps  
    unsigned int    nSpsLen;  
    unsigned char   Sps[1024];  
    unsigned int    nPpsLen;  
    unsigned char   Pps[1024];  
  
    // audio, must be aac type  
    bool            bHasAudio;  
    unsigned int    nAudioSampleRate;  
    unsigned int    nAudioSampleSize;  
    unsigned int    nAudioChannels;  
    char            pAudioSpecCfg;  
    unsigned int    nAudioSpecCfgLen;  
  
};  


struct AAC_HEADER  
{  
	
	unsigned frames:2;
	unsigned buffer_fullness:11;
	unsigned length:13;
	unsigned copyright_start:1;
	unsigned copyrighted_stream:1;
	unsigned home:1;
	unsigned originality:1;
	unsigned mp4_channel_configuration:3;
	unsigned private_stream:1;
	unsigned mp4_sampling_frequency_index :4;
	unsigned profile :2;
	unsigned protection_absent:1;
	unsigned layer:2;
	unsigned mp_version:1;
	unsigned syncword:12;
} __attribute__((packed));  

//http://blog.csdn.net/tx3344/article/details/7349019


struct AudioSpecificConfig
{
	unsigned extensionFlag:1;
	unsigned dependsOnCoreCoder:1;
	unsigned frameLengthFlag:1;
	unsigned channelConfiguration:4;
	unsigned samplingFrequencyIndex:4;
	unsigned audioObjectType:5;
}__attribute__((packed));  

struct AudioTagHeader
{
	unsigned SoundType:1;
	unsigned SoundSize:1;
	unsigned SoundRate:2;
	unsigned SoundFormat:4;
//	unsigned AACPacketType:8;
} __attribute__((packed));  




}

