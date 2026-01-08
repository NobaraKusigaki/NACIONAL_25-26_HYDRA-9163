package frc.robot.subsystems.Score;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.Score.MotorTestSubsystem;

public class StreamDeckMotorController extends SubsystemBase {

    private final StringSubscriber commandSub;
    private String lastCommand = "STOP";
    private final MotorTestSubsystem motor;

    public StreamDeckMotorController(MotorTestSubsystem motor) {
        this.motor = motor;

        commandSub = NetworkTableInstance.getDefault()
                .getStringTopic("/StreamDeck/motorCommand")
                .subscribe("STOP");
    }

    @Override
    public void periodic() {
        String cmd = commandSub.get();
        if (cmd.equals(lastCommand)) return;
        lastCommand = cmd;

        switch (cmd) {
            case "FORWARD": motor.forward(); break;
            case "REVERSE": motor.reverse(); break;
            default: motor.stop(); break;
        }
}
}
