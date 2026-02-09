// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.auto_blocks;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.commands.drive.DriveToPoseCommand;

public class AutoGoAndAlign extends SequentialCommandGroup {

  public AutoGoAndAlign(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      Pose2d targetPose
  ) {

    addCommands(
        DriveToPoseCommand.goTo(targetPose),
        new AimAtTagCommand(swerve, vision)
    );
  }
}
