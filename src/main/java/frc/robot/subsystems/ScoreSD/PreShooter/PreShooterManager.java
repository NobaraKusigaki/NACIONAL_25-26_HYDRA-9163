package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;

public class PreShooterManager extends SubsystemBase {

    // ==================== STATES ====================
    public enum PreShooterState {
        IDLE,
        ARMED,           // Manual feeding
        AUTO_FEEDING,    // Auto feeding
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

    public void toggleMode() {
        mode = (mode == ControlMode.MANUAL)
                ? ControlMode.AUTO_DISTANCE
                : ControlMode.MANUAL;

        setState(PreShooterState.IDLE);

        SmartDashboard.putString("PreShooter/Mode", mode.name());
    }

    public ControlMode getMode() {
        return mode;
    }

    // ==================== MANUAL TOGGLE ====================

    public void toggleManualFeed() {

        if (mode != ControlMode.MANUAL) return;

        if (state == PreShooterState.ARMED) {
            setState(PreShooterState.IDLE);
        } else {
            setState(PreShooterState.ARMED);
        }
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

        System.out.println("mode: " + mode.name() + " | state: " + state.name());


        // ===== AUTO MODE DECISION =====
        if (mode == ControlMode.AUTO_DISTANCE && state != PreShooterState.DISABLED) {
    
            // Corte imediato se shooter desligar
            if (!shooterManager.isEnabled()) {
                setState(PreShooterState.IDLE);
            } else {
    
                int detectedTag = vision.getDetectedTagId();
                double distance = vision.getDistanceToTag();
    
                boolean correctTag = detectedTag == 22;
                boolean withinDistance = distance <= 1.7;
                boolean shooterReady = shooterManager.isAtSpeed();
    
                if (correctTag && withinDistance && shooterReady) {
                    setState(PreShooterState.AUTO_FEEDING);
                } else {
                    setState(PreShooterState.IDLE);
                }
            }
        }
    
        // ===== EXECUTION BASED ON STATE =====
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