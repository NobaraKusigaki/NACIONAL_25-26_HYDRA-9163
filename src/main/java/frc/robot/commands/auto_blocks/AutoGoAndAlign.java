// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.auto_blocks;

import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.Constants;
import frc.robot.commands.drive.PathfindToPose;

public class AutoGoAndAlign extends SequentialCommandGroup {

  public AutoGoAndAlign() {}
  
  // public static Command build (SwerveSubsystem swerve, ViewSubsystem vision, Pose2d targetPose){
  //   PathConstraints constraints = new PathConstraints(
  //   Constants.MAX_SPEED, 
  //   Constants.MAX_ACCELERATION,
  //   Constants.MAX_ANGULAR_SPEED,
  //   Constants.MAX_ANGULAR_ACCELERATION);
      
  //   return Commands.sequence(
  //     PathfindToPose.toPose(targetPose, constraints),
  //     new AimAtTagCommand(swerve, vision)
  //     );
  // }
  
  }

