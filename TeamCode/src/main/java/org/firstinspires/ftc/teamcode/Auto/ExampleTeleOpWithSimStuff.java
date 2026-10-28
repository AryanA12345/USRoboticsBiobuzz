/**
 * Field-centric teleop with waypoint functionality and position holding
 * Updated on 10/11/2026
 */
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

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


@TeleOp(name = "Example TeleOpWSim")
public class ExampleTeleOpWithSimStuff extends LinearOpMode {

        //private final PoseFactory poseFactory = PoseFactory.degrees();

        //private final Pose start = poseFactory.of(133.6022, 8.5551, 90);
        //private final Pose point1 = poseFactory.of(107.0103, 8.4454, 90);
        /*public Path path1(Pose end) {
            return Paths.line(follower.pose(), end).constant(end);
        }*/



    private Follower follower;
    /**
     * {@code Pose} position set by user in TeleOp by pressing A. Can be traveled back to by pressing B.
     */
    Pose waypoint = null;



    @Override
    public void runOpMode() throws InterruptedException {

        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        DcMotor flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        DcMotor lift = hardwareMap.get(DcMotor.class, "lift");
        DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        Servo hood = hardwareMap.get(Servo.class, "hood");
        Servo release = hardwareMap.get(Servo.class, "release");
        Servo gate = hardwareMap.get(Servo.class, "gate");
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hood.setPosition(0.5);
        release.setPosition(0.0);
        gate.setPosition(0.0);
        waitForStart();

        //Pose waypoint = null;//this will be a waypoint that is set when a is pressed and can be followed when b is pressed
        // new Pose(10.5, 10.5, Math.toRadians(90));

 
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
    
        while (opModeIsActive()) {
        double speed = 1.0;
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
        telemetry.addData("waypoint", waypoint);


        intake.setPower((gamepad1.b || gamepad1.x) ? -1.0 : (gamepad1.a ? 1.0 : gamepad1.left_trigger));
        flywheel.setPower(gamepad1.right_trigger);
        lift.setPower(gamepad1.dpad_up ? 0.65 : (gamepad1.dpad_down ? -0.45 : 0.0));
        boolean turretLeft = gamepad1.dpad_left;
        boolean turretRight = gamepad1.dpad_right;
        turret.setPower(turretLeft == turretRight ? 0.0 : (turretLeft ? 0.35 : -0.35));
        hood.setPosition(gamepad1.left_bumper ? 0.2 : (gamepad1.right_bumper ? 0.8 : 0.5));
        release.setPosition(gamepad1.y ? 1.0 : 0.0);
        gate.setPosition(gamepad1.b ? 1.0 : 0.0);
        double turretRevolutions = turret.getCurrentPosition() / (537.7 * 5.0);
        double turretPosition = ((turretRevolutions % 1.0) + 1.0) % 1.0;
        telemetry.addData("Turret", "%.1f° (%.3f rev)", turretPosition * 360.0, turretPosition);
        //telemetry.addData("Input", "drive %.2f, strafe %.2f, turn %.2f", drive, strafe, turn);
        telemetry.addData("Speed", "%.0f%%", speed * 100.0);

        

    }
    


}
}