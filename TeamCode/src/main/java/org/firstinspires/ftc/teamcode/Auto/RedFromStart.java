package org.firstinspires.ftc.teamcode;

import static com.pedropathing.api.Paths.*;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.*;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx; 
import com.qualcomm.robotcore.hardware.PIDFCoefficients; // Added for maximum acceleration tuning
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.pedropathing.paths.interpolator.Interpolator;
import com.pedropathing.ivy.behaviors.EndCondition; 

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "SimRedShoot", group = "Autonomous")
public class RedFromStart extends LinearOpMode {

    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(8.6308, 32.2741, 0);
    private final Pose path1 = poseFactory.of(58.4486, 13.065, 90);
    private final Pose path1Control1 = poseFactory.of(45.9211, 26.9174, 0);
    private final Pose point2 = poseFactory.of(59.994, 132.1856, -93.9874);
    private final Pose point2Control1 = poseFactory.of(64.7338, 64.4955, 0);
    private final Pose point2Segment1Target = poseFactory.of(58.2686, 84.79, 0);
    private final Pose point3 = poseFactory.of(9.4482, 95.975, -1.6702);
    private final Pose point3Control1 = poseFactory.of(58.4443, 94.356, 0);

    /**
     * Master autonomous routing logic utilizing Ivy Scheduler parallelism.
     */
    public Command autoRoutine(DcMotorEx flywheel, DcMotor intake) {
        
        // --- 1. FIRST COMBINATION ---
        Command timedIntake1 = sequential(
            instant(() -> intake.setPower(1.0)),
            waitMs(800),
            instant(() -> intake.setPower(0.0))
        );

        // --- 2. SECOND COMBINATION ---
        Command waitTimedIntake2 = sequential(
            instant(() -> intake.setPower(1.0)), 
            waitMs(800),
            instant(() -> intake.setPower(0.0))
        );

        // --- 3. MASTER CHOREOGRAPHY SEQUENCE ---
        return sequential(
            // Spin up the flywheel immediately at match start to run continuously
            instant(() -> {
                flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                flywheel.setVelocity(2000);
            }),

            // Fire off Path 1 and Intake 1 simultaneously using the scheduler
            instant(() -> {
                schedule(follow(follower, path1()));
                schedule(timedIntake1);
            }),
            follow(follower, path1()), 

            // Perform the first shooting station sequence at path1 endpoint
            shootSequence(flywheel, intake),

            // Fire off Path 2 and Delayed Intake 2 simultaneously
            instant(() -> {
                schedule(follow(follower, path2()));
                schedule(waitTimedIntake2);
            }),
            follow(follower, path2()),

            // Perform the second shooting station sequence at path2 endpoint
            shootSequence(flywheel, intake),

            // Final push to the finish line
            instant(() -> {
                schedule(follow(follower, path3()));
                schedule(waitTimedIntake2);
            }),
            follow(follower, path3()),
            
            // Clean up and shutdown the flywheel entirely at the end of autonomous
            instant(() -> flywheel.setVelocity(0))
        );
    }

    /**
     * Handles the non-blocking velocity-driven shooting sequence.
     */
    public Command shootSequence(DcMotorEx flywheel, DcMotor intake) {
        return sequential(
            // 1. Wait dynamically until the flywheel hits a steady 2000 ticks/sec
            waitForFlywheelStable(flywheel),
            
            // 2. Engage intake to feed elements immediately upon stability
            instant(() -> intake.setPower(1.0)),
            
            // 3. Keep feeding elements for exactly 2 seconds
            waitMs(2000),
            
            // 4. Shut down intake, leaving the flywheel powered at 2000 to save time
            instant(() -> intake.setPower(0.0)),
            
            // 5. Enforce a mandatory 3-second rest delay after shooting before triggering the next path
            waitMs(3000)
        );
    }

    /**
     * Custom Ivy command builder that holds execution until 
     * the motor velocity remains stable within ±20 ticks of 2000 for 150ms.
     */
    private Command waitForFlywheelStable(DcMotorEx flywheel) {
        ElapsedTime stabilityTimer = new ElapsedTime();
        
        return Command.build()
            .setStart(stabilityTimer::reset)
            .setExecute(() -> {
                double currentVelocity = flywheel.getVelocity();
                double error = Math.abs(2000 - currentVelocity);
                
                // If velocity strays outside our tolerance band, reset the window timer
                if (error > 20) {
                    stabilityTimer.reset();
                }
            })
            .setDone(() -> stabilityTimer.milliseconds() >= 150)
            .setEnd((EndCondition endCondition) -> {
                // Leaves flywheel engaged
            });
    }

    @Override
    public void runOpMode() throws InterruptedException {
        // Hardware initialization
        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        
        // Formally cast flywheel to DcMotorEx to leverage REV internal PID loops
        DcMotorEx flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        
        // --- VELOCITY CONTROLLER OPTIMIZATION TUNING ---
        // Change the motor internal behavior to maximize initial acceleration kick.
        // Formula: Power = (Target * F) + (Error * P)
        double P = 15.0;  // Aggressive push back against velocity error drop
        double I = 3.0;   // Eliminates steady state gaps
        double D = 0.0;   // No damping needed for open-air flywheels
        double F = 12.5;  // Baseline feedforward to force immediate maximum power output
        
        PIDFCoefficients fastCoefficients = new PIDFCoefficients(P, I, D, F);
        flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
        
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

        // Reset the Command Scheduler and establish starting location
        Scheduler.reset();
        follower.setPose(start);
        follower.update();

        waitForStart();
        
        // Pass your motors directly into your master auto layout
        schedule(autoRoutine(flywheel, intake));
        
        // Core execution loop: updates both the follower math and scheduled steps
        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            // Telemetry diagnostics
            telemetry.addData("x", follower.pose().x());
            telemetry.addData("y", follower.pose().y());
            telemetry.addData("heading", follower.pose().heading());
            telemetry.addData("Flywheel Velocity", flywheel.getVelocity());

            if (follower.currentPath() != null) {
                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
                telemetry.addData("Path number", follower.pathIndex());
            }
            telemetry.update();
        }
        OpModeStorage.autonomousEndPose = follower.pose();
    }

    public Path path1() {
        return Paths.curve(start, path1Control1, path1).linear(start, path1);
    }

    public Path path2() {
        return Paths.curve(path1, point2Control1, point2).heading(Interpolator.piecewise().until(1, Interpolator.facingPoint(point2Segment1Target)));
    }

    public Path path3() {
        return Paths.curve(point2, point3Control1, point3).tangent();
    }
}
