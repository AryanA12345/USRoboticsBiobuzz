package org.firstinspires.ftc.teamcode;


import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.controllers.PIDController;
import com.pedropathing.math.Pose;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.skeletonarmy.marrow.telemetry.FormatBuilder;
import com.skeletonarmy.marrow.telemetry.HtmlColor;
import com.skeletonarmy.marrow.telemetry.MarrowTelemetry;
import com.skeletonarmy.marrow.telemetry.modifiers.BoldModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.ColorModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.EmptyModifier;
import com.skeletonarmy.marrow.telemetry.modifiers.HtmlTextSize;
import com.skeletonarmy.marrow.telemetry.modifiers.SizeModifier;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.CV.CVPathGenWeaver;
import org.firstinspires.ftc.teamcode.DisabledTeleOp.CVTest1;
import org.firstinspires.ftc.teamcode.helper.Launcher;
import org.firstinspires.ftc.teamcode.helper.OpModeStorage;
import org.firstinspires.ftc.teamcode.helper.RobotRenderer;
import org.firstinspires.ftc.teamcode.helper.ShooterConstants;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

@TeleOp(name = "Tabley TeleOp Test B")
public class TableyTeleOpTestAErrors extends LinearOpMode {

    //    private int launchP = ShooterConstants.launchP;
//    private int launchN = ShooterConstants.launchN;
    private Launcher launcher;
    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose waypoint1 = poseFactory.of(56.14, 11.77, 270);
    private final Pose waypoint2 = poseFactory.of(61.9166, 131.2101, 90);

    // --- TELEOP SHOOT STATE MACHINE DEFINITION ---
    /*
    private enum ShootState {
        IDLE,
        WAIT_FOR_FLY_STABLE,
        RUN_INTAKE,
        COOLDOWN
    }

    private ShootState currentShootState = ShootState.IDLE;
    private final ElapsedTime shootTimer = new ElapsedTime();
    private final ElapsedTime stabilityTimer = new ElapsedTime();

     */

    // Button edge detection variables
    private boolean lastRightBumper = false;
    private CVPathGenWeaver pather;
    /*
    private boolean lastY = false;
    private boolean lastDpadDown = false;
    private boolean lastDpadUp = false;
    */

    // --- HEADING LOCK & ALLIANCE / HIVE VARIABLES ---
    private boolean hiveIsRight = true;
    private boolean isOriented = false;
    private double allianceOffset = 0.0;
    private double targetX;
    private double targetY;
    private AprilTagProcessor aprilTagProcessor;
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
        //intake.setDirection(DcMotorSimple.Direction.REVERSE);
        isRed = OpModeStorage.isRedAlliance;
        boolean toggleIntakeDir = false;

        Servo releaseP = hardwareMap.get(Servo.class, "releaseP");


        follower = Constants.create(hardwareMap);

        follower.setPose(OpModeStorage.autonomousEndPose);
        follower.update();
        pather = new CVPathGenWeaver(hardwareMap, follower, isRed, telemetry);
        /*if(useCamera) {
            aprilTagProcessor = new AprilTagProcessor.Builder()
                    .setTagLibrary(AprilTagGameDatabase.getCurrentGameTagLibrary())
                    .build();

            VisionPortal visionPortal = new VisionPortal.Builder()
                    .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1")) // Make sure your webcam is named "Webcam 1" in your configuration
                    .addProcessor(aprilTagProcessor)
                    .build();
        }

         */

        waitForStart();
        resetRuntime();
        launcher.spinUp();
        //follower.setPose(OpModeStorage.autonomousEndPose);

        follower.update();


        while (opModeIsActive()) {
            double speed = 1.0;
            boolean currentRightBumper = gamepad1.right_bumper;
            boolean triggerShoot = (currentRightBumper && !lastRightBumper);
            launcher.update(triggerShoot);
            //launchP = ShooterConstants.launchP;
            offset = ShooterConstants.offset;

            // --- ALLIANCE-AWARE FIELD-CENTRIC DRIVING ---
//            double effectiveHeading = follower.pose().heading() + allianceOffset+Math.toRadians(180);
            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );

            // --- Y BUTTON TOGGLE (MIDDLE-HIVE HEADING LOCK) ---
            if (gamepad1.yWasPressed()) {
                isOriented = !isOriented;
            }

            // --- D-PAD CONFIGURATION FOR HIVE SIDE ---
            if (gamepad1.dpadDownWasPressed()) {
                hiveIsRight = !hiveIsRight; // Toggle hive side
            }
            /*
            if (gamepad1.dpadUpWasPressed()) {
                hiveIsRight = true; // Failsafe reset to default
            }
             */

