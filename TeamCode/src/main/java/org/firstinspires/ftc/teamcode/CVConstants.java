package org.firstinspires.ftc.teamcode;

import com.pedropathing.math.Matrix;

public class CVConstants {
    public static final double CAMERA_HEIGHT = 9.0-1.4; // CHANGE THIS
    public static final double CAMERA_PITCH = Math.toRadians(1.0); // CHANGE THIS
    public static final double FORWARD_OFFSET = 8.5; // CHANGE THIS

    public static final Matrix xyzConverter = new Matrix(
            new double[][] {
                    {1, 0, 0},
                    {0, 0, 1},
                    {0, 1, 0},
            });

    public static Matrix createRotationX(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Matrix(
                new double[][] {
                        {1, 0,  0},
                        {0, c, -s},
                        {0, s,  c},
                }
        );
    }

}