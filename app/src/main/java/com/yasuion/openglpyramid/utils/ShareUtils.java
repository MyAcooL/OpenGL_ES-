package com.yasuion.openglpyramid.utils;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

//加载顶点Shader与片元Shader的工具类
public class ShareUtils {

    /**
     * @Effect 顶点坐标;
     */
    public float[] cubePositions = {
            0f, 1f, 0f, //p0
            -1f, -1f, 0f, //p1
            1f, -1f, 0f, //p2
            0f, 0f, 2f //p3
    };
    /**
     * @Effect 顶点索引（6个面，一个面有六个顶点，也就需要六个索引）;
     */
    public short index[] = {
            0, 1, 2,
            0, 3, 1,
            0, 2, 3,
            2, 1, 3
    };

    /**
     * @Effect 顶点着色(一共8个点，所以需要8个点颜色数据);
     */
    public float color[] = {
            1f, 1f, 0f, 1f,
            1f, 0f, 1f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 1f, 1f,
    };
    /**
     * @Effect 顶点着色器;
     */
    public final String vertexShaderCode =
            "attribute vec4 vPosition;" +//声明一个用attribute修饰的变量（顶点）
                    "uniform mat4 vMatrix;" +//总变换矩阵
                    "varying  vec4 vColor;" +//颜色易变变量（成对出现）
                    "attribute vec4 aColor;" +//声明一个用attribute修饰的变量（颜色）
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +//根据总变换的矩阵计算绘制此顶点的位置
                    "  vColor=aColor;" + //将接收的颜色传递给片元着色器
                    "}";
    /**
     * @Effect 片段着色器;
     */
    public final String fragmentShaderCode =
//   片元语言没有默认浮点精度修饰符
//    因此，对于浮点数，浮点数向量和矩阵变量声明，
// 要么声明必须包含一个精度修饰符，要么不默认精度修饰符在之前 已经被声明过。
            "precision mediump float;" +//预定义的全局默认精度
                    "varying vec4 vColor;" +//接收从顶点着色器过来的参数
                    "void main() {" +
                    "  gl_FragColor = vColor;" + //给此片源颜色值
                    "}";
    /**
     * @param ver_Tex(数组,一般为顶点数据);
     * @return Float型缓冲
     * @Effect 获取FloatBuffer数据缓冲;
     */
    public FloatBuffer getFloatBuffer(float ver_Tex[]) {
        //创建顶点坐标数据缓冲
        //vc.length*4是因为一个整数四个字节
        ByteBuffer bb = ByteBuffer.allocateDirect(ver_Tex.length * 4);
        //设置字节顺序
        // 由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer转换
        bb.order(ByteOrder.nativeOrder());
        //转换为Float型缓冲
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        //向缓冲区中放入顶点坐标数据
        vertexBuffer.put(ver_Tex);
        //设置缓冲区起始位置
        vertexBuffer.position(0);
        return vertexBuffer;
    }

    //加载制定shader的方法
    public int loadShader(int shaderType, String source) {
        //创建一个新shader
        int shader = GLES20.glCreateShader(shaderType);
        //若创建成功则加载shader
        if (shader != 0) {
            //加载shader的源代码
            GLES20.glShaderSource(shader, source);
            //编译shader
            GLES20.glCompileShader(shader);
            //存放编译成功shader数量的数组
            int[] compiled = new int[1];
            //获取Shader的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {//若编译失败则显示错误日志并删除此shader
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    //创建shader程序的方法
    public int createProgram(String vertexSource, String fragmentSource) {
        //加载顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        //加载片元着色器
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        //创建程序
        int program = GLES20.glCreateProgram();
        //若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {
            //向程序中加入顶点着色器
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            //向程序中加入片元着色器
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            //链接程序
            GLES20.glLinkProgram(program);
            //存放链接成功program数量的数组
            int[] linkStatus = new int[1];
            //获取program的链接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            //若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    //检查每一步操作是否有错误的方法
    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    //从sh脚本中加载shader内容的方法
    public String loadFromAssetsFile(String fname, Resources r) {
        String result = null;
        try {
            InputStream in = r.getAssets().open(fname);
            int ch = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * @param index(这个是绘制顶点的索引数组);
     * @return Short型缓冲
     * @Effect 获取ShortBuffer数据缓冲;
     */
    public ShortBuffer getShortBuffer(short index[]) {
        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
        cc.order(ByteOrder.nativeOrder());
        ShortBuffer indexBuffer = cc.asShortBuffer();
        indexBuffer.put(index);
        indexBuffer.position(0);
        return indexBuffer;
    }
}
