package frc.robot.utils.DriveUtils;

import edu.wpi.first.math.geometry.Rotation2d;

public class AntiTipController {

    private static final double WARNING_ANGLE = 10.0;
    private static final double DANGER_ANGLE  = 15.0;
    private static final double MAX_ANGLE     = 20.0;

    public double calculateSafetyFactor(Rotation2d pitch, Rotation2d roll) {
        double absPitch = Math.abs(pitch.getDegrees());
        double absRoll  = Math.abs(roll.getDegrees());
        double worst = Math.max(absPitch, absRoll);

        if (worst < WARNING_ANGLE) return 0.9;
        if (worst < DANGER_ANGLE)  return 0.6;
        if (worst < MAX_ANGLE)     return 0.35;
        return 0.18;
    }
}
