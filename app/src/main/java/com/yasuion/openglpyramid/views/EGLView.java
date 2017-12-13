package com.yasuion.openglpyramid.views;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import com.yasuion.openglpyramid.Pyramid;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Create by AcooL 2017/12/13
 */

public class EGLView extends GLSurfaceView {

    public EGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //设置版本号
       setEGLContextClientVersion(2);
        // 设置渲染器
        setRenderer(new EGLRender());
        // 主动渲染
        setRenderMode(RENDERMODE_CONTINUOUSLY);


    }

    class EGLRender implements Renderer{
        Pyramid pyramid;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //设置背景色（RGBA）
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1);
            //开启深度测试
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            pyramid=new Pyramid();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            pyramid.xAngle = pyramid.xAngle + 0.2f;
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // 设置视口
            GLES20.glViewport(0,0,width,height);
            // 计算宽高比
            float r=(float) width / height;
            // 设置透视投影
            Matrix.frustumM(Pyramid.mProjMatrix ,0,-r,r,-1,1,3,20);
            // 相机位置
            Matrix.setLookAtM(Pyramid.mVMatrix,0,
                    5f,5f,10f, // 相机位置
                    -1f,-1f ,0f,  // 目标位置
                    1f,1f,0f ); // up向量
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            // 清除颜色和深度缓存
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            pyramid.drawSelf();
        }
    }
}
