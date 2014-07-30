/*
 * gl_bg_video_sprite_stub.c
 *
 *  Created on: Feb 1, 2012
 *      Author: "Dmytro Baryskyy"
 */
#include <android/bitmap.h>
#include <jni.h>
#include <android/log.h>
#include <java_callbacks.h>
#include <com_vmc_ipc_view_gl_GLBGVideoSprite.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/bitmap.h>
#include "yuv2rgb.h"
#include "vmcipc.h"
#include "vmcipc_videostream.h"

#include "gl_bg_video_sprite_stub.h"

#include "vmcipc_defines.h"
#include "time.h"
#include <stdio.h>
#include <stdlib.h>

#define FALSE false
#define TRUE true

#ifdef __cplusplus
extern "C" {
#endif

#define YUV_SAVE_PATH /sdcard/yuv/data_
FILE *stream = NULL;

struct yuvData {
    char* data;
    int width;	/* callback if this number of bytes transferred */
    int htight;	/* callback if this many milliseconds have elapsed */
};

//static char* TAG = "gl_bg_video_sprite";

FILE *yuvfile = NULL;
int ind = 0;
void createStream(int width,int height){
	char path[256];
	time_t rawtime;
	time ( &rawtime );
	sprintf(path,"/sdcard/yuv/data_%d_%d_%d",rawtime,width,height);
	strcat(path,".yuv");

	LOGI ( "the path is: %s", path );
	if(yuvfile != NULL) fclose(yuvfile);
	if((yuvfile = fopen(path,"w+")) == NULL){
		LOGI("open file %s fail",path);
		return;
	}
	ind = 0;
}

void writeYUVData(char *data,int width,int height){
	if(yuvfile == NULL) return;
	if(ind++>30){
			fclose(yuvfile);
			return;
		}
	unsigned int size = 960 * 544 * 3 / 2;
	int cnt = 0;
	while((cnt+=fwrite(data+cnt,sizeof(char),size,yuvfile))<=size){
//		LOGI("it has been writen %d chars",cnt);
	}
	LOGI("writeYUVData---%d",ind);

}

static opengl_size_t screen_size;
static bool recalculate_video_texture = false;
static int32_t current_num_picture_decoded = 0;
static int32_t current_num_frames = 0;

opengl_scaling	 scaling;
static opengl_texture_t texture;
static opengl_video_stage_config_t config;
static bool texture_initialized = false;
short int buffer[1280*720*2];
static long indexd = 0;
static short indexf = 0;

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}



static GLuint g_texYId;
static GLuint g_texUId;
static GLuint g_texVId;
enum {
    ATTRIB_VERTEX,
    ATTRIB_TEXTURE,
};



