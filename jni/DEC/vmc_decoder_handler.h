#ifndef VMC__DECODER_HANDLER_H

#define VMC__DECODER_HANDLER_H


#include "DataQueue.h"



enum  error_id{
	DEC_OK=0,
	DEC_INIT_ERR,
	DEC_ERR,
	DEC_START_ERR,
	DEC_DECODE_CHANGED,
		
};


#define INIT_ERROR "uninit or init error!"
#define DECODE_ERROR "decode_error!"
#define START_DEC_THREAD_ERROR "start decoder thread error!"


typedef void  (*preview_callback)(int state, char * error );

typedef void   (*updateframe_callback)(void *ext);






typedef struct video_info_{
	int w;
	int h;
	int fps;
	char *extradata;
	int exdatasize;
	
}video_info;



struct input_buffer{
	
	char * pdata;
	int    size;
	int64_t pts;
	int dataType;
	//int 	 id;
	int 	refcount;
	input_buffer():pdata(NULL),size(0),pts(0),refcount(0),dataType(0)
	{
		//memset(this,0,sizeof(input_buffer));
	}
	~input_buffer()
	{
#if 0	
		if(pdata)
		{
			free(pdata);
		}
#endif		
	}
	void add_ref()
	{
		refcount++;
	}
	void release()
	{
		refcount--;
		if(refcount <= 0)
		{
			if(pdata)
			{
				free(pdata);
			}
			
			delete this;
		}
	}
			
};

typedef DataQueue<input_buffer *>  other_input_buffer_queue;



static char * erro_info[]={
  NULL, 				// 0,
  INIT_ERROR,			// 1
  DECODE_ERROR, 	   // 2
  START_DEC_THREAD_ERROR   // 3
  
};

int64_t  get_sys_tickcount( void );

//int get_outbuffer_for_photo(output_buffer * pbuffer);
void get_rtmp_video_info( video_info * *info);
int GetFrameType(char *pstream);
int reg_other_buffer_queue( other_input_buffer_queue * pqueue);
int unreg_other_buffer_queue( other_input_buffer_queue * pqueue);



void lock_output_buffer(void);
void unlock_output_buffer(void);

int  push_filled_buffer(input_buffer * pbuffer);
int  get_filled_buffer_num(void);
int get_cur_buffer_num();

void get_filled_buffer(input_buffer * pbuf);
void release_filled_buffer(input_buffer *pbuffer);

void  set_rtmp_video_info( video_info * info); 
double get_soft_dec_fps();

int  init_decoder_hander(void * framedata, short codec_capbility, \
	preview_callback preview_callback_handle, updateframe_callback updateframe_callback_handle );
int start_decoder_hander(void);
int close_decoder_hander(void);

//初始化系统解码器
struct ANativeWindow;
int VmcInitSysDecoder(ANativeWindow *window);

void VmcCloseSysDecoder();


//设置解码策略
#define DECODE_AUTO		0 //默认采用系统解码器，如果不可用则采用软件解码器
#define DECODE_SOFTWARE	1
#define DECODE_SYS		2
void VmcSetDecodeStrategy(int sta);

int VmcGetAjustedDecodeMode();

class SpeedStat
{
private:
	const char * m_sTip;
	int countLast;
	int count;
	time_t tLast;
	time_t t;
	
public:
	SpeedStat(const char *s);
	void Update();
};

class FPSCalc
{
private:
	uint64_t m_tLast;//ms
	double m_fpsLast;
	double m_d;
	int m_frames;
	int m_test;
	bool m_begin;
	int m_diff;
public:
	FPSCalc(double d);
	void Reset();
	void Update();
	double FPS();
	void Begin();
};




#endif //VMC__DECODER_HANDLER_H

