package frc.robot;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;

import frc.robot.Dashboards.RobotStress.DashboardPublisher;
import frc.robot.Dashboards.RobotStress.RobotStressController;
import frc.robot.Dashboards.RobotStress.RobotStressData;
import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

public class RobotContainer {

  private final CommandPS5Controller controller = new CommandPS5Controller(0);

  private final SwerveSubsystem drivebase = new SwerveSubsystem(
    new File(Filesystem.getDeployDirectory(), "swerve/neo"));

  private final RobotStressMonitor stressMonitor = new RobotStressMonitor();
  private final RobotStressController stressController = new RobotStressController();
  private final DashboardPublisher dashboardPublisher = new DashboardPublisher();

  private final NetworkTable stressTable =
      NetworkTableInstance.getDefault().getTable("RobotStress");

  private final NetworkTableEntry stressSpeedScaleEntry =
      stressTable.getEntry("speedScale");

  private double driveSpeedScale = 1.0;

  public RobotContainer() {
    configureBindings();
    configureDefaultCommands();
    DriverStation.silenceJoystickConnectionWarning(true);
  }

  private void configureDefaultCommands() {
    drivebase.setDefaultCommand(
        Commands.run(() -> {

          driveSpeedScale = MathUtil.clamp(
              stressSpeedScaleEntry.getDouble(1.0),
              0.3,   // limite mínimo de segurança
              1.0    // limite máximo
          );

          double speed = Constants.MAX_SPEED * driveSpeedScale;

          drivebase.drive(
              new Translation2d(
                  -controller.getLeftY() * speed,
                  -controller.getLeftX() * speed
              ),
              controller.getRightX() * speed
          );

        }, drivebase)
    );
  }

  private void configureBindings() {

    //#ANGULAR DRIVE COMMANDS

    // controller.triangle().onTrue(
    //     drivebase.snapToAngleOnce(Rotation2d.fromDegrees(0)));

    // controller.circle().onTrue(
    //     drivebase.snapToAngleOnce(Rotation2d.fromDegrees(90)));

    // controller.cross().onTrue(
    //     drivebase.snapToAngleOnce(Rotation2d.fromDegrees(180)));

    // controller.square().onTrue(
    //     drivebase.snapToAngleOnce(Rotation2d.fromDegrees(270)));

    //#STREAM DECK SYSTEMS COMMANDS

    // MODOS DAS LIMELIGHTS

    controller.square().onTrue(
        drivebase.toggleAimLockLime4Command()
    );

    controller.circle().onTrue(
        drivebase.toggleAimLockLime2Command()
    );

    controller.triangle().onTrue(
        Commands.runOnce(
            () -> drivebase.beginAlignLime2(
                () -> controller.triangle().getAsBoolean()),
            drivebase
        )
    );

    controller.triangle().onFalse(
        Commands.runOnce(drivebase::endAlignLime2, drivebase)
    );
  }

  public void updateDashboards() {

    RobotStressData stressData =
        stressMonitor.generateData(drivebase);

    stressController.update(stressData);

    double chassisSpeed =
        Math.abs(drivebase.getRobotVelocity().vxMetersPerSecond);

    dashboardPublisher.publish(
        stressData,
        driveSpeedScale,
        chassisSpeed
    );
  }

  public Command getAutonomousCommand() {
    return drivebase.getAutonomousCommand("AutoSimple");
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}
