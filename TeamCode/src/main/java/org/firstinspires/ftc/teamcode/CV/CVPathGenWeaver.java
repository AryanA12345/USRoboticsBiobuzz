package org.firstinspires.ftc.teamcode.CV;
import com.pedropathing.api.Paths;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.skeletonarmy.marrow.weaver.PathConfig;
import com.skeletonarmy.marrow.weaver.PathResult;
import com.skeletonarmy.marrow.weaver.PedroPathingConverter;
import com.skeletonarmy.marrow.weaver.Weaver;
import com.skeletonarmy.marrow.zones.CircleZone;
import com.skeletonarmy.marrow.zones.Point;
import com.skeletonarmy.marrow.zones.PolygonZone;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
public class CVPathGenWeaver {
    private Limelight3A ll;
    private Follower follower;
    private boolean isRed;
    CVCostAlgo algo = new CVCostAlgo();

    Telemetry telemetry;

    public ArrayList<Pose> simBall = new ArrayList<>();
    public boolean simulation = false;
    public Pose simBotPose = new Pose(0,0,0);

    PolygonZone bottomWall = new PolygonZone(new Point(-10, -10), new Point(154, -10), new Point(154, 0), new Point(-10, 0));
    PolygonZone topWall    = new PolygonZone(new Point(-10, 144), new Point(154, 144), new Point(154, 154), new Point(-10, 154));
    PolygonZone leftWall   = new PolygonZone(new Point(-10, 0), new Point(0, 0), new Point(0, 144), new Point(-10, 144));
    PolygonZone rightWall  = new PolygonZone(new Point(144, 0), new Point(154, 0), new Point(154, 144), new Point(144, 144));
    private static Matrix cameraAdjustments(Pose robotPose) {
        double heading = robotPose.heading();

        Matrix m = Matrix.rotationTransform(heading - Math.PI / 2).times(CVConstants.createRotationX(-CVConstants.CAMERA_PITCH));
        m = m.times(CVConstants.xyzConverter);
        return m;
    }

    public CVPathGenWeaver(HardwareMap hardwareMap, Follower follower, boolean isRed, Telemetry telemetry){
        this.follower = follower;
        this.isRed = isRed;
        this.telemetry = telemetry;


        ll = hardwareMap.get(Limelight3A.class, "limelight"); //change to your name
        ll.start();
        ll.pipelineSwitch(4); // change to your pipeline

        Weaver.setConfig(new PathConfig()
                .intakeWidth(10.0)
                .robotWidth(16.0)
                .robotHeight(18.0)
                .clearance(2.0)
        );
    }
    public CVPathGenWeaver(boolean isRed){

        this.isRed = isRed;

        Weaver.setConfig(new PathConfig()
                .intakeWidth(12.0)
                .robotWidth(16.0)
                .robotHeight(18.0)
                .clearance(2.0)
        );

    }
    public ArrayList<Pose> getBalls(boolean sim){
        if(sim) return simBall;
        ArrayList<double[]> relativeBalls = new ArrayList<>();
        ArrayList<Pose> absoluteBalls = new ArrayList<>();
        LLResult result = ll.getLatestResult();

        if (result != null && result.isValid()) {
            Pose po = follower.pose();
            for (LLResultTypes.DetectorResult dr : result.getDetectorResults()) {
                if (!("yellow_pollen".equalsIgnoreCase(dr.getClassName()) || (isRed ? "red_nectar".equalsIgnoreCase(dr.getClassName()) : "blue_nectar".equalsIgnoreCase(dr.getClassName())))) {
                    continue;
                }
                relativeBalls.add(new double[]{Math.toRadians(dr.getTargetXDegrees()), Math.toRadians(dr.getTargetYDegrees())});
            }
            for(double[] ball : relativeBalls){
                absoluteBalls.add(ballConverter(ball, po));
            }
        }
        drawAll(absoluteBalls);
        return absoluteBalls;
    }
    public Pose ballConverter(double[] ball, Pose curpose){
        Matrix cameraRay = new Matrix(
                new double[][] {
                        {Math.tan(ball[0])},
                        {Math.tan(ball[1])},
                        {1.0}
                }
        );
        Matrix absoluteRay = cameraAdjustments(curpose).times(cameraRay);

        double dist = -CVConstants.CAMERA_HEIGHT / absoluteRay.get(2, 0);

        Matrix cam = new Matrix(
                new double[][] {
                        {curpose.x() + CVConstants.FORWARD_OFFSET * Math.cos(curpose.heading())},
                        {curpose.y() + CVConstants.FORWARD_OFFSET * Math.sin(curpose.heading())},
                        {CVConstants.CAMERA_HEIGHT}
                }
        );
        double x = cam.get(0,0) + absoluteRay.get(0,0) * dist;
        double y = cam.get(1,0) + absoluteRay.get(1,0) * dist;
        return new Pose(x, y, 0);
    }

    private ArrayList<Pose> sortBalls() {
        ArrayList<Pose> balls = getBalls(simulation);
        Pose po = simulation ? simBotPose : follower.pose();
        balls.removeIf(ball ->
                ball.x() < 6 || ball.x() > 138 ||
                        ball.y() < 6 || ball.y() > 138 /*||
                        (isRed ? ball.x() > 70 : ball.x() < 70)*/
        );
        //ArrayList<Pose> best = new ArrayList<>();
        //best.addAll(algo.costFunc(balls, po));
        return balls;//algo.combiner(best, po);
    }


    public Path pathGen() {
        ArrayList<Pose> ballPoses = new ArrayList<>();
        //ballPoses.add(follower.pose());
        ballPoses.addAll(sortBalls());
        if (ballPoses.isEmpty()) {
            return null;
        }
        //Pose[] balls = ballPoses.toArray(new Pose[0]);
        PathResult result = Weaver.builder()
                .start(new Point(follower.pose().x(), follower.pose().y(), follower.pose().heading()))
                .addObstacle(isRed ? new PolygonZone(new Point(43, 48), new Point(43,91), new Point(52,91), new Point (52,48)) : new PolygonZone(new Point(90, 92), new Point(98,92), new Point(98,48), new Point (90,48)))
                //.addObstacle(isRed ? new PolygonZone(new Point(43, 48), new Point(43,91), new Point(52,91), new Point (52,48)) : new PolygonZone(new Point(90, 92), new Point(98,92), new Point(98,48), new Point (90,48)))
                .addObstacle(bottomWall)
                .addObstacle(leftWall)
                .addObstacle(rightWall)
                .addObstacle(topWall)
                .targets(ballPoses, e -> new Point(e.x(), e.y()))
                .maxTargets(4)
                .build();
        if(result == null || result.getSegments().isEmpty()){
            return null;
        }
        //drawChosen(ballPoses);
        //Path p = Paths.through(balls).tangent();
        return PedroPathingConverter.toPath(result);

    }
    public ArrayList<Pose> pathGenSim() {
        ArrayList<Pose> ballPoses = new ArrayList<>();
        ballPoses.add(simBotPose);
        ballPoses.addAll(sortBalls());
        if (ballPoses.size()<2) {
            return null;
        }
        Pose[] balls = ballPoses.toArray(new Pose[0]);
        Path p = Paths.through(balls).tangent();
        return ballPoses;
    }

    public void drawChosen(ArrayList<Pose> balls){
        for (int i = 0; i < balls.size(); i++) {
            Pose ball = balls.get(i);
            if (i == 0) {
            } else {
            }

        }
    }
    public void drawAll(ArrayList<Pose> balls){
        for (int i = 0; i < balls.size(); i++) {
            Pose ball = balls.get(i);
        }
    }
}