yuvData yuvbuffer720p;
yuvData yuvbuffer360p;
static void createYUV(yuvData* yuv,int width,int height,int color)
{
	LOGI("-->get in createYUV(%p,%d,%d,%d)",yuv,width,height,color);
	memset(yuv, 0, sizeof(yuvData));
    unsigned int size = width * height * 3 / 2;
    yuv->data = (char *)malloc(size);
    memset(yuv->data,color,size);
//
//	for(int i = 0;i<size;i++){
//		if(i>width*100)
//			*(yuv->data+i) = 0xaaaa;
//		else
//			*(yuv->data+i) = 0x8888;
//
//		//LOGI("%x==========%x",i,buffer[i]);
//	}
	LOGI("-->get out createYUV(%p,%d,%d,%d)",yuv,width,height,color);
}
static void initYUVTextureID(){
    glGenTextures(1, &g_texYId);
    glGenTextures(1, &g_texUId);
    glGenTextures(1, &g_texVId);
}
static GLuint bindTexture(GLuint texture, const char *buffer, GLuint w , GLuint h)
{
//  GLuint texture;
//  glGenTextures ( 1, &texture );
	checkGlError("glGenTextures");
    glBindTexture ( GL_TEXTURE_2D, texture );
    checkGlError("glBindTexture");
    glTexImage2D ( GL_TEXTURE_2D, 0, GL_LUMINANCE, w, h, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
    checkGlError("glTexImage2D");
    glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
    checkGlError("glTexParameteri");
    glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
    checkGlError("glTexParameteri");
    glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
    checkGlError("glTexParameteri");
    glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    checkGlError("glTexParameteri");
    //glBindTexture(GL_TEXTURE_2D, 0);

    return texture;
}

static void bindYUV(int simpleProgram){
    GLint tex_y = glGetUniformLocation(simpleProgram, "SamplerY");
    checkGlError("glGetUniformLocation");
    GLint tex_u = glGetUniformLocation(simpleProgram, "SamplerU");
    checkGlError("glGetUniformLocation");
    GLint tex_v = glGetUniformLocation(simpleProgram, "SamplerV");
    checkGlError("glGetUniformLocation");


    glBindAttribLocation(simpleProgram, ATTRIB_VERTEX, "vPosition");
    checkGlError("glBindAttribLocation");
    glBindAttribLocation(simpleProgram, ATTRIB_TEXTURE, "a_texCoord");
    checkGlError("glBindAttribLocation");

//    glVertexAttribPointer(ATTRIB_VERTEX, 2, GL_FLOAT, 0, 0, squareVertices);
//    checkGlError("glVertexAttribPointer");
//    glEnableVertexAttribArray(ATTRIB_VERTEX);
//    checkGlError("glEnableVertexAttribArray");
//
//    glVertexAttribPointer(ATTRIB_TEXTURE, 2, GL_FLOAT, 0, 0, coordVertices);
//    checkGlError("glVertexAttribPointer");
//    glEnableVertexAttribArray(ATTRIB_TEXTURE);
//    checkGlError("glEnableVertexAttribArray");

    glActiveTexture(GL_TEXTURE0);
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, g_texYId);
    checkGlError("glBindTexture");
    glUniform1i(tex_y, 0);
    checkGlError("glUniform1i");

    glActiveTexture(GL_TEXTURE1);
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, g_texUId);
    checkGlError("glBindTexture");
    glUniform1i(tex_u, 1);
    checkGlError("glUniform1i");

    glActiveTexture(GL_TEXTURE2);
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, g_texVId);
    checkGlError("glBindTexture");
    glUniform1i(tex_v, 2);
    checkGlError("glUniform1i");

    //glEnable(GL_TEXTURE_2D);
    //checkGlError("glEnable");
//    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
//    checkGlError("glDrawArrays");
}
//---------------------------------

void init_opengl_video_stage_config_t(){
	memset(&config, 0, sizeof(opengl_video_stage_config_t));
	int width,height;
	config.bytesPerPixel = 3;
	videostream::GetVideoMetrics(&width,&height);
	config.widthImage = width;
	config.heightImage = height;
	config.widthTexture = width;
	config.heightTexture = height;
	config.format = GL_RGB;
	config.type = GL_UNSIGNED_SHORT_5_6_5;
	LOGI("-->init_opengl_video_stage_config_t,,%d",sizeof(short int));

	//int *buffer = new int[1280*720]
//	LOGI("-->init_opengl_video_stage_config_t1");
//	for(int i = 0;i<1280*720;i++){
//		if(i>128000)
//			buffer[i] = 0xaaaa;
//		else
//			buffer[i] = 0x8888;
//
//		//LOGI("%x==========%x",i,buffer[i]);
//	}
//	delete [] buffer;
//	memset(buffer,0x88,1280*720*1);
//	LOGI("-->init_opengl_video_stage_config_t2");
//	config.data = buffer;
//	config.data = yuvbuffer;
}

static void init_texture()
{
	LOGI("-->init_texture");
	memset(&texture, 0, sizeof(opengl_texture_t));
	init_opengl_video_stage_config_t();
//	createYUV(&yuvbuffer720p,1280,720,0x00);
//	createYUV(&yuvbuffer360p,640,360,0xff);
	initYUVTextureID();
}

