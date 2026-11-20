package org.firstinspires.ftc.teamcode.DisabledTeleOp;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.skeletonarmy.marrow.weaver.PathConfig;
import com.skeletonarmy.marrow.weaver.Weaver;

import org.firstinspires.ftc.teamcode.CV.CVPathGen;
import org.firstinspires.ftc.teamcode.CV.CVPathGenWeaver;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;


//@Disabled
@TeleOp
public class CVTest1 extends LinearOpMode {
    private CVPathGenWeaver pather;
    private Follower follower;
    public static boolean isRed = true;
    ArrayList<Pose> allBalls = new ArrayList<>();

    @Override
    public void runOpMode() throws InterruptedException {
        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        //intake.setDirection(DcMotorSimple.Direction.REVERSE);
        follower.setPose(new Pose(35, 8.5, Math.toRadians(90)));
        follower.update();


        pather = new CVPathGenWeaver(hardwareMap, follower, isRed, telemetry);

        waitForStart();
        while (opModeIsActive()) {
            follower.update();
            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );
            boolean driverMovingSticks = Math.abs(gamepad1.left_stick_x) > 0.1
                    || Math.abs(gamepad1.left_stick_y) > 0.1
                    || Math.abs(gamepad1.right_stick_x) > 0.1;
            if (!follower.isBusy() || driverMovingSticks) {
                ManualDrive.driveOrHold(follower, powers);
            }
            allBalls = pather.getBalls(false);
            if (gamepad1.aWasPressed()) {
                Path path = pather.pathGen();
                if (path != null) {
                    follower.follow(path);
                }
            }
            if (gamepad1.right_trigger_pressed) {
                intake.setPower(1);
            } else {
                intake.setPower(0);
            }
            if (follower.isBusy() && follower.atParametricEnd()) {
                follower.stop();
            }
            for (Pose ball : allBalls) {
                telemetry.addLine("(" + ball.x() + ", " + ball.y() + ")");
            }
            if (!follower.isBusy()) {
                telemetry.addData("Robot X", follower.pose().x());
                telemetry.addData("Robot Y", follower.pose().y());
                telemetry.addData("Remaining", follower.remainingDistance());
                telemetry.addData("Dist to endpoint", follower.distanceToEndpoint());
                telemetry.addData("Completion", follower.completion());
                telemetry.update();
            }
        }
    }
}