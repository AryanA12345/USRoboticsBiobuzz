/**
 * OpModeStorage: code snippet from https://pedropathing.com/docs/pathing/guide/teleop-usage#passing-your-position-from-autonomous-to-teleop
 * This snippet takes the robot's ending Pose and sets it in a class to be used as a TeleOp's starting position
 */
package org.firstinspires.ftc.teamcode;
//Make sure this matches your file location

import com.pedropathing.math.Pose;

/**
 * Storage class for {@link #autonomousEndPose} so the robot keeps its position between Autonomous and TeleOp.
 * Default value is {@code Pose(0,0,0);}
 */
public class OpModeStorage {
    public static Pose autonomousEndPose = new Pose(0, 0, 0);

    public static boolean isRedAlliance = true;

    // default pose to avoid null errors
}