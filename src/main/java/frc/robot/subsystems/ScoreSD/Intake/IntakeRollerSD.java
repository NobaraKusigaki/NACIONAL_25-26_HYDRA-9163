package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeRollerSD extends SubsystemBase {

    private final IntakeManager manager;
    private final StringSubscriber commandSub;

    private String lastCmd = "";

    public IntakeRollerSD(IntakeManager manager) {
        this.manager = manager;

        commandSub = NetworkTableInstance.getDefault()
            .getStringTopic("/StreamDeck/Intake/command")
            .subscribe("STOP");
    }

    @Override
    public void periodic() {

        String cmd = commandSub.get();
        if (cmd.equals(lastCmd)) return;
        lastCmd = cmd;

        switch (cmd) {

            case "ARM":
                manager.armIntake();
                break;

            case "OUTTAKE":
                manager.forceOuttake();
                break;

            case "STOP":
            default:
                manager.stop();
                break;
        }
    }
}
