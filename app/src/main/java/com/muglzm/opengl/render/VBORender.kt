package com.muglzm.opengl.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.muglzm.opengl.util.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

class VBORender:GLSurfaceView.Renderer {
    private val vertexShaderCode =
        "#version 300 es\n" +
                "precision mediump float;\n" +
                "layout(location = 0) in vec4 a_position;\n" +
                "layout(location = 1) in vec2 a_textureCoordinate;\n" +
                "out vec2 v_textureCoordinate;\n" +
                "void main() {\n" +
                "    v_textureCoordinate = a_textureCoordinate;\n" +
                "    gl_Position = a_position;\n" +
                "}"
    private val fragmentShaderCode =
        "#version 300 es\n" +
                "precision mediump float;\n" +
                "precision mediump sampler2D;\n" +
                "layout(location = 0) out vec4 fragColor;\n" +
                "layout(location = 0) uniform sampler2D u_texture;\n" +
                "in vec2 v_textureCoordinate;\n" +
                "void main() {\n" +
                "    fragColor = texture(u_texture, v_textureCoordinate);\n" +
                "}"

    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0
    //XY:vertex UV: texture
    private val vertexData = floatArrayOf(
        -1f, -1f,   0f, 1f,     // x, y, u, v
        -1f, 1f,    0f, 0f,
        1f, 1f,     1f, 0f,
        1f, -1f,    1f, 1f
    )
    // 顶点索引数据
    private val indexData = intArrayOf(0, 1, 2, 0, 2, 3)

    // 要渲染的图片纹理
    private var imageTexture = 0

    // a_position、a_textureCoordinate和u_texture的layout位置，与shader中写的对应
    private val LOCATION_ATTRIBUTE_POSITION = 0
    private val LOCATION_ATTRIBUTE_TEXTURE_COORDINATE = 1
    private val LOCATION_UNIFORM_TEXTURE = 0
    private val TAG = "LZM"

    // VBO和IBO
    private var vbo = 0
    private var ibo = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        //创建GL程序
        val programId = GLES30.glCreateProgram()

        //创建顶点shader
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        //链接shader和顶点数组
        GLES30.glShaderSource(vertexShader,vertexShaderCode)
        GLES30.glShaderSource(fragmentShader,fragmentShaderCode)
        //编译shader
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)

        //将shader贴到GL程序上
        GLES30.glAttachShader(programId,vertexShader)
        GLES30.glAttachShader(programId,fragmentShader)

        //链接程序
        GLES30.glLinkProgram(programId)

        //使用程序
        GLES30.glUseProgram(programId)

        //创建VBO和IBO
        val buffers = IntArray(2)
        GLES30.glGenBuffers(buffers.size,buffers,0)
        vbo = buffers[0]
        ibo = buffers[1]
        //将顶点和纹理数据放入buffers
        val vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * java.lang.Float.SIZE / 8, vertexDataBuffer, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, 2, GLES30.GL_FLOAT, false, 16, 0)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, 2, GLES30.GL_FLOAT, false, 16, 8)

        // 将顶点索引数据载入IBO
        val indexDataBuffer = ByteBuffer.allocateDirect(indexData.size * Integer.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
        indexDataBuffer.put(indexData)
        indexDataBuffer.position(0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexDataBuffer.capacity() * Integer.SIZE / 8, indexDataBuffer, GLES30.GL_STATIC_DRAW)

        //创建图片纹理
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture = textures[0]

        // 将图片解码并加载到纹理中
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        val bitmap = Util.decodeBitmapFromAssets("test.png")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)

        //设置环绕过滤参数
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        //贴图
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width,
            bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)
        bitmap.recycle()

        // 启动对应位置的参数，这里直接使用LOCATION_UNIFORM_TEXTURE，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_UNIFORM_TEXTURE, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glUniform1i(LOCATION_UNIFORM_TEXTURE, 0)

        Log.d("LZM", "onSurfaceCreated: create render")

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glSurfaceViewHeight = height
        glSurfaceViewWidth = width
    }

    override fun onDrawFrame(gl: GL10?) {
        //Clear
        GLES30.glClearColor(0.9f,0.9f,0.9f,1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        Log.d(TAG, "onDrawFrame: Draw frame")
        //Set view area
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER,ibo)

        //激活纹理0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理0
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        GLES30.glDrawElements(GLES30.GL_TRIANGLES,indexData.size, GLES30.GL_UNSIGNED_INT, 0)
    }
}