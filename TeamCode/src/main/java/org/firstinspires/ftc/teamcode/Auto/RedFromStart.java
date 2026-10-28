// package org.firstinspires.ftc.teamcode;

// import static com.pedropathing.api.Paths.*;
// import com.pedropathing.api.Paths;
// import com.pedropathing.api.PoseFactory;
// import com.pedropathing.follower.Follower;
// import com.pedropathing.math.Pose;
// import com.pedropathing.paths.Path;
// import com.pedropathing.ivy.Command;
// import com.pedropathing.ivy.Scheduler;
// import static com.pedropathing.ivy.Scheduler.schedule;
// import static com.pedropathing.ivy.commands.Commands.*;
// import static com.pedropathing.ivy.groups.Groups.sequential;
// import static com.pedropathing.ivy.pedro.PedroCommands.follow;
// import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
// import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
// import com.qualcomm.robotcore.hardware.DcMotor;
// import com.qualcomm.robotcore.hardware.Servo;

// import org.firstinspires.ftc.teamcode.pedro.Constants;

// @Autonomous(name = "SimRedShoot", group = "Autonomous")
// public class RedFromStart extends LinearOpMode {

//     private Follower follower;
//     private final PoseFactory poseFactory = PoseFactory.degrees();

//     private final Pose start = poseFactory.of(8.6308, 32.2741, 0);
//     private final Pose path1 = poseFactory.of(58.4486, 13.065, 90);
//     private final Pose path1Control1 = poseFactory.of(7.87761506276151, 15.148535564853566, 0);
//     private final Pose point2 = poseFactory.of(59.994, 132.1856, -93.9874);
//     private final Pose point2Control1 = poseFactory.of(90.5352, 64.4955, 0);
//     private final Pose point2Control2 = poseFactory.of(60.1579, 139.6498, 0);
//     private final Pose point3 = poseFactory.of(9.4482, 95.975, -1.6702);
//     private final Pose point3Control1 = poseFactory.of(58.4443, 94.356, 0);

//     /**
//      * Complete autonomous routing logic.
//      * Drives to position, performs the shooting sequence, then drives back.
//      */
//         public Command autoRoutine(DcMotor flywheel, DcMotor intake) {
//         // Create the timed intake command chain
//         Command timedIntake = sequential(
//             instant(() -> intake.setPower(1.0)), // Turn on intake
//             waitMs(800),                        // Wait 2 seconds
//             //instant(() -> intake.setPower(-0.5)),  // Turn off intake
//             //waitMs(100),
//             instant(() -> intake.setPower(0))
//         );

//         Command waitTimedIntake = sequential(
//             waitMs(500),
//             instant(() -> intake.setPower(1.0)), 
//             waitMs(800),
//             instant(() -> intake.setPower(0))
//         );
//         Command finishDrive = sequential(
//             follow(follower, path)
//         )

//         // Create the pathing and shooting command chain
//         Command driveAndShoot = sequential(
//             follow(follower, path1()),       // Drive path1
//             shootSequence(flywheel, intake)   // Pause and shoot, then drive path2
//             instant(() -> schedule(waitTimedIntake, finishDrive))
//         );
        

//         // Schedule both chains to run AT THE SAME TIME
//         // Because they run in parallel, your intake will run for 2 seconds while driving!
//         return instant(() -> schedule(timedIntake, driveAndShoot));
//     }

//     /**
//      * Handles the non-blocking timed shooting sequence.
//      */
//         /**
//      * Handles the non-blocking timed shooting sequence using native Ivy utilities.
//      */
//     public Command shootSequence(DcMotor flywheel, DcMotor intake, Path path) {
//         return sequential(
//             // 1. Turn on flywheel to spool up
//             instant(() -> flywheel.setPower(1.0)),
            
//             // 2. Pause 2.5 seconds (2500 milliseconds) for flywheel to reach full speed
//             waitMs(2200),
            
//             // 3. Turn on intake to feed the ring/element
//             instant(() -> intake.setPower(1.0)),
            
//             // 4. Wait 3.5 seconds (3500 milliseconds) for the intake to finish clearing out
//             waitMs(3500),
            
//             // 5. Turn off all mechanisms
//             instant(() -> {
//                 flywheel.setPower(0.0);
//                 intake.setPower(0.0);
//             }),
            
//             // 6. Return safely back to base
//             follow(follower, path)
//         );
//     }


//     @Override
//     public void runOpMode() throws InterruptedException {
//         // Hardware initialization
//         follower = Constants.create(hardwareMap);
//         DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
//         DcMotor flywheel = hardwareMap.get(DcMotor.class, "flywheel");
//         DcMotor lift = hardwareMap.get(DcMotor.class, "lift");
//         DcMotor turret = hardwareMap.get(DcMotor.class, "turret");
//         Servo hood = hardwareMap.get(Servo.class, "hood");
//         Servo release = hardwareMap.get(Servo.class, "release");
//         Servo gate = hardwareMap.get(Servo.class, "gate");
        
//         lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//         turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//         turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//         hood.setPosition(0.5);
//         release.setPosition(0.0);
//         gate.setPosition(0.0);

