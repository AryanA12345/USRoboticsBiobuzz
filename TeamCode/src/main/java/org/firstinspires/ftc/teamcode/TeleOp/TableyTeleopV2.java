package org.firstinspires.ftc.teamcode.TeleOp;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.controllers.PIDController;
import com.pedropathing.math.Pose;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.skeletonarmy.marrow.telemetry.FormatBuilder;
import com.skeletonarmy.marrow.telemetry.HtmlColor;
import com.skeletonarmy.marrow.telemetry.MarrowTelemetry;
import com.skeletonarmy.marrow.telemetry.modifiers.BoldModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.ColorModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.EmptyModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.HtmlTextSize;
import com.skeletonarmy.marrow.telemetry.modifiers.SizeModifier;

import org.firstinspires.ftc.teamcode.CV.CVPathGenWeaver;
import org.firstinspires.ftc.teamcode.helper.Launcher;
import org.firstinspires.ftc.teamcode.helper.OpModeStorage;
import org.firstinspires.ftc.teamcode.helper.RobotRenderer;
import org.firstinspires.ftc.teamcode.helper.ShooterConstants;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Tabley TeleOp V2")
public class TableyTeleopV2 extends LinearOpMode {
    private Launcher launcher;
    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    // Original waypoints
    private final Pose waypoint1 = poseFactory.of(56.14, 11.77, 270);
    private final Pose waypoint2 = poseFactory.of(61.9166, 131.2101, 90);

    // New shooting waypoints (waypoint3 and its counterpart flipped across y = 72)
    private final Pose waypoint3 = poseFactory.of(14.1948, 116.8291, 0);
    private final Pose waypoint4 = poseFactory.of(14.1948, 27.1709, 0);

    // Quadrant Flower Coordinates
    private final Pose flowerQ1 = poseFactory.of(140.31, 94.97, 0);
    private final Pose flowerQ2 = poseFactory.of(47.09, 139.74, 0);
    private final Pose flowerQ3 = poseFactory.of(1.34, 46.86, 0);
    private final Pose flowerQ4 = poseFactory.of(94.78, 1.62, 0);

    private boolean lastRightBumper = false;
    private CVPathGenWeaver pather;

    private boolean hiveIsRight = true;
    private boolean isOriented = false;
    private double targetX;
    private double targetY;
    private PIDController headingPID = new PIDController(1.2, 0.0, 0.0);

