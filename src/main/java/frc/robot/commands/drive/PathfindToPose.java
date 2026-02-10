package frc.robot.commands.drive;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;

public class PathfindToPose {

  private PathfindToPose() {
  }

  public static Command toPose(
      Pose2d targetPose,
      PathConstraints constraints
  ) {

    return AutoBuilder.pathfindToPose(
        targetPose,
        constraints
    );
  }
}
