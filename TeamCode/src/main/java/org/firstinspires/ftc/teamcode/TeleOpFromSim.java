//package org.firstinspires.ftc.teamcode;
//
//import static com.pedropathing.api.Paths.*;
//
//import com.pedropathing.api.Paths;
//import com.pedropathing.api.PoseFactory;
//import com.pedropathing.math.Pose;
//import com.pedropathing.paths.Path;
//import com.pedropathing.drivetrain.DrivePowers;
//import com.pedropathing.follower.Follower;
//import com.pedropathing.follower.ManualDrive;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.PIDFCoefficients;
//import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.teamcode.pedro.Constants;
//
//@TeleOp(name = "Example TeleOpWSim")
//public class TeleOpFromSim extends LinearOpMode {
//
//    private Follower follower;
//    Pose waypoint = null;
//
//    // --- TELEOP SHOOT STATE MACHINE DEFINITION ---
//    private enum ShootState {
//        IDLE,
//        WAIT_FOR_FLY_STABLE,
//        RUN_INTAKE,
//        COOLDOWN
//    }
//
//    private ShootState currentShootState = ShootState.IDLE;
//    private final ElapsedTime shootTimer = new ElapsedTime();
//    private final ElapsedTime stabilityTimer = new ElapsedTime();
//
//    private boolean lastRightBumper = false;
//
//    @Override
//    public void runOpMode() throws InterruptedException {
//
//        follower = Constants.create(hardwareMap);
//        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
//        DcMotorEx flywheelN = hardwareMap.get(DcMotorEx.class, "flywheel");
//
//        // Apply optimized high-acceleration PIDF constants to keep velocity pinned at 2000
//        PIDFCoefficients fastCoefficients = new PIDFCoefficients(15.0, 3.0, 0.0, 12.5);
//        flywheelN.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
//
//        DcMotorEx flywheelP = hardwareMap.get(DcMotorEx.class, "flywheel");
//
//        // Apply optimized high-acceleration PIDF constants to keep velocity pinned at 2000
//        flywheelP.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
//
//        //DcMotor lift = hardwareMap.get(DcMotor.class, "lift");
//        //DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
//        //Servo hood = hardwareMap.get(Servo.class, "hood");
//        Servo releaseN = hardwareMap.get(Servo.class, "releaseP");
//        Servo releaseP = hardwareMap.get(Servo.class, "releaseN");
//
//        //lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        //turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        //turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        //hood.setPosition(0.5);
//        releaseN.setPosition(0.0);
//        releaseP.setPosition(0.0);
//        //gate.setPosition(0.0);
//
//        waitForStart();
//
//        follower.setPose(OpModeStorage.autonomousEndPose);
//        follower.update();
//
//        // SPIN UP FLYWHEEL IMMEDIATELY AT TELEOP START
//        // It stays on for the entire duration of the match loop
//        flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        flywheelP.setVelocity(1700);
//        flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        flywheelN.setVelocity(1700);
//
//        while (opModeIsActive()) {
//            double speed = 1.0;
//            DrivePowers powers = ManualDrive.fieldCentric(
//                    -gamepad1.left_stick_y,
//                    -gamepad1.left_stick_x,
//                    -gamepad1.right_stick_x,
//                    follower.pose().heading()
//            );
//
//            ManualDrive.driveOrHold(follower, powers);
//            follower.update();
//
//            Pose robotPose = follower.pose();
//            telemetry.addData("Robot X", robotPose.x());
//            telemetry.addData("Robot Y", robotPose.y());
//            telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
//            telemetry.addData("waypoint", waypoint);
//
//            boolean currentRightBumper = gamepad1.right_bumper;
//
//            // --- NON-BLOCKING AUTOMATED SHOOT SEQUENCE ---
//            // Flywheel logic is omitted from the state machine lifecycle below
//            // so it never drops power or shuts off.
//            switch (currentShootState) {
//                case IDLE:
//                    if (currentRightBumper && !lastRightBumper) {
//                        stabilityTimer.reset();
//                        currentShootState = ShootState.WAIT_FOR_FLY_STABLE;
//                    }
//                    break;
//
//                case WAIT_FOR_FLY_STABLE:
//                    double currentVelocity = flywheelN.getVelocity();
//                    double error = Math.abs(1700 - currentVelocity);
//
//                    if (error > 20) {
//                        stabilityTimer.reset(); // Wait for recovery if recovering from a previous shot
//                    }
//
//                    if (stabilityTimer.milliseconds() >= 150) {
//                        shootTimer.reset();
//                        currentShootState = ShootState.RUN_INTAKE;
//                    }
//                    break;
//
//                case RUN_INTAKE:
//                    if (shootTimer.milliseconds() < 2000) {
//                        intake.setPower(1.0);
//                    } else {
//                        intake.setPower(0.0); // Shut off ONLY the intake
//                        shootTimer.reset();
//                        currentShootState = ShootState.COOLDOWN;
//                    }
//                    break;
//
//                case COOLDOWN:
//                    if (shootTimer.milliseconds() >= 500) {
//                        currentShootState = ShootState.IDLE;
//                    }
//                    break;
//            }
//
//            lastRightBumper = currentRightBumper;
//
//            // --- MANUAL MECHANISM CONTROLS ---
//            if (currentShootState != ShootState.RUN_INTAKE) {
//                intake.setPower((gamepad1.b || gamepad1.x) ? -1.0 : (gamepad1.a ? 1.0 : gamepad1.left_trigger));
//            }
//
//            // Safety Check: Force flywheel velocity back to 2000 if encoder modes get mismatched accidentally
//            if (flywheel.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
//                flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            }
//            flywheel.setVelocity(1700);
//
//            lift.setPower(gamepad1.dpad_up ? 0.65 : (gamepad1.dpad_down ? -0.45 : 0.0));
//
//            boolean turretLeft = gamepad1.dpad_left;
//            boolean turretRight = gamepad1.dpad_right;
//            turret.setPower(turretLeft == turretRight ? 0.0 : (turretLeft ? 0.35 : -0.35));
//
//            hood.setPosition(gamepad1.left_bumper ? 0.2 : 0.5);
//            release.setPosition(gamepad1.y ? 1.0 : 0.0);
//            gate.setPosition(gamepad1.b ? 1.0 : 0.0);
//
//            double turretRevolutions = turret.getCurrentPosition() / (537.7 * 5.0);
//            double turretPosition = ((turretRevolutions % 1.0) + 1.0) % 1.0;
//
//            // Diagnostic Telemetry Monitoring
//            telemetry.addData("Shoot State", currentShootState);
//            telemetry.addData("Flywheel Target", 1950);
//            telemetry.addData("Flywheel Velocity", flywheel.getVelocity());
//            telemetry.addData("Turret", "%.1f° (%.3f rev)", turretPosition * 360.0, turretPosition);
//            telemetry.addData("Speed", "%.0f%%", speed * 100.0);
//            telemetry.update();
//        }
//
//        // Safeguard to shut down the motor safely when the driver stops the driver station match execution
//        flywheel.setVelocity(0);
//    }
//}

