package org.firstinspires.ftc.teamcode.Auto;

import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.skeletonarmy.marrow.prompts.OptionPrompt;
import com.skeletonarmy.marrow.prompts.Prompter;
import com.skeletonarmy.marrow.prompts.ValuePrompt;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.ShooterConstants;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.Launcher;

import java.util.EnumMap;

@Autonomous(name = "PrompterAuto", group = "Autonomous")
public class PrompterAuto extends LinearOpMode {

    private Follower follower;

    private Launcher launcher;
    private Prompter prompter;// = new Prompter(this);
    private int launchP = ShooterConstants.launchP;
    private int launchN = ShooterConstants.launchN;

    EnumMap<Team, Pose> map = new EnumMap<>(Team.class);

    enum Team {
        RED,
        BLUE
    }

    private Team team = Team.RED;
    private double delay;



    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final EnumMap<Team, Pose> start = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> path1 = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> path1Control1 = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> point2 = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> point2Control1 = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> point2Segment1Target = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> point3 = new EnumMap<>(Team.class);
    private final EnumMap<Team, Pose> point3Control1 = new EnumMap<>(Team.class);

    private void initPoints() {
        start.put(Team.RED, poseFactory.of(8.6308, 32.2741, 0));
        start.put(Team.BLUE, poseFactory.of(132.9268, 107.869, 180));


        path1.put(Team.RED, poseFactory.of(58.4486, 13.065, 90));
        path1.put(Team.BLUE, poseFactory.of(83.5642, 112.3938, 90));

        path1Control1.put(Team.RED, poseFactory.of(45.9211, 26.9174, 0));
        path1Control1.put(Team.BLUE, poseFactory.of(98.9682, 105.628, 0));

        point2.put(Team.RED, poseFactory.of(59.994, 132.1856, -93.9874));
        point2.put(Team.BLUE, poseFactory.of(83.3403, 29.0562, -89.8794));

        point2Control1.put(Team.RED, poseFactory.of(95.0, 65.4955, 0));
        point2Control1.put(Team.BLUE, poseFactory.of(44.9146, 69.6564, 0));

        point2Segment1Target.put(Team.RED, poseFactory.of(58.2686, 84.79, 0));
        point2Segment1Target.put(Team.BLUE, poseFactory.of(83.2314, 80.79, 0));

        point3.put(Team.RED, poseFactory.of(9.4482, 95.975, -1.6702));
        point3.put(Team.BLUE, poseFactory.of(129.244, 31.6238, 33.2046));

        point3Control1.put(Team.RED, poseFactory.of(58.4443, 94.356, 0));
        point3Control1.put(Team.BLUE, poseFactory.of(109.4303, 18.6557, 0));
    }

    /**
     * Master autonomous routing logic utilizing Ivy Scheduler parallelism.
     */
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

    /**
     * Handles the non-blocking velocity-driven shooting sequence with dual flywheels and release servos.
     */
    /*public Command shootSequence(DcMotorEx flywheelN, DcMotorEx flywheelP, DcMotor intake, Servo releaseN, Servo releaseP) {
        return sequential(
                // 1. Wait dynamically until both flywheels hit steady velocity
                waitForFlywheelStable(flywheelN, flywheelP),

                // 2. Engage intake and open release servos (90 degrees / 0.5) simultaneously
                instant(() -> {
                    //intake.setPower(1.0);
                    releaseN.setPosition(0.0);
                    releaseP.setPosition(1.0);
                }),

                // 3. Keep feeding elements for exactly 2 seconds
                waitMs(2000),

                // 4. Shut down intake and return release servos to closed position (0 degrees / 0.0)
                instant(() -> {
                    //intake.setPower(0.0);
                    releaseN.setPosition(1.0);
                    releaseP.setPosition(0.0);
                }),

                // 5. Enforce a mandatory 3-second rest delay after shooting before triggering the next path
                waitMs(3000)
        );
    }*/

    /**
     * Custom Ivy command builder that holds execution until
     * both motor velocities remain stable within ±20 ticks of 1700 for 150ms.
     */
    /*private Command waitForFlywheelStable(DcMotorEx flywheelN, DcMotorEx flywheelP) {
        ElapsedTime stabilityTimer = new ElapsedTime();

        return Command.build()
                .setStart(stabilityTimer::reset)
                .setExecute(() -> {
                    double errorN = Math.abs(launchN - flywheelN.getVelocity());
                    double errorP = Math.abs(launchP - flywheelP.getVelocity());

                    // If either velocity strays outside our tolerance band, reset the window timer
                    if (errorP > 20) {
                        stabilityTimer.reset();
                    }
                })
                .setDone(() -> stabilityTimer.milliseconds() >= 150)
                .setEnd((EndCondition endCondition) -> {
                    // Leaves flywheels engaged
                });
    }*/