//         // Reset the Command Scheduler and establish starting location
//         Scheduler.reset();
//         follower.setPose(start);
//         follower.update();

//         waitForStart();
        
//         // Pass your motors directly into your master auto layout
//         schedule(autoRoutine(flywheel, intake));
        
//         // Simplified loop: Scheduler handles the timing and action delays natively
//         while (opModeIsActive()) {
//             follower.update();
//             Scheduler.execute();

//             // Telemetry
//             telemetry.addData("x", follower.pose().x());
//             telemetry.addData("y", follower.pose().y());
//             telemetry.addData("heading", follower.pose().heading());

//             if (follower.currentPath() != null) {
//                 telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
//                 telemetry.addData("Path number", follower.pathIndex());
//             }
//             telemetry.update();
//         }
//     }

//     public Path path1() {
//         return Paths.curve(start, path1Control1, path1).linear(start, path1);
//     }

//         public Path path2() {
//         return Paths.curve(path1, point2Control1, point2Control2, point2).tangent();
//     }

//     public Path path3() {
//         return Paths.curve(point2, point3Control1, point3).reverseTangent();
//     }
// }

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
import com.pedropathing.paths.interpolator.Interpolator;
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
import com.pedropathing.paths.interpolator.Interpolator;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "SimRedShoot", group = "Autonomous")
public class RedFromStart extends LinearOpMode {

    private Follower follower;
    private final PoseFactory poseFactory = PoseFactory.degrees();

    // Renamed 'path1' to 'path1Pose' to avoid method namespace collision
    private final Pose start = poseFactory.of(8.6308, 32.2741, 0);
    private final Pose path1Pose = poseFactory.of(58.4486, 13.065, 90);
    private final Pose path1Control1 = poseFactory.of(7.87761506276151, 15.148535564853566, 0);
    private final Pose point2 = poseFactory.of(59.994, 132.1856, -93.9874);
    private final Pose point2Control1 = poseFactory.of(64.7338, 64.4955, 0);
    private final Pose point2Segment1Target = poseFactory.of(58.2686, 84.79, 0);
    //private final Pose point2Control2 = poseFactory.of(59.6147, 138.2918, 0);
    private final Pose point3 = poseFactory.of(9.4482, 95.975, -1.6702);
    private final Pose point3Control1 = poseFactory.of(58.4443, 94.356, 0);

    /**
     * Master autonomous routing logic utilizing Ivy Scheduler parallelism.
     */
    public Command autoRoutine(DcMotor flywheel, DcMotor intake) {
        
        // --- 1. FIRST COMBINATION ---
        // Runs intake immediately at the start of path1
        Command timedIntake1 = sequential(
            instant(() -> intake.setPower(1.0)),
            waitMs(800),
            instant(() -> intake.setPower(0.0))
        );

        // --- 2. SECOND COMBINATION ---
        // Delays intake action by 500ms after path2 begins
        Command waitTimedIntake2 = sequential(
            waitMs(500),
            instant(() -> intake.setPower(1.0)), 
            waitMs(800),
            instant(() -> intake.setPower(0.0))
        );

        // --- 3. MASTER CHOREOGRAPHY SEQUENCE ---
        return sequential(
            // Fire off Path 1 and Intake 1 simultaneously using the scheduler
            instant(() -> {
                schedule(follow(follower, path1()));
                schedule(timedIntake1);
            }),
            // Halt execution of this sequential block until Path 1 is done
            follow(follower, path1()), 

            // Perform the first shooting station sequence at path1 endpoint
            shootSequence(flywheel, intake),

            // Fire off Path 2 and Delayed Intake 2 simultaneously
            instant(() -> {
                schedule(follow(follower, path2()));
                schedule(waitTimedIntake2);
            }),
            // Halt execution until Path 2 is done
            follow(follower, path2()),

            // Perform the second shooting station sequence at path2 endpoint
            shootSequence(flywheel, intake),

            // Final push to the finish line
            follow(follower, path3())
        );
    }

    /**
     * Handles the non-blocking timed shooting sequence using native Ivy utilities.
     */
    public Command shootSequence(DcMotor flywheel, DcMotor intake) {
        return sequential(
            // Spin up the shooter flywheel
            instant(() -> flywheel.setPower(1.0)),
            
            // Allow 2.2 seconds to reach maximum velocity
            waitMs(2200),
            
            // Engage intake to chamber/feed the ring elements
            instant(() -> intake.setPower(1.0)),
            
            // Keep feeding for 3.5 seconds to fully clear the conveyor
            waitMs(3500),
            
            // Shutdown mechanisms safely
            instant(() -> {
                flywheel.setPower(0.0);
                intake.setPower(0.0);
            })
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
        
        // Core execution loop: updates both the follower math and scheduled steps
        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            // Telemetry diagnostics
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
        return Paths.curve(start, path1Control1, path1Pose).linear(start, path1Pose);
    }

    public Path path2() {
        return Paths.curve(path1, point2Control1, point2).heading(Interpolator.piecewise().until(1, Interpolator.facingPoint(point2Segment1Target)));
    }

    public Path path3() {
        return Paths.curve(point2, point3Control1, point3).tangent();
    }
}
