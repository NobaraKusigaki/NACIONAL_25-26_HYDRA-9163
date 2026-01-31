package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;

import frc.robot.Dashboards.RobotStress.DashboardPublisherStress;
import frc.robot.Dashboards.RobotStress.RobotStressController;
import frc.robot.Dashboards.RobotStress.RobotStressData;
import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleManager;
import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleSDInput;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;

import java.io.File;

public class RobotContainer {

  private final CommandPS5Controller driver = new CommandPS5Controller(0);

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(
          new File(Filesystem.getDeployDirectory(), "swerve/neo"));

// ==================== STREAM DECK ==================== 

 private final IntakeAngleManager intakeAngle =
 new IntakeAngleManager();

private final IntakeAngleSDInput intakeAngleSD =
 new IntakeAngleSDInput(intakeAngle);
  
  // ================= ROBOT STRESS MONITORING =================
  private final RobotStressMonitor stressMonitor =
      new RobotStressMonitor();

  private final RobotStressController stressController =
      new RobotStressController();

  private final DashboardPublisherStress dashboardPublisher =
      new DashboardPublisherStress();

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
              0.3,
              1.0
          );

          double speed = Constants.MAX_SPEED * driveSpeedScale;

          drivebase.drive(
              new Translation2d(
                  driver.getLeftY() * speed,
                  -driver.getLeftX() * speed
              ),
              driver.getRightX() * speed
          );

        }, drivebase)
    );
  }

  private void configureBindings() {


    // driver.square().onTrue(
    //     drivebase.toggleAimLockLime4Command()
    // );

    // driver.circle().onTrue(
    //     drivebase.toggleAimLockLime2Command()
    // );

    // driver.triangle().onTrue(
    //     Commands.runOnce(
    //         () -> drivebase.beginAlignLime2(
    //             () -> driver.triangle().getAsBoolean()),
    //         drivebase
    //     )
    // );

    // driver.triangle().onFalse(
    //     Commands.runOnce(
    //         drivebase::endAlignLime2,
    //         drivebase
    //     )
    // );

    driver.cross().onTrue(
        Commands.runOnce(intakeAngle::calibrateZero, intakeAngle)
    );
    
    driver.circle().onTrue(
        Commands.runOnce(intakeAngle::calibrateTarget, intakeAngle)
    );
    
      new Trigger(driver.L2())
      .whileTrue(
          Commands.run(intakeAngle::manualUp, intakeAngle)
      )
      .onFalse(
          Commands.runOnce(intakeAngle::stop, intakeAngle)
      );
  
  new Trigger(driver.R2())
      .whileTrue(
          Commands.run(intakeAngle::manualDown, intakeAngle)
      )
      .onFalse(
          Commands.runOnce(intakeAngle::stop, intakeAngle)
      );
  
      driver.square().onTrue(
      Commands.runOnce(intakeAngle::togglePosition, intakeAngle)
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