package org.firstinspires.ftc.teamcode;

import static com.pedropathing.api.Paths.*;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Example TeleOpWSim")
public class TeleOpFromSim extends LinearOpMode {

    private int launchP = ShooterConstants.launchP;
    private int launchN = ShooterConstants.launchN;
    private Follower follower;
    Pose waypoint = null;

    // --- TELEOP SHOOT STATE MACHINE DEFINITION ---
    private enum ShootState {
        IDLE,
        WAIT_FOR_FLY_STABLE,
        RUN_INTAKE,
        COOLDOWN
    }

    private ShootState currentShootState = ShootState.IDLE;
    private final ElapsedTime shootTimer = new ElapsedTime();
    private final ElapsedTime stabilityTimer = new ElapsedTime();

    private boolean lastRightBumper = false;

    @Override
    public void runOpMode() throws InterruptedException {

        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");

        DcMotorEx flywheelN = hardwareMap.get(DcMotorEx.class, "flywheelN");
        DcMotorEx flywheelP = hardwareMap.get(DcMotorEx.class, "flywheelP");
        flywheelN.setDirection(DcMotorEx.Direction.REVERSE);

        // Apply optimized high-acceleration PIDF constants to keep velocity pinned
        PIDFCoefficients fastCoefficients = new PIDFCoefficients(15.0, 3.0, 0.0, 12.5);
        flywheelN.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
        flywheelP.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);

        //DcMotor lift = hardwareMap.get(DcMotor.class, "lift");
        //DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        //Servo hood = hardwareMap.get(Servo.class, "hood");
        Servo releaseN = hardwareMap.get(Servo.class, "releaseN");
        Servo releaseP = hardwareMap.get(Servo.class, "releaseP");
        //Servo gate = hardwareMap.get(Servo.class, "gate");

        //lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        //hood.setPosition(0.5);
        releaseN.setPosition(1.0); // 0 degrees
        releaseP.setPosition(0.0); // 0 degrees
        //gate.setPosition(0.0);

        waitForStart();

        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();

        // SPIN UP BOTH FLYWHEELS IMMEDIATELY AT TELEOP START
        flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelN.setVelocity(0);
        flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelP.setVelocity(0);

