package com.example.kamranshamloo.fromscratchopengles;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView mGLView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create a GLSurfaceView instance set it
        // as the ContentView for this activity
        setContentView(R.layout.activity_main);
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainlayout);
        mGLView = new MyGLSurfaceView(this);
        mainLayout.addView(mGLView);

        //setContentView(mGLView); // fullscreen for OpenGL
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        //consume significant memory here.
        mGLView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphics objects for onPause(),
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }
}
