package frc.robot.subsystems.ScoreSD.Climb;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ClimbSDInput extends SubsystemBase {

    private final ClimbManager climb;
    private final StringSubscriber commandSub;

    private String lastCommand = "";

    public ClimbSDInput(ClimbManager climb) {
        this.climb = climb;

        commandSub = NetworkTableInstance.getDefault()
            .getStringTopic("/StreamDeck/Climb/command")
            .subscribe("");
    }

    @Override
    public void periodic() {

        String cmd = commandSub.get();
        if (cmd.isEmpty() || cmd.equals(lastCommand)) return;
        lastCommand = cmd;

        if (cmd.equals("TOGGLE")) {
            climb.togglePosition();
        } else if (cmd.equals("STOP")) {
            climb.stop();
        }
    }
}
