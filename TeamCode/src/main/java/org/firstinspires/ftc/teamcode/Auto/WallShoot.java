package org.firstinspires.ftc.teamcode.Auto;

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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.skeletonarmy.marrow.prompts.OptionPrompt;
import com.skeletonarmy.marrow.prompts.Prompter;
import com.skeletonarmy.marrow.prompts.ValuePrompt;

import org.firstinspires.ftc.teamcode.helper.Launcher;
import org.firstinspires.ftc.teamcode.helper.OpModeStorage;
import org.firstinspires.ftc.teamcode.helper.RobotRenderer;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "WallShoot", group = "Autonomous")
public class WallShoot extends LinearOpMode {

    private Follower follower;
    private Launcher launcher;

    private Prompter prompter;

    private double delay;

    private final PoseFactory poseFactory = PoseFactory.degrees();
    private final Pose originalPos = poseFactory.of(9.3,131.5, 0);

    private final Pose start = poseFactory.of(9.4284, 32.8862, 90);
    private final Pose path1 = poseFactory.of(56.1786, 12.9178, -84.3293);
    private final Pose path1Control1 = poseFactory.of(56.2006, 33.2765, 0);
    private final Pose path1Segment1Start = poseFactory.of(56.1786, 12.9178, 0);
    private final Pose path1Segment1End = poseFactory.of(56.1786, 12.9178, -90);
    private final Pose path1Segment2Target = poseFactory.of(52, 55, 0);
    private final Pose point2 = poseFactory.of(61.2462, 128.7203, 91.1602);
    private final Pose point2Control1 = poseFactory.of(70.2923, 17.2616, 0);
    private final Pose point2Control2 = poseFactory.of(59.9091, 71.4654, 0);
    private final Pose point2Control3 = poseFactory.of(66.0505, 105.399, 0);
    private final Pose point2Segment1Start = poseFactory.of(61.2462, 128.7203, -90);
    private final Pose point2Segment1End = poseFactory.of(61.2462, 128.7203, 180);
    private final Pose point2Segment2Heading = poseFactory.of(61.2462, 128.7203, 180);
    private final Pose point2Segment3Start = poseFactory.of(61.2462, 128.7203, 180);
    private final Pose point2Segment3End = poseFactory.of(61.2462, 128.7203, 90);
    private final Pose point2Segment4Target = poseFactory.of(62, 91.5, 0);
    private final Pose point3 = poseFactory.of(14.9129, 119.4215, -17.7834);
    private final Pose point3Control1 = poseFactory.of(57.6911, 105.7006, 0);
    // Autonomous routine

    public Command autoRoutine(/*DcMotorEx flywheelN, DcMotorEx flywheelP, */DcMotor intake, Launcher launcher/*, Servo releaseN, Servo releaseP*/) {

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
                // Spin up both flywheels immediately at match start to run continuously
                waitMs(delay*1000),
                /*instant(() -> {
                    flywheelN.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    flywheelN.setVelocity(launchN);
                    flywheelP.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    flywheelP.setVelocity(launchP);

                })*/
                instant(launcher::spinUp),

                // Fire off Path 1 and Intake 1 simultaneously using the scheduler
                instant(() -> {
                    schedule(follow(follower, path1()));
                    //schedule(timedIntake1);
                }),
                follow(follower, path1()),

                // Perform the first shooting station sequence at path1 endpoint
                //shootSequence(flywheelN, flywheelP, intake, releaseN, releaseP),
                launcher.getShootCommand(),

                // Fire off Path 2 and Delayed Intake 2 simultaneously
                instant(() -> {
                    schedule(follow(follower, path2()));
                    schedule(waitTimedIntake2);
                }),
                follow(follower, path2()),

                // Perform the second shooting station sequence at path2 endpoint
                //shootSequence(flywheelN, flywheelP, intake, releaseN, releaseP),
                launcher.getShootCommand(),

                // Final push to the finish line
                instant(() -> {
                    schedule(follow(follower, path3()));
                    schedule(waitTimedIntake2);
                }),
                follow(follower, path3())/*,

                // Clean up and shutdown both flywheels entirely at the end of autonomous
                instant(() -> {
                    flywheelN.setVelocity(0);
                    flywheelP.setVelocity(0);
                })
                */
        );
    }

    public void onPromptsComplete() {
        //team = prompter.get("alliance");
        delay = prompter.get("delay");


        //telemetry.addData("Selected Alliance", team);
        telemetry.addData("Selected Delay", delay);
        telemetry.update();
    }

    @Override
    public void runOpMode() {
        Scheduler.reset();
        prompter = new Prompter(this);
        follower = Constants.create(hardwareMap);
        follower.setPose(originalPos);
        //follower.setPose(start);
        follower.update();
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        launcher = new Launcher(hardwareMap);

        prompter.prompt("delay", new ValuePrompt<>("Start Delay", Double.class, 0.0, 30.0, 0.0, 1.0))
                .showSummary()
                .onComplete(this::onPromptsComplete);

        waitForStart();
        schedule(autoRoutine(intake, launcher/*flywheelN, flywheelP, intake, releaseN, releaseP*/));

        // Core execution loop: updates both the follower math and scheduled steps
        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            // Telemetry diagnostics
            telemetry.addData("x", follower.pose().x());
            telemetry.addData("y", follower.pose().y());
            telemetry.addData("heading", follower.pose().heading());
            telemetry.addData("Flywheel N Velocity", launcher.getFlywheelNVelocity());
            telemetry.addData("Flywheel P Velocity", launcher.getFlywheelPVelocity());

            if (follower.currentPath() != null) {
                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
                telemetry.addData("Path number", follower.pathIndex());
            }
            RobotRenderer.drawRobot(follower);
            telemetry.update();
            OpModeStorage.autonomousEndPose = follower.pose();
        }
        OpModeStorage.autonomousEndPose = follower.pose();
    }

    public Path path1() {
        return Paths.curve(/*start*/follower.pose(), path1Control1, path1).heading(Interpolator.piecewise().until(0.8304, Interpolator.linear(path1Segment1Start, path1Segment1End)).until(1, Interpolator.facingPoint(path1Segment2Target).reverse()));
    }

    public Path path2() {
        return Paths.curve(path1, point2Control1, point2Control2, point2Control3, point2).heading(Interpolator.piecewise().until(0.2595, Interpolator.linear(point2Segment1Start, point2Segment1End)).until(0.728, Interpolator.constant(point2Segment2Heading)).until(0.8607, Interpolator.linear(point2Segment3Start, point2Segment3End)).until(1, Interpolator.facingPoint(point2Segment4Target).reverse()));
    }

    public Path path3() {
        return Paths.curve(point2, point3Control1, point3).reverseTangent();
    }
}
