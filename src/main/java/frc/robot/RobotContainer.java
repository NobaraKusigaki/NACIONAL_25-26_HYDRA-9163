package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Utils.SD_Button1_Command;
import frc.robot.Utils.StreamDeckNT;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import java.io.File;
import swervelib.SwerveInputStream;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);
  private final StreamDeckNT streamDeck = new StreamDeckNT();

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  SwerveInputStream driveAngularVelocity =
      SwerveInputStream.of(
              drivebase.getSwerveDrive(),
              () -> controller.getLeftY() * -1,
              () -> controller.getLeftX() * -1)
          .withControllerRotationAxis(controller::getRightX)
          .deadband(OperatorConstants.DEADBAND)
          .scaleTranslation(0.8)
          .allianceRelativeControl(true);

  SwerveInputStream driveDirectAngle =
      driveAngularVelocity
          .copy()
          .withControllerHeadingAxis(controller::getRightX, controller::getRightY)
          .headingWhile(true);

  SwerveInputStream driveRobotOriented =
      driveAngularVelocity.copy().robotRelative(true).allianceRelativeControl(false);

  public RobotContainer() {
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);
    NamedCommands.registerCommand("test", Commands.print("I EXIST"));
  }

  private void configureBindings() {
    Command driveFieldOrientedDirectAngle =
        drivebase.driveFieldOriented(driveDirectAngle);

    drivebase.setDefaultCommand(driveFieldOrientedDirectAngle);

    controller.cross().onTrue(Commands.runOnce(drivebase::zeroGyro));
    controller.L1().whileTrue(Commands.runOnce(drivebase::lock, drivebase).repeatedly());

     new Trigger(streamDeck::b1)
    .onTrue(new SD_Button1_Command());
  }

  public Command getAutonomousCommand() {
    return drivebase.getAutonomousCommand("AutoSimple");
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
