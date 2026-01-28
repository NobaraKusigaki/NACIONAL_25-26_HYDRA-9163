package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeManager extends SubsystemBase {

    public enum IntakeState {
        IDLE,
        ARMED,        // humano autorizou
        INTAKING,
        OUTTAKING,
        DISABLED      
    }

    private final IntakeRollerSubsystem rollers;

    private IntakeState state = IntakeState.IDLE;

    private boolean aiSuggestsIntake = false;

    public IntakeManager(IntakeRollerSubsystem rollers) {
        this.rollers = rollers;
        SmartDashboard.putString("Intake/State", state.name());
    }


    public void armIntake() {
        setState(IntakeState.ARMED);
    }

    public void forceOuttake() {
        setState(IntakeState.OUTTAKING);
    }

    public void stop() {
        setState(IntakeState.IDLE);
    }

    public void setAiSuggestsIntake(boolean shouldIntake) {
        this.aiSuggestsIntake = shouldIntake;
    }

    public void disable(String reason) {
        setState(IntakeState.DISABLED);
        SmartDashboard.putString("Intake/DisabledReason", reason);
    }


    private void setState(IntakeState newState) {

        if (state == IntakeState.DISABLED && newState != IntakeState.DISABLED) {
            return;
        }

        if (state == newState) return;

        state = newState;
        SmartDashboard.putString("Intake/State", state.name());
    }

    @Override
    public void periodic() {

        switch (state) {

            case ARMED:
                rollers.stop();
                if (aiSuggestsIntake) {
                    setState(IntakeState.INTAKING);
                }
                break;

            case INTAKING:
                if (!aiSuggestsIntake) {
                    setState(IntakeState.ARMED);
                    break;
                }
                rollers.intake();
                break;

            case OUTTAKING:
                rollers.outtake();
                break;

            case DISABLED:
            case IDLE:
            default:
                rollers.stop();
                break;
        }
    }

    public IntakeState getState() {
        return state;
    }
}
