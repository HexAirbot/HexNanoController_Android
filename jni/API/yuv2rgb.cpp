#include <jni.h>  
#include <android/log.h>  
  
#include <GLES2/gl2.h>  
#include <GLES2/gl2ext.h>  
  
#include <stdio.h>  
#include <stdlib.h>  
#include <math.h>  
#include <pthread.h>  
#include "yuv2rgb.h"
#include "vmcipc.h"
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "yuv2rgb", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "yuv2rgb", __VA_ARGS__))
#define log(...) ((void)__android_log_print(ANDROID_LOG_WARN, "yuv2rgb", __VA_ARGS__))
#define log_easy(...) ((void)__android_log_print(ANDROID_LOG_WARN, "yuv2rgb", __VA_ARGS__))


enum {  
    ATTRIB_VERTEX,  
    ATTRIB_TEXTURE,  
};  
  
static GLuint g_texYId;  
static GLuint g_texUId;  
static GLuint g_texVId;  
static GLuint simpleProgram;  
  
static char *              g_buffer = NULL;  
static int                 g_width = 0;  
static int                 g_height = 0;  
  


static const char* FRAG_SHADER =  
    "varying lowp vec2 tc;\n"  
    "uniform sampler2D SamplerY;\n"  
    "uniform sampler2D SamplerU;\n"  
    "uniform sampler2D SamplerV;\n"  
    "void main(void)\n"  
    "{\n"  
        "mediump vec3 yuv;\n"  
        "lowp vec3 rgb;\n"  
        "yuv.x = texture2D(SamplerY, tc).r;\n"  
        "yuv.y = texture2D(SamplerU, tc).r - 0.5;\n"  
        "yuv.z = texture2D(SamplerV, tc).r - 0.5;\n"  
        "rgb = mat3( 1,   1,   1,\n"  
                    "0,       -0.39465,  2.03211,\n"  
                    "1.13983,   -0.58060,  0) * yuv;\n"  
        "gl_FragColor = vec4(rgb, 1);\n"  
    "}\n";  


static const char* VERTEX_SHADER =    
      "attribute vec4 vPosition;    \n"  
      "attribute vec2 a_texCoord;   \n"  
      "varying vec2 tc;     \n"  
      "void main()                  \n"  
      "{                            \n"  
      "   gl_Position = vPosition;  \n"  
      "   tc = a_texCoord;  \n"  
      "}                            \n";  



  
static void checkGlError(const char* op)   
{  
    GLint error;  
    for (error = glGetError(); error; error = glGetError())   
    {  
        log("error::after %s() glError (0x%x)\n", op, error);  
    }  
}  
  
static GLuint bindTexture(GLuint texture, const char *buffer, GLuint w , GLuint h)  
{  
//  GLuint texture;  
//  glGenTextures ( 1, &texture );  
//    checkGlError("glGenTextures");  
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
  
static void renderFrame2()  
{  
#if 0  
    // Galaxy Nexus 4.2.2  
    static GLfloat squareVertices[] = {  
        -1.0f, -1.0f,  
        1.0f, -1.0f,  
        -1.0f,  1.0f,  
        1.0f,  1.0f,  
    };  
  
    static GLfloat coordVertices[] = {  
        0.0f, 1.0f,  
        1.0f, 1.0f,  
        0.0f,  0.0f,  
        1.0f,  0.0f,  
    };  
#else  
 // HUAWEIG510-0010 4.1.1  
    static GLfloat squareVertices[] = {  
        0.0f, 0.0f,  
        1.0f, 0.0f,  
        0.0f,  1.0f,  
        1.0f,  1.0f,  
    };  
  
    static GLfloat coordVertices[] = {  
            -1.0f, 1.0f,  
            1.0f, 1.0f,  
            -1.0f,  -1.0f,  
            1.0f,  -1.0f,  
    };  
#endif  
  
    glClearColor(0.5f, 0.5f, 0.5f, 1);  
    checkGlError("glClearColor");  
    glClear(GL_COLOR_BUFFER_BIT);  
    checkGlError("glClear");  
    //PRINTF("setsampler %d %d %d", g_texYId, g_texUId, g_texVId);  
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
  
    glVertexAttribPointer(ATTRIB_VERTEX, 2, GL_FLOAT, 0, 0, squareVertices);  
    checkGlError("glVertexAttribPointer");  
    glEnableVertexAttribArray(ATTRIB_VERTEX);  
    checkGlError("glEnableVertexAttribArray");  
  
    glVertexAttribPointer(ATTRIB_TEXTURE, 2, GL_FLOAT, 0, 0, coordVertices);  
    checkGlError("glVertexAttribPointer");  
    glEnableVertexAttribArray(ATTRIB_TEXTURE);  
    checkGlError("glEnableVertexAttribArray");  
  
    glActiveTexture(GL_TEXTURE0);  
    checkGlError("glActiveTexture");  
    glBindTexture(GL_TEXTURE_2D, g_texYId);  
    checkGlError("glBindTexture");  
	LOGW("%s tex_y:%d\n",__FUNCTION__,tex_y);
    glUniform1i(tex_y, 0);  
    checkGlError("glUniform1i0");  
  
    glActiveTexture(GL_TEXTURE1);  
    checkGlError("glActiveTexture");  
    glBindTexture(GL_TEXTURE_2D, g_texUId);  
    checkGlError("glBindTexture");  
	LOGW("%s tex_u:%d\n",__FUNCTION__,tex_u);
    glUniform1i(tex_u, 1);  
    checkGlError("glUniform1i1");  
  
    glActiveTexture(GL_TEXTURE2);  
    checkGlError("glActiveTexture");  
    glBindTexture(GL_TEXTURE_2D, g_texVId);  
    checkGlError("glBindTexture");  
	LOGW("%s tex_v:%d\n",__FUNCTION__,tex_v);
    glUniform1i(tex_v, 2);  
    checkGlError("glUniform1i2");  
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);  
    checkGlError("glDrawArrays");  
}  
  
