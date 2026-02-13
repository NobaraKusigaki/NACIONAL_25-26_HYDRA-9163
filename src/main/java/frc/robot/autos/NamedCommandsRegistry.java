package frc.robot.autos;

import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.commands.auto_blocks.AutoGoAndAlign;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class NamedCommandsRegistry {

  private NamedCommandsRegistry() {
  }

  public static void register(
      SwerveSubsystem swerve,
      ViewSubsystem vision
  ) {

    // ========== AUTO BLOCKS ========== 
    // NamedCommands.registerCommand(
    //     "GoAndAlign",
    //     new AutoGoAndAlign(
    //         swerve,
    //         vision,
    //         new Pose2d() 
    //     )
    // );

    
  }
}