static void recalculate_video_texture_bounds(JNIEnv *env, jobject obj, opengl_texture_t* texture)
{
	java_set_field_int(env, obj, "imageWidth", texture->image_size.width);
	java_set_field_int(env, obj, "imageHeight",  texture->image_size.height);
	java_set_field_int(env, obj, "textureWidth", texture->texture_size.width);
	java_set_field_int(env, obj, "textureHeight", texture->texture_size.height);
}


void opengl_texture_scale_compute(opengl_texture_t *texture, opengl_size_t screen_size, opengl_scaling scaling)
{
//	LOGD(TAG, "%s sizes %f, %f, %f, %f\n", __FUNCTION__, texture->image_size.width, texture->image_size.height, texture->texture_size.width, texture->texture_size.height);
	switch(scaling)
	{
		case NO_SCALING:
			texture->scaleModelX = texture->image_size.height / screen_size.width;
			texture->scaleModelY = texture->image_size.width / screen_size.height;
			break;
		case FIT_X:
			texture->scaleModelX = (screen_size.height * texture->image_size.height) / (screen_size.width * texture->image_size.width);
			texture->scaleModelY = 1.0f;
			break;
		case FIT_Y:
			texture->scaleModelX = 1.0f;
			texture->scaleModelY = (screen_size.width * texture->image_size.width) / (screen_size.height * texture->image_size.height);
			break;
		default:
			texture->scaleModelX = 1.0f;
			texture->scaleModelY = 1.0f;
			break;
	}

	texture->scaleTextureX = texture->image_size.width / (float)texture->texture_size.width;
	texture->scaleTextureY = texture->image_size.height / (float)texture->texture_size.height;
}




static opengl_video_stage_config_t *opengl_video_stage_get(){
	return &config;
}




void renderFrame() {
    static float grey;
    grey += 0.01f;
    if (grey > 1.0f) {
        grey = 0.0f;
    }
    glClearColor(grey, grey, grey, 1.0f);
    checkGlError("glClearColor");
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    checkGlError("glClear");

//    glUseProgram(gProgram);
//    checkGlError("glUseProgram");

//    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
//    checkGlError("glVertexAttribPointer");
//    glEnableVertexAttribArray(gvPositionHandle);
//    checkGlError("glEnableVertexAttribArray");
//    glDrawArrays(GL_TRIANGLES, 0, 3);
//    checkGlError("glDrawArrays");
}

