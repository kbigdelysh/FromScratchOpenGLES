package com.example.kamranshamloo.fromscratchopengles;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

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
    private float mStartTime;

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
    private final Float[] LEFT_BOTTOM_FAR_CORNER   = {-2.0f, -1.0f, -1.0f};
    private final Float[] LEFT_BOTTOM_NEAR_CORNER  = {-2.0f, -1.0f,  1.0f};
    private final Float[] LEFT_TOP_NEAR_CORNER     = {-2.0f,  1.0f,  1.0f};
    private final Float[] LEFT_TOP_FAR_CORNER      = {-2.0f,  1.0f, -1.0f};
    private final Float[] RIGHT_BOTTOM_FAR_CORNER  = {2.0f,  -1.0f, -1.0f};
    private final Float[] RIGHT_BOTTOM_NEAR_CORNER = {2.0f,  -1.0f,  1.0f};
    private final Float[] RIGHT_TOP_NEAR_CORNER    = {2.0f,   1.0f,  1.0f};
    private final Float[] RIGHT_TOP_FAR_CORNER     = {2.0f,   1.0f, -1.0f};

    private ArrayList<Float> mPoints = new ArrayList<>();
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 0.5f};
//    private long mPreviousTime = 0;
//    private float mDeltaTime = 0;
    private float mSpeed = 3.0f;
    private boolean mIsFrame1Finished = false;
    private boolean mIsFrame2Finished = false;
    private float mFrame1FinishedTime = 0;
    private float mFrame2FinishedTime = 0;
    private boolean mIsFrame3Finished = false;
    private boolean mIsAnimationFinished = false;

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
        // Calculate Pt(Pt is a point between P1 and P2)
        //float[] vt = new float[]{};
        mPoints = new ArrayList<>();
        mPoints.addAll(drawFrame1());
        if (mIsFrame1Finished) {
            mPoints.addAll(drawFrame2());
        }
        if (mIsFrame2Finished){
            mPoints.addAll(drawFrame3());
        }
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                mPoints.size() * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        for (Float fp : mPoints) {
            vertexBuffer.put(fp);
        }
        //vertexBuffer.put(cubeLineSegmentsPositionData);
        vertexBuffer.position(0);
