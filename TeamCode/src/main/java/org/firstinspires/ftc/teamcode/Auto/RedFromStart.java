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
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "SimRedShoot", group = "Autonomous")
public class RedFromStart extends LinearOpMode {

    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(8.6308, 32.2741, 0);
    private final Pose path1 = poseFactory.of(58.4486, 13.065, 90);
    private final Pose path1Control1 = poseFactory.of(7.87761506276151, 15.148535564853566, 0);
    private final Pose point2 = poseFactory.of(13.0084, 110.4582, 11.4132);
    private final Pose point2Control1 = poseFactory.of(105.4728, 129.6778, 0);

    /**
     * Complete autonomous routing logic.
     * Drives to position, performs the shooting sequence, then drives back.
     */
        public Command autoRoutine(DcMotor flywheel, DcMotor intake) {
        // Create the timed intake command chain
        Command timedIntake = sequential(
            instant(() -> intake.setPower(1.0)), // Turn on intake
            waitMs(800),                        // Wait 2 seconds
            //instant(() -> intake.setPower(-0.5)),  // Turn off intake
            //waitMs(100),
            instant(() -> intake.setPower(0))
        );

        // Create the pathing and shooting command chain
        Command driveAndShoot = sequential(
            follow(follower, path1()),       // Drive path1
            shootSequence(flywheel, intake)   // Pause and shoot, then drive path2
        );

        // Schedule both chains to run AT THE SAME TIME
        // Because they run in parallel, your intake will run for 2 seconds while driving!
        return instant(() -> schedule(timedIntake, driveAndShoot));
    }

    /**
     * Handles the non-blocking timed shooting sequence.
     */
        /**
     * Handles the non-blocking timed shooting sequence using native Ivy utilities.
     */
    public Command shootSequence(DcMotor flywheel, DcMotor intake) {
        return sequential(
            // 1. Turn on flywheel to spool up
            instant(() -> flywheel.setPower(1.0)),
            
            // 2. Pause 2.5 seconds (2500 milliseconds) for flywheel to reach full speed
            waitMs(2200),
            
            // 3. Turn on intake to feed the ring/element
            instant(() -> intake.setPower(1.0)),
            
            // 4. Wait 3.5 seconds (3500 milliseconds) for the intake to finish clearing out
            waitMs(3500),
            
            // 5. Turn off all mechanisms
            instant(() -> {
                flywheel.setPower(0.0);
                intake.setPower(0.0);
            }),
            
            // 6. Return safely back to base
            follow(follower, path2())
        );
    }


    @Override
    public void runOpMode() throws InterruptedException {
        // Hardware initialization
        follower = Constants.create(hardwareMap);
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

        // Reset the Command Scheduler and establish starting location
        Scheduler.reset();
        follower.setPose(start);
        follower.update();

        waitForStart();
        
        // Pass your motors directly into your master auto layout
        schedule(autoRoutine(flywheel, intake));
        
        // Simplified loop: Scheduler handles the timing and action delays natively
        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            // Telemetry
            telemetry.addData("x", follower.pose().x());
            telemetry.addData("y", follower.pose().y());
            telemetry.addData("heading", follower.pose().heading());

            if (follower.currentPath() != null) {
                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
                telemetry.addData("Path number", follower.pathIndex());
            }
            telemetry.update();
        }
    }

    public Path path1() {
        return Paths.curve(start, path1Control1, path1).linear(start, path1);
    }

    public Path path2() {
        return Paths.curve(path1, point2Control1, point2).reverseTangent();
    }
}