            if(gamepad1.dpadUpWasPressed()){
                if(follower.pose().y()>70) {
                    follower.follow(Paths.line(follower.pose(), waypoint2).linear(follower.pose(), waypoint2));
                } else {
                    follower.follow(Paths.line(follower.pose(), waypoint1).linear(follower.pose(), waypoint1));
                }
            }
            /*if(gamepad1.bWasPressed()){
                Path path = pather.pathGen();
                if (path != null) {
                    follower.follow(path);
                }
            }
            */
            if(follower.pose().y()>70) {
                hiveIsRight = false;
            } else {
                hiveIsRight = true;
            }


            // --- APPLY AUTO-ALIGN HEADING LOCK (BACK-LEFT SHOOTER WITH DISTANCE COMPENSATION) ---

            if (isOriented) {
                //if (OpModeStorage.isRedAlliance) {
                if (hiveIsRight) {
                    targetX = 58;
                    targetY = 52;
                } else {
                    targetX = 58;
                    targetY = 90;
                }
                /*} else {
                    if (hiveIsRight) {
                        targetX = 84;
                        targetY = 52;
                    } else {
                        targetX = 84;
                        targetY = 90;
                    }
                }
                 */

                Pose robotPose = follower.pose();
                double currentX = robotPose.x();
                double currentY = robotPose.y();

                // 1. Calculate distance from robot center to target hive
                double distanceToTarget = Math.hypot(targetX - currentX, targetY - currentY);

                // 2. Base angle pointing from robot center to target
                double baseAngle = Math.atan2(targetY - currentY, targetX - currentX);

                // 3. Physical offset of your shooter from the center of rotation (in inches)
                double lateralOffset = offset;

                // 4. Compute dynamic angle adjustment based on distance
                double dynamicOffset = Math.atan2(lateralOffset, distanceToTarget);

                // 5. Math.PI points the back toward target, plus the dynamic lateral offset
                double arctanOrientation = baseAngle + Math.PI + dynamicOffset;

                powers = ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
            }


            /*if (isOriented) {
                boolean isRed = OpModeStorage.isRedAlliance;

                    org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection desiredCluster = null;

                    // Scan visible detections for AprilTag Clusters (SDK 12.0+)
                    for (org.firstinspires.ftc.vision.apriltag.AprilTagDetection detection : aprilTagProcessor.getDetections()) {
                        if (detection instanceof org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection) {
                            org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection clusterDet =
                                    (org.firstinspires.ftc.vision.apriltag.AprilTagClusterDetection) detection;

                            // Check if cluster matches our alliance and is right-side up (|roll| < 90)
                            if (clusterDet.metadata != null &&
                                    clusterDet.metadata.name.contains(isRed ? "RED" : "BLUE") &&
                                    Math.abs(clusterDet.ftcPose.roll) < 90.0) {
                                desiredCluster = clusterDet;
                                break;
                            }
                        }
                    }


                double arctanOrientation;

                if (desiredCluster != null && desiredCluster.ftcPose != null) {
                    // --- OPTION A: VISUAL SERVOING (Hive Cluster in view) ---
                    double tagRelativeAngle = Math.toRadians(desiredCluster.ftcPose.bearing);
                    arctanOrientation = tagRelativeAngle + Math.PI + Math.atan2(offset, desiredCluster.ftcPose.range);
                } else {
                    // --- OPTION B: COORDINATE FALLBACK (Cluster not visible) ---
                    if (isRed) {
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

                    Pose robotPose = follower.pose();
                    double currentX = robotPose.x();
                    double currentY = robotPose.y();

                    double distanceToTarget = Math.hypot(targetX - currentX, targetY - currentY);
                    double baseAngle = Math.atan2(targetY - currentY, targetX - currentX);
                    double dynamicOffset = Math.atan2(offset, distanceToTarget);

                    arctanOrientation = baseAngle + Math.PI + dynamicOffset;
                }

                powers = ManualDrive.headingLock(follower, headingPID, powers, arctanOrientation);
            }

             */

            boolean driverMovingSticks = Math.abs(gamepad1.left_stick_x) > 0.1
                    || Math.abs(gamepad1.left_stick_y) > 0.1
                    || Math.abs(gamepad1.right_stick_x) > 0.1;

            if (!follower.isBusy() || driverMovingSticks) {
                ManualDrive.driveOrHold(follower, powers);
            }
            follower.update();
            //ManualDrive.driveOrHold(follower, powers);
            //follower.update();

