// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.ScoreSD.Intake;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeInputSubsystem extends SubsystemBase {
    private final SparkMax intakeLeaderMotor = 
    new SparkMax(Constants.IntakeConstants.INTAKE_LEADER_ID, MotorType.kBrushed);

    private final SparkMax intakeFollowerMotor = 
    new SparkMax(Constants.IntakeConstants.INTAKE_FOLLOWER_ID, MotorType.kBrushed);
  
    public IntakeInputSubsystem() {

        SparkMaxConfig leaderConfig = new SparkMaxConfig();
        leaderConfig
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        SparkMaxConfig followerConfig = new SparkMaxConfig();
        followerConfig
            .follow(intakeLeaderMotor, false ) //TESTAR PRA VER SE PRECISA INVERTER OU NAO
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);
            
        intakeLeaderMotor.configure(
            leaderConfig, 
            ResetMode.kResetSafeParameters, 
            PersistMode.kPersistParameters);

       intakeFollowerMotor.configure(
        followerConfig, 
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);     
            

    }
    public void setPower(double power){
        intakeLeaderMotor.set(power);
    }

    public void stopMotors(){
        intakeLeaderMotor.stopMotor();
    }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

}