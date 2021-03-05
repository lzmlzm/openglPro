package com.muglzm.opengl.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.muglzm.opengl.util.Util
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class vrRender(private val vertexShaderPath: String, private val fragmentShaderPath: String):GLSurfaceView.Renderer,OnParameterChangeCallback {
    private lateinit var textureCoordinateDataBuffer : FloatBuffer
    private lateinit var vertexDataBuffer : FloatBuffer
    private lateinit var indicesBuffer  :  ShortBuffer
    private  var indicesNum:Int = 0

    // 要渲染的图片纹理
    // The texture of the image to be rendered
    private var imageTexture = 0

    // a_position、a_textureCoordinate和u_texture的位置，与shader中写的对应
    // The location of a_position、a_textureCoordinate and u_texture, corresponding with which in shader
    private val LOCATION_ATTRIBUTE_POSITION = 0
    private val LOCATION_ATTRIBUTE_TEXTURE_COORDINATE = 1
    private val LOCATION_UNIFORM_MVP = 2
    private val LOCATION_UNIFORM_TEXTURE = 0

    var translateX = 0f
    var translateY = 0f
    var translateZ = 0f
    var rotateX = 0f
    var rotateY = 0f
    var rotateZ = 0f
    var scaleX = 1f
    var scaleY = 1f
    var scaleZ = 1f
    var cameraPositionX = 0f
    var cameraPositionY = 0f
    var cameraPositionZ = 5f
    var lookAtX = 0f
    var lookAtY = 0f
    var lookAtZ = 0f
    var cameraUpX = 0f
    var cameraUpY = 1f
    var cameraUpZ = 0f
    var nearPlaneLeft = -1f
    var nearPlaneRight = 1f
    var nearPlaneBottom = -1f
    var nearPlaneTop = 1f
    var nearPlane = 2f
    var farPlane = 100f

    private val VERTEX_COMPONENT_COUNT = 3

    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2

    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    fun arrayToBuffer(arrays: FloatArray):FloatBuffer
    {
        var tmpBuffer:FloatBuffer

        tmpBuffer = ByteBuffer.allocateDirect(arrays.size * java.lang.Float.SIZE / 8)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
        tmpBuffer.put(arrays)
        tmpBuffer.position(0)
        return tmpBuffer
    }

    fun generateSphere(radius: Float, rings: Int, sectors: Int) {
        val PI = Math.PI.toFloat()
        val PI_2 = (Math.PI / 2).toFloat()

        val R = 1f / rings.toFloat()
        val S = 1f / sectors.toFloat()
        var r: Short
        var s: Short
        var x: Float
        var y: Float
        var z: Float

        val numPoint = (rings + 1) * (sectors + 1)
        val vertexs = FloatArray(numPoint * 3)
        val texcoords = FloatArray(numPoint * 2)
        val indices = ShortArray(numPoint * 6)



        var t = 0
        var v = 0
        val n = 0
        r=0
        while (r < rings) {
            s = 0
            while (s < sectors) {
                x = (Math.cos((2 * PI * s * S).toDouble()) * Math.sin((PI * r * R).toDouble())).toFloat()
                y = (-Math.sin((-PI_2 + PI * r * R).toDouble())).toFloat()
                z = (Math.sin((2 * PI * s * S).toDouble()) * Math.sin((PI * r * R).toDouble())).toFloat()
                texcoords[t++] = s * S
                texcoords[t++] = r * R
                vertexs[v++] = x * radius
                vertexs[v++] = y * radius
                vertexs[v++] = z * radius
                s++
            }
            r++
        }
        var counter = 0
        val sectorsPlusOne = sectors + 1
        r = 0
        while (r < rings) {
            s = 0
            while (s < sectors) {
                indices[counter++] = (r * sectorsPlusOne + s).toShort()       //(a)
                indices[counter++] = ((r + 1) * sectorsPlusOne + s).toShort()    //(b)
                indices[counter++] = (r * sectorsPlusOne + (s + 1)).toShort()  // (c)
                indices[counter++] = (r * sectorsPlusOne + (s + 1)).toShort()  // (c)
                indices[counter++] = ((r + 1) * sectorsPlusOne + s).toShort()    //(b)
                indices[counter++] = ((r + 1) * sectorsPlusOne + (s + 1)).toShort()  // (d)
                s++
            }
            r++
        }
        vertexDataBuffer = arrayToBuffer(vertexs)
        textureCoordinateDataBuffer = arrayToBuffer(texcoords)

        indicesBuffer = ByteBuffer.allocateDirect(indices.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
        indicesBuffer.put(indices)
        indicesBuffer.position(0)
        indicesNum=indices.size
    }

    fun getIdentity(): FloatArray {
        return floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        )
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 创建GL程序
        // Create GL program
        val programId = GLES30.glCreateProgram()

        // 加载、编译vertex shader和fragment shader
        // Load and compile vertex shader and fragment shader
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        val fragmentShader= GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(vertexShader, Util.loadShaderFromAssets(vertexShaderPath))
        GLES30.glShaderSource(fragmentShader, Util.loadShaderFromAssets(fragmentShaderPath))
        GLES30.glCompileShader(vertexShader)
        GLES30.glCompileShader(fragmentShader)

        // 将shader程序附着到GL程序上
        // Attach the compiled shaders to the GL program
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)

        // 链接GL程序
        // Link the GL program
        GLES30.glLinkProgram(programId)

        // 应用GL程序
        // Use the GL program
        GLES30.glUseProgram(programId)

        generateSphere(18.0f,90,180)

        // 启动对应位置的参数，这里直接使用LOCATION_ATTRIBUTE_POSITION，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_ATTRIBUTE_POSITION, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)

        // 指定a_position所使用的顶点数据
        // Specify the data of a_position
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)

        // 启动对应位置的参数，这里直接使用LOCATION_ATTRIBUTE_TEXTURE_COORDINATE，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)

        // 指定a_textureCoordinate所使用的顶点数据
        // Specify the data of a_textureCoordinate
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)

        // 创建图片纹理
        // Create texture for image
        val textures = IntArray(1)
        GLES30.glGenTextures(textures.size, textures, 0)
        imageTexture = textures[0]

        // 将图片解码并加载到纹理中
        // Decode image and load it into texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        val bitmap = Util.decodeBitmapFromAssets("test.png")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap.width,
                bitmap.height, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, b)
        bitmap.recycle()

        // 启动对应位置的参数，这里直接使用LOCATION_UNIFORM_TEXTURE，而无需像OpenGL 2.0那样需要先获取参数的location
        // Enable the parameter of the location. Here we can simply use LOCATION_UNIFORM_TEXTURE, while in OpenGL 2.0 we have to query the location of the parameter
        GLES30.glUniform1i(LOCATION_UNIFORM_TEXTURE, 0)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 记录GLSurfaceView的宽高
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height
        nearPlaneBottom = -height.toFloat() / width
        nearPlaneTop = height.toFloat() / width
    }

    override fun onDrawFrame(gl: GL10?) {

        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES30.glViewport(0, 0, glSurfaceViewWidth, glSurfaceViewHeight)

        // 设置好状态，准备渲染
        // Set the status before rendering
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_POSITION)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_POSITION, VERTEX_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, vertexDataBuffer)
        GLES30.glEnableVertexAttribArray(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE)
        GLES30.glVertexAttribPointer(LOCATION_ATTRIBUTE_TEXTURE_COORDINATE, TEXTURE_COORDINATE_COMPONENT_COUNT, GLES30.GL_FLOAT, false,0, textureCoordinateDataBuffer)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)

        val mvpMatrix = getIdentity()
        val translateMatrix = getIdentity()
        val rotateMatrix = getIdentity()
        val scaleMatrix = getIdentity()
        val modelMatrix = getIdentity()
        val viewMatrix = getIdentity()
        val projectMatrix = getIdentity()


        Log.d("LZM", "onDrawFrame: ")


        // 模型矩阵计算
        // Calculate the Model matrix
        Matrix.translateM(translateMatrix, 0, translateX, translateY, translateZ)
        Matrix.rotateM(rotateMatrix, 0,rotateX , 1f, 0f, 0f)
        Matrix.rotateM(rotateMatrix, 0, rotateY, 0f, 1f, 0f)
        Matrix.rotateM(rotateMatrix, 0, rotateZ, 0f, 0f, 1f)
        Matrix.scaleM(scaleMatrix, 0, scaleX, scaleY, scaleZ)
        Matrix.multiplyMM(modelMatrix, 0, rotateMatrix, 0, scaleMatrix, 0)
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, translateMatrix, 0)

        // 视图矩阵计算
        // Calculate the View matrix
        Matrix.setLookAtM(
                viewMatrix,
                0,
                cameraPositionX, cameraPositionY, cameraPositionZ,
                lookAtX, lookAtY, lookAtZ,
                cameraUpX, cameraUpY, cameraUpZ
        )

        // 透视投影矩阵计算
        // Calculate the Project matrix
        Matrix.frustumM(
                projectMatrix,
                0,
                nearPlaneLeft, nearPlaneRight, nearPlaneBottom, nearPlaneTop,
                nearPlane,
                farPlane
        )

        // MVP矩阵计算
        // Calculate the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, mvpMatrix, 0)

        GLES30.glUniformMatrix4fv(LOCATION_UNIFORM_MVP, 1, false, mvpMatrix, 0)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        // Call the draw method with GL_TRIANGLES to render 3 vertices
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indicesNum,GLES30.GL_UNSIGNED_SHORT,indicesBuffer )
    }

    override fun onParameterChange(parameterKey: String, parameterValue: Float) {
        when (parameterKey) {
            "translateX" -> { translateX = parameterValue }
            "translateY" -> { translateY = parameterValue }
            "translateZ" -> { translateZ = parameterValue }
            "rotateX" -> { rotateX = parameterValue }
            "rotateY" -> { rotateY = parameterValue }
            "rotateZ" -> { rotateZ = parameterValue }
            "scaleX" -> { scaleX = parameterValue }
            "scaleY" -> { scaleY = parameterValue }
            "scaleZ" -> { scaleZ = parameterValue }
            "cameraPositionX" -> { cameraPositionX = parameterValue }
            "cameraPositionY" -> { cameraPositionY = parameterValue }
            "cameraPositionZ" -> { cameraPositionZ = parameterValue }
            "lookAtX" -> { lookAtX = parameterValue }
            "lookAtY" -> { lookAtY = parameterValue }
            "lookAtZ" -> { lookAtZ = parameterValue }
            "cameraUpX" -> { cameraUpX = parameterValue }
            "cameraUpY" -> { cameraUpY = parameterValue }
            "cameraUpZ" -> { cameraUpZ = parameterValue }
            "nearPlaneLeft" -> { nearPlaneLeft = parameterValue }
            "nearPlaneRight" -> { nearPlaneRight = parameterValue }
            "nearPlaneBottom" -> { nearPlaneBottom = parameterValue }
            "nearPlaneTop" -> { nearPlaneTop = parameterValue }
            "nearPlane" -> { nearPlane = parameterValue }
            "farPlane" -> { farPlane = parameterValue }
        }
    }

    override fun onParameterReset() {
        translateX = 0f
        translateY = 0f
        translateZ = 0f
        rotateX = 0f
        rotateY = 0f
        rotateZ = 0f
        scaleX = 1f
        scaleY = 1f
        scaleZ = 1f
        cameraPositionX = 0f
        cameraPositionY = 0f
        cameraPositionZ = 5f
        lookAtX = 0f
        lookAtY = 0f
        lookAtZ = 0f
        cameraUpX = 0f
        cameraUpY = 1f
        cameraUpZ = 0f
        nearPlaneLeft = -1f
        nearPlaneRight = 1f
        nearPlaneBottom = -glSurfaceViewHeight.toFloat() / glSurfaceViewWidth
        nearPlaneTop = glSurfaceViewHeight.toFloat() / glSurfaceViewWidth
        nearPlane = 2f
        farPlane = 100f
    }
}
