package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.JavaUtil;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

@Autonomous(name = "AryanRFNWithTurnAdjustAndNewShoot (Blocks to Java)")
public class AryanRFNWithTurnAdjustAndNewShoot extends LinearOpMode {

  private DcMotor driveLeft;
  private DcMotor driveRight;
  private DcMotor feederMotor;
  private DcMotor shootwheel;

  double newLeftTarget;
  double newRightTarget;
  double COUNTS_PER_INCH;
  ElapsedTime runtime;
  double trackWidth;
  AprilTagProcessor myAprilTagProcessor;
  boolean adjusted;
  List<AprilTagDetection> myAprilTagDetections;
  AprilTagDetection myAprilTagDetection;
  double Bearing;
  YawPitchRollAngles myYawPitchRollAngles;

  /**
   * This OpMode illustrates the concept of driving a path based on encoder counts.
   * This OpMode requires that you have encoders on the wheels, otherwise you would
   * use RobotAutoDriveByTime. This OpMode also requires that the drive Motors
   * have been configured such that a positive power command moves them forward,
   * and causes the encoders to count up. The desired path in this example is: -
   * Drive forward for 48 inches - Spin right for 12 Inches - Drive backward for
   * 24 inches This OpMode has a function called encoderDrive that performs the
   * actual movement. This function assumes that each movement is relative to the
   * last stopping place. There are other ways to perform encoder based moves, but
   * this method is probably the simplest. This OpMode uses the RUN_TO_POSITION mode.
   */
  @Override
  public void runOpMode() {
    driveLeft = hardwareMap.get(DcMotor.class, "driveLeft");
    driveRight = hardwareMap.get(DcMotor.class, "driveRight");
    feederMotor = hardwareMap.get(DcMotor.class, "feederMotor");
    shootwheel = hardwareMap.get(DcMotor.class, "shootwheel");

    // Disabled due to camera lag
    initializeVisionPortal();
    runtime = new ElapsedTime();
    Measurements();
    MotorSettings();
    // Send telemetry message to indicate successful Encoder reset.
    telemetry.addData("Starting at", JavaUtil.formatNumber(driveLeft.getCurrentPosition(), 7, 0) + " : " + JavaUtil.formatNumber(driveRight.getCurrentPosition(), 7, 0));
    telemetry.update();
    // Wait for the game to start (driver presses START).
    waitForStart();
    // Step through each leg of the path.
    // Note: Reverse movement is obtained by setting a negative distance (not speed).
    StraightDrive(70, 1);
    sleep(1000);
    Turn(45, 0.5);
    AprilTagRightTurn(0.5);
    shoot3(5);
    sleep(1000);
    Turn(83, 0.5);
    sleep(1000);
    StraightDrive(20, 0.5);
  }

  /**
   * Describe this function...
   */
  private void initializeVisionPortal() {
    VisionPortal.Builder myVisionPortalBuilder;
    AprilTagProcessor.Builder myAprilTagProcessorBuilder;
    Position cameraPosition;
    YawPitchRollAngles cameraOrientation;
    VisionPortal myVisionPortal;

    cameraPosition = new Position(DistanceUnit.CM, 0, 0, 0, 0);
    cameraOrientation = new YawPitchRollAngles(AngleUnit.DEGREES, 0, 70, 180, 0);
    // Create a VisionPortal.Builder object so you can specify attributes about the cameras.
    myVisionPortalBuilder = new VisionPortal.Builder();
    // Set the camera to the specified webcam name.
    myVisionPortalBuilder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
    // Create a new AprilTagProcessor.Builder object and assign it to a variable.
    myAprilTagProcessorBuilder = new AprilTagProcessor.Builder();
    // Set whether or not to draw the cube projection on detections.
    myAprilTagProcessorBuilder.setDrawCubeProjection(true);
    // Set whether or not to draw the tag outline on detections.
    myAprilTagProcessorBuilder.setDrawTagOutline(true);
    // Set whether or not to draw the tag ID on detections.
    myAprilTagProcessorBuilder.setDrawTagID(true);
    // Set whether or not to draw the axes on detections.
    myAprilTagProcessorBuilder.setDrawAxes(true);
    myAprilTagProcessorBuilder.setCameraPose(cameraPosition, cameraOrientation);
    // Build the AprilTag processor and assign it to a variable.
    myAprilTagProcessor = myAprilTagProcessorBuilder.build();
    // Add the AprilTag processor.
    myVisionPortalBuilder.addProcessor(myAprilTagProcessor);
    // Build the VisionPortal object and assign it to a variable.
    myVisionPortal = myVisionPortalBuilder.build();
    // Start the LiveView (RC preview) again.
    myVisionPortal.resumeLiveView();
  }

