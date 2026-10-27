// package org.firstinspires.ftc.teamcode.pedro;

// import com.pedropathing.algorithm.Foresight;
// import com.pedropathing.algorithm.ForesightConfig;
// import com.pedropathing.controllers.Controller;
// import com.pedropathing.follower.Follower;
// import com.pedropathing.math.Matrix;
// import com.pedropathing.math.Vector2D;
// import com.pedropathing.revhub.drivetrains.Mecanum;
// import com.pedropathing.revhub.drivetrains.MecanumConfig;
// import com.pedropathing.revhub.localizers.PinpointConfig;
// import com.pedropathing.revhub.localizers.PinpointLocalizer;
// import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
// import com.qualcomm.robotcore.hardware.DcMotorSimple;
// import com.qualcomm.robotcore.hardware.HardwareMap;

// import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

// public class Constants {
//     public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
//         c.frontLeftName.set("frontLeft");
//         c.frontRightName.set("frontRight");
//         c.backLeftName.set("backLeft");
//         c.backRightName.set("backRight");
//         c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
//         c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
//         c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
//         c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
//         c.manualBrakeMode.set(true);
//     });

//     public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
//         c.name.set("pinpoint");
//         c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
//         c.xPodOffset.set(-3.02704);
//         c.yPodOffset.set(0.65811);
//         c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection./*FORWARD*/REVERSED);
//         c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection./*FORWARD*/REVERSED);
//         c.globalDistanceUnit.set(DistanceUnit.INCH);
//         c.offsetUnits.set(DistanceUnit.INCH);
//     });

//     public static ForesightConfig foresightConfig = new ForesightConfig(
//             c -> {
//                 Controller primaryTranslationalForward = Controller.proportional(0.2611113170536415);
//                 Controller secondaryTranslationalForward = Controller.proportional(0.09647365462843119);
//                 Controller primaryTranslationalLateral = Controller.proportional(0.38051625729118727);
//                 Controller secondaryTranslationalLateral = Controller.proportional(0.14059058948743985);

//                 c.forwardTranslational.set(Controller.piecewise((secondaryTranslationalForward).put(2.5, primaryTranslationalForward)));
//                 c.strafeTranslational.set(Controller.piecewise((secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral)));

//                 c.coast.set(Controller.proportionalFeedforward(/*0.02835551257603697*/0.01));
//                 c.brake.set(Controller.proportionalFeedforward(/*0.024102185689631423*/0.05));

//                 c.headingFeedback.set(Controller.proportional(/*3.286620310547003*/1.0));
//                 c.headingBrakeCoefficients.set(Vector2D.cartesian(/*0.052017774343255135, 0.00563686682587759*/0.1,0.01));

//                 c.linearBrakeCoefficients.set(Matrix.diag(/*0.08948782443574778, 0.0626582642905701*/0.13,0.1));
//                 c.quadraticBrakeCoefficients.set(Matrix.diag(/*9.690653622293105E-4, 0.0019103469490468125*/3.6E-2,0.056));

//                 c.maxAchievableForwardVelocity.set(/*40.34373717820616*/30.0);
//                 c.maxAchievableStrafeVelocity.set(/*32.74066849637569*/30.0);
//                 c.naturalForwardDeceleration.set(/*52.16987024772851*/70.0);
//                 c.naturalStrafeDeceleration.set(/*79.67230601197282*/70.0);
//             }
//     );


//     public static Follower create(HardwareMap h) {
//         return new Follower(
//                 new PinpointLocalizer(h, localizerConfig),
//                 new Mecanum(h, drivetrainConfig),
//                 new Foresight(foresightConfig)
//         );
//     }

// }

package org.firstinspires.ftc.teamcode.pedro;

import com.acmerobotics.dashboard.config.Config;
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

@Config
public class Constants {
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.manualBrakeMode.set(true);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-3.02704);
        c.yPodOffset.set(0.65811);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                // Initialize the piecewise segments explicitly to prevent compilation errors
                Controller primaryTranslationalForward = Controller.proportional(0.08);
                Controller secondaryTranslationalForward = Controller.proportional(0.03);
                Controller primaryTranslationalLateral = Controller.proportional(0.10);
                Controller secondaryTranslationalLateral = Controller.proportional(0.04);

                // Path error correction (smoothly ramps up correction scaling if the robot drifts)
                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                // Feedforward mapping (static power adjustments to overcome physical system friction)
                c.coast.set(Controller.proportionalFeedforward(0.015));
                c.brake.set(Controller.proportionalFeedforward(0.01));

                // Heading / Rotational stability
                c.headingFeedback.set(Controller.proportional(1.0));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05, 0.005));

                // Linear & Quadratic predictive deceleration mapping (controls braking sharpness)
                c.linearBrakeCoefficients.set(Matrix.diag(0.08, 0.06));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.001, 0.0015));

                // Physical limits (Safe starting targets for standard 19.2:1 / 435RPM GoBilda drivetrains)
                c.maxAchievableForwardVelocity.set(55.0);
                c.maxAchievableStrafeVelocity.set(40.0);
                c.naturalForwardDeceleration.set(85.0);
                c.naturalStrafeDeceleration.set(100.0);
                c.brakeAggression.set(0.7); // Try dropping this down to 0.7 or 0.8 to force earlier braking



            }
    );

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
        );
    }
}
