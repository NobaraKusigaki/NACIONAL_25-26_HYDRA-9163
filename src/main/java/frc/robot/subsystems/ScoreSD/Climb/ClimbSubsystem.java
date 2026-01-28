package frc.robot.subsystems.ScoreSD.Climb;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ClimbSubsystem extends SubsystemBase {

    private final SparkMax leftMotor =
        new SparkMax(Constants.Climb.LEFT_MOTOR_ID, MotorType.kBrushless);

    private final SparkMax rightMotor =
        new SparkMax(Constants.Climb.RIGHT_MOTOR_ID, MotorType.kBrushless);

    public ClimbSubsystem() {

        SparkMaxConfig cfg = new SparkMaxConfig();
          cfg
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        leftMotor.configure(cfg, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rightMotor.configure(cfg, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public void setPower(double power) {
        leftMotor.set(power);
        rightMotor.set(power);
    }

    public void stop() {
        leftMotor.stopMotor();
        rightMotor.stopMotor();
    }

    public double getPosition() {
        return (
            leftMotor.getEncoder().getPosition() +
            rightMotor.getEncoder().getPosition()
        ) / 2.0;
    }
}
