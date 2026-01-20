package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class PreShooterManager extends SubsystemBase {
  public enum PreShooterState {
    IDLE,
    SPINNING
}
    private final PreShooterSubsystem preshooter;
    private PreShooterState currentState = PreShooterState.IDLE;

    public PreShooterManager() {
        preshooter = new PreShooterSubsystem();
    }

    public void setState(PreShooterState newState) {
        if (currentState == newState) return;

        currentState = newState;
        SmartDashboard.putString("PreShooter/State", currentState.name());
    }

    public PreShooterState getState() {
        return currentState;
    }

    @Override
    public void periodic() {
        switch (currentState) {
            case SPINNING:
                preshooter.setPower(0.5);
                break;

            case IDLE:
            default:
                preshooter.stop();
                break;
        }
    }

    public void stop() {
        setState(PreShooterState.IDLE);
    }
}
