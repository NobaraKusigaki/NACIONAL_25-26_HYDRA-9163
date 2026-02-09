package frc.robot.commands.vision;

import java.util.Set;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AimAtTagCommand extends Command {

  private final SwerveSubsystem swerve;
  private final  ViewSubsystem vision;

  public AimAtTagCommand(SwerveSubsystem swerve, ViewSubsystem vision) {
    this.swerve = swerve;
    this.vision = vision;
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    if (!vision.hasFrontTarget()) {
      swerve.drive(new Translation2d(), 0.0);
      return;
    }

    double rot =
        swerve.getHeadingPID().calculate(
            0.0,
            vision.getFrontTxRad());

    swerve.drive(new Translation2d(), rot);
  }

  @Override
  public void end(boolean interrupted) {
    swerve.drive(new Translation2d(), 0.0);
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public Set<Subsystem> getRequirements() {
    return Set.of(swerve);
  }
}
