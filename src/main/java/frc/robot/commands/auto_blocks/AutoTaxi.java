package frc.robot.commands.auto_blocks;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

public class AutoTaxi extends SequentialCommandGroup {

  public AutoTaxi(SwerveSubsystem swerveSubsystem) {
    addCommands(
      swerveSubsystem
        .run(() -> swerveSubsystem.drive(
            new Translation2d(-Constants.MAX_SPEED, 0),
            0
        ))
        .withTimeout(0.5),

        swerveSubsystem.runOnce(() ->
          swerveSubsystem.drive(new Translation2d(0, 0), 0))
    );
  }
}