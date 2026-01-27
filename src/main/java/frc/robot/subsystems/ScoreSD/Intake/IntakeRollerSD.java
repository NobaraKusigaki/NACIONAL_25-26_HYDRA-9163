package frc.robot.subsystems.ScoreSD.Intake;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeSDInput extends SubsystemBase {

    private final IntakeManager intakeManager;
    private final StringSubscriber commandSub;

    private String lastCommand = "IDLE";

    public IntakeSDInput(IntakeManager intakeManager) {
        this.intakeManager = intakeManager;

        commandSub = NetworkTableInstance.getDefault()
            .getStringTopic("/StreamDeck/Intake/command")
            .subscribe("IDLE");
    }

    @Override
    public void periodic() {
        String cmd = commandSub.get();

        if (cmd.equals(lastCommand)) return;
        lastCommand = cmd;

        switch (cmd) {
            case "INTAKE":
                intakeManager.setState(IntakeManager.IntakeState.INTAKING);
                break;

            case "OUTTAKE":
                intakeManager.setState(IntakeManager.IntakeState.OUTTAKING);
                break;

            case "IDLE":
            default:
                intakeManager.setState(IntakeManager.IntakeState.IDLE);
                break;
        }
    }
}