JNIEXPORT jboolean JNICALL
Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative(JNIEnv *env, jobject obj, jint program, jint textureId)
{
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative");
	if (texture_initialized == FALSE){// && videostream::IsVideoInited()) {
		init_texture();
		texture_initialized = TRUE;
	}

	opengl_video_stage_config_t *config = opengl_video_stage_get();

#if 1
		int width,height;
#else
		int width = texture.texture_size.width;
		int height = texture.texture_size.height;
#endif
	videostream::lock_disp_buffer();
	if(videostream::GetVideoMetrics(&width,&height) != 0){
		videostream::unlock_disp_buffer();
		return FALSE;
	}
	if(width == 0 || height == 0){
		LOGI("-->width or height is zero.");
		videostream::unlock_disp_buffer();
		return FALSE;
	}
	config->widthImage = width;
	config->widthTexture = width;
	config->heightImage = height;
	config->heightTexture = height;
	config->data = videostream::GetYUVBuffer();
//	if(((indexd++)*30 / 3000)%2==0){
//		LOGI("-->------------------show 720p");
//		width = 1280;
//		height = 720;
//		config->widthImage = width;
//		config->widthTexture = width;
//		config->heightImage = height;
//		config->heightTexture = height;
//		config->data = yuvbuffer720p.data;
//	}
//	else{
//		LOGI("-->------------------show 360p");
//		width = 640;
//		height = 360;
//		config->widthImage = width;
//		config->widthTexture = width;
//		config->heightImage = height;
//		config->heightTexture = height;
//		config->data = yuvbuffer360p.data;
//	}
//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative1");
	if ((config != NULL) && (config->data != NULL))// && (config->num_picture_decoded > current_num_picture_decoded))
	{
		if (texture.image_size.width != config->widthImage) {
			recalculate_video_texture = TRUE;
//			LOGI("size has changed to (%d,%d)",width,height);
//			LOGI("size has changed to (%d,%d)",texture.image_size.width,config->widthImage);
//			createStream(width,height);
			indexf = 0;
		}
//		writeYUVData((char *)config->data,width,height);
		texture.bytesPerPixel       = config->bytesPerPixel;
		texture.image_size.width    = config->widthImage;
		texture.image_size.height   = config->heightImage;
		texture.texture_size.width	= config->widthTexture;
		texture.texture_size.height	= config->heightTexture;
		texture.format              = config->format;
		texture.type                = config->type;
		texture.data                = config->data;
		texture.state = OPENGL_STATE_GENERATED;

        current_num_picture_decoded = config->num_picture_decoded;
		current_num_frames = config->num_frames;
	}

	if (recalculate_video_texture) {
		recalculate_video_texture_bounds(env, obj, &texture);
		recalculate_video_texture = FALSE;
	}

	indexf++;
	if(indexf<10){
		LOGI("-->recalculate_video_texture_bounds");
		videostream::unlock_disp_buffer();
		return TRUE;
	}
	else
	{
		indexf = 10;
	}

	if(texture.state == OPENGL_STATE_GENERATED)
	{
//		LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative2");
		// Load the texture in the GPU
		if (texture.data != NULL) {
//			LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative3");
//			renderFrame();
//			LOGI("fmt: %d, w: %f, h: %f, type: %d, data: %p", texture.format, texture.texture_size.width, texture.texture_size.height, texture.type, texture.data);
//			glTexImage2D(GL_TEXTURE_2D, 0, texture.format, texture.texture_size.width, texture.texture_size.height, 0, texture.format, texture.type, texture.data);
//			checkGlError("glTexImage2D");
//			LOGI("-->video size(%d,%d)",width,height);
//			char *p = (char *)videostream::GetYUVBuffer();
			bindTexture(g_texYId, (char *)texture.data, width, height);
			bindTexture(g_texUId, (char *)texture.data + width * height, width/2, height/2);
			bindTexture(g_texVId, (char *)texture.data + width * height * 5 / 4, width/2, height/2);
			bindYUV(program);
			//MyStat();
			videostream::unlock_disp_buffer();
			texture.state = OPENGL_STATE_SEND_TO_GPU;
			return TRUE;
		}
	}

	return FALSE;

	/**
	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative");
	if (texture_initialized == FALSE && videostream::IsVideoInited()) {
		init_texture();
		texture_initialized = TRUE;
	}

	opengl_video_stage_config_t *config = opengl_video_stage_get();

//	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative1");
	if ((config != NULL) && (config->data != NULL))// && (config->num_picture_decoded > current_num_picture_decoded))
	{
		if (texture.image_size.width != config->widthImage) {
			recalculate_video_texture = TRUE;
		}

		texture.bytesPerPixel       = config->bytesPerPixel;
		texture.image_size.width    = config->widthImage;
		texture.image_size.height   = config->heightImage;
		texture.texture_size.width	= config->widthTexture;
		texture.texture_size.height	= config->heightTexture;
		texture.format              = config->format;
		texture.type                = config->type;
		texture.data                = config->data;
		texture.state = OPENGL_STATE_GENERATED;

        current_num_picture_decoded = config->num_picture_decoded;
		current_num_frames = config->num_frames;
	}

	if (recalculate_video_texture) {
		recalculate_video_texture_bounds(env, obj, &texture);
		recalculate_video_texture = FALSE;
	}

	if(texture.state == OPENGL_STATE_GENERATED)
	{
//		LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative2");
		// Load the texture in the GPU
		if (texture.data != NULL) {
//			LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onUpdateVideoTextureNative3");
			renderFrame();
//			LOGI("fmt: %d, w: %f, h: %f, type: %d, data: %p", texture.format, texture.texture_size.width, texture.texture_size.height, texture.type, texture.data);
//			glTexImage2D(GL_TEXTURE_2D, 0, texture.format, texture.texture_size.width, texture.texture_size.height, 0, texture.format, texture.type, texture.data);
//			checkGlError("glTexImage2D");
#if 1
			int width,height;
#else
			int width = texture.texture_size.width;
			int height = texture.texture_size.height;
#endif
			videostream::lock_disp_buffer();
			videostream::GetVideoMetrics(&width,&height);
			LOGI("-->video size(%d,%d)",width,height);
			char *p = (char *)videostream::GetYUVBuffer();
			bindTexture(g_texYId, p, width, height);
			bindTexture(g_texUId, p + width * height, width/2, height/2);
			bindTexture(g_texVId, p + width * height * 5 / 4, width/2, height/2);
			bindYUV(program);
			//MyStat();
			videostream::unlock_disp_buffer();
			texture.state = OPENGL_STATE_SEND_TO_GPU;
			return TRUE;
		}
	}

	return FALSE;
	**/
}


