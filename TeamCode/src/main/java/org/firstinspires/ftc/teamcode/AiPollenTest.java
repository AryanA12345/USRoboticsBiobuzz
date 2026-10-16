/**
 * TeleOp with dynamic Pedro Pathing coordinate tracking and safe blind-spot handling
 * Updated for Pedro Pathing v3 standards
 */
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.paths.Path;
import static com.pedropathing.api.Paths.*;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@TeleOp(name = "Locked Pedro Vision Pathing", group = "Vision")
public class AiPollenTest extends LinearOpMode {

    private Follower follower;
    private PoseFactory poseFactory;

    // Camera and targeting constants
    static final double CAMERA_HEIGHT_IN = 8.25;
    static final double MOUNT_ANGLE_DEG = -2;
    static final double BALL_RADIUS_IN = 2.5;
    static final double STOP_DIST_IN = 6.0;
    static final double ARRIVAL_TOLERANCE_IN = 1.5;

    // Target tracking state variables
    private boolean isTrackingLocked = false;
    private Pose lockedTargetPose = null;

    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(0);
        limelight.start();

        follower = Constants.create(hardwareMap);
        poseFactory = PoseFactory.degrees(); // Initialize Pedro v3 PoseFactory for degrees

        waitForStart();

        while (opModeIsActive()) {
            follower.update(); // Keep Pedro Pathing localizer updated every frame

            if (gamepad1.dpad_up) {
                executeBlindZoneChase(limelight, 0);
            } else if (gamepad1.dpad_left) {
                    executeBlindZoneChase(limelight, 1);
            } else if (gamepad1.dpad_right) {
                executeBlindZoneChase(limelight, 2);
            } else {
                // Reset tracking lock on manual driver input
                isTrackingLocked = false;
                lockedTargetPose = null;

                Pose currentPose = follower.pose();
                DrivePowers powers = ManualDrive.fieldCentric(
                        -gamepad1.left_stick_y,
                        -gamepad1.left_stick_x,
                        -gamepad1.right_stick_x,
                        currentPose.heading()
                );
                ManualDrive.driveOrHold(follower, powers);
            }

            telemetry.addData("Tracking Locked?", isTrackingLocked);
            if (lockedTargetPose != null) {
                telemetry.addData("Target X", "%.2f", lockedTargetPose.x());
                telemetry.addData("Target Y", "%.2f", lockedTargetPose.y());
            }
            telemetry.update();
        }
        limelight.stop();
    }

    double distanceToBall(double ty) {
        double angleDownRad = Math.toRadians(MOUNT_ANGLE_DEG - ty);
        return (CAMERA_HEIGHT_IN - BALL_RADIUS_IN) / Math.tan(angleDownRad);
    }

    /**
     * Finds the ball using Limelight once, locks its global coordinate,
     * and lets Pedro Pathing navigate into the intake blind zone.
     */
    void executeBlindZoneChase(Limelight3A limelight, int pipe) {
        limelight.pipelineSwitch(pipe);

        // Phase 1: Search and Latch
        if (!isTrackingLocked) {
            LLResult result = limelight.getLatestResult();

            if (result == null || !result.isValid()) {
                ManualDrive.driveOrHold(follower, new DrivePowers(0, 0, 0));
                return;
            }

            double tx = result.getTx();
            double dist = distanceToBall(result.getTy());

            Pose currentPose = follower.pose();

            // Transform relative camera angles into global field coordinates (X, Y)
            double forwardOffset = (dist - STOP_DIST_IN) * Math.cos(Math.toRadians(tx));
            double lateralOffset = (dist - STOP_DIST_IN) * -Math.sin(Math.toRadians(tx));

            double globalTargetX = currentPose.x() + (forwardOffset * Math.cos(currentPose.heading()) - lateralOffset * Math.sin(currentPose.heading()));
            double globalTargetY = currentPose.y() + (forwardOffset * Math.sin(currentPose.heading()) + lateralOffset * Math.cos(currentPose.heading()));

            // Lock target pose using PoseFactory
            lockedTargetPose = poseFactory.of(globalTargetX, globalTargetY, currentPose.heading());
            isTrackingLocked = true;

            // Generate a straight line path with linear heading interpolation using Pedro v3 static imports
            Path dynamicPath = line(currentPose, lockedTargetPose).linear(currentPose, lockedTargetPose);

            follower.follow(dynamicPath);
        }
        // Phase 2: Execution (Ignores Vision / Drives through Blind Spot)
        else {
            Pose currentPose = follower.pose();
            double distanceToTarget = Math.hypot(
                    lockedTargetPose.x() - currentPose.x(),
                    lockedTargetPose.y() - currentPose.y()
            );

            // Unlock once arrived or path completion is reached
            if (distanceToTarget <= ARRIVAL_TOLERANCE_IN || !follower.isBusy()) {
                isTrackingLocked = false;
                lockedTargetPose = null;
            }
        }
    }
}