    public void onPromptsComplete() {
        team = prompter.get("alliance");
        delay = prompter.get("delay");


        telemetry.addData("Selected Alliance", team);
        telemetry.addData("Selected Delay", delay);
        telemetry.update();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        prompter = new Prompter(this);
        launcher = new Launcher(hardwareMap);
        initPoints();
        prompter.prompt("alliance", new OptionPrompt<>("Select Alliance", Team.RED, Team.BLUE))
                .prompt("delay", new ValuePrompt<>("Start Delay", Double.class, 0.0, 15.0, 0.0, 1.0))
                //.prompt(new MessagePrompt("MAKE SURE ALL SYSTEMS ARE READY"))
                .showSummary()
                .onComplete(this::onPromptsComplete);

        // Hardware initialization
        follower = Constants.create(hardwareMap);
        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
        /*
        DcMotorEx flywheelN = hardwareMap.get(DcMotorEx.class, "flywheelN");
        DcMotorEx flywheelP = hardwareMap.get(DcMotorEx.class, "flywheelP");
        flywheelN.setDirection(DcMotorEx.Direction.REVERSE);
        // --- VELOCITY CONTROLLER OPTIMIZATION TUNING ---
        double P = ShooterConstants.P;
        double I = ShooterConstants.I;
        double D = ShooterConstants.D;
        double F = ShooterConstants.F;

        PIDFCoefficients fastCoefficients = new PIDFCoefficients(P, I, D, F);
        flywheelN.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);
        flywheelP.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, fastCoefficients);

        //Servo hood = hardwareMap.get(Servo.class, "hood");
        Servo releaseN = hardwareMap.get(Servo.class, "releaseN");
        Servo releaseP = hardwareMap.get(Servo.class, "releaseP");

        //hood.setPosition(0.5);
        releaseN.setPosition(1.0);
        releaseP.setPosition(0.0);
        */

        // Reset the Command Scheduler and establish starting location
        Scheduler.reset();

        follower.update();
        while (!isStarted() && !isStopRequested()) {
            prompter.run();
            /*if (team == Team.RED) {
                telemetry.addLine("     +---\\/-----------------+");
                telemetry.addLine("     |                      |");
                telemetry.addLine("     |                      |");
                telemetry.addLine("     |        |/\\ /\\|      <|");
                telemetry.addLine(" RED |        |-----|       |");
                telemetry.addLine("     |>       |\\/ \\/|       |");
                telemetry.addLine("     |X                     |");
                telemetry.addLine("     |                      |");
                telemetry.addLine("     +-----------------/\\----+");
            } else if (team == Team.BLUE){
                telemetry.addLine("+---\\/-----------------+     ");
                telemetry.addLine("|                      |     ");
                telemetry.addLine("|                     X|     ");
                telemetry.addLine("|        |/\\ /\\|      <|     ");
                telemetry.addLine("|        |-----|       | BLUE");
                telemetry.addLine("|>       |\\/ \\/|       |     ");
                telemetry.addLine("|                      |     ");
                telemetry.addLine("|                      |     ");
                telemetry.addLine("+-----------------/\\----+     ");


            }*/
        }

        // Once start is pressed and the loop exits, safety-check or set your pose:
        if (team != null) {
            follower.setPose(start.get(team));
        } else {
            // Fallback default just in case no prompt was completed
            follower.setPose(start.get(Team.RED));
        }
        waitForStart();

        // Pass your motors and servos directly into your master auto layout
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
            telemetry.update();
        }
        OpModeStorage.autonomousEndPose = follower.pose();
        if(team == Team.BLUE){
            OpModeStorage.isRedAlliance = false;
        }
    }

    public Path path1() {
        return Paths.curve(start.get(team), path1Control1.get(team), path1.get(team)).heading(Interpolator.piecewise().until(1, Interpolator.facingPoint(point2Segment1Target.get(team)).reverse()));
    }

    public Path path2() {
        return Paths.curve(path1.get(team), point2Control1.get(team), point2.get(team)).heading(Interpolator.piecewise().until(1, Interpolator.facingPoint(point2Segment1Target.get(team)).reverse()));
    }

    public Path path3() {
        return Paths.curve(point2.get(team), point3Control1.get(team), point3.get(team)).tangent();
    }
}