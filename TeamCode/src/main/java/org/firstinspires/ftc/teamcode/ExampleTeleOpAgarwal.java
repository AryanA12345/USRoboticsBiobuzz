/**
 * Field-centric teleop with waypoint functionality and position holding
 * Updated on 10/11/2026
 */
package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.Paths;
import com.pedropathing.controllers.PIDController;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.internal.camera.delegating.DelegatingCaptureSequence;
import org.firstinspires.ftc.teamcode.pedro.Constants;


@TeleOp(name = "ExampleTeleOpFixedHeadingTest")
public class ExampleTeleOpAgarwal extends OpMode {

    private Follower follower;
    /**
     * {@code Pose} position set by user in TeleOp by pressing A. Can be traveled back to by pressing B.
     */
    Pose waypoint = null;
    Boolean hiveIsRight = true;
    Boolean isOriented = false;
    double arctanOrientation;
    double targetX;
    double targetY;
    PIDController headingPID = new PIDController(1.2, 0.0, 0.0);


    @Override
    public void init() {

        follower = Constants.create(hardwareMap);

        //Pose waypoint = null;//this will be a waypoint that is set when a is pressed and can be followed when b is pressed
        // new Pose(10.5, 10.5, Math.toRadians(90));


    }
    @Override
    public void start() {
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
    }

//    public void rotateAroundPoint(){
//        double targetX;
//        double targetY;
//        PIDController headingPID = new PIDController(1.2, 0.0, 0.0);
//
//
//        double arctanOrientation;
//        while(!gamepad1.yWasPressed()){
//            if (hiveIsRight){
//                targetX = 58;
//                targetY = 52;
//            } else {
//                targetX = 58;
//                targetY = 90;
//            }
//            arctanOrientation = Math.atan2(targetY-follower.pose().y(), targetX - follower.pose().x());
//              double headingError = arctanOrientation - follower.pose().heading();
//              while (headingError > Math.PI)  headingError -= 2 * Math.PI;
//              while (headingError < -Math.PI) headingError += 2 * Math.PI;
//
//            ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
//            follower.update();
//
//        }
//
//
//    }
    @Override
    public void loop() {
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                follower.pose().heading()
        );

        if (isOriented){
            if (hiveIsRight){
                targetX = 58;
                targetY = 52;
            } else {
                targetX = 58;
                targetY = 90;
            }
            arctanOrientation = Math.atan2(targetY-follower.pose().y(), targetX - follower.pose().x());
            powers = ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
        }

        ManualDrive.driveOrHold(follower, powers);
        follower.update();
        Pose robotPose = follower.pose(); // returns a Pose object
        telemetry.addData("Robot X", robotPose.x());
        telemetry.addData("Robot Y", robotPose.y());
        telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
        telemetry.addData("waypoint", waypoint);
        telemetry.addData("Is Oriented", isOriented);

        if (gamepad1.a) {
            waypoint = new Pose(robotPose.x(), robotPose.y(), Math.toDegrees(robotPose.heading()));
            telemetry.addData("waypoint", waypoint);
        }
        if (gamepad1.bWasPressed() && waypoint != null) {
            follower.follow(Paths.line(robotPose, waypoint).constant(waypoint.heading()));
        }
        if (gamepad1.dpadDownWasPressed()){ //This is made so Connor Registers which hive is up in teleop
                                            // Also important for automating teleop without really needed april tags that much
            hiveIsRight = !hiveIsRight;

        }
        if (gamepad1.dpadUpWasPressed()){  // If Connor forgets how many times he's pressed dpaddown or
                                           // accidently presses it multiple times (I'm sure he will),
                                           // this is used as a failsafe to set to default
            hiveIsRight = true;

        }
        if (gamepad1.yWasPressed()) {
            isOriented = !isOriented;
        }

    }


}