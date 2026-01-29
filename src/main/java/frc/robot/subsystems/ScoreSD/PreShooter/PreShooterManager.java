package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class PreShooterManager extends SubsystemBase {

    public enum PreShooterState {
        IDLE,
        ARMED,
        FEEDING,
        DISABLED
    }

    private final PreShooterSubsystem subsystem;

    private PreShooterState state = PreShooterState.IDLE;

    private boolean shooterReady = false;
    private boolean aligned = false;

    public PreShooterManager(PreShooterSubsystem subsystem) {
        this.subsystem = subsystem;
    }

    public void arm() {
        if (state != PreShooterState.DISABLED)
            setState(PreShooterState.ARMED);
    }

    public void stop() {
        setState(PreShooterState.IDLE);
    }

    public void updateShooterReady(boolean ready) {
        shooterReady = ready;
    }

    public void updateAlignment(boolean isAligned) {
        aligned = isAligned;
    }

    public void disable(String reason) {
        setState(PreShooterState.DISABLED);
        SmartDashboard.putString("PreShooter/DisabledReason", reason);
    }

    private void setState(PreShooterState newState) {
        if (state == newState) return;
        state = newState;
        SmartDashboard.putString("PreShooter/State", state.name());
    }

    @Override
    public void periodic() {

        switch (state) {

            case ARMED:
                if (shooterReady && aligned) {
                    setState(PreShooterState.FEEDING);
                }
                subsystem.stop();
                break;

            case FEEDING:
                subsystem.feed();
                break;

            case DISABLED:
            case IDLE:
            default:
                subsystem.stop();
                break;
        }
    }
}
