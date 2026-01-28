package frc.robot.adl;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.ScoreSD.Intake.IntakeManager;
import frc.robot.adl.ADLManager;
import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleManager;

public class ADLExecutor extends SubsystemBase {

    private final ADLManager adlManager;

    private final IntakeManager intake;
    private final IntakeAngleManager intakeAngle;

    private ADLState lastState = ADLState.IDLE;

    public ADLExecutor(
        ADLManager adlManager,
        IntakeManager intake,
        IntakeAngleManager intakeAngle
    ) {
        this.adlManager = adlManager;
        this.intake = intake;
        this.intakeAngle = intakeAngle;
    }

    @Override
    public void periodic() {

        ADLState current = adlManager.getCurrentState();

        if (current == lastState) return;
        lastState = current;

        switch (current) {

            case IDLE:
                stopAll();
                break;

            case ACQUIRING:
                executeAcquire();
                break;

            case MOVING:
                prepareForMovement();
                break;

            case SCORING:
                prepareForScoring();
                break;

            case CLIMBING:
                stopAll();
                break;

            case EMERGENCY:
                emergencyStop();
                break;

            default:
                stopAll();
                break;
        }
    }

    private void executeAcquire() {
        intakeAngle.togglePosition(); 
        intake.setState(IntakeManager.IntakeState.INTAKING);
    }

    private void prepareForMovement() {
        intake.stop();
        intakeAngle.togglePosition(); 
    }

    private void prepareForScoring() {
        intake.stop();
        intakeAngle.togglePosition(); 
    }

    private void emergencyStop() {
        stopAll();
    }

    private void stopAll() {
        intake.stop();
        intakeAngle.stop();
    }
}
