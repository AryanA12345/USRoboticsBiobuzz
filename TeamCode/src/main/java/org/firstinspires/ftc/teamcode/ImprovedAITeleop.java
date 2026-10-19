/**
 * TargetAndMoveOpMode with Neural Detector, Non-Blocking State Machine,
 * Submersible Avoidance, Bottom-Right Field Origin (144,0), and Pedro Pathing v3.0.0.
 */
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.paths.Path;
import static com.pedropathing.api.Paths.*;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

@TeleOp(name = "ImprovedAITeleop", group = "Vision")
public class ImprovedAITeleop extends LinearOpMode {

    private Follower follower;
    private PoseFactory poseFactory;

    private final Pose start = poseFactory.of(127.5586, 10.296, 90);

    // Camera specifications
    static final double CAMERA_HEIGHT_IN = 8.25;
    static final double MOUNT_ANGLE_DEG = -2;
    static final double BALL_RADIUS_IN = 2.5;

    // Movement Tuning
    static final double STOP_DIST_IN = 0.0;
    static final double ARRIVAL_TOLERANCE_IN = 1.5;
    static final double SEARCH_SPIN_POWER = 0.45;

    // Submersible Bounding Box (Centered at 72,72 on a 144x144 field)
    static final double SUB_MIN_X = 54.0;
    static final double SUB_MAX_X = 90.0;
    static final double SUB_MIN_Y = 54.0;
    static final double SUB_MAX_Y = 90.0;

    // State Machine States
    private enum ChaseState {
        IDLE,
        SEARCHING,
        PATHING_TO_BALL,
        WAITING_AT_TARGET
    }

    private ChaseState currentState = ChaseState.IDLE;
    private int activeTargetClassId = -1;
    private Pose lockedTargetPose = null;
    private final ElapsedTime waitTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(4); // Pipeline 4 (Neural Detector)
        limelight.start();

        follower = Constants.create(hardwareMap);
        poseFactory = PoseFactory.degrees(); // Pedro v3 PoseFactory using degrees
        follower.setPose(start);

        waitForStart();

