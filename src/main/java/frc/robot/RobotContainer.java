package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
import edu.wpi.first.wpilibj2.command.button.Trigger;

// ================= REMOVIDOS TEMPORARIAMENTE =================
// import edu.wpi.first.math.MathUtil;
// import edu.wpi.first.math.geometry.Translation2d;
// import edu.wpi.first.networktables.*;
// import edu.wpi.first.wpilibj.Filesystem;
// import frc.robot.Dashboards.*;
// import frc.robot.autos.*;
// import frc.robot.commands.vision.*;
// import frc.robot.subsystems.Sensors.ViewSubsystem;
// import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
// import frc.robot.subsystems.ScoreSD.Angular.*;
// import java.io.File;

import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;
import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterSubsystem;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterSubsystem;

public class RobotContainer {

  private final CommandPS5Controller driver = new CommandPS5Controller(0);

  // ================= SUBSYSTEMS ATIVOS =================

  private final ShooterSubsystem shooterSubsystem;
  private final ShooterManager shooterManager;

  private final PreShooterSubsystem preShooterSubsystem;
  private final PreShooterManager preShooterManager;

  // ================= BOTÕES =================

  Trigger povUp = driver.povUp();
  Trigger longPress = povUp.debounce(1.0);

  public RobotContainer() {

    shooterSubsystem = new ShooterSubsystem();
    shooterManager = new ShooterManager(shooterSubsystem);

    preShooterSubsystem = new PreShooterSubsystem();

    // Vision removido temporariamente → passar null
    preShooterManager = new PreShooterManager(
        preShooterSubsystem,
        null,
        shooterManager
    );

    configureBindings();

    DriverStation.silenceJoystickConnectionWarning(true);

    
  }

  private void configureBindings() {

    // ================= TOGGLE MODO (LONG PRESS) =================
    longPress.onTrue(
      Commands.runOnce(() -> preShooterManager.toggleMode())
    );

    // ================= TOGGLE FEED (SHORT PRESS) =================
    driver.povDown().onTrue(
      Commands.waitSeconds(0.5)
          .unless(longPress)
          .andThen(
              Commands.runOnce(() -> preShooterManager.toggleManualFeed())
          )
    );

    // ================= TOGGLE SHOOTER =================
    driver.povUp().onTrue(
      Commands.runOnce(() -> shooterManager.toggleShooter())
    );
  }

  // ================= DESATIVADOS =================

  public Command getAutonomousCommand() {
    return null;
  }

  public void setMotorBrake(boolean brake) {
    // desativado temporariamente
  }

  public void updateDashboards() {
    // desativado temporariamente
  }

  
}
