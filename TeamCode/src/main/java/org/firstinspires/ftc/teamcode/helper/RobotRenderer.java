package org.firstinspires.ftc.teamcode.helper;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;

public class RobotRenderer {

    /**
     * Draws an 18x18 inch square representing the robot on the FTC Dashboard field view.
     * @param follower Your PedroPathing Follower instance
     */
    public static void drawRobot(Follower follower) {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        TelemetryPacket packet = new TelemetryPacket();
        Canvas canvas = packet.fieldOverlay();

        Pose pose = follower.pose();
        double x = pose.x();
        double y = pose.y();
        double heading = pose.heading(); // PedroPathing heading is in radians

        // Robot is 18x18 inches -> half size is 9 inches from center
        double half = 9.0;
        double quarter = 4.5;


        // Local 4 corners of the 18x18 square
        double[][] corners = {
                {-half, -half},
                { half, -half},
                { half,  half},
                {-half,  half}
        };

        double[] wx = new double[4];
        double[] wy = new double[4];

        // Rotate and translate corners to match field coordinates
        for (int i = 0; i < 4; i++) {
            double lx = corners[i][0];
            double ly = corners[i][1];
            wx[i] = x + (lx * Math.cos(heading) - ly * Math.sin(heading));
            wy[i] = y + (lx * Math.sin(heading) + ly * Math.cos(heading));
        }

        // Draw the robot body boundary (Blue Square)
        canvas.setStroke("blue");
        canvas.strokePolygon(wx, wy);

        // Draw a heading indicator line pointing forward (Red Line)
        double frontX = x + (half * Math.cos(heading));
        double frontY = y + (half * Math.sin(heading));
        canvas.setStroke("red");
        canvas.strokeLine(x, y, frontX, frontY);

        dashboard.sendTelemetryPacket(packet);
    }
}