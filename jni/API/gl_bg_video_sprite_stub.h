/*
 * gl_bg_video_sprite_stub.h
 *
 *  Created on: Jul 27, 2012
 *      Author: "Dmytro Baryskyy"
 */

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#ifndef GL_BG_VIDEO_SPRITE_STUB_H_
#define GL_BG_VIDEO_SPRITE_STUB_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef enum
{
	NO_SCALING,
	FIT_X,
	FIT_Y,
	FIT_XY
} opengl_scaling;


typedef enum
{
    OPENGL_STATE_INITIALIZED = 0,
    OPENGL_STATE_GENERATED,
    OPENGL_STATE_SEND_TO_GPU
} opengl_state;


typedef struct
{
    GLfloat width;
    GLfloat height;
} opengl_size_t;


typedef struct
{
	opengl_size_t image_size;
	opengl_size_t texture_size;
	GLfloat scaleModelX;
	GLfloat scaleModelY;
	GLfloat scaleTextureX;
	GLfloat scaleTextureY;
	GLuint bytesPerPixel;
	GLenum format;
	GLenum type;
	void* data;
	GLuint textureId[2];
	GLuint vertexBufferId;
	GLuint indexBufferId;

	opengl_state state;
} opengl_texture_t;

typedef struct _opengl_video_config_t
{
//    video_decoder_config_t *video_decoder;

//	vp_os_mutex_t mutex;
	GLuint widthImage;
	GLuint heightImage;
	GLuint widthTexture;
	GLuint heightTexture;

	GLfloat scaleModelX;
	GLfloat scaleModelY;
	GLfloat scaleTextureX;
	GLfloat scaleTextureY;
	GLuint bytesPerPixel;
	GLenum format;
	GLenum type;
	void* data;
	GLuint identifier;
	uint32_t num_picture_decoded;
	uint32_t num_frames;
} opengl_video_stage_config_t;

#ifdef __cplusplus
}
#endif

#endif /* GL_BG_VIDEO_SPRITE_STUB_H_ */


