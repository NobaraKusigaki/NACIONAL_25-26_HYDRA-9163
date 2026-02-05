package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleManager extends SubsystemBase {

    public enum PositionState { ZERO, TARGET }
    public enum ControlMode { MANUAL, AUTOMATIC, DISABLED }

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
        targetAngleDeg = Preferences.getDouble("IntakeAngleTarget", 0.0);
    }


    public void manualUp() {
        mode = ControlMode.MANUAL;
        io.setPower(0.07);
    }

    public void manualDown() {
        mode = ControlMode.MANUAL;
        io.setPower(-0.07);
    }

    public void stop() {
        io.stop();
        mode = ControlMode.DISABLED;
    }

    public void calibrateZero() {
        io.recalibrateZero();
        lastPosition = PositionState.ZERO;
    }

    public void calibrateTarget() {
        targetAngleDeg = io.getAngleDeg();
        Preferences.setDouble("IntakeAngleTarget", targetAngleDeg);
        lastPosition = PositionState.TARGET;
    }

    public void togglePosition() {
        mode = ControlMode.AUTOMATIC;
        if (lastPosition == PositionState.ZERO) {
            moveToTarget();
        } else {
            moveToZero();
        }
    }
    

    private void moveToZero() {
        targetAngleDeg = 0.0;
        lastPosition = PositionState.ZERO;
        mode = ControlMode.AUTOMATIC;
    }

    private void moveToTarget() {
        targetAngleDeg =
            Preferences.getDouble("IntakeAngleTarget", targetAngleDeg);
        lastPosition = PositionState.TARGET;
        mode = ControlMode.AUTOMATIC;
    }

    @Override
    public void periodic() {
        double current = io.getAngleDeg();
    
        if (mode != ControlMode.AUTOMATIC) return;
    
        double output =
            pid.calculate(current, targetAngleDeg)
            + ff.calculate(Math.toRadians(current), 0.0);
    
        output = Math.max(Math.min(output, 0.07), -0.07);
        io.setPower(output);
    
        if (pid.atSetpoint()) {
            io.stop();
            mode = ControlMode.DISABLED;
        }
    }
    
}
