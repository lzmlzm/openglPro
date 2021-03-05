//
// Created by Administrator on 2021/3/2.
//
#include "LogUtil.h"
#include "GLUtils.h"
#include <GLES3/gl3ext.h>
#include <stdlib.h>
#include <cstring>

/**
 * 加载已读取的shader指针
 * @param shaderType
 * @param pSource
 * @return
 */
GLuint GLUtils::LoadShader(GLenum shaderType, const char *pSource)
{
    GLuint shader = 0;
    FUN_BEGIN_TIME("GLUtils::LoadShader")
        shader = glCreateShader(shaderType);
        if (shader)
        {
            glShaderSource(shader, 1, &pSource, NULL);
            glCompileShader(shader);
            GLint compiled = 0;
            glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
            if (!compiled)
            {
                GLint infoLen = 0;
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
                if (infoLen)
                {
                    char* buf = (char*) malloc((size_t)infoLen);
                    if (buf)
                    {
                        glGetShaderInfoLog(shader, infoLen, NULL, buf);
                        LOGCATE("GLUtils::LoadShader Could not compile shader %d:\n%s\n", shaderType, buf);
                        free(buf);
                    }
                    glDeleteShader(shader);
                    shader = 0;
                }
            }
        }
    FUN_END_TIME("GLUtils::LoadShader")
    return shader;
}
/**
 * 创建program
 * @param pVertexShaderSource
 * @param pFragShaderSource
 * @param vertexShaderHandle
 * @param fragShaderHandle
 * @return
 */
GLuint GLUtils::CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource, GLuint &vertexShaderHandle, GLuint &fragShaderHandle)
{
    GLuint program = 0;
    FUN_BEGIN_TIME("GLUtils::CreateProgram")
        vertexShaderHandle = LoadShader(GL_VERTEX_SHADER, pVertexShaderSource);
        if (!vertexShaderHandle) return program;
        fragShaderHandle = LoadShader(GL_FRAGMENT_SHADER, pFragShaderSource);
        if (!fragShaderHandle) return program;

        program = glCreateProgram();
        if (program)
        {
            glAttachShader(program, vertexShaderHandle);
            CheckGLError("glAttachShader");
            glAttachShader(program, fragShaderHandle);
            CheckGLError("glAttachShader");
            glLinkProgram(program);
            GLint linkStatus = GL_FALSE;
            glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);

            glDetachShader(program, vertexShaderHandle);
            glDeleteShader(vertexShaderHandle);
            vertexShaderHandle = 0;
            glDetachShader(program, fragShaderHandle);
            glDeleteShader(fragShaderHandle);
            fragShaderHandle = 0;
            if (linkStatus != GL_TRUE)
            {
                GLint bufLength = 0;
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
                if (bufLength)
                {
                    char* buf = (char*) malloc((size_t)bufLength);
                    if (buf)
                    {
                        glGetProgramInfoLog(program, bufLength, NULL, buf);
                        LOGCATE("GLUtils::CreateProgram Could not link program:\n%s\n", buf);
                        free(buf);
                    }
                }
                glDeleteProgram(program);
                program = 0;
            }
        }
    FUN_END_TIME("GLUtils::CreateProgram")
    LOGCATE("GLUtils::CreateProgram program = %d", program);
    return program;
}
/**
 *
 * @param pVertexShaderSource
 * @param pFragShaderSource
 * @param vertexShaderHandle
 * @param fragShaderHandle
 * @param varying
 * @param varyingCount
 * @return
 */
GLuint GLUtils::CreateProgramWithFeedback(const char *pVertexShaderSource, const char *pFragShaderSource, GLuint &vertexShaderHandle, GLuint &fragShaderHandle, GLchar const **varying, int varyingCount)
{
    GLuint program = 0;
    FUN_BEGIN_TIME("GLUtils::CreateProgramWithFeedback")
        vertexShaderHandle = LoadShader(GL_VERTEX_SHADER, pVertexShaderSource);
        if (!vertexShaderHandle) return program;

        fragShaderHandle = LoadShader(GL_FRAGMENT_SHADER, pFragShaderSource);
        if (!fragShaderHandle) return program;

        program = glCreateProgram();
        if (program)
        {
            glAttachShader(program, vertexShaderHandle);
            CheckGLError("glAttachShader");
            glAttachShader(program, fragShaderHandle);
            CheckGLError("glAttachShader");

            //transform feedback
            glTransformFeedbackVaryings(program, varyingCount, varying, GL_INTERLEAVED_ATTRIBS);
            GO_CHECK_GL_ERROR();

            glLinkProgram(program);
            GLint linkStatus = GL_FALSE;
            glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);

            glDetachShader(program, vertexShaderHandle);
            glDeleteShader(vertexShaderHandle);
            vertexShaderHandle = 0;
            glDetachShader(program, fragShaderHandle);
            glDeleteShader(fragShaderHandle);
            fragShaderHandle = 0;
            if (linkStatus != GL_TRUE)
            {
                GLint bufLength = 0;
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
                if (bufLength)
                {
                    char* buf = (char*) malloc((size_t)bufLength);
                    if (buf)
                    {
                        glGetProgramInfoLog(program, bufLength, NULL, buf);
                        LOGCATE("GLUtils::CreateProgramWithFeedback Could not link program:\n%s\n", buf);
                        free(buf);
                    }
                }
                glDeleteProgram(program);
                program = 0;
            }
        }
    FUN_END_TIME("GLUtils::CreateProgramWithFeedback")
    LOGCATE("GLUtils::CreateProgramWithFeedback program = %d", program);
    return program;
}

void GLUtils::DeleteProgram(GLuint &program)
{
    LOGCATE("GLUtils::DeleteProgram");
    if (program)
    {
        glUseProgram(0);
        glDeleteProgram(program);
        program = 0;
    }
}

/**
 * 检查GL操作
 * @param pGLOperation
 */
void GLUtils::CheckGLError(const char *pGLOperation)
{
    for (GLint error = glGetError(); error; error = glGetError())
    {
        LOGCATE("GLUtils::CheckGLError GL Operation %s() glError (0x%x)\n", pGLOperation, error);
    }

}

/**
 * 根据传入的顶点和片段着色器创建program
 * @param pVertexShaderSource
 * @param pFragShaderSource
 * @return programid
 */
GLuint GLUtils::CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource) {
    GLuint vertexShaderHandle, fragShaderHandle;
    return CreateProgram(pVertexShaderSource, pFragShaderSource, vertexShaderHandle, fragShaderHandle);
}

/**
 * 从路径中读取shader.glsl文件
 * @param filePath
 * @return strig指针
 */
string GLUtils::readShaderSource(const char *filePath) {
    string content;
    ifstream fileStream(filePath, ios::in);
    string line = "";
    if (fileStream.is_open()) {
        while (!fileStream.eof()) {
            getline(fileStream, line);
            content.append(line + "\n");
        }
        cout << "file opened" << endl;
    }
    else
    {
        cout << "open errors" << endl;
    }
    fileStream.close();
    return content;
}

/**
 * 将两个glsl文件存入pair
 * @param file1:vertex
 * @param file2:fragment
 * @return 存储两个shader string的指针
 */
pair<string, string> GLUtils::readShader(const char *file1, const char *file2) {
    //(......as before plus)
    string vertShaderStr = readShaderSource(file1);
    string fragShaderStr = readShaderSource(file2);

    pair<string, string> p(vertShaderStr, fragShaderStr);
    cout << "p.first:\n" << p.first<< endl;
    cout << "p.second:\n" << p.second << endl;
    return p;
}
