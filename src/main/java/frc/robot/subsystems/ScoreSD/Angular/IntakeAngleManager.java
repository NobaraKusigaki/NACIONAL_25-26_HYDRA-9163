package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleManager extends SubsystemBase {

    private final IntakeAngleSubsystem angle;

    private final PIDController pid =
        new PIDController(0.035, 0.0, 0.002);

    private final ArmFeedforward ff =
        new ArmFeedforward(0.05, 0.2, 0.01, 0.0);

    private AngleState state = AngleState.DISABLED;
    private double targetDeg = 0.0;

    public IntakeAngleManager(IntakeAngleSubsystem angle) {
        this.angle = angle;

        pid.setTolerance(2.0);

        targetDeg = Preferences.getDouble("Intake/TargetAngle", 0.0);
    }

    // -------- CONTROLE --------
    public void setManual(double power) {
        state = AngleState.MANUAL;
        angle.setPower(power);
    }

    public void moveTo(double angleDeg) {
        targetDeg = angleDeg;
        Preferences.setDouble("Intake/TargetAngle", targetDeg);
        state = AngleState.MOVING;
    }

    public void hold() {
        targetDeg = angle.getAngleDeg();
        state = AngleState.HOLDING;
    }

    public void disable() {
        state = AngleState.DISABLED;
        angle.stop();
    }

    @Override
    public void periodic() {

        if (state == AngleState.MOVING || state == AngleState.HOLDING) {

            double current = angle.getAngleDeg();
            double error = targetDeg - current;

            double pidOut = pid.calculate(current, targetDeg);
            double ffOut  = ff.calculate(Math.toRadians(current), 0.0);

            double output =
                Math.max(Math.min(pidOut + ffOut, 0.6), -0.6);

            angle.setPower(output);

            if (pid.atSetpoint() && state == AngleState.MOVING) {
                state = AngleState.HOLDING;
            }

            SmartDashboard.putNumber("Intake/AngleTarget", targetDeg);
            SmartDashboard.putString("Intake/AngleState", state.name());
        }
    }
}
