package com.muglzm.opengl.render.lighting

import javax.microedition.khronos.opengles.GL10

class ParticlesRender:LightingRenderer("lighting/particles_vertex.glsl","lighting/particles_fragment.glsl") {

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)

    }
}