  /**
   * Describe this function...
   */
  private void StraightDrive(int Distance, double Power) {
    if (opModeIsActive()) {
      // Determine new target position, and pass to motor controller.
      newLeftTarget = driveLeft.getCurrentPosition() + Math.floor(Distance * COUNTS_PER_INCH);
      newRightTarget = driveRight.getCurrentPosition() + Math.floor(Distance * COUNTS_PER_INCH);
      driveLeft.setTargetPosition((int) newLeftTarget);
      driveRight.setTargetPosition((int) newRightTarget);
      // Turn On RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      driveRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      // Reset the timeout time and start motion.
      driveLeft.setPower(Math.abs(Power));
      driveRight.setPower(Math.abs(Power));
      // Keep looping while we are still active, and there is time left, and both motors are running.
      // Note: We use (isBusy() and isBusy()) in the loop test, which means that when EITHER motor hits
      // its target position, the motion will stop.  This is "safer" in the event that the robot will
      // always end the motion as soon as possible.
      // However, if you require that BOTH motors have finished their moves before the robot continues
      // onto the next step, use (isBusy() or isBusy()) in the loop test.
      while (opModeIsActive() && driveLeft.isBusy() && driveRight.isBusy()) {
        // Display it for the driver.
        telemetry.addData("Running to", JavaUtil.formatNumber(newLeftTarget, 7, 0) + " :" + JavaUtil.formatNumber(newRightTarget, 7, 0));
        telemetry.addData("Currently at", JavaUtil.formatNumber(driveLeft.getCurrentPosition(), 7, 0) + " :" + JavaUtil.formatNumber(driveRight.getCurrentPosition(), 7, 0));
        telemetry.update();
      }
      // Stop all motion.
      driveLeft.setPower(0);
      driveRight.setPower(0);
      // Turn off RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      driveRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      // Optional pause after each move.
      sleep(250);
    }
  }

  /**
   * Describe this function...
   */
  private void IMUStraight(double Distance, double Power) {
    double robotOrientation;
    double newRobotOrientation;
    double orientationError;
    double orientationCorrection;

    if (opModeIsActive()) {
      // Determine new target position, and pass to motor controller.
      newLeftTarget = driveLeft.getCurrentPosition() + Math.floor(Distance * COUNTS_PER_INCH);
      newRightTarget = driveRight.getCurrentPosition() + Math.floor(Distance * COUNTS_PER_INCH);
      robotOrientation = myYawPitchRollAngles.getYaw(AngleUnit.DEGREES);
      driveLeft.setTargetPosition((int) newLeftTarget);
      driveRight.setTargetPosition((int) newRightTarget);
      // Turn On RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      driveRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      // Reset the timeout time and start motion.
      driveLeft.setPower(Math.abs(Power));
      driveRight.setPower(Math.abs(Power));
      // Keep looping while we are still active, and there is time left, and both motors are running.
      // Note: We use (isBusy() and isBusy()) in the loop test, which means that when EITHER motor hits
      // its target position, the motion will stop.  This is "safer" in the event that the robot will
      // always end the motion as soon as possible.
      // However, if you require that BOTH motors have finished their moves before the robot continues
      // onto the next step, use (isBusy() or isBusy()) in the loop test.
      while (opModeIsActive() && driveLeft.isBusy() && driveRight.isBusy()) {
        // Display it for the driver.
        newRobotOrientation = myYawPitchRollAngles.getYaw(AngleUnit.DEGREES);
        orientationError = newRobotOrientation - robotOrientation;
        // KP vl
        orientationCorrection = 0.02 * orientationError;
        if (newRobotOrientation < robotOrientation) {
          driveLeft.setPower(Math.abs(Power) + orientationCorrection);
          driveRight.setPower(Math.abs(Power) - orientationCorrection);
        } else if (newRobotOrientation > robotOrientation) {
          driveLeft.setPower(Math.abs(Power) - orientationCorrection);
          driveRight.setPower(Math.abs(Power) + orientationCorrection);
        }
        telemetry.addData("Running to", JavaUtil.formatNumber(newLeftTarget, 7, 0) + " :" + JavaUtil.formatNumber(newRightTarget, 7, 0));
        telemetry.addData("Currently at", JavaUtil.formatNumber(driveLeft.getCurrentPosition(), 7, 0) + " :" + JavaUtil.formatNumber(driveRight.getCurrentPosition(), 7, 0));
        telemetry.update();
      }
      // Stop all motion.
      driveLeft.setPower(0);
      driveRight.setPower(0);
      // Turn off RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      driveRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      // Optional pause after each move.
      sleep(250);
    }
  }

