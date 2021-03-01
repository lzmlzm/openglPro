package com.muglzm.opengl.render.lighting

import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

class PointLightRender:LightingRenderer("lighting/pointlight_vertex.glsl","lighting/pointlight_fragment.glsl") {
    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightPos"), 5f, 0f, 0f)
    }
}