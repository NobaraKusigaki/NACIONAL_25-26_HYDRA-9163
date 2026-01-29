package frc.robot.subsystems.ScoreSD.PreShooter;

import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class PreShooterSD extends SubsystemBase {

    private final PreShooterManager manager;
    private final StringSubscriber commandSub;
    private String lastCmd = "";

    public PreShooterSD(PreShooterManager manager) {
        this.manager = manager;

        commandSub = NetworkTableInstance.getDefault()
            .getStringTopic("/StreamDeck/PreShooter/command")
            .subscribe("IDLE");
    }

    @Override
    public void periodic() {

        String cmd = commandSub.get();
        if (cmd.equals(lastCmd)) return;
        lastCmd = cmd;

        switch (cmd) {
            case "ARM":
                manager.arm();
                break;

            case "STOP":
            default:
                manager.stop();
                break;
        }
    }
}
