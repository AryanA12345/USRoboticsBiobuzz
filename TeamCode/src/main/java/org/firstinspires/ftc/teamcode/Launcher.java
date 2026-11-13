package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.EndCondition;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.ShooterConstants;

public class Launcher {
    private DcMotorEx flywheelN;
    private DcMotorEx flywheelP;
    private Servo releaseN;
    private Servo releaseP;

    // --- TELEOP STATE MACHINE DEFINITION ---
    public enum ShootState {
        IDLE,
        WAIT_FOR_FLY_STABLE,
        RUN_RELEASE,
        COOLDOWN
    }

    private ShootState currentShootState = ShootState.IDLE;
    private final ElapsedTime teleopShootTimer = new ElapsedTime();
    private final ElapsedTime teleopStabilityTimer = new ElapsedTime();

    // --- CONSTRUCTOR ---
    public Launcher(HardwareMap hardwareMap) {
        flywheelN = hardwareMap.get(DcMotorEx.class, "flywheelN");
        flywheelP = hardwareMap.get(DcMotorEx.class, "flywheelP");
        flywheelN.setDirection(DcMotorEx.Direction.REVERSE);

        // Apply optimized PIDF coefficients
        PIDFCoefficients fastCoefficients = new PIDFCoefficients(
                ShooterConstants.P,
                ShooterConstants.I,
                ShooterConstants.D,
                ShooterConstants.F
        );
        flywheelN.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
        flywheelP.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);

        releaseN = hardwareMap.get(Servo.class, "releaseN");
        releaseP = hardwareMap.get(Servo.class, "releaseP");

        releaseN.setPosition(1.0); // Closed position
        releaseP.setPosition(0.0); // Closed position

        spinUp();
    }

    // --- COMMON MOTOR SPIN-UP METHOD ---
    public void spinUp() {
        if (flywheelN.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        if (flywheelP.getMode() != DcMotor.RunMode.RUN_USING_ENCODER) {
            flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }

        flywheelN.setVelocity(ShooterConstants.launchN);
        flywheelP.setVelocity(ShooterConstants.launchP);
    }

    // ==========================================
    // --- 1. TELEOP UPDATE METHOD (LOOP-BASED) ---
    // ==========================================
    public void update(boolean triggerShoot) {
        spinUp(); // Keep flywheels active

        switch (currentShootState) {
            case IDLE:
                releaseN.setPosition(1.0);
                releaseP.setPosition(0.0);

                if (triggerShoot) {
                    teleopStabilityTimer.reset();
                    currentShootState = ShootState.WAIT_FOR_FLY_STABLE;
                }
                break;

            case WAIT_FOR_FLY_STABLE:
                double currentVelocityP = flywheelP.getVelocity();
                double errorP = Math.abs(ShooterConstants.launchP - currentVelocityP);

                if (errorP > 20) {
                    teleopStabilityTimer.reset(); // Reset if recovering from a previous shot drop
                }

                if (teleopStabilityTimer.milliseconds() >= 150) {
                    teleopShootTimer.reset();
                    currentShootState = ShootState.RUN_RELEASE;
                }
                break;

            case RUN_RELEASE:
                if (teleopShootTimer.milliseconds() < 2000) {
                    releaseN.setPosition(0.0);
                    releaseP.setPosition(1.0);
                } else {
                    releaseN.setPosition(1.0);
                    releaseP.setPosition(0.0);
                    teleopShootTimer.reset();
                    currentShootState = ShootState.COOLDOWN;
                }
                break;

            case COOLDOWN:
                if (teleopShootTimer.milliseconds() >= 500) {
                    currentShootState = ShootState.IDLE;
                }
                break;
        }
    }

    // ==========================================
    // --- 2. AUTONOMOUS IVY COMMAND BUILDER ---
    // ==========================================
    public Command getShootCommand() {
        return sequential(
                waitForFlywheelStable(),
                instant(() -> {
                    releaseN.setPosition(0.0);
                    releaseP.setPosition(1.0);
                }),
                waitMs(2000),
                instant(() -> {
                    releaseN.setPosition(1.0);
                    releaseP.setPosition(0.0);
                }),
                waitMs(3000)
        );
    }

    private Command waitForFlywheelStable() {
        ElapsedTime stabilityTimer = new ElapsedTime();
        return Command.build()
                .setStart(stabilityTimer::reset)
                .setExecute(() -> {
                    double errorP = Math.abs(ShooterConstants.launchP - flywheelP.getVelocity());
                    if (errorP > 20) {
                        stabilityTimer.reset();
                    }
                })
                .setDone(() -> stabilityTimer.milliseconds() >= 150)
                .setEnd((EndCondition cond) -> {});
    }

    // --- HELPER / GETTER METHODS ---
    public ShootState getCurrentState() {
        return currentShootState;
    }

    public double getFlywheelNVelocity() {
        return flywheelN.getVelocity();
    }

    public double getFlywheelPVelocity() {
        return flywheelP.getVelocity();
    }

    public void stop() {
        flywheelN.setVelocity(0);
        flywheelP.setVelocity(0);
    }
}