        while (opModeIsActive()) {
            double speed = 1.0;
            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );
            /*ManualDrive.driveOrHold(
                    follower,
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x
            );*/
            ManualDrive.driveOrHold(follower, powers);

            //ManualDrive.driveOrHold(follower, powers);
            follower.update();

            Pose robotPose = follower.pose();
            telemetry.addData("Robot X", robotPose.x());
            telemetry.addData("Robot Y", robotPose.y());
            telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
            telemetry.addData("waypoint", waypoint);

            boolean currentRightBumper = gamepad1.right_bumper;

            // --- NON-BLOCKING AUTOMATED SHOOT SEQUENCE ---
            switch (currentShootState) {
                case IDLE:
                    // Keep release servos closed at 0 degrees during idle
                    releaseN.setPosition(1.0);
                    releaseP.setPosition(0.0);
                    flywheelN.setVelocity(launchN);
                    flywheelP.setVelocity(launchP);

                    if (currentRightBumper && !lastRightBumper) {
                        stabilityTimer.reset();
                        flywheelN.setVelocity(launchN);
                        flywheelP.setVelocity(launchP);
                        currentShootState = ShootState.WAIT_FOR_FLY_STABLE;
                    }
                    break;

                case WAIT_FOR_FLY_STABLE:
                    double currentVelocityN = flywheelN.getVelocity();
                    double currentVelocityP = flywheelP.getVelocity();
                    double errorN = Math.abs(launchN - currentVelocityN);
                    double errorP = Math.abs(launchP - currentVelocityP);

                    if (errorN > 20 || errorP > 20) {
                        stabilityTimer.reset(); // Wait for recovery if recovering from a previous shot
                    }

                    if (stabilityTimer.milliseconds() >= 150) {
                        shootTimer.reset();
                        currentShootState = ShootState.RUN_INTAKE;
                    }
                    break;

                case RUN_INTAKE:
                    if (shootTimer.milliseconds() < 2000) {
                        //intake.setPower(1.0);
                        // Open release servos to 90 degrees (0.5 on standard 180° servos)
                        releaseN.setPosition(0.0);
                        releaseP.setPosition(1.0);
                    } else {
                        //intake.setPower(0.0); // Shut off intake
                        releaseN.setPosition(1.0); // Return release servos to 0 degrees
                        releaseP.setPosition(0.0);
                        flywheelN.setVelocity(0.0);
                        flywheelP.setVelocity(0.0);
                        shootTimer.reset();
                        currentShootState = ShootState.COOLDOWN;
                    }
                    break;

                case COOLDOWN:
                    if (shootTimer.milliseconds() >= 500) {
                        currentShootState = ShootState.IDLE;
                    }
                    break;
            }

            lastRightBumper = currentRightBumper;

            // --- MANUAL MECHANISM CONTROLS ---
            if (currentShootState != ShootState.RUN_INTAKE) {
                intake.setPower((gamepad1.b || gamepad1.x) ? -1.0 : (gamepad1.a ? 1.0 : gamepad1.left_trigger));
            }

            // Safety Check: Force both flywheel velocity back to target if encoder modes get mismatched
            if (flywheelN.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if (flywheelP.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            //flywheelN.setVelocity(0.0);
            //flywheelP.setVelocity(0.0);

            //lift.setPower(gamepad1.dpad_up ? 0.65 : (gamepad1.dpad_down ? -0.45 : 0.0));

            //boolean turretLeft = gamepad1.dpad_left;
            //boolean turretRight = gamepad1.dpad_right;
            //turret.setPower(turretLeft == turretRight ? 0.0 : (turretLeft ? 0.35 : -0.35));

            //hood.setPosition(gamepad1.left_bumper ? 0.2 : 0.5);
            //gate.setPosition(gamepad1.b ? 1.0 : 0.0);

            //double turretRevolutions = turret.getCurrentPosition() / (537.7 * 5.0);
            //double turretPosition = ((turretRevolutions % 1.0) + 1.0) % 1.0;

            // Diagnostic Telemetry Monitoring
            telemetry.addData("Shoot State", currentShootState);
            telemetry.addData("Flywheel N Velocity", flywheelN.getVelocity());
            telemetry.addData("Flywheel P Velocity", flywheelP.getVelocity());
            //telemetry.addData("Turret", "%.1f° (%.3f rev)", turretPosition * 360.0, turretPosition);
            //telemetry.addData("Speed", "%.0f%%", speed * 100.0);
            telemetry.update();
        }

        // Safeguard to shut down motors safely when the match ends
        flywheelN.setVelocity(0);
        flywheelP.setVelocity(0);
    }
}