package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager.PreShooterState;

public class PreShooterSD extends SubsystemBase {

    private final PreShooterManager preshooterManager;
    private final StringSubscriber commandSub;

    private String lastCommand = "IDLE";

    public PreShooterSD(PreShooterManager preshooterManager) {
        this.preshooterManager = preshooterManager;

        commandSub = NetworkTableInstance.getDefault()
            .getStringTopic("/StreamDeck/PreShooter/command")
            .subscribe("IDLE");
    }

    @Override
    public void periodic() {
        String cmd = commandSub.get();

        if (cmd.equals(lastCommand)) return;
        lastCommand = cmd;

        switch (cmd) {
            case "SPIN":
                preshooterManager.setState(PreShooterState.SPINNING);
                break;

            case "IDLE":
            default:
                preshooterManager.setState(PreShooterState.IDLE);
                break;
        }
    }
}
