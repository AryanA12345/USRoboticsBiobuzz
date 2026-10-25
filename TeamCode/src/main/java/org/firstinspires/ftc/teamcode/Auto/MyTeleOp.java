package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


@TeleOp(name = "MyFTCProject Driver Controlled", group = "FTC Simulator")
public class MyTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor[] motors = {
            hardwareMap.get(DcMotor.class, "frontLeft"),
            hardwareMap.get(DcMotor.class, "frontRight"),
            hardwareMap.get(DcMotor.class, "backLeft"),
            hardwareMap.get(DcMotor.class, "backRight")
        };
        motors[0].setDirection(DcMotor.Direction.REVERSE);
        motors[1].setDirection(DcMotor.Direction.REVERSE);
        motors[3].setDirection(DcMotor.Direction.REVERSE);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        DcMotor flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        DcMotor lift = hardwareMap.get(DcMotor.class, "lift");
        DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
        Servo hood = hardwareMap.get(Servo.class, "hood");
        Servo release = hardwareMap.get(Servo.class, "release");
        Servo gate = hardwareMap.get(Servo.class, "gate");
        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        hood.setPosition(0.5);
        release.setPosition(0.0);
        gate.setPosition(0.0);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            double drive = -gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;
            double turn = gamepad1.right_stick_x;
            double speed = 1.0;
            double denominator = Math.max(Math.abs(drive) + Math.abs(strafe) + Math.abs(turn), 1.0);
            double[] powers = {
                (drive + strafe + turn) / denominator,
                (drive - strafe - turn) / denominator,
                (drive - strafe + turn) / denominator,
                (drive + strafe - turn) / denominator
            };
            for (int index = 0; index < motors.length; index++) {
                motors[index].setPower(powers[index] * speed);
            }
            intake.setPower((gamepad1.b || gamepad1.x) ? -1.0 : (gamepad1.a ? 1.0 : gamepad1.left_trigger));
            flywheel.setPower(gamepad1.right_trigger);
            lift.setPower(gamepad1.dpad_up ? 0.65 : (gamepad1.dpad_down ? -0.45 : 0.0));
            boolean turretLeft = gamepad1.dpad_left;
            boolean turretRight = gamepad1.dpad_right;
            turret.setPower(turretLeft == turretRight ? 0.0 : (turretLeft ? 0.35 : -0.35));
            hood.setPosition(gamepad1.left_bumper ? 0.2 : (gamepad1.right_bumper ? 0.8 : 0.5));
            release.setPosition(gamepad1.y ? 1.0 : 0.0);
            gate.setPosition(gamepad1.b ? 1.0 : 0.0);
            double turretRevolutions = turret.getCurrentPosition() / (537.7 * 5.0);
            double turretPosition = ((turretRevolutions % 1.0) + 1.0) % 1.0;
            telemetry.addData("Turret", "%.1f° (%.3f rev)", turretPosition * 360.0, turretPosition);
            telemetry.addData("Input", "drive %.2f, strafe %.2f, turn %.2f", drive, strafe, turn);
            telemetry.addData("Speed", "%.0f%%", speed * 100.0);
            telemetry.update();
            idle();
        }
    }
}
