package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import frc.robot.Dashboards.RobotStress.DashboardPublisher;
import frc.robot.Dashboards.RobotStress.RobotStressController;
import frc.robot.Dashboards.RobotStress.RobotStressData;
import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
import frc.robot.subsystems.Score.MotorTestSubsystem;
import frc.robot.subsystems.Score.StreamDeckMotorController;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);

  private final MotorTestSubsystem motorTestSubsystem =
      new MotorTestSubsystem(20);

  private final StreamDeckMotorController streamDeckMotor =
      new StreamDeckMotorController(motorTestSubsystem);

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  private final RobotStressMonitor stressMonitor = new RobotStressMonitor();
  private final RobotStressController stressController = new RobotStressController();
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();

  private double driveSpeedScale = 1.0;

  public RobotContainer() {
    configureBindings();
    configureDefaultCommands();
    DriverStation.silenceJoystickConnectionWarning(true);
  }

  private void configureDefaultCommands() {
    drivebase.setDefaultCommand(
        Commands.run(() -> {
          drivebase.drive(
              new Translation2d(
                  -controller.getLeftY(),
                  controller.getLeftX()
              ),
              controller.getRightX()
          );
        }, drivebase)
    );
  }

  private void configureBindings() {

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

    RobotStressData stressData = stressMonitor.generateData(drivebase);
    stressController.update(stressData);

    driveSpeedScale = stressController.getMaxAllowedSpeedMps() / Constants.MAX_SPEED;

    double chassisSpeed = Math.abs(drivebase.getRobotVelocity().vxMetersPerSecond);

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
