package org.firstinspires.ftc.teamcode.TeleOp;

import com.pedropathing.controllers.PIDController;
import com.pedropathing.math.Pose;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.ShooterConstants;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Tabley TeleOp TestA")
public class TableyTeleOpTestA extends LinearOpMode {

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

    // Button edge detection variables
    private boolean lastRightBumper = false;
    private boolean lastY = false;
    private boolean lastDpadDown = false;
    private boolean lastDpadUp = false;

    // --- HEADING LOCK & ALLIANCE / HIVE VARIABLES ---
    private boolean hiveIsRight = true;
    private boolean isOriented = false;
    private double allianceOffset = 0.0;
    private double targetX;
    private double targetY;
    private PIDController headingPID = new PIDController(1.2, 0.0, 0.0);

    @Override
    public void runOpMode() throws InterruptedException {

        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");

        DcMotorEx flywheelN = hardwareMap.get(DcMotorEx.class, "flywheelN");
        DcMotorEx flywheelP = hardwareMap.get(DcMotorEx.class, "flywheelP");
        flywheelN.setDirection(DcMotorEx.Direction.REVERSE);

        // Apply optimized high-acceleration PIDF constants to keep velocity pinned
        PIDFCoefficients fastCoefficients = new PIDFCoefficients(ShooterConstants.P, ShooterConstants.I, ShooterConstants.D, ShooterConstants.F);
        flywheelN.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
        flywheelP.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);

        Servo releaseN = hardwareMap.get(Servo.class, "releaseN");
        Servo releaseP = hardwareMap.get(Servo.class, "releaseP");

        releaseN.setPosition(1.0); // 0 degrees
        releaseP.setPosition(0.0); // 0 degrees

        waitForStart();

        // Set alliance offset (0.0 for Red, Math.PI for Blue)
        allianceOffset = OpModeStorage.isRedAlliance ? 0.0 : Math.PI;

        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();

        // SPIN UP BOTH FLYWHEELS IMMEDIATELY AT TELEOP START
        flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelN.setVelocity(launchN);
        flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheelP.setVelocity(launchP);

        while (opModeIsActive()) {
            double speed = 1.0;

            // --- ALLIANCE-AWARE FIELD-CENTRIC DRIVING ---
            double effectiveHeading = follower.pose().heading() + allianceOffset;
            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    effectiveHeading
            );

            // --- Y BUTTON TOGGLE (MIDDLE-HIVE HEADING LOCK) ---
            boolean currentY = gamepad1.y;
            if (currentY && !lastY) {
                isOriented = !isOriented;
            }
            lastY = currentY;

            // --- D-PAD CONFIGURATION FOR HIVE SIDE ---
            boolean currentDpadDown = gamepad1.dpad_down;
            boolean currentDpadUp = gamepad1.dpad_up;

            if (currentDpadDown && !lastDpadDown) {
                hiveIsRight = !hiveIsRight; // Toggle hive side
            }
            if (currentDpadUp && !lastDpadUp) {
                hiveIsRight = true; // Failsafe reset to default
            }
            lastDpadDown = currentDpadDown;
            lastDpadUp = currentDpadUp;

            // --- APPLY AUTO-ALIGN HEADING LOCK IF ENABLED ---
            if (isOriented) {
                if (OpModeStorage.isRedAlliance) {
                    if (hiveIsRight) {
                        targetX = 58;
                        targetY = 52;
                    } else {
                        targetX = 58;
                        targetY = 90;
                    }
                } else {
                    if (hiveIsRight) {
                        targetX = 84;
                        targetY = 52;
                    } else {
                        targetX = 84;
                        targetY = 90;
                    }
                }
                double arctanOrientation = Math.atan2(targetY - follower.pose().y(), targetX - follower.pose().x());
                powers = ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
            }

            ManualDrive.driveOrHold(follower, powers);
            follower.update();

            Pose robotPose = follower.pose();
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

                    if (/*errorN > 20 || */errorP > 20) { //@TODO when we get the nectar motor back up, modify this to actually work
                        stabilityTimer.reset(); // Wait for recovery if recovering from a previous shot
                    }

                    if (stabilityTimer.milliseconds() >= 150) {
                        shootTimer.reset();
                        currentShootState = ShootState.RUN_INTAKE;
                    }
                    break;

                case RUN_INTAKE:
                    if (shootTimer.milliseconds() < 3000) {
                        // Open release servos
                        releaseN.setPosition(0.0);
                        releaseP.setPosition(1.0);
                    } else {
                        releaseN.setPosition(1.0); // Return release servos to 0 degrees
                        releaseP.setPosition(0.0);
                        //flywheelN.setVelocity(0.0);
                        //flywheelP.setVelocity(0.0);
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

            // Diagnostic Telemetry Monitoring
            telemetry.addData("Shoot State", currentShootState);
            telemetry.addData("Flywheel N Velocity", flywheelN.getVelocity());
            telemetry.addData("Flywheel P Velocity", flywheelP.getVelocity());
            telemetry.addData("Robot X", robotPose.x());
            telemetry.addData("Robot Y", robotPose.y());
            telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
            telemetry.addData("Is Oriented (Auto-Align)", isOriented);
            telemetry.addData("Hive Is Right", hiveIsRight);
            telemetry.update();
        }

        // Safeguard to shut down motors safely when the match ends
        flywheelN.setVelocity(0);
        flywheelP.setVelocity(0);
    }
}