JNIEXPORT void JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceCreatedNative
  (JNIEnv *, jobject){
	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceCreatedNative");
}

JNIEXPORT void JNICALL
Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceChangedNative(JNIEnv *env, jobject obj, jint width, jint height)
{
	LOGI("-->Java_com_vmc_ipc_view_gl_GLBGVideoSprite_onSurfaceChangedNative");
	screen_size.width = width;
	screen_size.height = height;

	recalculate_video_texture = TRUE;
}


JNIEXPORT jboolean JNICALL Java_com_vmc_ipc_view_gl_GLBGVideoSprite_getVideoFrameNative(JNIEnv *env, jobject obj, jobject bitmap, jfloatArray videoSize)
{
	AndroidBitmapInfo  info;
	void*              pixels;
	int                ret;
	jboolean result = FALSE;

	if (screen_size.width == 0 || screen_size.height == 0)
		return FALSE;

	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		return FALSE;
	}

	if (info.format != ANDROID_BITMAP_FORMAT_RGB_565) {
		return FALSE;
	}

	opengl_video_stage_config_t *config = opengl_video_stage_get();

	if ((config != NULL) && (config->data != NULL) && (config->num_picture_decoded > current_num_picture_decoded))
	{
		if (texture.image_size.width != config->widthImage) {
			recalculate_video_texture = TRUE;
		}

		texture.bytesPerPixel       = config->bytesPerPixel;
		texture.image_size.width    = config->widthImage;
		texture.image_size.height   = config->heightImage;
		texture.texture_size.width	= config->widthTexture;
		texture.texture_size.height	= config->heightTexture;
		texture.format              = config->format;
		texture.type                = config->type;
		texture.data                = config->data;
		texture.state = OPENGL_STATE_GENERATED;

        current_num_picture_decoded = config->num_picture_decoded;
		current_num_frames = config->num_frames;
	}

	if (recalculate_video_texture && screen_size.width != 0 && screen_size.height != 0) {
		opengl_texture_scale_compute(&texture, screen_size, FIT_X);
//		LOGD("VIDEO", "Screen Widht: %f", screen_size.width);
		recalculate_video_texture = FALSE;
	}

	if (texture.state == OPENGL_STATE_GENERATED)
	{
		if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		}

		result = TRUE;

		memcpy(pixels, texture.data, texture.image_size.width * texture.image_size.height  * texture.bytesPerPixel);

		texture.state = OPENGL_STATE_SEND_TO_GPU;

		jfloat *body = env->GetFloatArrayElements(videoSize, 0);
		body[0] = (float)texture.image_size.width;
		body[1] = (float)texture.image_size.height;
		body[2] = (float)texture.scaleModelX;
		body[3] = (float)texture.scaleModelY;

		env->ReleaseFloatArrayElements(videoSize, body, 0);

		AndroidBitmap_unlockPixels(env, bitmap);

	}

	return result;
}

#ifdef __cplusplus
}
#endif
