package org.firstinspires.ftc.teamcode;
import static com.pedropathing.api.Paths.*;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedro.Constants;



@TeleOp(name = "Example TeleOp")
public class ExampleTeleOp extends OpMode {
    public class TeleopPaths {

        //private final PoseFactory poseFactory = PoseFactory.degrees();

        //private final Pose start = poseFactory.of(133.6022, 8.5551, 90);
        //private final Pose point1 = poseFactory.of(107.0103, 8.4454, 90);
        /*public Path path1(Pose end) {
            return Paths.line(follower.pose(), end).constant(end);
        }*/

    }
    private Follower follower;
    Pose waypoint = null;
    @Override
    public void init() {

        follower = Constants.create(hardwareMap);

        //Pose waypoint = null;//this will be a waypoint that is set when a is pressed and can be followed when b is pressed
        // new Pose(10.5, 10.5, Math.toRadians(90));

    }

    @Override
    public void loop() {
        DrivePowers powers = ManualDrive.fieldCentric(
                -gamepad1.left_stick_y,
                -gamepad1.left_stick_x,
                -gamepad1.right_stick_x,
                follower.pose().heading()
        );

        ManualDrive.driveOrHold(follower, powers);
        follower.update();
        Pose robotPose = follower.pose(); // returns a Pose object
        telemetry.addData("Robot X", robotPose.x());
        telemetry.addData("Robot Y", robotPose.y());
        telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
        telemetry.addData("waypoint",waypoint);

        if (gamepad1.a) {
            waypoint = new Pose(robotPose.x(), robotPose.y(), Math.toDegrees(robotPose.heading()));
            telemetry.addData("waypoint",waypoint);
        }
        if(gamepad1.bWasPressed() && waypoint!=null){
            follower.follow(Paths.line(robotPose, waypoint).constant(waypoint.heading()));
        }

    }


}