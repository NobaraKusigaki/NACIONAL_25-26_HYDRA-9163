package frc.robot.commands.vision;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.subsystems.Sensors.ViewSubsystem;

public class AimAtTagCommand extends Command {

  public enum CameraSide {
    FRONT,  // Torre
    BACK    // Hub + Outpost
  }

  private final SwerveSubsystem swerve;
  private final ViewSubsystem vision;
  private final CameraSide side;

  public AimAtTagCommand(
      SwerveSubsystem swerve,
      ViewSubsystem vision,
      CameraSide side
  ) {
    this.swerve = swerve;
    this.vision = vision;
    this.side = side;
    addRequirements(swerve);
  }

  @Override
  public void initialize() {
    swerve.getHeadingPID().reset(0);
  }

  @Override
  public void execute() {

    boolean valid;
    double tx;

    if (side == CameraSide.FRONT) {
      valid = vision.hasValidFrontTarget();
      tx = vision.getFrontTxRad();
    } else {
      valid = vision.hasValidBackTarget();
      tx = vision.getBackTxRad();
    }

    if (!valid) {
      swerve.stop();
      return;
    }

    double rot =
        swerve.getHeadingPID().calculate(
            tx,
            0.0
        );

    rot = Math.max(Math.min(rot, 2.0), -2.0);

    swerve.drive(new Translation2d(), rot);
  }

  @Override
  public void end(boolean interrupted) {
    swerve.stop();
  }

  @Override
  public boolean isFinished() {

    boolean valid =
        side == CameraSide.FRONT
            ? vision.hasValidFrontTarget()
            : vision.hasValidBackTarget();

    double tx =
        side == CameraSide.FRONT
            ? vision.getFrontTxRad()
            : vision.getBackTxRad();

    return valid && Math.abs(tx) < Math.toRadians(1.2);
  }
}
