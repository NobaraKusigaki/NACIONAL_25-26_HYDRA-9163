// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.ScoreSD.Spindexer;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class SpindexerSubsystem extends SubsystemBase {
  private SparkMax spindexerMotor = new SparkMax(Constants.SpinConstants.SPINDEXER_ID, MotorType.kBrushed);
  
  public SpindexerSubsystem() {}

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
