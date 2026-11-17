//package org.firstinspires.ftc.teamcode.Auto;
//
//import static com.pedropathing.api.Paths.*;
//
//import com.google.blocks.ftcrobotcontroller.runtime.CRServoAccess;
//import com.pedropathing.api.Paths;
//
//import com.pedropathing.api.PoseFactory;
//import com.pedropathing.follower.Follower;
//import com.pedropathing.math.Pose;
//import com.pedropathing.paths.Path;
//import com.pedropathing.ivy.Command;
//import com.pedropathing.ivy.Scheduler;
//import static com.pedropathing.ivy.Scheduler.schedule;
//import static com.pedropathing.ivy.commands.Commands.*;
//import static com.pedropathing.ivy.groups.Groups.sequential;
//import static com.pedropathing.ivy.pedro.PedroCommands.follow;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.pedropathing.paths.interpolator.Interpolator;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//
//import org.firstinspires.ftc.teamcode.helper.Launcher;
//import org.firstinspires.ftc.teamcode.pedro.Constants;
//
//@Autonomous(name = "FlowerAuto", group = "Autonomous")
//public class FlowerAuto extends LinearOpMode {
//
//    private Follower follower;
//    private Launcher launcher;
//
//    private final PoseFactory poseFactory = PoseFactory.degrees();
//
//    private final Pose start = poseFactory.of(54.8241, 9.1829, 90);
//    private final Pose point1 = poseFactory.of(55.891, 12.3049, -88.2174);
//    private final Pose point1Segment1Heading = poseFactory.of(55.891, 12.3049, 270);
//    private final Pose point1Segment2Target = poseFactory.of(54.5, 57, 0);
//    private final Pose point2 = poseFactory.of(9.0021, 9.0491, -90);
//    private final Pose point2Control1 = poseFactory.of(22.967, 46.7774, 0);
//    private final Pose point2Segment1Heading = poseFactory.of(9.0021, 9.0491, 270);
//    private final Pose point2Segment2Start = poseFactory.of(9.0021, 9.0491, -90);
//    private final Pose point2Segment2End = poseFactory.of(9.0021, 9.0491, 270);
//    private final Pose point2Segment3Heading = poseFactory.of(9.0021, 9.0491, -90);
//    private final Pose point3 = poseFactory.of(60.3467, 36.4895, -270);
//    private final Pose point3Control1 = poseFactory.of(61.1441, 15.4866, 0);
//    private final Pose point3Segment1Heading = poseFactory.of(60.3467, 36.4895, 270);
//    private final Pose point3Segment2Start = poseFactory.of(60.3467, 36.4895, -90);
//    private final Pose point3Segment2End = poseFactory.of(60.3467, 36.4895, 90);
//    private final Pose point4 = poseFactory.of(60.1467, 88.8708, 90);
//    private final Pose point5 = poseFactory.of(47.6745, 120.8887, 107.4299);
//    private final Pose point5Control1 = poseFactory.of(60.1223, 99.5216, 0);
//    private final Pose point5Control2 = poseFactory.of(51.5925, 106.0281, 0);
//    private final Pose point5Control3 = poseFactory.of(47.2902, 119.6069, 0);
//    private final Pose point5Segment2Target = poseFactory.of(58, 88, 0);
//    private final Pose point6 = poseFactory.of(47.0818, 130.8684, 93.3989);
//    private final Pose point7 = poseFactory.of(10.2468, 47.4569, 180);
//    private final Pose point7Control1 = poseFactory.of(15.3277, 99.041, 0);
//    private final Pose point7Control2 = poseFactory.of(42.5517, 55.7283, 0);
//    private final Pose point7Segment1Target = poseFactory.of(46, 139, 0);
//    private final Pose point7Segment2Start = poseFactory.of(10.2468, 47.4569, 69.2631);
//    private final Pose point7Segment2End = poseFactory.of(10.2468, 47.4569, 180);
//    private final Pose point8 = poseFactory.of(14.1948, 116.8291, 157.7073);
//    private final Pose point8Control1 = poseFactory.of(54.4988, 123.3861, 0);
//    private final Pose point8Segment1Target = poseFactory.of(1.75, 46.5, 0);
//    private final Pose point8Segment2Start = poseFactory.of(14.1948, 116.8291, -136.7676);
//    private final Pose point8Segment2End = poseFactory.of(14.1948, 116.8291, 150);
//    private final Pose point8Segment3Target = poseFactory.of(65, 96, 0);
//
//
//    // Autonomous routine
//
//    private DcMotorSimple intake;
//    Command timedIntake1 = sequential(
//            instant(() -> intake.setPower(1.0)),
//            waitMs(100),
//            instant(() -> intake.setPower(0.0))
//    );
//    public Command autoRoutine() {
//        return sequential(
//                follow(follower, path1()),
//                launcher.getShootCommand(),
//
//
//                follow(follower, path3()),
//                follow(follower, path4()),
//                follow(follower, path5()),
//                follow(follower, path6()),
//                follow(follower, path7()),
//                follow(follower, path8())
//        );
//    }
//
//    @Override
//    public void runOpMode() {
//        Scheduler.reset();
//        follower = Constants.create(hardwareMap);
//        follower.setPose(start);
//        follower.update();
//        DcMotor intake = hardwareMap.get(DcMotor.class, "intake");
//
//        waitForStart();
//        schedule(autoRoutine());
//
//        while (opModeIsActive()) {
//            follower.update();
//            Scheduler.execute();
//
//            telemetry.addData("x", follower.pose().x());
//            telemetry.addData("y", follower.pose().y());
//            telemetry.addData("heading", follower.pose().heading());
//
//            if (follower.currentPath() != null) {
//                telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
//                telemetry.addData("Path number", follower.pathIndex());
//            }
//
//            telemetry.update();
//        }
//    }
//
//    public Path path1() {
//        return Paths.line(start, point1).heading(Interpolator.piecewise().until(0.1268, Interpolator.constant(point1Segment1Heading)).until(1, Interpolator.facingPoint(point1Segment2Target).reverse()));
//    }
//
//    public Path path2() {
//        return Paths.curve(point1, point2Control1, point2).heading(Interpolator.piecewise().until(0.1306, Interpolator.constant(point2Segment1Heading)).until(0.5501, Interpolator.linear(point2Segment2Start, point2Segment2End)).until(1, Interpolator.constant(point2Segment3Heading)));
//    }
//
//    public Path path3() {
//        return Paths.curve(point2, point3Control1, point3).heading(Interpolator.piecewise().until(0.193, Interpolator.constant(point3Segment1Heading)).until(1, Interpolator.linear(point3Segment2Start, point3Segment2End)));
//    }
//
//    public Path path4() {
//        return Paths.line(point3, point4).constant(point4);
//    }
//
//    public Path path5() {
//        return Paths.curve(point4, point5Control1, point5Control2, point5Control3, point5).heading(Interpolator.piecewise().until(0.7151, Interpolator.tangent).until(1, Interpolator.facingPoint(point5Segment2Target).reverse()));
//    }
//
//    public Path path6() {
//        return Paths.line(point5, point6).tangent();
//    }
//
//    public Path path7() {
//        return Paths.curve(point6, point7Control1, point7Control2, point7).heading(Interpolator.piecewise().until(0.1319, Interpolator.facingPoint(point7Segment1Target)).until(1, Interpolator.linear(point7Segment2Start, point7Segment2End)));
//    }
//
//    public Path path8() {
//        return Paths.curve(point7, point8Control1, point8).heading(Interpolator.piecewise().until(0.1892, Interpolator.facingPoint(point8Segment1Target)).until(0.75, Interpolator.linear(point8Segment2Start, point8Segment2End)).until(1, Interpolator.facingPoint(point8Segment3Target).reverse()));
//    }
//}
