package com.muglzm.opengl.samples

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muglzm.opengl.R
import com.muglzm.opengl.render.FBORender
import com.muglzm.opengl.render.VBORender

class FBO: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //获取layout视图
        Log.d("LZM", "onCreateView: create shader fragment")
        val rootview = inflater.inflate(R.layout.fragment_shader,container,false)
        val glSurfaceView = rootview.findViewById<GLSurfaceView>(R.id.glsurfaceview)
        //Set EGL Version 3
        glSurfaceView.setEGLContextClientVersion(3)
        //Set RGBA:8888
        glSurfaceView.setEGLConfigChooser(8,8,8,8,0,0)
        glSurfaceView.setRenderer(FBORender())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        return rootview

    }
}