//        vertexBuffer.position(3);
//        vertexBuffer.put(vt,0,3);

        //int len = vertexBuffer.limit();
        //Log.d("buffer length", String.valueOf(len));

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
        GLES20.glLineWidth(8); // Make the edges thicker
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, mPoints.size()/3); //36 vertexes, 6 vertex for each side
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, cubePositionData.length/3); //36 vertexes, 6 vertex for each side
        //GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, and GL_TRIANGLES are accepted.

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private ArrayList<Float> drawFrame1() {
        ArrayList<Float> points = new ArrayList<>();

        if (!mIsFrame1Finished) {
            float ax = LEFT_BOTTOM_NEAR_CORNER[0] - LEFT_BOTTOM_FAR_CORNER[0];
            float ay = LEFT_BOTTOM_NEAR_CORNER[1] - LEFT_BOTTOM_FAR_CORNER[1];
            float az = LEFT_BOTTOM_NEAR_CORNER[2] - LEFT_BOTTOM_FAR_CORNER[2];
            float[] a = new float[]{ax, ay, az};

            float bx = LEFT_TOP_FAR_CORNER[0] - LEFT_TOP_NEAR_CORNER[0];
            float by = LEFT_TOP_FAR_CORNER[1] - LEFT_TOP_NEAR_CORNER[1];
            float bz = LEFT_TOP_FAR_CORNER[2] - LEFT_TOP_NEAR_CORNER[2];
            float[] b = new float[]{bx, by, bz};

            float currentTime = SystemClock.uptimeMillis() * 0.001f;
            float distCovered = (currentTime - mStartTime) * mSpeed;
            float journeyLength_ab = Matrix.length(ax, ay, az);
            float fracJourney = distCovered / journeyLength_ab;
            if (fracJourney > 1) {
                fracJourney = 1;
                mIsFrame1Finished = true;
                mFrame1FinishedTime = currentTime;
            }
            Float[] at = new Float[]{
                    LEFT_BOTTOM_FAR_CORNER[0] + a[0] * fracJourney,
                    LEFT_BOTTOM_FAR_CORNER[1] + a[1] * fracJourney,
                    LEFT_BOTTOM_FAR_CORNER[2] + a[2] * fracJourney
            };
            Float[] bt = new Float[]{
                    LEFT_TOP_NEAR_CORNER[0] + b[0] * fracJourney,
                    LEFT_TOP_NEAR_CORNER[1] + b[1] * fracJourney,
                    LEFT_TOP_NEAR_CORNER[2] + b[2] * fracJourney
            };

            // Frame 1 (two lines)
            points.addAll(Arrays.asList(LEFT_BOTTOM_FAR_CORNER));
            points.addAll(Arrays.asList(at));

            points.addAll(Arrays.asList(LEFT_TOP_NEAR_CORNER));
            points.addAll(Arrays.asList(bt));
        }
        else // Frame1 is already finished. No need for calculation, just draw it
        {
            // Frame 1 (two lines)
            points.addAll(Arrays.asList(LEFT_BOTTOM_FAR_CORNER));
            points.addAll(Arrays.asList(LEFT_BOTTOM_NEAR_CORNER));

            points.addAll(Arrays.asList(LEFT_TOP_NEAR_CORNER));
            points.addAll(Arrays.asList(LEFT_TOP_FAR_CORNER));
        }
        return points;
    }

    private ArrayList<Float> drawFrame2() {
        ArrayList<Float> points = new ArrayList<>();

        if (!mIsFrame2Finished) {
            float cx = LEFT_TOP_NEAR_CORNER[0] - LEFT_BOTTOM_NEAR_CORNER[0];
            float cy = LEFT_TOP_NEAR_CORNER[1] - LEFT_BOTTOM_NEAR_CORNER[1];
            float cz = LEFT_TOP_NEAR_CORNER[2] - LEFT_BOTTOM_NEAR_CORNER[2];
            float[] c = new float[]{cx, cy, cz};

            float dx = LEFT_BOTTOM_FAR_CORNER[0] - LEFT_TOP_FAR_CORNER[0];
            float dy = LEFT_BOTTOM_FAR_CORNER[1] - LEFT_TOP_FAR_CORNER[1];
            float dz = LEFT_BOTTOM_FAR_CORNER[2] - LEFT_TOP_FAR_CORNER[2];
            float[] d = new float[]{dx, dy, dz};

            float currentTime = SystemClock.uptimeMillis() * 0.001f;
            float distCovered = (currentTime - mFrame1FinishedTime) * mSpeed;
            float journeyLength_ab = Matrix.length(cx, cy, cz);
            float fracJourney = distCovered / journeyLength_ab;
            if (fracJourney > 1) {
                fracJourney = 1;
                mIsFrame2Finished = true;
                mFrame2FinishedTime = currentTime;
            }
            Float[] ct = new Float[]{
                    LEFT_BOTTOM_NEAR_CORNER[0] + c[0] * fracJourney,
                    LEFT_BOTTOM_NEAR_CORNER[1] + c[1] * fracJourney,
                    LEFT_BOTTOM_NEAR_CORNER[2] + c[2] * fracJourney
            };
            Float[] dt = new Float[]{
                    LEFT_TOP_FAR_CORNER[0] + d[0] * fracJourney,
                    LEFT_TOP_FAR_CORNER[1] + d[1] * fracJourney,
                    LEFT_TOP_FAR_CORNER[2] + d[2] * fracJourney
            };

            // Frame 2 (two lines)
            points.addAll(Arrays.asList(LEFT_BOTTOM_NEAR_CORNER));
            points.addAll(Arrays.asList(ct));

            points.addAll(Arrays.asList(LEFT_TOP_FAR_CORNER));
            points.addAll(Arrays.asList(dt));
        }
        else // Frame 2 is already finished. No need for calculation, just draw it
        {
            // Frame 2 (two lines)
            points.addAll(Arrays.asList(LEFT_BOTTOM_NEAR_CORNER));
            points.addAll(Arrays.asList(LEFT_TOP_NEAR_CORNER));

            points.addAll(Arrays.asList(LEFT_TOP_FAR_CORNER));
            points.addAll(Arrays.asList(LEFT_BOTTOM_FAR_CORNER));
        }
        return points;
    }
    private ArrayList<Float> drawFrame3() {
        ArrayList<Float> points = new ArrayList<>();

        if (!mIsFrame3Finished) {
            float mx = RIGHT_BOTTOM_FAR_CORNER[0] - LEFT_BOTTOM_FAR_CORNER[0];
            float my = RIGHT_BOTTOM_FAR_CORNER[1] - LEFT_BOTTOM_FAR_CORNER[1];
            float mz = RIGHT_BOTTOM_FAR_CORNER[2] - LEFT_BOTTOM_FAR_CORNER[2];
            float[] m = new float[]{mx, my, mz};

            float nx = RIGHT_BOTTOM_NEAR_CORNER[0] - LEFT_BOTTOM_NEAR_CORNER[0];
            float ny = RIGHT_BOTTOM_NEAR_CORNER[1] - LEFT_BOTTOM_NEAR_CORNER[1];
            float nz = RIGHT_BOTTOM_NEAR_CORNER[2] - LEFT_BOTTOM_NEAR_CORNER[2];
            float[] n = new float[]{nx, ny, nz};

            float ox = RIGHT_TOP_NEAR_CORNER[0] - LEFT_TOP_NEAR_CORNER[0];
            float oy = RIGHT_TOP_NEAR_CORNER[1] - LEFT_TOP_NEAR_CORNER[1];
            float oz = RIGHT_TOP_NEAR_CORNER[2] - LEFT_TOP_NEAR_CORNER[2];
            float[] o = new float[]{ox, oy, oz};

            float px = RIGHT_TOP_FAR_CORNER[0] - LEFT_TOP_FAR_CORNER[0];
            float py = RIGHT_TOP_FAR_CORNER[1] - LEFT_TOP_FAR_CORNER[1];
            float pz = RIGHT_TOP_FAR_CORNER[2] - LEFT_TOP_FAR_CORNER[2];
            float[] p = new float[]{px, py, pz};

            float currentTime = SystemClock.uptimeMillis() * 0.001f;
            float distCovered = (currentTime - mFrame2FinishedTime) * mSpeed;
            float journeyLength_ab = Matrix.length(mx, my, mz);
            float fracJourney = distCovered / journeyLength_ab;
            if (fracJourney > 1) {
                fracJourney = 1;
                mIsFrame3Finished = true;
                mIsAnimationFinished = true;
            }
            Float[] mt = new Float[]{
                    LEFT_BOTTOM_FAR_CORNER[0] + m[0] * fracJourney,
                    LEFT_BOTTOM_FAR_CORNER[1] + m[1] * fracJourney,
                    LEFT_BOTTOM_FAR_CORNER[2] + m[2] * fracJourney
            };
            Float[] nt = new Float[]{
                    LEFT_BOTTOM_NEAR_CORNER[0] + n[0] * fracJourney,
                    LEFT_BOTTOM_NEAR_CORNER[1] + n[1] * fracJourney,
                    LEFT_BOTTOM_NEAR_CORNER[2] + n[2] * fracJourney
            };
            Float[] ot = new Float[]{
                    LEFT_TOP_NEAR_CORNER[0] + o[0] * fracJourney,
                    LEFT_TOP_NEAR_CORNER[1] + o[1] * fracJourney,
                    LEFT_TOP_NEAR_CORNER[2] + o[2] * fracJourney
            };
            Float[] pt = new Float[]{
                    LEFT_TOP_FAR_CORNER[0] + p[0] * fracJourney,
                    LEFT_TOP_FAR_CORNER[1] + p[1] * fracJourney,
                    LEFT_TOP_FAR_CORNER[2] + p[2] * fracJourney
            };

            // Frame 3 (four lines)
            // m
            points.addAll(Arrays.asList(LEFT_BOTTOM_FAR_CORNER));
            points.addAll(Arrays.asList(mt));

            // n
            points.addAll(Arrays.asList(LEFT_BOTTOM_NEAR_CORNER));
            points.addAll(Arrays.asList(nt));

            // o
            points.addAll(Arrays.asList(LEFT_TOP_NEAR_CORNER));
            points.addAll(Arrays.asList(ot));

            // p
            points.addAll(Arrays.asList(LEFT_TOP_FAR_CORNER));
            points.addAll(Arrays.asList(pt));

            // ---- moving square-----
            points.addAll(Arrays.asList(mt));
            points.addAll(Arrays.asList(nt));

            points.addAll(Arrays.asList(nt));
            points.addAll(Arrays.asList(ot));

            points.addAll(Arrays.asList(ot));
            points.addAll(Arrays.asList(pt));

            points.addAll(Arrays.asList(pt));
            points.addAll(Arrays.asList(mt));

        }
        else // Frame 3 is already finished. No need for calculation, just draw it
        {
            // Frame 3 (four lines)
            // m
            points.addAll(Arrays.asList(LEFT_BOTTOM_FAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_BOTTOM_FAR_CORNER));

            // n
            points.addAll(Arrays.asList(LEFT_BOTTOM_NEAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_BOTTOM_NEAR_CORNER));

            // o
            points.addAll(Arrays.asList(LEFT_TOP_NEAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_TOP_NEAR_CORNER));

            // p
            points.addAll(Arrays.asList(LEFT_TOP_FAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_TOP_FAR_CORNER));

            // ----RIGHT square-----
            points.addAll(Arrays.asList(RIGHT_BOTTOM_FAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_BOTTOM_NEAR_CORNER));

            points.addAll(Arrays.asList(RIGHT_BOTTOM_NEAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_TOP_NEAR_CORNER));

            points.addAll(Arrays.asList(RIGHT_TOP_NEAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_TOP_FAR_CORNER));

            points.addAll(Arrays.asList(RIGHT_TOP_FAR_CORNER));
            points.addAll(Arrays.asList(RIGHT_BOTTOM_FAR_CORNER));
        }
        return points;
    }
    public float getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        this.mAnimationDuration = animationDuration;
    }

    public boolean isAnimationFinished() {
        return mIsAnimationFinished;
    }
}