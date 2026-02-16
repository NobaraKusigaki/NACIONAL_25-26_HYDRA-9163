package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.Constants;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;

public class PreShooterManager extends SubsystemBase {

    // ==================== STATES ====================
    public enum PreShooterState {
        IDLE,
        ARMED,
        AUTO_FEEDING,
        DISABLED
    }

    // ==================== MODES ====================
    public enum ControlMode {
        MANUAL,
        AUTO_DISTANCE
    }

    private final PreShooterSubsystem subPreShooter;
    private final ViewSubsystem vision;
    private final ShooterManager shooterManager;

    private PreShooterState state = PreShooterState.IDLE;
    private ControlMode mode = ControlMode.MANUAL;

    public PreShooterManager(
        PreShooterSubsystem subPreShooter,
        ViewSubsystem vision,
        ShooterManager shooterManager
    ) {
        this.subPreShooter = subPreShooter;
        this.vision = vision;
        this.shooterManager = shooterManager;
    }

    // ==================== MODE CONTROL ====================

    /** Usar no TELEOP se quiser alternar */
    public void toggleMode() {
        if (mode == ControlMode.MANUAL) {
            enableAutoDistanceMode();
        } else {
            enableManualMode();
        }
    }

    /** Usar no AUTÔNOMO */
    public void enableAutoDistanceMode() {
        mode = ControlMode.AUTO_DISTANCE;
        setState(PreShooterState.IDLE);
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    /** Voltar para modo manual */
    public void enableManualMode() {
        mode = ControlMode.MANUAL;
        setState(PreShooterState.IDLE);
        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    public ControlMode getMode() {
        return mode;
    }
    
    public PreShooterState getState() {
        return state;
    }
    
    // ==================== MANUAL CONTROL ====================

    public void toggleManualFeed() {

        if (mode != ControlMode.MANUAL) return;

        if (state == PreShooterState.ARMED) {
            setState(PreShooterState.IDLE);
        } else {
            setState(PreShooterState.ARMED);
        }
    }

    /** Força parada segura (usar no fim do auto) */
    public void forceStop() {
        setState(PreShooterState.IDLE);
    }

    public void disable(String reason) {
        setState(PreShooterState.DISABLED);
        SmartDashboard.putString("PreShooter/DisabledReason", reason);
    }

    public void setState(PreShooterState newState) {
        if (state == newState) return;
        state = newState;
        SmartDashboard.putString("PreShooter/State", state.name());
    }

    // ==================== PERIODIC ====================
    @Override
    public void periodic() {

        // ===== AUTO MODE =====
        if (mode == ControlMode.AUTO_DISTANCE && state != PreShooterState.DISABLED) {

            // Shooter desligado? não alimenta.
            if (!shooterManager.isEnabled()) {
                setState(PreShooterState.IDLE);
            } else {

                int detectedTag = vision.getDetectedTagId();
                double distance = vision.getDistanceToTag();

                boolean correctTag = vision.isFrontTagAllowed();

                double targetDistance = Constants.LimelightConstants.distance4Shoot;
                double tolerance = 0.05; // 5cm

                boolean withinDistance =
                    Math.abs(distance - targetDistance) <= tolerance;

                double angularToleranceRad = Math.toRadians(1.5);
                boolean aligned =
                    Math.abs(vision.getFrontTxRad()) <= angularToleranceRad;

                boolean shooterReady = shooterManager.isAtSpeed();

                if (correctTag && withinDistance && aligned && shooterReady) {
                    setState(PreShooterState.AUTO_FEEDING);
                } else {
                    setState(PreShooterState.IDLE);
                }
            }
        }

        // ===== EXECUÇÃO =====
        switch (state) {

            case ARMED:
            case AUTO_FEEDING:
                subPreShooter.feed();
                break;

            case DISABLED:
            case IDLE:
            default:
                subPreShooter.stop();
                break;
        }
    }
}
