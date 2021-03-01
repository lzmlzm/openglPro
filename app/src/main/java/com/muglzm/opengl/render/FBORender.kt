package com.muglzm.opengl.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.muglzm.opengl.util.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FBORender:GLSurfaceView.Renderer {

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

    private val fragmentShaderCode0 =
        "#version 300 es\n" +
        "precision mediump float;\n" +
                "precision mediump sampler2D;\n" +
                "in vec2 v_textureCoordinate;\n" +
                "layout(location = 0) out vec4 fragColor;\n" +
                "layout(location = 0) uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    vec4 color = texture(u_texture, v_textureCoordinate);\n" +
                "    color.b = 0.5;\n" +
                "    fragColor = color;\n" +
                "}"

    private val fragmentShaderCode1 =
        "#version 300 es\n" +
        "precision mediump float;\n" +
                "in vec2 v_textureCoordinate;\n" +
                "layout(location = 0) out vec4 fragColor;\n" +
                "layout(location = 0) uniform sampler2D u_texture;\n" +
                "void main() {\n" +
                "    float offset = 0.005;\n" +
                "    vec4 color = texture(u_texture, v_textureCoordinate) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x - offset, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x + offset, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x - offset * 2.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x + offset * 2.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x - offset * 3.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x + offset * 3.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x - offset * 4.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    color += texture(u_texture, vec2(v_textureCoordinate.x + offset * 4.0, v_textureCoordinate.y)) * 0.11111;\n" +
                "    fragColor = color;\n" +
                "}"

    // GLSurfaceView的宽高
    // The width and height of GLSurfaceView
    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    // 纹理顶点数据
    // The vertex data of the texture
    private val vertexData = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)
    private val VERTEX_COMPONENT_COUNT = 2
    private lateinit var vertexDataBuffer : FloatBuffer

    // 纹理坐标
    // The texture coordinate
    private val textureCoordinateData0 = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)
    private val textureCoordinateData1 = floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f)
    private lateinit var textureCoordinateDataBuffer0 : FloatBuffer
    private lateinit var textureCoordinateDataBuffer1 : FloatBuffer
    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2

    // 2个GL Program
    // two GL Programs
    private var programId0 = 0
    private var programId1 = 0

    // 帧缓存
    // frame buffer
    private var frameBuffer = 0

    // 帧缓绑定的texture
    // the texture bind on the frame buffer
    private var frameBufferTexture = 0

    // 图片texture
    // image texture
    private var imageTexture = 0
    // a_position、a_textureCoordinate和u_texture的layout位置，与shader中写的对应
    private val LOCATION_ATTRIBUTE_POSITION = 0
    private val LOCATION_ATTRIBUTE_TEXTURE_COORDINATE = 1
    private val LOCATION_UNIFORM_TEXTURE = 0
    private val TAG = "LZM"


    private fun createGLProgram(vertexShaderCode : String, fragmentShaderCode : String) : Int {

        // 创建GL程序
        // Create the GL program
        val programId = GLES30.glCreateProgram()

        // 加载、编译vertex shader和fragment shader
        // Load and compile vertex shader and fragment shader
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader= GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderCode)
        GLES30.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)

        // 将shader程序附着到GL程序上
        // Attach the compiled shaders to the GL program
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)

        // 链接GL程序
        // Link the GL program
        GLES30.glLinkProgram(programId)

        Util.checkGLError()

        return programId

    }

    private fun bindGLProgram(programId : Int, texture : Int, textureCoordinateDataBuffer : FloatBuffer) {

        // 应用GL程序
        // Use the GL program
        GLES30.glUseProgram(programId)


        // 启动对应位置的参数
        // Enable the parameter of the location
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)

        // 指定a_position所使用的顶点数据
        // Specify the data of a_position
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)

        // 启动对应位置的参数
        // Enable the parameter of the location
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)

        // 指定a_textureCoordinate所使用的顶点数据
        // Specify the data of a_textureCoordinate
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)

        // 绑定纹理并设置u_texture参数
        // Bind the texture and set the u_texture parameter
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glUniform1i(LOCATION_UNIFORM_TEXTURE, 0)

    }

    private fun initData()
    {
        // 将三角形顶点数据放入buffer中
        // Put the triangle vertex data into the vertexDataBuffer
        vertexDataBuffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexDataBuffer.put(vertexData)
        vertexDataBuffer.position(0)

        // 将纹理坐标数据放入buffer中
        // Put the texture coordinates into the textureCoordinateDataBuffer
        textureCoordinateDataBuffer0 = ByteBuffer.allocateDirect(textureCoordinateData0.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateDataBuffer0.put(textureCoordinateData0)
        textureCoordinateDataBuffer0.position(0)

        textureCoordinateDataBuffer1 = ByteBuffer.allocateDirect(textureCoordinateData1.size * java.lang.Float.SIZE / 8)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureCoordinateDataBuffer1.put(textureCoordinateData1)
        textureCoordinateDataBuffer1.position(0)

        // 创建图片纹理
        // Create texture
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture = textures[0]

        // 设置纹理参数
        // Set texture parameters
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        // 解码图片并加载到纹理中
        // Decode the image and load it into texture
        val bitmap = Util.decodeBitmapFromAssets("test.png")
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width, bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)

    }
    private fun render() {

        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        // Call the draw method with GL_TRIANGLES to render 3 vertices
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexData.size / VERTEX_COMPONENT_COUNT)

    }

    private fun initFrameBuffer(width:Int,height:Int)
    {
        //创建framebuffer绑定的纹理
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size,textures,0)
        frameBufferTexture = textures[0]

        //创建framebuffer
        val framebuffers = IntArray(1)
        GLES30.glGenFramebuffers(framebuffers.size,framebuffers,0)
        frameBuffer = framebuffers[0]

        //绑定frame buffer和texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,frameBufferTexture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, frameBufferTexture, 0)


    }

    private fun bindFrameBuffer(frameBuffer : Int) {

        // 绑定frame buffer
        // Bind the frame buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)

    }
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // 初始化坐标、图片数据
        // Init the coordinates and image texture
        initData()

        // 创建2个GL Program，第一个将图片的蓝色通道全部设为0.5，第二做水平方向模糊
        // Create two GL programs, and one is used for set the blue channel to 0.5, while the other is used for horizontal blur
        programId0 = createGLProgram(vertexShaderCode, fragmentShaderCode0)
        programId1 = createGLProgram(vertexShaderCode, fragmentShaderCode1)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height


        // 初始化frame buffer
        // Init the frame buffer
        initFrameBuffer(width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // 绑定第0个GL Program
        // Bind GL program 0
        bindGLProgram(programId0, imageTexture, textureCoordinateDataBuffer0)

        // 绑定frame buffer
        // Bind the frame buffer
        bindFrameBuffer(frameBuffer)

        // 执行渲染，渲染效果为将图片的蓝色通道全部设为0.5
        // Perform rendering, and we can get the result of blue channel set to 0.5
        render()

        // 绑定第1个GL Program
        // Bind GL program 1
        bindGLProgram(programId1, frameBufferTexture, textureCoordinateDataBuffer1)

        // 绑定0号frame buffer
        // Bind the 0# frame buffer
        bindFrameBuffer(0)

        // 执行渲染，渲染效果水平方向的模糊
        // Perform rendering, and we can get the result of horizontal blur base on the previous result
        render()
    }
}