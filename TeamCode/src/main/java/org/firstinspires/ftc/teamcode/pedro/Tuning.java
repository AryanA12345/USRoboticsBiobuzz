package org.firstinspires.ftc.teamcode.pedro;

public class Tuning {
    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }
}
