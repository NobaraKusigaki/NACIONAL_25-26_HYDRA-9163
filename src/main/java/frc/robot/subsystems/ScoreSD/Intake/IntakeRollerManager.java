package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeManager extends SubsystemBase {
        
        public enum IntakeState {
            IDLE,
            INTAKING,
            OUTTAKING
        }
    
    private final IntakeInputSubsystem intake;
    private IntakeState currentState = IntakeState.IDLE;

    public IntakeManager() {
        intake = new IntakeInputSubsystem();
    }
    
  
    public void setState(IntakeState newState) {
        if (currentState == newState) return;

        currentState = newState;
        SmartDashboard.putString("Intake/State", currentState.name());
    }

    public IntakeState getState() {
        return currentState;
    }

    @Override
    public void periodic() {
        switch (currentState) {

            case INTAKING:
                intake.setPower(0.6);
                break;

            case OUTTAKING:
                intake.setPower(-0.6);
                break;

            case IDLE:
            default:
                intake.stopMotors();
                break;
        }
    }

    public void stop() {
        setState(IntakeState.IDLE);
    }
}
