package com.example.kamranshamloo.fromscratchopengles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * Created by kamran.shamloo on 2016-07-21.
 */
public class MyGLSurfaceView extends GLSurfaceView implements RotationGestureDetector.OnRotationGestureListener{
    private final MyGLRenderer mRenderer;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mDensity;
    private float mPreviousX;
    private float mPreviousY;
    private RotationGestureDetector mRotationDetector;


    public MyGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((AppCompatActivity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDensity = displayMetrics.density;
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mRotationDetector = new RotationGestureDetector(this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        mRotationDetector.onTouchEvent(motionEvent);

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = motionEvent.getX();
        float y = motionEvent.getY();


        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_MOVE: {
                if (!mRotationDetector.isInProgress()) {
                    float deltaX = (x - mPreviousX) / mDensity / 2f;
                    float deltaY = (y - mPreviousY) / mDensity / 2f;

                    mRenderer.mDeltaX += deltaX;
                    mRenderer.mDeltaY += deltaY;
                    //mRenderer.setAngle(mRenderer.getAngle() + (deltaX + deltaY));
                    requestRender();
                }

                // reverse direction of rotation above the mid-line
//                if (y > getHeight()/ 2){
//                    dx = dx * -1;
//                }
//
//                if (x < getWidth() / 2) {
//                    dy  = dy * -1;
//                }

//                mRenderer.setAngle(
//                        mRenderer.getAngle() +
//                                ((dx + dy) * TOUCH_SCALE_FACTOR));
//                requestRender();
            }
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;

//        else
//        {
//            return super.onTouchEvent(motionEvent);
//        }

    }

    @Override
    public void OnRotation(RotationGestureDetector rotationDetector) {
        mRenderer.setAngleAroundZ(rotationDetector.getAngle());
        requestRender();
    }
}
