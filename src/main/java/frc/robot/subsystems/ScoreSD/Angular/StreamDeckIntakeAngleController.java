package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class StreamDeckIntakeAngleController extends SubsystemBase {

    private final IntakeAngleManager intake;
    private final NetworkTable table;

    private double lastToggleCount = -1;

    public StreamDeckIntakeAngleController(IntakeAngleManager intake) {
        this.intake = intake;
        table = NetworkTableInstance.getDefault()
            .getTable("StreamDeck/IntakeAngle");
    }

    @Override
    public void periodic() {
        if (!DriverStation.isEnabled()) return;

        double count = table.getEntry("toggleCount").getDouble(0);

        if (count != lastToggleCount) {
            lastToggleCount = count;
            intake.togglePosition();
            System.out.println("[SD] TOGGLE via counter: " + count);
        }
    }
}
