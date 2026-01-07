package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.Utils.StreamDeckNT;
import frc.robot.dashboards.RobotStressMonitor;
import frc.robot.dashboards.DashboardPublisher;
import frc.robot.dashboards.RobotStressController;
import frc.robot.dashboards.RobotStressData;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.io.File;

import swervelib.SwerveInputStream;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);
  @SuppressWarnings("unused")
  private final StreamDeckNT streamDeck = new StreamDeckNT();
  private final RobotStressController stressController = new RobotStressController();
  private double driveSpeedScale = 1.0;

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  private final RobotStressMonitor stressMonitor = new RobotStressMonitor(30); 
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();

  private final int[] drivetrainPDHChannels = {0, 2, 3, 6, 10, 14, 15, 19};

  SwerveInputStream driveAngularVelocity =
    SwerveInputStream.of(
        drivebase.getSwerveDrive(),
        () -> -controller.getLeftY(),
        () -> -controller.getLeftX()
    )
    .withControllerRotationAxis(() -> controller.getRightX())
    .deadband(OperatorConstants.DEADBAND)
    .scaleTranslation(driveSpeedScale)
    .allianceRelativeControl(true);


  SwerveInputStream driveDirectAngle =
      driveAngularVelocity
          .copy()
          .withControllerHeadingAxis(controller::getRightX, controller::getRightY)
          .headingWhile(true);

  public RobotContainer() {
    configureBindings();

    DriverStation.silenceJoystickConnectionWarning(true);
    NamedCommands.registerCommand("test", Commands.print("I EXIST"));
  }

  private void configureBindings() {
    Command driveFieldOrientedDirectAngle =
        drivebase.driveFieldOriented(driveDirectAngle);

    drivebase.setDefaultCommand(driveFieldOrientedDirectAngle);
    // controller.cross().onTrue(Commands.runOnce(drivebase::zeroGyro));
   
    controller.triangle().onTrue(
    drivebase.snapToAngleOnce(
        Rotation2d.fromDegrees(0),
        () -> new Translation2d()));

      controller.circle().onTrue(
      drivebase.snapToAngleOnce(
          Rotation2d.fromDegrees(90),
          () -> new Translation2d()));

       controller.cross().onTrue(
       drivebase.snapToAngleOnce(
         Rotation2d.fromDegrees(180),
         () -> new Translation2d()));

         controller.square().onTrue(
          drivebase.snapToAngleOnce(
              Rotation2d.fromDegrees(270),
              () -> new Translation2d()));   

  }

  public void updateDashboards() {

    RobotStressData stressData =
        stressMonitor.generateData(drivetrainPDHChannels);
        stressController.update(stressData);

    driveSpeedScale = stressController.getMaxAllowedSpeedMps() / Constants.MAX_SPEED;
    double chassisSpeed =drivebase.getRobotVelocity().vxMetersPerSecond;

    dashboardPublisher.publish(
        stressData,
        driveSpeedScale,
        Math.abs(chassisSpeed)
    );
}

public Command getAutonomousCommand() {
  return drivebase.getAutonomousCommand("AutoSimple");
}

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
