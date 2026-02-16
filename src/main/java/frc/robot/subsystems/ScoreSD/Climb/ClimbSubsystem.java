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

        SparkMaxConfig leftConfig  = new SparkMaxConfig();
        SparkMaxConfig rightConfig = new SparkMaxConfig();

        leftConfig.idleMode(IdleMode.kBrake)
           .smartCurrentLimit(40)
           .inverted(false);

           rightConfig.idleMode(IdleMode.kBrake)
              .smartCurrentLimit(40)
                .inverted(true);

        leftMotor.configure(leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rightMotor.configure(rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

        resetEncoders();
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

    public void resetEncoders() {
        leftMotor.getEncoder().setPosition(0);
        rightMotor.getEncoder().setPosition(0);
    }
}