            Pose robotPose = follower.pose();
            //boolean currentRightBumper = gamepad1.right_bumper;

            // --- NON-BLOCKING AUTOMATED SHOOT SEQUENCE ---
            /*
            switch (currentShootState) {
                case IDLE:
                    // Keep release servos closed at 0 degrees during idle
                    releaseN.setPosition(1.0);
                    releaseP.setPosition(1.0);
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

                    if (//errorN > 20 || errorP > 20) { //@TODO when we get the nectar motor back up, modify this to actually work
                        stabilityTimer.reset(); // Wait for recovery if recovering from a previous shot
                    }

                    if (stabilityTimer.milliseconds() >= 150) {
                        shootTimer.reset();
                        currentShootState = ShootState.RUN_INTAKE;
                    }
                    break;

                case RUN_INTAKE:
                    if (shootTimer.milliseconds() < 4000) {
                        // Open release servos
                        releaseN.setPosition(0.0);
                        releaseP.setPosition(0.0);
                    } else {
                        releaseN.setPosition(1.0); // Return release servos to 0 degrees
                        releaseP.setPosition(1.0);
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

             */

            lastRightBumper = currentRightBumper;
            if(gamepad1.a){
                follower.setPose(poseFactory.of(9.3,131.5, 0));
            }
            if(gamepad1.dpad_left){
                flowerArm0.setPosition(0.0);
                flowerArm1.setPosition(1.0);
            }
            if(gamepad1.dpad_right){
                flowerArm0.setPosition(1.0);
                flowerArm1.setPosition(0.0);
            }
            if(gamepad2.a){
                intake.setPower(0.0);
            }
            if(gamepad2.bWasPressed()){
                if(toggleIntakeDir){
                    intake.setDirection(DcMotorSimple.Direction.REVERSE);
                    toggleIntakeDir = !toggleIntakeDir;
                } else {
                    intake.setDirection(DcMotorSimple.Direction.FORWARD);
                    toggleIntakeDir = !toggleIntakeDir;
                }
            }
            if(gamepad2.dpadUpWasPressed()){
                follower.setY(follower.pose().y()+2);
            }
            if(gamepad2.dpadUpWasPressed()){
                follower.setY(follower.pose().y()-2);
            }
            if(gamepad2.dpadLeftWasPressed()){
                follower.setX(follower.pose().x()-2);
            }
            if(gamepad2.dpadRightWasPressed()){
                follower.setX(follower.pose().x()+2);
            }
            if(gamepad2.leftBumperWasPressed()){
                follower.setHeading(follower.pose().heading()-2);
            }
            if(gamepad2.rightBumperWasPressed()){
                follower.setHeading(follower.pose().heading()+2);
            }
            if(gamepad2.leftTriggerWasPressed()){
                ShooterConstants.launchP -=20;
            }
            if(gamepad2.rightTriggerWasPressed()){
                ShooterConstants.launchP +=20;
            }


            // --- MANUAL MECHANISM CONTROLS ---
            if(launcher.getCurrentState() != Launcher.ShootState.RUN_RELEASE) {
                releaseP.setPosition((gamepad1.left_bumper) ? 0.0 : 1.0);
            }
            intake.setPower(gamepad1.left_trigger_pressed ? -1.0 : gamepad1.right_trigger);

            // Safety Check: Force both flywheel velocity back to target if encoder modes get mismatched
            /*
            if (flywheelN.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            if (flywheelP.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
                flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }

             */

            // Diagnostic Telemetry Monitoring
            /*
            for (Pose ball : CVTest1.allBalls) {
                telemetry.addLine("(" + ball.x() + ", " + ball.y() + ")");
            }
            */
            telemetry.addData("Shoot State", String.valueOf(launcher.getCurrentState()),new BoldModifier());
            //telemetry.addData("Flywheel N Velocity", launcher.getFlywheelNVelocity());
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
            telemetry.addData("intake", toggleIntakeDir);
            //telemetry.addData("Is Oriented (Auto-Align)", String.valueOf(isOriented), new BoldModifier());
            //telemetry.addData("Hive Is Right", hiveIsRight);
            RobotRenderer.drawRobot(follower);
            telemetry.update();
        }

        // Safeguard to shut down motors safely when the match ends
        //flywheelN.setVelocity(0);
        //flywheelP.setVelocity(0);
        launcher.stop();
    }

    private int timeLeft() {
        return (int)(120-getRuntime());
    }
    private boolean last10Seconds(){
        return timeLeft()<=10;
    }

    private boolean isOriented() {
        return isOriented;
    }
}