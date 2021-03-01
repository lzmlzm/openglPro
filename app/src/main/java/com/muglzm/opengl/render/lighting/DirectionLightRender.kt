package com.muglzm.opengl.render.lighting

import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

class DirectionLightRender :LightingRenderer("lighting/directlight_vertex.glsl", "lighting/directlight_fragment.glsl"){
    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightDirection"), -5f, 0f, 0f)
        //assert(GLES30.glGetError() == 0)
    }
}