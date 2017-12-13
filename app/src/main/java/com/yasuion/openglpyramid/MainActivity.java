package com.yasuion.openglpyramid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.yasuion.openglpyramid.views.EGLView;

public class MainActivity extends AppCompatActivity {

    private EGLView eglView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        eglView = (EGLView) findViewById(R.id.eglview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eglView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eglView.onPause();
    }
}
