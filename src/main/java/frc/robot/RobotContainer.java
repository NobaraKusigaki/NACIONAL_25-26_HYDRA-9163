package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;

import frc.robot.Constants.OperatorConstants;
import frc.robot.DataDashboards.*;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

import swervelib.SwerveInputStream;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  private final RobotStressMonitor stressMonitor = new RobotStressMonitor();
  private final RobotStressController stressController = new RobotStressController();
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();

  private double driveSpeedScale = 1.0;

  private final SwerveInputStream driveAngularVelocity =
      SwerveInputStream.of(
              drivebase.getSwerveDrive(),
              () -> -controller.getLeftY(),
              () -> -controller.getLeftX())
          .withControllerRotationAxis(() -> controller.getRightX())
          .deadband(OperatorConstants.DEADBAND)
          .scaleTranslation(driveSpeedScale)
          .allianceRelativeControl(true);

  public RobotContainer() {
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);
  }

  private void configureBindings() {

    drivebase.setDefaultCommand(
        drivebase.driveFieldOriented(driveAngularVelocity));

    controller.triangle().onTrue(
        drivebase.snapToAngleOnce(Rotation2d.fromDegrees(0)));

    controller.circle().onTrue(
        drivebase.snapToAngleOnce(Rotation2d.fromDegrees(90)));

    controller.cross().onTrue(
        drivebase.snapToAngleOnce(Rotation2d.fromDegrees(180)));

    controller.square().onTrue(
        drivebase.snapToAngleOnce(Rotation2d.fromDegrees(270)));
  }


  public void updateDashboards() {

    RobotStressData stressData =
        stressMonitor.generateData(drivebase);

    stressController.update(stressData);

    driveSpeedScale = stressController.getMaxAllowedSpeedMps() / Constants.MAX_SPEED;

    double chassisSpeed =
        Math.abs(drivebase.getRobotVelocity().vxMetersPerSecond);

    dashboardPublisher.publish(
        stressData,
        driveSpeedScale,
        chassisSpeed);
  }

  public Command getAutonomousCommand() {
    return drivebase.getAutonomousCommand("AutoSimple");
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
