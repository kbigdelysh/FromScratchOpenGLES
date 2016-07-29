package com.example.kamranshamloo.fromscratchopengles;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Animation that shows creation of a cuboid using OpenGL ES 2.0.
 */
public class CuboidAnimation {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    //"  gl_FragColor = vColor;" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private final float mStartTime;

    private FloatBuffer vertexBuffer;
    //private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private float mAnimationDuration = 4.0f; // in seconds, playback speed of the animation.

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    final float[] cubeLineSegmentsPositionData =
      {
        -2.0f, -1.0f, -1.0f,   // left-bottom-far corner
        -2.0f, -1.0f, -1.0f,    // moving point
      };

    // Define corners of the cuboid
    private final float[] LEFT_BOTTOM_FAR_CORNER   = {-2.0f, -1.0f, -1.0f};
    private final float[] LEFT_BOTTOM_NEAR_CORNER  = {-2.0f, -1.0f,  1.0f};
    private final float[] LEFT_TOP_NEAR_CORNER     = {-2.0f,  1.0f,  1.0f};
    private final float[] LEFT_TOP_FAR_CORNER      = {-2.0f,  1.0f, -1.0f};
    private final float[] RIGHT_BOTTOM_FAR_CORNER  = {2.0f,  -1.0f, -1.0f};
    private final float[] RIGHT_BOTTOM_NEAR_CORNER = {2.0f,  -1.0f,  1.0f};
    private final float[] RIGHT_TOP_NEAR_CORNER    = {2.0f,   1.0f,  1.0f};
    private final float[] RIGHT_TOP_FAR_CORNER     = {2.0f,   1.0f, -1.0f};

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float color[] = { 1.0f, 0.709803922f, 0.898039216f, 0.5f };
    private long mPreviousTime = 0;
    private float mDeltaTime = 0;
    private float mSpeed = 1.0f;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public CuboidAnimation() {

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        mStartTime = SystemClock.uptimeMillis() * 0.001f; // in seconds
    }
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(final float[] mvpMatrix, final float[] globalRotationMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the cuboid animation vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the cuboid animation coordinate data
        // TODO: create step-by-step animation
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                cubeLineSegmentsPositionData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeLineSegmentsPositionData);
        vertexBuffer.position(0);

        // Calculate Pt(Pt is a point between P1 and P2)
        //float[] vt = new float[]{};
        float vx = LEFT_BOTTOM_NEAR_CORNER[0] - LEFT_BOTTOM_FAR_CORNER[0];
        float vy = LEFT_BOTTOM_NEAR_CORNER[1] - LEFT_BOTTOM_FAR_CORNER[1];
        float vz = LEFT_BOTTOM_NEAR_CORNER[2] - LEFT_BOTTOM_FAR_CORNER[2];
        float[] v = new float[]{vx, vy, vz};
        //float t = 1.0f;
        //long time = SystemClock.uptimeMillis() % 4000L;
        float currentTime = SystemClock.uptimeMillis() * 0.001f;
        float distCovered = (currentTime - mStartTime) * mSpeed;
        float journeyLength = Matrix.length(vx, vy,vz);
        float fracJourney = distCovered / journeyLength;
        if (fracJourney > 1) fracJourney = 1;

        //mDeltaTime += (currentTime - mPreviousTime) * 0.001f ; // in seconds
        //mPreviousTime = currentTime;
        float[] vt = new float[]{
                LEFT_BOTTOM_FAR_CORNER[0] + v[0] * fracJourney,
                LEFT_BOTTOM_FAR_CORNER[1] + v[1] * fracJourney,
                LEFT_BOTTOM_FAR_CORNER[2] + v[2] * fracJourney
        };
        vertexBuffer.position(3);
        vertexBuffer.put(vt,0,3);
        //int len = vertexBuffer.limit();
        //Log.d("buffer length", String.valueOf(len));
        vertexBuffer.position(0);


        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the animation
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, globalRotationMatrix, 0);
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the cuboid
        GLES20.glLineWidth(13); // Make the edges thicker
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, cubeLineSegmentsPositionData.length/3); //36 vertexes, 6 vertex for each side
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, cubePositionData.length/3); //36 vertexes, 6 vertex for each side
        //GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, and GL_TRIANGLES are accepted.

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public float getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        this.mAnimationDuration = animationDuration;
    }
}