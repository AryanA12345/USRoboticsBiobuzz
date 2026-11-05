package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(7.8985998198742005);
        c.yPodOffset.set(2.0160036762868327);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.2611113170536415);
                Controller secondaryTranslationalForward = Controller.proportional(0.09647365462843119);
                Controller primaryTranslationalLateral = Controller.proportional(0.38051625729118727);
                Controller secondaryTranslationalLateral = Controller.proportional(0.14059058948743985);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.02835551257603697));
                c.brake.set(Controller.proportionalFeedforward(0.024102185689631423));

                c.headingFeedback.set(Controller.proportional(3.286620310547003));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.052017774343255135, 0.00563686682587759));

                c.linearBrakeCoefficients.set(Matrix.diag(0.08948782443574778, 0.0626582642905701));
                c.quadraticBrakeCoefficients.set(Matrix.diag(9.690653622293105E-4, 0.0019103469490468125));

                c.maxAchievableForwardVelocity.set(40.34373717820616);
                c.maxAchievableStrafeVelocity.set(32.74066849637569);
                c.naturalForwardDeceleration.set(52.16987024772851);
                c.naturalStrafeDeceleration.set(79.67230601197282);
            }
    );
//    public static ForesightConfig foresightConfig = new ForesightConfig(
//            c -> {
//                Controller primaryTranslationalForward = Controller.proportional(0.7969506825497126);
//                Controller secondaryTranslationalForward = Controller.proportional(0.29445198228768704);
//                Controller primaryTranslationalLateral = Controller.proportional(0.18051625729118727);
//                Controller secondaryTranslationalLateral = Controller.proportional(0.14059058948743985);
//
//                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
//                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));
//
//                c.coast.set(Controller.proportionalFeedforward(0.01373095280130505));
//                c.brake.set(Controller.proportionalFeedforward(0.011671309881109291));
//
//                c.headingFeedback.set(Controller.proportional(23.98303703525768));
//                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.10521616552800547, 0.016487278732422263));
//
//                c.linearBrakeCoefficients.set(Matrix.diag(0.14564102175429305, 0.05935315192144766));
//                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0031120384307696185, 0.002901283430438488));
//
//                c.maxAchievableForwardVelocity.set(54.29135130936572);
//                c.maxAchievableStrafeVelocity.set(39.39520933929602);
//                c.naturalForwardDeceleration.set(37.62050812419055);
//                c.naturalStrafeDeceleration.set(74.80456244314877);
//            }
//    );


    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }

}