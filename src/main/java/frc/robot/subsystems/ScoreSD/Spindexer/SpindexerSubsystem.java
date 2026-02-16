package frc.robot.subsystems.ScoreSD.Spindexer;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class SpindexerSubsystem extends SubsystemBase {

  private final SparkMax spinStarMotor =
      new SparkMax(Constants.SpindexerConstants.spinStarMotorPort,
                   MotorType.kBrushed);

  private final DutyCycleEncoder encoder =
      new DutyCycleEncoder(Constants.SpindexerConstants.indexerEncoderPort);

  private double lastPosition = 0.0;
  private double accumulatedRotations = 0.0;

  public SpindexerSubsystem() {

    SparkMaxConfig config = new SparkMaxConfig();
    config
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(40)
        .inverted(false);

    spinStarMotor.configure(
        config,
        com.revrobotics.spark.SparkBase.ResetMode.kResetSafeParameters,
        com.revrobotics.spark.SparkBase.PersistMode.kPersistParameters
    );

    lastPosition = encoder.get();
  }

  public void setPower(double power) {
    spinStarMotor.set(power);
  }

  public void stop() {
    spinStarMotor.stopMotor();
  }

  public double getAccumulatedRotations() {

    double current = encoder.get();
    double error = current - lastPosition;

    if (error > 0.5) {
      error -= 1.0;
    } else if (error < -0.5) {
      error += 1.0;
    }

    accumulatedRotations += error;
    lastPosition = current;

    return accumulatedRotations;
  }

  
  public void resetAccumulatedPosition() {
    accumulatedRotations = 0.0;
    lastPosition = encoder.get();
  }

}
