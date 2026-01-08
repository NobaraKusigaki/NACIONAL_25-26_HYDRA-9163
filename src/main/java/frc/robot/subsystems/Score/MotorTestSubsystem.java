package frc.robot.subsystems.Score;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class MotorTestSubsystem extends SubsystemBase {

    private final SparkMax motor;

    public MotorTestSubsystem(int canId) {
        motor = new SparkMax(canId, MotorType.kBrushed);
        motor.set(0.0);
    }

    public void forward() {
        motor.set(0.4);
    }

    public void reverse() {
        motor.set(-0.4);
    }

    public void stop() {
        motor.set(0.0);
    }
}
