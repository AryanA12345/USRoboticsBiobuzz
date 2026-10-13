package org.firstinspires.ftc.teamcode;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

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

@TeleOp(name = "Pollen Vision Test", group = "Vision")
public class PollenVisionTest extends LinearOpMode {
    // Measure these on YOUR robot (Step 1)
    private Follower follower;
    static final double CAMERA_HEIGHT_IN = 8.25; // lens to floor
    static final double MOUNT_ANGLE_DEG = -2; // tilted down from horizontal
    static final double BALL_RADIUS_IN = 2.5; // measure a pollen ball
    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100); // ask for fresh data 100x per second
        limelight.pipelineSwitch(0); // pollen-yellow pipeline
        limelight.start();
        follower = Constants.create(hardwareMap);
        waitForStart();
        while (opModeIsActive()) {

            follower.update();
            Pose robotPose = follower.pose(); // returns a Pose object

            LLResult result = limelight.getLatestResult();
            if (result != null && result.isValid()) {
                double tx = result.getTx();
                double ty = result.getTy();
                double ta = result.getTa();
                telemetry.addData("Pollen", "FOUND");
                telemetry.addData("tx (deg)", "%.1f", tx);
                telemetry.addData("ty (deg)", "%.1f", ty);
                telemetry.addData("ta (%)", "%.2f", ta);
                telemetry.addData("distance (in)", "%.1f", distanceToBall(ty));

            } else {
                telemetry.addData("Pollen", "none in view");
            }
            telemetry.update();
            chasePollen(limelight);
        }
        limelight.stop();
    }
    // Camera looks down at MOUNT_ANGLE_DEG; the ball sits ty degrees above or below the crosshair
    double distanceToBall(double ty) {
        double angleDownRad = Math.toRadians(MOUNT_ANGLE_DEG - ty);
        return (CAMERA_HEIGHT_IN - BALL_RADIUS_IN) / Math.tan(angleDownRad);
    }
    // Tuning constants: start small, raise until it's quick without wobbling
    static final double TURN_KP = 0.02; // power per degree of tx
    static final double DRIVE_KP = 0.03; // power per inch of distance
    static final double TX_TOLERANCE = 2.0; // degrees: "lined up"
    static final double STOP_DIST_IN = 8.0; // stop here and let the intake grab it
    static final double MAX_POWER = 0.5;





    /** Returns true when the robot is lined up and close enough to intake. */
    boolean chasePollen(Limelight3A limelight) {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            drive(follower, 0, 0, 0.25); // no ball: slow search spin
            return false;
        }
        double tx = result.getTx();
        double dist = distanceToBall(result.getTy());
        double turn = clip(tx * TURN_KP);
        // Only drive forward once we're mostly facing the ball
        double forward = Math.abs(tx) < 10 ? clip((dist - STOP_DIST_IN) * DRIVE_KP) : 0;
        drive(follower, forward, 0, turn); // forward, strafe, turn
        return Math.abs(tx) < TX_TOLERANCE && dist <= STOP_DIST_IN;
    }
    double clip(double p) { return Math.max(-MAX_POWER, Math.min(MAX_POWER, p)); }
    // Connect to YOUR drivetrain: your mecanum drive method, or Pedro Pathing's TeleOp drive
    void drive(Follower follower, double forward, double lateral, double turn) {
        follower.manual(forward, lateral, -turn); // Drives the robot robot-centric
        follower.update();                       // Keeps Pedro's localizer tracking active
    }
}