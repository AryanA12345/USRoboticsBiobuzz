/**
 * Path visualization at /PedroVisualization/RedShoot.pp
 * Autonomous starting below red goal, shoots, then parks in red zone
 * Place robot at bottom-right corner: (133,8)
 * Now includes AutoTelemetry class to shorten loop()
 * Updated on 10/11/2026
 */
package org.firstinspires.ftc.teamcode.Auto;

import com.pedropathing.api.Paths;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Autonomous(name = "RedShoot", group = "Autonomous")
public class RedShoot extends /*Linear*/OpMode {

    private AutoTelemetry autoTelemetry;
    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(133.5586, 8.296, 90);
    private final Pose path1 = poseFactory.of(59.2563, 8.296, 90);
    private final Pose path1Control1 = poseFactory.of(89.6496, 63.5596, 0);

    private final Pose point2 = poseFactory.of(59.8159, 38.1161, 88.9249);
    private final Pose point3Start = poseFactory.of(59.8159, 38.1161, 90);
    private final Pose point3 = poseFactory.of(9.7699, 97.6318, 0);
    private final Pose point3Control1 = poseFactory.of(19.4195, 25.5889, 0);

    // Autonomous routine

    /**
     * Moves robot from bottom left corner (starting position) {@link #start} to autonomous starting point {@link #path1}
     * @return path to starting position
     */
    public Command autoInit() {
        return sequential(
                follow(follower, path1())
        );
    }

    /**
     * Moves robot through autonomous sequence
     * @return path for auto
     */
    public Command autoRoutine() {
        return sequential(
                follow(follower, path2()),
                follow(follower, path3())
        );
    }

    @Override
    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(start);
        follower.update();
        /*AutoTelemetry */autoTelemetry = new AutoTelemetry(follower, telemetry); //New class to clean up loop()

        schedule(autoInit()); //This should move the robot to the gameplay starting position when INIT is pressed

    }

    @Override
    public void start() {
        schedule(autoRoutine());
    }

    @Override
    public void loop() {
        //follower.update();
        Scheduler.execute();

        /*telemetry.addData("x", follower.pose().x());
        telemetry.addData("y", follower.pose().y());
        telemetry.addData("heading", follower.pose().heading());

        if (follower.currentPath() != null) {
            telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
            telemetry.addData("Path number", follower.pathIndex());
        }

        telemetry.update();*/
        autoTelemetry.update();
        telemetry.update();//excluded from autoTelemetry so more telemetry can be added
    }
    @Override
    public void stop() {
        OpModeStorage.autonomousEndPose = follower.pose(); //saves your position in that file
    }
    //I needed to change it from a linearOpMode to an OpMode, so I had to change the structure some. The original structure is below

    /*@Override
    public void runOpMode() {


        waitForStart();
        schedule(autoRoutine());

        while (opModeIsActive()) {
            follower.update();
            Scheduler.execute();

            telemetry.addData("x", follower.pose().x());
            telemetry.addData("y", follower.pose().y());
            telemetry.addData("heading", follower.pose().heading());

            if (follower.currentPath() != null) {
                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
                telemetry.addData("Path number", follower.pathIndex());
            }

            telemetry.update();
        }
    }*/

    /**
     * @return auto initialization path from {@link #start} to {@link #path1}
     */
    public Path path1() {
        return Paths.curve(start, path1Control1, path1).linear(start, path1);
    }

    /**
     * @return auto path
     */
    public Path path2() {
        return Paths.line(path1, point2).tangent();
    }

    /**
     * @return auto path
     */
    public Path path3() {
        return Paths.curve(point3Start, point3Control1, point3).linear(point3Start, point3);
    }
}
