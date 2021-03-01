package com.muglzm.opengl.samples

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.muglzm.opengl.R
import com.muglzm.opengl.render.lighting.BumpLightRender
import com.muglzm.opengl.render.lighting.DirectionLightRender
import com.muglzm.opengl.render.lighting.PointLightRender
import com.muglzm.opengl.render.lighting.SpotLightRender

class Lighting:Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lighting, container,  false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val directionalLight=view.findViewById<Button>(R.id.directionalLight)
        val pointLight=view.findViewById<Button>(R.id.pointLight)
        val spotLight=view.findViewById<Button>(R.id.spotLight)
        val bumplight=view.findViewById<Button>(R.id.bumped)
        val glSurfaceViewContainer = view.findViewById<FrameLayout>(R.id.glSurfaceViewContainer)
        directionalLight.setOnClickListener {
            updateSample(glSurfaceViewContainer, DirectionLightRender())
        }
        pointLight.setOnClickListener {
            updateSample(glSurfaceViewContainer, PointLightRender())
        }
        spotLight.setOnClickListener {
            updateSample(glSurfaceViewContainer,SpotLightRender())
        }
        bumplight.setOnClickListener {
            updateSample(glSurfaceViewContainer,BumpLightRender())
        }


    }

    private fun updateSample(rootView: View, renderer: GLSurfaceView.Renderer) {
        rootView as ViewGroup
        rootView.removeAllViews()
        rootView.addView(createGLSurfaceView(rootView.context, renderer))
    }

    private fun createGLSurfaceView(context: Context, renderer: GLSurfaceView.Renderer): GLSurfaceView {
        val glSurfaceView = GLSurfaceView(context)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 8, 0)
        glSurfaceView.setRenderer(renderer)
        return glSurfaceView
    }
}