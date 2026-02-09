package frc.robot.commands.drive;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;

public class DriveToPoseCommand {

  private static final PathConstraints CONSTRAINTS =
      new PathConstraints(
          3.0,
          2.0,
          3.0,
          4.0,
          12.0,
          false
      );

      //TEM Q VERIFICAR SE ESSES VALORES BATEM CERTINHOS, COLOQUEI DE MANIERA ALEATORIA, MENOS A VOLTAGEM DA BATERIA

  public static Command goTo(Pose2d pose) {
    return AutoBuilder.pathfindToPose(
        pose,
        CONSTRAINTS,
        0.0 
    );
  }
}
