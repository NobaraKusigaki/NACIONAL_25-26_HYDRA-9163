package frc.robot.subsystems.ScoreSD.Shooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ShooterManager extends SubsystemBase {

    // ==================== STATES ====================
    public enum ShooterState {
        IDLE,
        SPINNING,
        AT_SPEED,
        DISABLED
    }

    private final ShooterSubsystem subShooter;
    private ShooterState state = ShooterState.IDLE;

    public ShooterManager(ShooterSubsystem subShooter) {
        this.subShooter = subShooter;
    }

    // ==================== CONTROL ====================

    public void toggleShooter() {
        if (state == ShooterState.IDLE) {
            setState(ShooterState.SPINNING);
        } else {
            setState(ShooterState.IDLE);
        }
    }

    public void disable() {
        setState(ShooterState.DISABLED);
    }

    public boolean isEnabled() {
        return state == ShooterState.SPINNING || state == ShooterState.AT_SPEED;
    }

    public boolean isAtSpeed() {
        return state == ShooterState.AT_SPEED;
    }

    private void setState(ShooterState newState) {
        if (state == newState) return;
        state = newState;
        SmartDashboard.putString("Shooter/State", state.name());
    }

    // ==================== PERIODIC ====================

    @Override
    public void periodic() {

        switch (state) {

            case SPINNING:
                subShooter.shoot();

                if (subShooter.isAtSpeed()) {
                    setState(ShooterState.AT_SPEED);
                }
                break;

            case AT_SPEED:
                subShooter.shoot();

                if (!subShooter.isAtSpeed()) {
                    setState(ShooterState.SPINNING);
                }
                break;

            case DISABLED:
            case IDLE:
            default:
                subShooter.stop();
                break;
        }
    }
}
