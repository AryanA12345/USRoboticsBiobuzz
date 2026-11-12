package org.firstinspires.ftc.teamcode.DisabledTeleOp;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;

@Disabled
@TeleOp(name = "LargeShooterTest", group = "Tuning")
public class LargeShooterTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        DcMotorEx shooter = hardwareMap.get(DcMotorEx.class, "largeShooter");
        shooter.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addLine("Shooter Tuning Loaded");
        telemetry.addLine("Right Trigger = increase power");
        telemetry.addLine("Left Trigger  = decrease power");
        telemetry.addLine("Press A to reset encoder");
        telemetry.update();

        waitForStart();

        double power = 0.0;

        while (opModeIsActive()) {

            // adjust power up/down smoothly
            power += gamepad1.right_trigger * 0.0001;
            power -= gamepad1.left_trigger * 0.0001;

            // clip to [-1, 1]
            power = Math.max(-1.0, Math.min(1.0, power));

            shooter.setPower(power);

            if (gamepad1.a) {
                shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                power = 0;
            }

            // raw encoder ticks/sec
            double velocityTicks = shooter.getVelocity();
            double rpm = velocityTicks * 60.0 / 28.0;  // REV Core Hex = 28 ticks per rev

            telemetry.addData("Motor Power", "%.3f", power);
            telemetry.addData("Velocity (ticks/sec)", "%.1f", velocityTicks);
            telemetry.addData("Velocity (RPM)", "%.1f", rpm);
            telemetry.update();
        }
    }
}

