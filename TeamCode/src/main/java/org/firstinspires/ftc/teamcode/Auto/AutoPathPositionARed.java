package org.firstinspires.ftc.teamcode.Auto;

import com.pedropathing.api.Paths;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.pedro.Constants;

@Autonomous(name = "AutoPathPositionARed", group = "Autonomous")
public class AutoPathPositionARed extends OpMode {

    private AutoTelemetry autoTelemetry;
    private Follower follower;

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(70.5196, 9, 90);
    private final Pose path1 = poseFactory.of(57.32, 35.472, 90);
    private final Pose point2 = poseFactory.of(8.2668, 13.7854, 270);
    private final Pose point3 = poseFactory.of(7.833, 9, 270);
    private final Pose point4 = poseFactory.of(57.3451, 29.7295, 90);

    // Autonomous routine
    public Command autoRoutine() {
        return sequential(
                follow(follower, path1()),
                follow(follower, path2()),
                follow(follower, path3()),
                follow(follower, path4())
        );
    }

    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(start);
        follower.update();
        /*AutoTelemetry */
        autoTelemetry = new AutoTelemetry(follower, telemetry);

    }

    @Override
    public void init_loop(){
        autoTelemetry.update();
        telemetry.update();
        follower.update();

    }

    @Override
    public void start() {
        schedule(autoRoutine());
    }

    @Override
    public void loop() {
        Scheduler.execute();
        autoTelemetry.update();
        telemetry.update();
    }

    public void stop() {
        OpModeStorage.autonomousEndPose = follower.pose();
        OpModeStorage.isRedAlliance = true;                //VERY IMPORTANT CHANGE FOR EVERY AUTO
    }

    public Path path1() {
        return Paths.line(start, path1).constant(path1);
    }

    public Path path2() {
        return Paths.line(path1, point2).linear(path1, point2);
    }

    public Path path3() {
        return Paths.line(point2, point3).constant(point3);
    }

    public Path path4() {
        return Paths.line(point3, point4).linear(point3, point4);
    }
}
