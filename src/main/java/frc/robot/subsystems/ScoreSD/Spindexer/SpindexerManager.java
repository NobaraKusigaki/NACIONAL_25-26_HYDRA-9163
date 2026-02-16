package frc.robot.subsystems.ScoreSD.Spindexer;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class SpindexerManager extends SubsystemBase {

  public enum SpindexerState {
    IDLE,
    SPINNING_TO_TARGET,
    HOLDING,
    DISABLED
  }

  private final SpindexerSubsystem subsystem;

  private SpindexerState state = SpindexerState.IDLE;

  private double targetRotations = 0.0;
  private double tolerance = 0.02;
  private double referencePosition = 0.0; 

  public SpindexerManager(SpindexerSubsystem subsystem) {
    this.subsystem = subsystem;
    SmartDashboard.putString("Spindexer/State", state.name());
  }

  public void spinThreeRotations() {
    subsystem.resetAccumulatedPosition();
    targetRotations = 3.0;
    setState(SpindexerState.SPINNING_TO_TARGET);
  }

  public void stop() {
    setState(SpindexerState.IDLE);
  }

  public void disable(String reason) {
    setState(SpindexerState.DISABLED);
    SmartDashboard.putString("Spindexer/DisabledReason", reason);
  }

  private void setState(SpindexerState newState) {

    if (state == SpindexerState.DISABLED &&
        newState != SpindexerState.DISABLED) {
      return;
    }

    if (state == newState) return;

    state = newState;
    SmartDashboard.putString("Spindexer/State", state.name());
  }

public void saveReference() {
  subsystem.resetAccumulatedPosition();
  referencePosition = 0.0;
}

public void spinThreeFromReference() {
  subsystem.resetAccumulatedPosition();
  targetRotations = 3.0;
  setState(SpindexerState.SPINNING_TO_TARGET);
}

  @Override
  public void periodic() {

    switch (state) {

      case SPINNING_TO_TARGET:

        double current = subsystem.getAccumulatedRotations();
        double error = targetRotations - current;

        if (Math.abs(error) <= tolerance) {
          subsystem.stop();
          setState(SpindexerState.HOLDING);
        } else {
          double direction = Math.signum(error);
          subsystem.setPower(direction *
              Constants.SpindexerConstants.spinPower);
        }

        break;

      case HOLDING:
        subsystem.stop();
        break;

      case DISABLED:
      case IDLE:
      default:
        subsystem.stop();
        break;
    }
  }

  public SpindexerState getState() {
    return state;
  }
}
