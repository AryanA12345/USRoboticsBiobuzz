package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.List;

@TeleOp(name = "TargetAndMoveOpMode", group = "Vision")
public class TargetAndMoveOpMode extends OpMode {

    private Limelight3A limelight;
    private DcMotor leftFront, rightFront, leftBack, rightBack;

    @Override
    public void init() {
        // Initialize the Limelight hardware map
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(0); // Ensure this is your Neural Detector pipeline index
        limelight.start();

        // Initialize your drivetrain motors (adjust names to match your robot configuration)
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");

        // Reverse motors if necessary for your drivetrain layout
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);

        telemetry.addData("Status", "Initialized and Ready");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Fetch the latest results container from Limelight
        LLResult result = limelight.getLatestResult();

        double drivePower = 0.0;
        double turnPower  = 0.0;
        boolean targetFound = false;

        // Check using official FTC SDK validation methods
        if (result != null && result.isValid()) {
            // Get all neural detector bounding boxes currently visible
            List<LLResultTypes.DetectorResult> detections = result.getDetectorResults();

            if (detections != null && !detections.isEmpty()) {
                for (LLResultTypes.DetectorResult detection : detections) {
                    int classId = detection.getClassId();         // Index from your labels.txt (0, 1, 2)
                    String className = detection.getClassName(); // Name of the class
                    double tx = detection.getTargetXDegrees();    // Horizontal offset (-29.8 to 29.8)
                    double confidence = detection.getConfidence();// Confidence score

                    telemetry.addData("Detected", "[ID: %d] %s (Conf: %.2f)", classId, className, confidence);

                    // --- SELECT WHICH CLASS TO TARGET ---
                    // Example: Let's look for Class ID 0 (your primary game piece)
                    if (classId == 0) {
                        targetFound = true;

                        // Proportional steering gain: turns sharper the further off-center the target is
                        double steerGain = 0.03;
                        turnPower = tx * steerGain;

                        // Drive forward slowly while steering toward it
                        drivePower = 0.3;

                        // Break out of the loop once we lock onto our priority target
                        break;
                    }
                }
            }
        }

        // If no target of our desired class is found, stop or search
        if (!targetFound) {
            drivePower = 0.0;
            turnPower = 0.0;
            telemetry.addData("Status", "Searching for Target Class 0...");
        } else {
            telemetry.addData("Status", "Tracking Target! Driving...");
        }

        // Apply calculated powers to a standard drivetrain layout
        double p1 = drivePower - turnPower; // Left side
        double p2 = drivePower + turnPower; // Right side

        leftFront.setPower(p1);
        leftBack.setPower(p1);
        rightFront.setPower(p2);
        rightBack.setPower(p2);

        telemetry.update();
    }
}