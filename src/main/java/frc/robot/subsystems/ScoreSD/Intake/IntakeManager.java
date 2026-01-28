package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeManager extends SubsystemBase {
        
        public enum IntakeState {
            IDLE,
            INTAKING,
            OUTTAKING
        }
    
    private final IntakeRollerSubsystem intake;
    private IntakeState currentState = IntakeState.IDLE;

    public IntakeManager() {
        intake = new IntakeRollerSubsystem();
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
                intake.setSpd(0.6);
                break;

            case OUTTAKING:
                intake.setSpd(-0.6);
                break;

            case IDLE:
            default:
                intake.stop();
                break;
        }
    }

    public void stop() {
        setState(IntakeState.IDLE);
    }
}