        while (opModeIsActive()) {
            follower.update();
            Pose robotPose = follower.pose();

            // 1. Button press triggers target sequence latched (no holding required)
            if (gamepad1.dpad_up) {
                startSequence(0);
            } else if (gamepad1.dpad_left) {
                startSequence(1);
            } else if (gamepad1.dpad_right) {
                startSequence(2);
            }
            // 2. Check for driver cancel inputs (any other button or joystick movement)
            else if (isCancelInputPressed()) {
                cancelSequence();
            }

            // 3. Execute State Machine
            switch (currentState) {
                case IDLE:
                    DrivePowers powers = ManualDrive.fieldCentric(
                            -gamepad1.left_stick_y,
                            -gamepad1.left_stick_x,
                            -gamepad1.right_stick_x,
                            robotPose.heading()
                    );
                    ManualDrive.driveOrHold(follower, powers);
                    break;

                case SEARCHING:
                    executeSearchingPhase(limelight);
                    break;

                case PATHING_TO_BALL:
                    executePathingPhase();
                    break;

                case WAITING_AT_TARGET:
                    ManualDrive.driveOrHold(follower, new DrivePowers(0, 0, 0));
                    if (waitTimer.seconds() >= 3.0) {
                        currentState = ChaseState.IDLE; // Sequence Complete
                    }
                    break;
            }

            // Telemetry output
            telemetry.addData("State", currentState);
            if (lockedTargetPose != null) {
                telemetry.addData("Target X", "%.2f", lockedTargetPose.x());
                telemetry.addData("Target Y", "%.2f", lockedTargetPose.y());
            }
            if (currentState == ChaseState.WAITING_AT_TARGET) {
                telemetry.addData("Wait Timer", "%.1f / 3.0s", waitTimer.seconds());
            }
            telemetry.update();
        }
        limelight.stop();
    }

    private void startSequence(int classId) {
        activeTargetClassId = classId;
        currentState = ChaseState.SEARCHING;
    }

    private void cancelSequence() {
        if (currentState != ChaseState.IDLE) {
            currentState = ChaseState.IDLE;
            lockedTargetPose = null;
            follower.stop(); // Stop active pathing immediately
        }
    }

    private boolean isCancelInputPressed() {
        boolean joystickActive = Math.abs(gamepad1.left_stick_x) > 0.1 ||
                Math.abs(gamepad1.left_stick_y) > 0.1 ||
                Math.abs(gamepad1.right_stick_x) > 0.1;

        boolean otherButtons = gamepad1.a || gamepad1.b || gamepad1.x || gamepad1.y ||
                gamepad1.dpad_down || gamepad1.left_bumper || gamepad1.right_bumper;

        return joystickActive || otherButtons;
    }

    /**
     * Searches for target class and builds a Pedro Path v3 path avoiding the Submersible.
     */
    private void executeSearchingPhase(Limelight3A limelight) {
        LLResult result = limelight.getLatestResult();

        if (result == null || !result.isValid()) {
            follower.manual(0, 0, -SEARCH_SPIN_POWER);
            return;
        }

        List<LLResultTypes.DetectorResult> detections = result.getDetectorResults();
        if (detections == null || detections.isEmpty()) {
            follower.manual(0, 0, -SEARCH_SPIN_POWER);
            return;
        }

        LLResultTypes.DetectorResult target = null;
        for (LLResultTypes.DetectorResult detection : detections) {
            if (detection.getClassId() == activeTargetClassId) {
                target = detection;
                break;
            }
        }

        if (target == null) {
            follower.manual(0, 0, -SEARCH_SPIN_POWER);
            return;
        }

        // Calculate global field coordinates (Bottom-left origin (0,0))
        double tx = target.getTargetXDegrees();
        double dist = distanceToTarget(target.getTargetYDegrees());
        Pose currentPose = follower.pose();

        double headingRad = Math.toRadians(currentPose.heading());
        double forwardOffset = (dist - STOP_DIST_IN) * Math.cos(Math.toRadians(tx));
        double lateralOffset = (dist - STOP_DIST_IN) * -Math.sin(Math.toRadians(tx));

        double globalTargetX = currentPose.x() + (forwardOffset * Math.cos(headingRad) - lateralOffset * Math.sin(headingRad));
        double globalTargetY = currentPose.y() + (forwardOffset * Math.sin(headingRad) + lateralOffset * Math.cos(headingRad));

        // Compute aggressive target heading directly pointing at the ball
        double targetHeadingRad = Math.atan2(globalTargetY - currentPose.y(), globalTargetX - currentPose.x());
        double targetHeadingDeg = Math.toDegrees(targetHeadingRad);

        lockedTargetPose = poseFactory.of(globalTargetX, globalTargetY, targetHeadingDeg);

        // Path generation using Pedro Pathing v3 static syntax with Submersible Avoidance
        Path dynamicPath;
        if (intersectsSubmersible(currentPose.x(), currentPose.y(), globalTargetX, globalTargetY)) {
            Pose controlPose = calculateAvoidanceControlPose(currentPose, lockedTargetPose);
            dynamicPath = curve(currentPose, controlPose, lockedTargetPose)
                    .linear(currentPose, lockedTargetPose);
        } else {
            dynamicPath = line(currentPose, lockedTargetPose)
                    .linear(currentPose, lockedTargetPose);
        }

        follower.follow(dynamicPath);
        currentState = ChaseState.PATHING_TO_BALL;
    }

    private boolean intersectsSubmersible(double x1, double y1, double x2, double y2) {
        double midX = (x1 + x2) / 2.0;
        double midY = (y1 + y2) / 2.0;
        return (midX >= SUB_MIN_X && midX <= SUB_MAX_X && midY >= SUB_MIN_Y && midY <= SUB_MAX_Y);
    }

    private Pose calculateAvoidanceControlPose(Pose start, Pose end) {
        double midX = (start.x() + end.x()) / 2.0;
        double midY = (start.y() + end.y()) / 2.0;

        double controlX = midX;
        double controlY = midY < 72.0 ? SUB_MIN_Y - 15.0 : SUB_MAX_Y + 15.0;
        double controlHeading = (start.heading() + end.heading()) / 2.0;

        return poseFactory.of(controlX, controlY, controlHeading);
    }

    private void executePathingPhase() {
        Pose currentPose = follower.pose();
        double distanceToTarget = Math.hypot(
                lockedTargetPose.x() - currentPose.x(),
                lockedTargetPose.y() - currentPose.y()
        );

        if (distanceToTarget <= ARRIVAL_TOLERANCE_IN || !follower.isBusy()) {
            waitTimer.reset();
            currentState = ChaseState.WAITING_AT_TARGET;
        }
    }

    double distanceToTarget(double ty) {
        double angleDownRad = Math.toRadians(MOUNT_ANGLE_DEG - ty);
        return (CAMERA_HEIGHT_IN - BALL_RADIUS_IN) / Math.tan(angleDownRad);
    }
}