package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleManager extends SubsystemBase {

    public enum PositionState {
        ZERO,
        TARGET
    }

    public enum ControlMode {
        MANUAL,
        AUTOMATIC,
        DISABLED
    }

    private final IntakeAngleSubsystem io = new IntakeAngleSubsystem();

    private final PIDController pid =
        new PIDController(
            Constants.IntakeConstants.ANGLE_KP,
            Constants.IntakeConstants.ANGLE_KI,
            Constants.IntakeConstants.ANGLE_KD
        );

    private final ArmFeedforward ff =
        new ArmFeedforward(
            Constants.IntakeConstants.ANGLE_KS,
            Constants.IntakeConstants.ANGLE_KG,
            Constants.IntakeConstants.ANGLE_KV
        );

    private ControlMode mode = ControlMode.DISABLED;
    private PositionState lastPosition = PositionState.ZERO;

    private double targetAngleDeg = 0.0;

    public IntakeAngleManager() {
        pid.setTolerance(Constants.IntakeConstants.ANGLE_TOLERANCE_DEG);

        io.loadZero();
        targetAngleDeg =
            Preferences.getDouble("IntakeAngleTarget", 0.0);
    }

    /* ================= MANUAL ================= */

    public void manualUp() {
        mode = ControlMode.MANUAL;
        io.setPower(0.05);
    }

    public void manualDown() {
        mode = ControlMode.MANUAL;
        io.setPower(-0.05);
    }

    public void stop() {
        io.stop();
        mode = ControlMode.DISABLED;
    }

    /* ================= CALIBRAÇÃO ================= */

    /** A posição atual vira ZERO absoluto */
    public void calibrateZero() {
        io.recalibrateZero();
        lastPosition = PositionState.ZERO;
    }

    /** Salva a posição atual como TARGET (relativa ao ZERO) */
    public void calibrateTarget() {
        targetAngleDeg = io.getAngleDeg();
        Preferences.setDouble("IntakeAngleTarget", targetAngleDeg);
        lastPosition = PositionState.TARGET;
    }

    /* ================= AUTOMÁTICO ================= */

    /** Botão único: alterna ZERO <-> TARGET */
    public void togglePosition() {
        if (lastPosition == PositionState.ZERO) {
            moveToTarget();
        } else {
            moveToZero();
        }
    }

    public void moveToZero() {
        targetAngleDeg = 0.0;
        lastPosition = PositionState.ZERO;
        mode = ControlMode.AUTOMATIC;
    }

    public void moveToTarget() {
        targetAngleDeg =
            Preferences.getDouble("IntakeAngleTarget", targetAngleDeg);
        lastPosition = PositionState.TARGET;
        mode = ControlMode.AUTOMATIC;
    }

    /* ================= LOOP ================= */

    @Override
    public void periodic() {

        double current = io.getAngleDeg();

        SmartDashboard.putNumber("IntakeAngle/Angle", current);
        SmartDashboard.putString("IntakeAngle/Mode", mode.name());
        SmartDashboard.putString("IntakeAngle/LastPosition", lastPosition.name());
        SmartDashboard.putNumber("IntakeAngle/Target", targetAngleDeg);

        if (mode != ControlMode.AUTOMATIC) return;

        double pidOut = pid.calculate(current, targetAngleDeg);
        double ffOut  = ff.calculate(Math.toRadians(current), 0.0);

        double output =
            Math.max(Math.min(pidOut + ffOut, 0.05), -0.05);

        io.setPower(output);

        if (pid.atSetpoint()) {
            io.stop();
            mode = ControlMode.DISABLED;
        }
    }
}
