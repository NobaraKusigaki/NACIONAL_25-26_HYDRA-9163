package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;

import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.Utils.StreamDeckNT;
import frc.robot.dashboards.RobotStressMonitor;
import frc.robot.dashboards.DashboardPublisher;
import frc.robot.dashboards.RobotStressData;

import java.io.File;

import swervelib.SwerveInputStream;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);
  private final StreamDeckNT streamDeck = new StreamDeckNT();

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  // ===== DASHBOARDS =====
  private final RobotStressMonitor stressMonitor = new RobotStressMonitor(1); // PDH ID
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();

  // Ajustar para os canais reais do drivetrain
  private final int[] drivetrainPDHChannels = {0, 1, 2, 3};

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
  }

  public void updateDashboards() {
    RobotStressData stressData =
        stressMonitor.generateData(drivetrainPDHChannels);

    dashboardPublisher.publish(stressData);
  }

  public Command getAutonomousCommand() {
    return drivebase.getAutonomousCommand("AutoSimple");
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
