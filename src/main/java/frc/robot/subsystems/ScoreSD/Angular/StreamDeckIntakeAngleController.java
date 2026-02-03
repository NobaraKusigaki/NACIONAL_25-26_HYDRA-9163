package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class StreamDeckIntakeAngleController extends SubsystemBase {

    private final IntakeAngleManager intake;
    private final StringSubscriber commandSub;
    private final NetworkTableEntry commandEntry;

    public StreamDeckIntakeAngleController(IntakeAngleManager intake) {
        this.intake = intake;

        var table =
            NetworkTableInstance.getDefault()
                .getTable("StreamDeck/IntakeAngle");

        commandSub =
            table.getStringTopic("command").subscribe("");

        commandEntry =
            table.getEntry("command");
    }

    @Override
    public void periodic() {
        if (!DriverStation.isEnabled()) return;

        String cmd = commandSub.get();
        if (cmd.isEmpty()) return;

        if (cmd.equals("TOGGLE")) {
            intake.togglePosition();

            commandEntry.setString("");
        }
    }
}
