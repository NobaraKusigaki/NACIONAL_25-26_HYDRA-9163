package frc.robot.autonomous.blue;

import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.wpilibj2.command.Command;

public class AutoBlueOutPost {

  private AutoBlueOutPost() {
   
  }

  public static Command build() {
    return new PathPlannerAuto("BlueOutpost");
  }
}
