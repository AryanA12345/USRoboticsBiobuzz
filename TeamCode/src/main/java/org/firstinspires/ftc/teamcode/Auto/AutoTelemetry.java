/**
 * Class to add {@link #follower} x, y, and heading to telemetry.
 * Also adds {@code follower.distanceToEndpoint()} and {@code follower.pathIndex()}
 */
package org.firstinspires.ftc.teamcode.Auto;

import com.pedropathing.follower.Follower; // Update this import to match your Pedro Pathing package if needed
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class AutoTelemetry {
    private final Follower follower; // Or whatever your follower type is (e.g., Follower)
    private final Telemetry telemetry;

    /**
     * Class to add {@link #follower} x, y, and heading to telemetry.
     * Also adds {@code follower.distanceToEndpoint()} and {@code follower.pathIndex()}
     * @param follower Robot follower
     * @param telemetry Driver station telemetry
     */
    public AutoTelemetry(Follower follower, Telemetry telemetry) {
        this.follower = follower;
        this.telemetry = telemetry;
    }

    /**
     * Adds {@link #follower} x, y, heading, {@code follower.distanceToEndpoint()}, and {@code follower.pathIndex()} to telemetry
     */
    public void update() {
        // Run standard loop updates if not already handled outside
        follower.update();

        // Push pose data
        telemetry.addData("x", follower.pose().x());
        telemetry.addData("y", follower.pose().y());
        telemetry.addData("heading", follower.pose().heading());

        // Push path data if available
        if (follower.currentPath() != null) {
            telemetry.addData("Current path distance remaining", follower.distanceToEndpoint());
            telemetry.addData("Path number", follower.pathIndex());
        }

        //removed so more telemetry can be added in loop()
        //telemetry.update();

    }
}