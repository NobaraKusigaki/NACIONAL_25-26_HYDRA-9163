package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeAngleSDInput extends SubsystemBase {

    private final IntakeAngleManager manager;
    private final StringSubscriber cmdSub;

    public IntakeAngleSDInput(IntakeAngleManager manager) {
        this.manager = manager;

        cmdSub = NetworkTableInstance.getDefault()
            .getTable("StreamDeck/IntakeAngle")
            .getStringTopic("command")
            .subscribe("");
    }

    @Override
    public void periodic() {
        String cmd = cmdSub.get();
        if (cmd.isEmpty()) return;

        if (cmd.equals("TOGGLE")) {
            manager.togglePosition();
        }
    }
}