  /**
   * Describe this function...
   */
  private void MotorSettings() {
    driveLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    driveRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    driveLeft.setDirection(DcMotor.Direction.FORWARD);
    driveRight.setDirection(DcMotor.Direction.REVERSE);
    feederMotor.setDirection(DcMotor.Direction.REVERSE);
    driveLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    driveRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    driveLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    driveRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
  }

  /**
   * Describe this function...
   */
  private void Measurements() {
    int COUNTS_PER_MOTOR_REV;
    int DRIVE_GEAR_REDUCTION;
    double WHEEL_DIAMETER_INCHES;
    double DRIVE_SPEED;
    double TURN_SPEED;

    COUNTS_PER_MOTOR_REV = 28;
    DRIVE_GEAR_REDUCTION = 20;
    WHEEL_DIAMETER_INCHES = 3.5433;
    COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_INCHES * Math.PI);
    trackWidth = 14.875;
    DRIVE_SPEED = 0.6;
    TURN_SPEED = 0.5;
  }

  /**
   * Describe this function...
   */
  private void LaunchBall(double LaunchPower) {
    shootwheel.setPower(-LaunchPower);
    sleep(3000);
    feederMotor.setPower(-1);
    sleep(800);
    feederMotor.setPower(0);
    shootwheel.setPower(0);
    sleep(3000);
  }

  /**
   * Describe this function...
   */
  private void AprilTagLeftTurn(double TurnSpeed) {
    adjusted = false;
    // Get a list containing the latest detections, which may be stale.
    myAprilTagDetections = myAprilTagProcessor.getDetections();
    for (AprilTagDetection myAprilTagDetection_item : myAprilTagDetections) {
      myAprilTagDetection = myAprilTagDetection_item;
      Bearing = myAprilTagDetection.ftcPose.bearing;
      telemetry.addData("Starting bearing", Bearing);
      Turn((int) (-18 - Bearing), TurnSpeed);
      adjusted = true;
      telemetry.addData("Starting bearing", myAprilTagDetection.ftcPose.bearing);
    }
    if (!adjusted) {
      Turn(5, TurnSpeed);
    }
  }

  /**
   * Describe this function...
   */
  private void RepeatedLaunchBall(double LaunchPower, double number_of_balls) {
    for (int count = 0; count < number_of_balls; count++) {
      shootwheel.setPower(-LaunchPower);
      sleep(3000);
      feederMotor.setPower(-1);
      sleep(800);
      feederMotor.setPower(0);
      shootwheel.setPower(0);
      sleep(1000);
    }
  }

  /**
   * Describe this function...
   */
  private void Turn(int Angle, double AngleSpeed) {
    // 11.69 inches is 90 degrees
    if (opModeIsActive()) {
      // Determine new target position, and pass to motor controller.
      newLeftTarget = driveLeft.getCurrentPosition() + Math.floor(3.14159 * trackWidth * (Angle / 360) * COUNTS_PER_INCH);
      newRightTarget = driveRight.getCurrentPosition() + Math.floor(-3.14159 * trackWidth * (Angle / 360) * COUNTS_PER_INCH);
      driveLeft.setTargetPosition((int) newLeftTarget);
      driveRight.setTargetPosition((int) newRightTarget);
      // Turn On RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      driveRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
      // Reset the timeout time and start motion.
      runtime.reset();
      driveLeft.setPower(Math.abs(AngleSpeed));
      driveRight.setPower(Math.abs(AngleSpeed));
      // Keep looping while we are still active, and there is time left, and both motors are running.
      // Note: We use (isBusy() and isBusy()) in the loop test, which means that when EITHER motor hits
      // its target position, the motion will stop.  This is "safer" in the event that the robot will
      // always end the motion as soon as possible.
      // However, if you require that BOTH motors have finished their moves before the robot continues
      // onto the next step, use (isBusy() or isBusy()) in the loop test.
      while (opModeIsActive() && driveLeft.isBusy() && driveRight.isBusy()) {
        // Display it for the driver.
        telemetry.addData("Running to", JavaUtil.formatNumber(newLeftTarget, 7, 0) + " :" + JavaUtil.formatNumber(newRightTarget, 7, 0));
        telemetry.addData("Currently at", JavaUtil.formatNumber(driveLeft.getCurrentPosition(), 7, 0) + " :" + JavaUtil.formatNumber(driveRight.getCurrentPosition(), 7, 0));
        telemetry.update();
      }
      // Stop all motion.
      driveLeft.setPower(0);
      driveRight.setPower(0);
      // Turn off RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      driveRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      // Optional pause after each move.
      sleep(250);
    }
  }

  /**
   * Describe this function...
   */
  private void AprilTagRightTurn(double TurnSpeed) {
    adjusted = false;
    // Get a list containing the latest detections, which may be stale.
    myAprilTagDetections = myAprilTagProcessor.getDetections();
    for (AprilTagDetection myAprilTagDetection_item2 : myAprilTagDetections) {
      myAprilTagDetection = myAprilTagDetection_item2;
      Bearing = myAprilTagDetection.ftcPose.bearing;
      telemetry.addData("Starting bearing", Bearing);
      Turn((int) (-18 - Bearing), TurnSpeed);
      adjusted = true;
      telemetry.addData("Starting bearing", myAprilTagDetection.ftcPose.bearing);
    }
    if (!adjusted) {
      Turn(5, TurnSpeed);
    }
  }

  /**
   * Describe this function...
   */
  private void IMUTurn(double Angle, double AngleSpeed) {
    // 11.69 inches is 90 degrees
    if (opModeIsActive()) {
      // Determine new target position, and pass to motor controller.
      newLeftTarget = myYawPitchRollAngles.getYaw(AngleUnit.DEGREES) + Angle;
      newRightTarget = myYawPitchRollAngles.getYaw(AngleUnit.DEGREES) + Angle;
      telemetry.addData("Running to", JavaUtil.formatNumber(newLeftTarget, 7, 0) + " :" + JavaUtil.formatNumber(newRightTarget, 7, 0));
      while (opModeIsActive() && driveLeft.isBusy() && driveRight.isBusy()) {
        // Display it for the driver.
        telemetry.addData("Currently at", JavaUtil.formatNumber(driveLeft.getCurrentPosition(), 7, 0) + " :" + JavaUtil.formatNumber(driveRight.getCurrentPosition(), 7, 0));
        telemetry.update();
      }
      // Stop all motion.
      while (!(myYawPitchRollAngles.getYaw(AngleUnit.DEGREES) == newLeftTarget && myYawPitchRollAngles.getYaw(AngleUnit.DEGREES) == newRightTarget)) {
        if (Angle < 0) {
          driveLeft.setPower(-AngleSpeed);
          driveRight.setPower(AngleSpeed);
        } else {
          driveLeft.setPower(AngleSpeed);
          driveRight.setPower(-AngleSpeed);
        }
      }
      driveLeft.setPower(0);
      driveRight.setPower(0);
      // Turn off RUN_TO_POSITION.
      driveLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      driveRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
      // Optional pause after each move.
      sleep(250);
    }
  }

  /**
   * Describe this function...
   */
  private void shoot3(int delay) {
    double Dtime;

    ((DcMotorEx) shootwheel).setVelocity(-1550);
    for (int count2 = 0; count2 < 3; count2++) {
      Dtime = getRuntime() + 5;
      while (!(Dtime <= getRuntime())) {
      }
      feederMotor.setPower(-1);
      Dtime = getRuntime() + 0.8;
      while (!(Dtime <= getRuntime())) {
      }
      feederMotor.setPower(0);
    }
    shootwheel.setPower(0);
  }
}
