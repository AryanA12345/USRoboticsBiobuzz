package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

@TeleOp(name = "TargetAndMoveOpMode", group = "Vision")
public class TargetAndMoveOpMode extends LinearOpMode {

    private Follower follower;

    // Camera specifications
    static final double CAMERA_HEIGHT_IN = 8.25; // lens to floor
    static final double MOUNT_ANGLE_DEG = -2;    // tilted down from horizontal
    static final double BALL_RADIUS_IN = 2.5;    // object radius estimate

    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(4); // Locked to Pipeline 4 (Neural Detector)
        limelight.start();

        follower = Constants.create(hardwareMap);
        waitForStart();

        while (opModeIsActive()) {
            follower.update();
            Pose robotPose = follower.pose();

            LLResult result = limelight.getLatestResult();
            boolean targetFound = false;

            if (result != null && result.isValid()) {
                List<LLResultTypes.DetectorResult> detections = result.getDetectorResults();
                if (detections != null && !detections.isEmpty()) {
                    targetFound = true;
                    // Display info for the primary detected object on screen
                    LLResultTypes.DetectorResult primary = detections.get(0);
                    telemetry.addData("Neural Detector", "Objects Visible: %d", detections.size());
                    telemetry.addData("Primary Class", "[ID: %d] %s (Conf: %.2f)", primary.getClassId(), primary.getClassName(), primary.getConfidence());
                    telemetry.addData("tx (deg)", "%.1f", primary.getTargetXDegrees());
                    telemetry.addData("ty (deg)", "%.1f", primary.getTargetYDegrees());
                    telemetry.addData("distance (in)", "%.1f", distanceToTarget(primary.getTargetYDegrees()));
                }
            }

            if (!targetFound) {
                telemetry.addData("Status", "No targets in view on Pipeline 4");
            }
            telemetry.update();

            // Dpad controls choose which specific class ID to chase (0, 1, or 2) on Pipeline 4
            if (gamepad1.dpad_up) {
                chaseTargetClass(limelight, 0);
            } else if (gamepad1.dpad_left) {
                chaseTargetClass(limelight, 1);
            } else if (gamepad1.dpad_right){
                chaseTargetClass(limelight, 2);
            } else {
                DrivePowers powers = ManualDrive.fieldCentric(
                        -gamepad1.left_stick_y,
                        -gamepad1.left_stick_x,
                        -gamepad1.right_stick_x,
                        follower.pose().heading()
                );
                ManualDrive.driveOrHold(follower, powers);
            }
        }
        limelight.stop();
    }

    double distanceToTarget(double ty) {
        double angleDownRad = Math.toRadians(MOUNT_ANGLE_DEG - ty);
        return (CAMERA_HEIGHT_IN - BALL_RADIUS_IN) / Math.tan(angleDownRad);
    }

    // Tuning constants
    static final double TURN_KP = 0.02;
    static final double DRIVE_KP = 0.3;
    static final double TX_TOLERANCE = 2.0;
    static final double STOP_DIST_IN = 0.0;
    static final double MAX_POWER = 0.9;

    /**
     * Scans neural detections on Pipeline 4 and chases the requested class ID (0, 1, or 2)
     */
    boolean chaseTargetClass(Limelight3A limelight, int targetClassId) {
        limelight.pipelineSwitch(4); // Ensure pipeline 4 remains active
        LLResult result = limelight.getLatestResult();

        if (result == null || !result.isValid()) {
            drive(follower, 0, 0, 0.25); // Search spin if no data
            return false;
        }

        List<LLResultTypes.DetectorResult> detections = result.getDetectorResults();
        if (detections == null || detections.isEmpty()) {
            drive(follower, 0, 0, 0.25);
            return false;
        }

        // Loop through all bounding boxes to find the one matching our requested class ID
        LLResultTypes.DetectorResult target = null;
        for (LLResultTypes.DetectorResult detection : detections) {
            if (detection.getClassId() == targetClassId) {
                target = detection;
                break;
            }
        }

        if (target == null) {
            // Desired class not currently in view, perform a slow search spin
            drive(follower, 0, 0, 0.25);
            return false;
        }

        double tx = target.getTargetXDegrees();
        double dist = distanceToTarget(target.getTargetYDegrees());
        double turn = clip(tx * TURN_KP);
        double forward = Math.abs(tx) < 10 ? clip((dist - STOP_DIST_IN) * DRIVE_KP) : 0;

        drive(follower, forward, 0, turn);
        return Math.abs(tx) < TX_TOLERANCE && dist <= STOP_DIST_IN;
    }

    double clip(double p) {
        return Math.max(-MAX_POWER, Math.min(MAX_POWER, p));
    }

    void drive(Follower follower, double forward, double lateral, double turn) {
        follower.manual(forward, lateral, -turn);
        follower.update();
    }
}