static GLuint buildShader(const char* source, GLenum shaderType)   
{  
    GLuint shaderHandle = glCreateShader(shaderType);  
  
    if (shaderHandle)  
    {  
        glShaderSource(shaderHandle, 1, &source, 0);  
        glCompileShader(shaderHandle);  
  
        GLint compiled = 0;  
        glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, &compiled);  
        if (!compiled)  
        {  
            GLint infoLen = 0;  
            glGetShaderiv(shaderHandle, GL_INFO_LOG_LENGTH, &infoLen);  
            if (infoLen)  
            {  
                char* buf = (char*) malloc(infoLen);  
                if (buf)  
                {  
                    glGetShaderInfoLog(shaderHandle, infoLen, NULL, buf);  
                    log_easy("error::Could not compile shader %d:\n%s\n", shaderType, buf);  
                    free(buf);  
                }  
                glDeleteShader(shaderHandle);  
                shaderHandle = 0;  
            }  
        }  
  
    }  
      
    return shaderHandle;  
}  
  
static GLuint buildProgram(const char* vertexShaderSource,  
        const char* fragmentShaderSource)   
{  
    GLuint vertexShader = buildShader(vertexShaderSource, GL_VERTEX_SHADER);  
    GLuint fragmentShader = buildShader(fragmentShaderSource, GL_FRAGMENT_SHADER);  
    GLuint programHandle = glCreateProgram();  
  
    if (programHandle)  
    {  
        glAttachShader(programHandle, vertexShader);  
        checkGlError("glAttachShader");  
        glAttachShader(programHandle, fragmentShader);  
        checkGlError("glAttachShader");  
        glLinkProgram(programHandle);  
  
        GLint linkStatus = GL_FALSE;  
        glGetProgramiv(programHandle, GL_LINK_STATUS, &linkStatus);  
        if (linkStatus != GL_TRUE) {  
            GLint bufLength = 0;  
            glGetProgramiv(programHandle, GL_INFO_LOG_LENGTH, &bufLength);  
            if (bufLength) {  
                char* buf = (char*) malloc(bufLength);  
                if (buf) {  
                    glGetProgramInfoLog(programHandle, bufLength, NULL, buf);  
                    log_easy("error::Could not link program:\n%s\n", buf);  
                    free(buf);  
                }  
            }  
            glDeleteProgram(programHandle);  
            programHandle = 0;  
        }  
  
    }  
  
    return programHandle;  
}  
  
static unsigned char * readYUV(const char *path)  
{  
  
    FILE *fp;  
    unsigned char * buffer;  
    long size = 1280 * 720 * 3 / 2;  
  
    if((fp=fopen(path,"rb"))==NULL)  
    {  
        log("cant open the file");  
       exit(0);  
    }  
  
    buffer = (unsigned char *)malloc(size);  
    memset(buffer,'\0',size);  
    long len = fread(buffer,1,size,fp);  
    //PRINTF("read data size:%ld", len);  
    fclose(fp);  
    return buffer;  
}  
  
void gl_initialize()   
{  
    g_buffer = NULL;  
  
    simpleProgram = buildProgram(VERTEX_SHADER, FRAG_SHADER);  
    glUseProgram(simpleProgram);  
    glGenTextures(1, &g_texYId);  
    glGenTextures(1, &g_texUId);  
    glGenTextures(1, &g_texVId);  
}  
  
void gl_uninitialize()  
{  
  
    g_width = 0;  
    g_height = 0;  
  
    if (g_buffer)  
    {  
        free(g_buffer);  
        g_buffer = NULL;  
    }  
}  
//ÉèÖÃÍ¼ÏñÊý¾Ý  
void gl_set_framebuffer(const char* buffer, int buffersize, int width, int height)  
{  
	g_buffer = (char *)buffer;
	g_width = width;  
	g_height = height;	
#if 0      
    if (g_width != width || g_height != height)  
    {  
        if (g_buffer)  
            free(g_buffer);  
  
        g_width = width;  
        g_height = height;  
  
        g_buffer = (char *)malloc(buffersize);  
    }  
  
    if (g_buffer)  
        memcpy(g_buffer, buffer, buffersize);  
#endif  
}  
//»­ÆÁ  
void gl_render_frame()  
{  
	static int j;
	if(j++>=300)
	{
		//exit(0);
	}
    if (0 == g_width || 0 == g_height)  
        return;  
  
#if 0  
    int width = 448;  
    int height = 336;  
    static unsigned char *buffer = NULL;  
  
    if (NULL == buffer)  
    {  
        char filename[128] = {0};  
        strcpy(filename, "/sdcard/yuv_448_336.yuv");  
        buffer = readYUV(filename);  
    }  
  
#else  
    const char *buffer = g_buffer;  
    int width = g_width;  
    int height = g_height;  
#endif  
    glViewport(0, 0, width, height);  
	lock_disp_buffer();
//    glUseProgram(simpleProgram);  
	
    bindTexture(g_texYId, buffer, width, height);  
    bindTexture(g_texUId, buffer + width * height, width/2, height/2);  
    bindTexture(g_texVId, buffer + width * height * 5 / 4, width/2, height/2);  
	renderFrame2();   
	unlock_disp_buffer();
}  

static pthread_mutex_t g_mut=PTHREAD_MUTEX_INITIALIZER;




