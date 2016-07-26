package com.example.kamranshamloo.fromscratchopengles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class Cube {

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

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
//    static float squareCoords[] = {
//            -0.5f,  0.5f, 0.0f,   // top left
//            -0.5f, -0.5f, 0.0f,   // bottom left
//            0.5f, -0.5f, 0.0f,   // bottom right
//            0.5f,  0.5f, 0.0f }; // top right
    //---------------------------
final float[] cubeLineSegmentsPositionData =
        {
                // Front face
                1.0f, 1.0f, 1.0f,    // right-top    corner
                1.0f, -1.0f, 1.0f,   // right-bottom corner

                1.0f, -1.0f, 1.0f,   // right-bottom corner
                -1.0f, -1.0f, 1.0f,  // left-bottom  corner

                -1.0f, -1.0f, 1.0f,  // left-bottom  corner
                -1.0f, 1.0f, 1.0f,   // left-top     corner

                -1.0f, 1.0f, 1.0f,   // left-top     corner
                1.0f, 1.0f, 1.0f,    // right-top    corner

                // Back face
                1.0f, 1.0f, -1.0f,    // right-top    corner
                1.0f, -1.0f, -1.0f,   // right-bottom corner

                1.0f, -1.0f, -1.0f,   // right-bottom corner
                -1.0f, -1.0f, -1.0f,  // left-bottom  corner

                -1.0f, -1.0f, -1.0f,  // left-bottom  corner
                -1.0f, 1.0f, -1.0f,   // left-top     corner

                -1.0f, 1.0f, -1.0f,   // left-top     corner
                1.0f, 1.0f, -1.0f,    // right-top    corner

                // Right face
                1.0f, 1.0f, -1.0f,    // top-far      corner
                1.0f, 1.0f, 1.0f,     // top-close    corner

                1.0f, -1.0f, -1.0f,   // bottom-far   corner
                1.0f, -1.0f, 1.0f,    // bottom-close corner

                // Left face
                -1.0f, 1.0f, -1.0f,    // top-far      corner
                -1.0f, 1.0f, 1.0f,     // top-close    corner

                -1.0f, -1.0f, -1.0f,   // bottom-far   corner
                -1.0f, -1.0f, 1.0f,    // bottom-close corner
        };

    //------------------------------------

    final float[] cubePositionData =
            {
                    // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                    // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                    // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                    // usually represent the backside of an object and aren't visible anyways.

                    // Front face
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,

                    // Right face
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Back face
                    1.0f, 1.0f, -1.0f,    // right-top    corner
                    1.0f, -1.0f, -1.0f,   // right-bottom corner
                    -1.0f, 1.0f, -1.0f,   // left-top     corner
                    1.0f, -1.0f, -1.0f,   // right-bottom corner
                    -1.0f, -1.0f, -1.0f,  // left-bottom  corner
                    -1.0f, 1.0f, -1.0f,   // left-top     corner
                    1.0f, 1.0f, -1.0f,    // right-top    corner // added to draw missing line
                    -1.0f, 1.0f, -1.0f,   // left-top     corner // added to draw missing line

                    // Left face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,

                    // Top face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Bottom face
                    1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
            };
    //---------------------------------------------

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 0.5f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Cube() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                cubeLineSegmentsPositionData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeLineSegmentsPositionData);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

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

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, globalRotationMatrix, 0);
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
//        GLES20.glDrawElements(
//                GLES20.GL_TRIANGLES, drawOrder.length,
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        // Draw the cube.

        GLES20.glLineWidth(8); // Make the edges thicker
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, cubeLineSegmentsPositionData.length/3); //36 vertexes, 6 vertex for each side
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, cubePositionData.length/3); //36 vertexes, 6 vertex for each side
        //GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, and GL_TRIANGLES are accepted.

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}