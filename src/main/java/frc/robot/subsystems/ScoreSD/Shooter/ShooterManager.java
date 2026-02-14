package frc.robot.subsystems.ScoreSD.Shooter;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ShooterManager extends SubsystemBase {

    private final ShooterSubsystem subShooter;
    private boolean neoEnabled = false;

    public ShooterManager(ShooterSubsystem subShooter) {
        this.subShooter = subShooter;
    }

    public void toggleShooter() {
        neoEnabled = !neoEnabled;

        if (neoEnabled) {
            subShooter.shoot();
        } else {
            subShooter.stop();
        }
    }

    public boolean isEnabled() {
        return neoEnabled;
    }

    public boolean isAtSpeed() {
        return subShooter.isAtSpeed();
    }

    @Override
    public void periodic() {
        // This method will be called once per scheduler run
    }
}
