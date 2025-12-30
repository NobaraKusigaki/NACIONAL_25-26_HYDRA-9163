package frc.robot;

import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.utils.StreamDeckNT;
import java.io.File;
import java.util.Map;

import swervelib.SwerveInputStream;
import frc.robot.dashboards.RobotStressMonitor;
import frc.robot.dashboards.DashboardPublisher;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;



public class RobotContainer {
  private final CommandPS5Controller controller = new CommandPS5Controller(0);
  private final StreamDeckNT streamDeck = new StreamDeckNT();
  private final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));
  private final RobotStressMonitor stressMonitor = new RobotStressMonitor(1); // ID da PDH
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();
  private final int[] drivetrainPDHChannels = {0, 1, 2, 3}; //AJUSTAAAARRRR 
  private ShuffleboardTab stressTab;

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
    
    configureStressDashboard();

  }

  private void configureBindings() {
    Command driveFieldOrientedDirectAngle = drivebase.driveFieldOriented(driveDirectAngle);
    drivebase.setDefaultCommand(driveFieldOrientedDirectAngle);
    controller.cross().onTrue(Commands.runOnce(drivebase::zeroGyro));
    
  }

  public void updateDashboards() {
    double drivetrainCurrent =
            stressMonitor.getDrivetrainCurrent(drivetrainPDHChannels);

    double stressScore =
            stressMonitor.calculateStressScore(drivetrainCurrent);

    dashboardPublisher.publish(
            stressMonitor.getBatteryVoltage(),
            stressMonitor.getTotalCurrent(),
            drivetrainCurrent,
            stressScore,
            stressMonitor.getStressLevel(stressScore)
    );
}

private void configureStressDashboard() {
  stressTab = Shuffleboard.getTab("Robot Stress");

  stressTab.add("Battery Voltage",
          Shuffleboard.getTab("Robot Stress")
              .addNumber("Battery Voltage", () -> stressMonitor.getBatteryVoltage()))
      .withWidget(BuiltInWidgets.kVoltageView)
      .withPosition(0, 0)
      .withSize(2, 1);

  stressTab.addNumber("Total Current (A)",
          () -> stressMonitor.getTotalCurrent())
      .withWidget(BuiltInWidgets.kNumberBar)
      .withPosition(0, 1)
      .withSize(3, 1)
      .withProperties(Map.of("min", 0, "max", 300));

  stressTab.addNumber("Stress Score",
          () -> {
              double drivetrainCurrent =
                      stressMonitor.getDrivetrainCurrent(drivetrainPDHChannels);
              return stressMonitor.calculateStressScore(drivetrainCurrent);
          })
      .withWidget(BuiltInWidgets.kDial)
      .withPosition(3, 0)
      .withSize(2, 2)
      .withProperties(Map.of("min", 0, "max", 100));

  stressTab.addString("Stress Level",
          () -> {
              double drivetrainCurrent =
                      stressMonitor.getDrivetrainCurrent(drivetrainPDHChannels);
              double score =
                      stressMonitor.calculateStressScore(drivetrainCurrent);
              return stressMonitor.getStressLevel(score);
          })
      .withPosition(5, 0)
      .withSize(2, 1);
}


  public Command getAutonomousCommand() {
    return drivebase.getAutonomousCommand("AutoSimple");
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