    private double offset = ShooterConstants.offset;
    private boolean isRed;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MarrowTelemetry(telemetry);
        launcher = new Launcher(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        Servo flowerArm0 = hardwareMap.get(Servo.class, "flowerArm0");
        Servo flowerArm1 = hardwareMap.get(Servo.class, "flowerArm1");
        isRed = OpModeStorage.isRedAlliance;

        Servo releaseP = hardwareMap.get(Servo.class, "releaseP");

        follower = Constants.create(hardwareMap);
        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
        pather = new CVPathGenWeaver(hardwareMap, follower, isRed, telemetry);

        waitForStart();
        resetRuntime();
        launcher.spinUp();
        follower.update();

        while (opModeIsActive()) {
            boolean currentRightBumper = gamepad1.right_bumper;
            boolean physicsShootTriggered = false;

            offset = ShooterConstants.offset;

            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );

            // --- FEATURE 2: Physics-Based Distance Shooting (Dpad Down) ---
            if (gamepad1.dpadDownWasPressed()) {
                Pose robotPose = follower.pose();
                double distance = Math.hypot(targetX - robotPose.x(), targetY - robotPose.y());
                double calculatedVelocity = 50.0 * Math.sqrt(distance) + 200.0;
                launcher.spinUpCustomVelocity(calculatedVelocity);
                physicsShootTriggered = true;
            }

            boolean triggerShoot = (currentRightBumper && !lastRightBumper) || physicsShootTriggered;
            launcher.update(triggerShoot);

            // --- Y BUTTON TOGGLE (MIDDLE-HIVE HEADING LOCK) ---
            if (gamepad1.yWasPressed()) {
                isOriented = !isOriented;
            }

            // --- FEATURE 3: Quadrant-Based Flower Navigation & Intake (X Button) ---
            boolean flowerIntakeActive = false;
            if (gamepad1.xWasPressed()) {
                Pose robotPose = follower.pose();
                Pose targetFlower;
                if (robotPose.x() > 72 && robotPose.y() > 72) {
                    targetFlower = flowerQ1;
                } else if (robotPose.x() <= 72 && robotPose.y() > 72) {
                    targetFlower = flowerQ2;
                } else if (robotPose.x() <= 72 && robotPose.y() <= 72) {
                    targetFlower = flowerQ3;
                } else {
                    targetFlower = flowerQ4;
                }

                follower.follow(Paths.line(robotPose, targetFlower).linear(robotPose, targetFlower));
                flowerArm0.setPosition(0.0);
                flowerArm1.setPosition(1.0);
                flowerIntakeActive = true;
            }

            // --- D-PAD UP: Original Hive Navigation ---
            if (gamepad1.dpadUpWasPressed()) {
                Pose robotPose = follower.pose();
                if (robotPose.y() > 72) {
                    follower.follow(Paths.line(robotPose, waypoint2).linear(robotPose, waypoint2));
                } else {
                    follower.follow(Paths.line(robotPose, waypoint1).linear(robotPose, waypoint1));
                }
            }

            // --- FEATURE 1: D-PAD RIGHT: New Shooting Position Navigation ---
            if (gamepad1.dpadRightWasPressed()) {
                Pose robotPose = follower.pose();
                if (robotPose.y() > 72) {
                    follower.follow(Paths.line(robotPose, waypoint3).linear(robotPose, waypoint3));
                } else {
                    follower.follow(Paths.line(robotPose, waypoint4).linear(robotPose, waypoint4));
                }
            }

            if(gamepad1.dpad_left){
                flowerArm0.setPosition(0.0);
                flowerArm1.setPosition(1.0);
            }

            if(follower.pose().y() > 70) {
                hiveIsRight = false;
            } else {
                hiveIsRight = true;
            }

            if (isOriented) {
                if (hiveIsRight) {
                    targetX = 58;
                    targetY = 52;
                } else {
                    targetX = 58;
                    targetY = 90;
                }

                Pose robotPose = follower.pose();
                double currentX = robotPose.x();
                double currentY = robotPose.y();

                double distanceToTarget = Math.hypot(targetX - currentX, targetY - currentY);
                double baseAngle = Math.atan2(targetY - currentY, targetX - currentX);
                double dynamicOffset = Math.atan2(offset, distanceToTarget);
                double arctanOrientation = baseAngle + Math.PI + dynamicOffset;

                powers = ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
            }

            boolean driverMovingSticks = Math.abs(gamepad1.left_stick_x) > 0.1
                    || Math.abs(gamepad1.left_stick_y) > 0.1
                    || Math.abs(gamepad1.right_stick_x) > 0.1;

            if (!follower.isBusy() || driverMovingSticks) {
                ManualDrive.driveOrHold(follower, powers);
            }
            follower.update();

            Pose robotPose = follower.pose();
            lastRightBumper = currentRightBumper;

            if(gamepad1.a){
                follower.setPose(poseFactory.of(9.3, 131.5, 0));
            }

            if(launcher.getCurrentState() != Launcher.ShootState.RUN_RELEASE) {
                releaseP.setPosition((gamepad1.left_bumper) ? 0.0 : 1.0);
            }

            if (flowerIntakeActive) {
                intake.setPower(1.0);
            } else {
                intake.setPower(gamepad1.left_trigger_pressed ? -1.0 : gamepad1.right_trigger);
            }

            telemetry.addData("Shoot State", String.valueOf(launcher.getCurrentState()), new BoldModifier());
            telemetry.addData("Flywheel P Velocity", launcher.getFlywheelPVelocity());
            telemetry.addData("Robot X", robotPose.x());
            telemetry.addData("Robot Y", robotPose.y());
            telemetry.addData("Robot Heading", Math.toDegrees(robotPose.heading()));
            new FormatBuilder(isOriented())
                    .addConditional(this::isOriented, new ColorModifier(HtmlColor.LIME_GREEN), new ColorModifier(HtmlColor.RED))
                    .italic()
                    .printData(telemetry, "Is Oriented (Auto-Align)");
            new FormatBuilder(timeLeft())
                    .addConditional(this::last10Seconds, new ColorModifier(HtmlColor.RED), new ColorModifier(HtmlColor.LIME_GREEN))
                    .bold()
                    .addConditional(this::last10Seconds, new SizeModifier(HtmlTextSize.BIG), new EmptyModifier())
                    .printData(telemetry, "Time Remaining");

            RobotRenderer.drawRobot(follower);
            telemetry.update();
        }

        launcher.stop();
    }

    private int timeLeft() {
        return (int)(120 - getRuntime());
    }
    private boolean last10Seconds(){
        return timeLeft() <= 10;
    }

    private boolean isOriented() {
        return isOriented;
    }
}
