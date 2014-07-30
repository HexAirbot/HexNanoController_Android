
#include <string>
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

#include "VCmdDefines.h"
#include "vmcipc_videostream.h"
#include "vmcipc_cmd.h"
#include "vmcipc_javatool.h"
#include "vmcipc_debug.h"
#include "SingleRTMP.h"
#include "VNetTool.h"

#include "vmc_decoder_handler.h"
#include "vmc_rec_handler.h"
#include "vmc_jpegenc.h"

namespace videostream
{
static bool g_bThreadRunning = false;
static bool g_bRTMPConnected = false;
static int g_frameRecved = 0;
static int g_frameDec = 0;
static int64_t g_timeDec0 = 0;
static double g_fpsDecLast = 0.0;

static bool g_bSendFPSAjust = false;
static std::string g_sVideoProfile;
static char server_address[100] = "rtmp://192.168.1.1/live/stream";



#define MAX_VIDEO_WIDTH 1280
#define MAX_VIDEO_HEIGHT 720

static unsigned char g_bufYUV420[MAX_VIDEO_WIDTH*MAX_VIDEO_HEIGHT*3/2];
//static unsigned char g_bufYUV420_2[MAX_VIDEO_WIDTH*MAX_VIDEO_HEIGHT*3/2];
static unsigned char g_rgb[MAX_VIDEO_WIDTH*MAX_VIDEO_HEIGHT*4];
static bool g_bYUVBufUpdated = false;
//static void ConvertYUVToRGB565();



static SpeedStat g_stat_dec("vmc_dec_fps");
static SpeedStat g_stat_net("vmc_net_fps");
static FPSCalc g_fcDec(0.9);
static FPSCalc g_fcNet(0.9);



//统计网络延迟
static uint64_t g_ndTimeLast = 0;
static uint64_t g_ndFrameTimeLast = 0;
static uint64_t g_ndTimeUpdate = 0;
//20秒最大网络延迟
int g_ndMax = 0;
//20秒最大帧延迟
int g_ndFrameMax = 0;





static void JavaSendMsgByJni(int cmd,const char *msg);



static void  preview_cb (int state, char * error )
{
	LOGW("preview_cb enter\n");
	if(state == DEC_DECODE_CHANGED)
	{
		javatool::JavaSendMsgByJni(MESSAGE_DECODEMODE_CHANGED,error);
	}
}


/**
*@brief 解码器回调函数
*@return void
*/

static void updateframe_cb (void *ext)
{
	g_stat_dec.Update();
	g_fcDec.Update();
#if 0	
	static int i;
	if((i++%10) == 9)
	{
		printf("%s fps:%.2f\n",__FUNCTION__,g_fcDec.FPS());
		printf("%s fps2:%.2f\n",__FUNCTION__,get_soft_dec_fps());
	}
#endif	
#if 1
	if(VmcGetAjustedDecodeMode() == DECODE_SOFTWARE)
	{
		double fps = get_soft_dec_fps();
		double d = fps - g_fpsDecLast;
		double fps0 = fps;
		if(d < 0.0)
		{
			d = -d;
		}
		fps -= 1.5;
		if(fps < 1.0)
		{
			fps = 1.0;
		}
		if(d >= 1.0)
		{
			cmd::AjustDeviceFps(fps);
			g_fpsDecLast = fps0;
		}
		debug::UpdateDebugInfo("SoftDec",fps);
		
	}
	else
	{
		if(g_timeDec0 == 0)
		{
			g_timeDec0 = get_sys_tickcount();
		}
		int64_t cur = get_sys_tickcount();
		if(cur - g_timeDec0 > 4500 && (g_fcNet.FPS() - g_fcDec.FPS()) > 0.7 && g_fcDec.FPS() >= 1.00 && get_cur_buffer_num() > 3
			&& !g_bSendFPSAjust && cmd::GetCmdClient() && g_fcDec.FPS() <= g_fpsDecLast)
		{
			//向服务器发送调整帧率的命令
			double fps = g_fcDec.FPS() - 1.5;
			if(fps < 1.0)
			{
				fps = 1.0;
			}
			//cmd::AjustDeviceFps(fps);
			g_bSendFPSAjust = true;
			LOGW("%s ajust fps %d send",__FUNCTION__,(int)fps);
			g_timeDec0 = cur;
		}
		debug::UpdateDebugInfo("SysDec",g_fcDec.FPS());
		
		g_fpsDecLast = g_fcNet.FPS();
	}
#endif
}

void StartSoftDecoder();

bool g_bDiu = false;
static int g_width = 0;
static int g_height = 0;
static ANativeWindow *g_window = NULL;
static bool g_bDecoderStarted = false;
static pthread_t g_tidRTMP;
static std::string g_sRecName;

void SetNativeWindow(ANativeWindow *window)
{
	if(window == NULL)
	{
		if(g_window)
		{
			ANativeWindow_release(g_window);
		}
	}

	g_window = window;
}


void ResetRTMPState()
{
	g_bRTMPConnected = false;
	g_frameRecved = 0;
	g_timeDec0 = 0;
	g_sVideoProfile.clear();
	g_bDiu = false;
	g_width = 0;
	g_height = 0;
	close_decoder_hander();
	g_bDecoderStarted = false;
	g_tidRTMP = 0;
	g_bSendFPSAjust = false;
	g_fpsDecLast = 0.0;
	g_fcDec.Reset();
	g_fcNet.Reset();

	g_ndTimeLast = 0;
	
	close_rec_hander();
}
static void H264DataCallback(void *p,int len,unsigned int timeMs)
{
	if(!g_bRTMPConnected)
	{
		g_bRTMPConnected = true;
		//JavaShowStatus("connect success",1);
		
	}
	{
		uint64_t cur = get_sys_tickcount();
		//统计网络延迟，每20秒
		if(!g_ndTimeLast)
		{
			g_ndTimeLast = cur;
			g_ndFrameTimeLast = timeMs;
			g_ndTimeUpdate = cur;
			g_ndMax = 0;
			g_ndFrameMax = 0;
		}
		int d = 0;
		
		d = cur - g_ndTimeLast - (timeMs - g_ndFrameTimeLast);
		//LOGW("%s %d %llu %llu %llu %llu %d\n",__FUNCTION__,g_ndMax,cur,g_ndTimeLast,(uint64_t)timeMs,(uint64_t)g_ndFrameTimeLast,d);
		if(d > g_ndMax)
		{
			g_ndMax = d;
			char buf[100];
			sprintf(buf,"%.2fs/%dK",((double)g_ndMax)/1000,len/1024);
			debug::UpdateDebugInfo("netDelay",buf);
		}
		
		d = (timeMs - g_ndFrameTimeLast);
		if(d > g_ndFrameMax)
		{
			g_ndFrameMax = d;
			//LOGW("%s frameDelay %d\n",__FUNCTION__,g_ndFrameMax);
			debug::UpdateDebugInfo("frameDelay",((double)g_ndFrameMax)/1000);
		}
		//LOGW("%s %d\n",__FUNCTION__,timeMs);

		
		if(cur - g_ndTimeUpdate > 20000)
		{
			g_ndTimeUpdate = cur;
			g_ndMax = 0;
			g_ndFrameMax = 0;
		}
		g_ndFrameTimeLast = timeMs;
		g_ndTimeLast = cur;
	}
	
	



	g_frameRecved++;

	g_stat_net.Update();
	//LOGW("%s len:%d time:%u ms",__FUNCTION__,len,timeMs);
	//return ;
	if(g_frameRecved <= 2)
	{
		if(g_frameRecved == 1)
		{
			g_sVideoProfile.clear();
			int height = 0;
			ParseMetricsFromSPS((unsigned char *)p,len,&g_width,&height);
			g_height = GetAdjustedHeight(height);
			char buf[100];
			sprintf(buf,"%d,%d",g_width,g_height);
			
			javatool::JavaSendMsgByJni(MESSAGE_RESOLUTION_CHANGED,buf);
			StartSoftDecoder();
			LOGW("%s g_window:%p %d %d",__FUNCTION__,g_window,g_width,g_height);
			if(g_window)
			{
				VmcInitSysDecoder(g_window);
			}
		}
		g_sVideoProfile.append("\x00\x00\x00\x01",4);
		g_sVideoProfile.append((char *)p,len);
		if(g_frameRecved == 2)
		{
			
			input_buffer input;
			input.pdata= (char *)g_sVideoProfile.data();
			input.size= g_sVideoProfile.size();
			input.dataType = 1;
			g_fcNet.Update();
			push_filled_buffer(&input);
		}
	}
	else
	{
		int n = get_filled_buffer_num();

		if(n > 300)
		{
			g_bDiu = true;
		}
		else
		if(n< 20)
		{
			g_bDiu = false;
		}
		g_fcNet.Update();

		debug::UpdateDebugInfo("Net",g_fcNet.FPS());

		if((g_frameRecved%30) == 29)
		{
			LOGW("%s %d %f %f %d %d %p",__FUNCTION__,g_frameRecved,(g_fcNet.FPS() - g_fcDec.FPS()),g_fcDec.FPS(),get_cur_buffer_num(),
				g_bSendFPSAjust,cmd::GetCmdClient());
		}

#if 0
		if(g_frameRecved > 60 && (g_fcNet.FPS() - g_fcDec.FPS()) > 0.7 && g_fcDec.FPS() >= 1.00 && get_cur_buffer_num() > 3
			&& !g_bSendFPSAjust && g_client)
		{
			//向服务器发送调整帧率的命令
			rapidjson::Document doc;
			char buf[50];
			doc.SetObject();
			doc.AddMember("cmd",(uint64_t)CMD_AJUST_FPS,doc.GetAllocator());
			sprintf(buf,"%d",(int)g_fcDec.FPS());
			doc.AddMember("fps",buf,doc.GetAllocator());
			g_bSendFPSAjust = true;
			g_client->SendAsyn(doc,true);
			LOGW("%s ajust fps %d send",__FUNCTION__,(int)g_fcDec.FPS());
		}
#endif
		if(g_bDiu == false)
		{
			input_buffer input;
			input.pdata= (char *)p;
			input.size= len;
			input.pts = timeMs;
			push_filled_buffer(&input);
		}
		else
		{
			if(n>250)
			{
				LOGW("%s diu %d",__FUNCTION__,n);
			}
		}
		
	}
end:
	return ;
}
static void  RecCallback(int state, char * error )
{
	if(state)
	{
		LOGW("%s err:%s",__FUNCTION__,error);
	}
}

void StartRec(const char *dir,const char *filename)
{
	start_rec_hander((char *)dir,(char *)filename,false,RecCallback);
}

#if 0
void ConvertYUVToRGB565()
{
	lock_disp_buffer();
	libyuv::I420ToRGB565(g_bufYUV420_2,g_width,
		&g_bufYUV420_2[g_width*g_height],g_width/2 ,
		&g_bufYUV420_2[g_width*g_height*5/4],g_width/2,
		g_rgb,g_width*2,g_width,g_height
		);
	unlock_disp_buffer();
}
#endif
unsigned char *GetRGBBuffer()
{
	return g_rgb;
}
unsigned char *GetYUVBuffer()
{
	return g_bufYUV420;
}

void lock_disp_buffer()
{
	//pthread_mutex_lock(&g_mut);
	lock_output_buffer();
}
void unlock_disp_buffer()
{
	//pthread_mutex_unlock(&g_mut);
	unlock_output_buffer();
}
char *GetServerAddr()
{
	return server_address;
}
void *RTMPThreadFun(void *)
{
	LOGW("%s",__FUNCTION__);
	//SetJniToRTMPConnectThread(true);
	char * urls[] =
	{
//		"rtmp://192.168.1.1/live/stream",
//		"rtmp://10.0.14.153/live/stream",
//		"rtmp://10.0.12.191/live/stream"
			server_address
	};
	int i;
	int ret2 = 0;

	//StartSoftDecoder();

	
	while(g_bThreadRunning)
	{
		for(i = 0;i<sizeof(urls)/sizeof(char *) ;i++)
		{
			ret2 = StartRTMPClient(urls[i],H264DataCallback);
			LOGW("%s ret2:%d tid:%d\n",__FUNCTION__,ret2,gettid());
			ResetRTMPState();
			if(!g_bThreadRunning)
			{
				break;
			}
			sleep(1);
		}
	}
	g_bThreadRunning = false;
	//SetJniToRTMPConnectThread(false);
	g_tidRTMP = 0;
	LOGW("%s exit\n",__FUNCTION__);
	return NULL;
}

void StartRTMPThread()
{
	if(!g_bThreadRunning)
	{
		g_bThreadRunning = true;
		
		pthread_attr_t attr;
		pthread_attr_init(&attr);
		pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
		pthread_create(&g_tidRTMP,&attr,RTMPThreadFun,NULL);
		LOGW("%s g_tidRTMP:%d\n",__FUNCTION__,g_tidRTMP);
	}
}

/**
*@brief 得到设备端视频流的宽度和高度
*@return 如果正常获取，返回0值，否则返回非0值
*/

int GetVideoMetrics(int *pWidth,int *pHeight)
{
	if(g_width)
	{
		*pWidth = g_width;
		*pHeight = g_height;
		return 0;
	}
	return 1;
}
/**
*@brief 判断视频流数据是否准备好并可以用于显示
*@return 返回true表明已经准备好
*/
bool IsVideoInited()
{
	if(VmcGetAjustedDecodeMode() == DECODE_SOFTWARE)
	{
		return g_width > 0;
	}
	else
	{
		return false;
	}
}

/**
*@brief 启动软件解码器
*
*
*/

void StartSoftDecoder()
{
	if(!g_bDecoderStarted)
	{
		int i = 0;
#ifndef SOFT_DECODE_ONLY
#if 0
		while(VmcGetAjustedDecodeMode() != DECODE_SOFTWARE && g_window == NULL)
		{
			usleep(100000);
			if(i%20 == 19)
			{
				LOGW("%s waiting for surface ready",__FUNCTION__);
			}
			i++;
		}
		VmcInitSysDecoder(g_window);
#endif		
#endif
		init_decoder_hander(g_bufYUV420,0,preview_cb,updateframe_cb);
		start_decoder_hander();
		g_bDecoderStarted = true;
	}
}

void CloseRTMPConnection()
{
	//ResetRTMPState();
	LOGW("%s\n",__FUNCTION__);
	void *result = NULL;
	g_bThreadRunning = false;
	CloseRTMPClientAsyn();
	if(g_tidRTMP != 0)
	{
		LOGW("%s g_tidRTMP:%d\n",__FUNCTION__,g_tidRTMP);
		pthread_join(g_tidRTMP,&result);
		g_tidRTMP = 0;
	}
	
	g_bDecoderStarted = false;
	LOGW("%s done\n",__FUNCTION__);
	
}

}


