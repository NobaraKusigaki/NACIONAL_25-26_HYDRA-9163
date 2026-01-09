package frc.robot.subsystems.Swervedrive;

import static edu.wpi.first.units.Units.Meter;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;

import frc.robot.Constants;
import frc.robot.Utils.DriveUtils.AntiTipController;
import frc.robot.Utils.DriveUtils.SlewLimiter;

import java.io.File;

import org.littletonrobotics.junction.Logger;

import swervelib.*;
import swervelib.parser.SwerveParser;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveSubsystem extends SubsystemBase {

  private static final Pose2d BLUE_START_POSE =
      new Pose2d(new Translation2d(Meter.of(1), Meter.of(4)), Rotation2d.fromDegrees(0));

  private static final Pose2d RED_START_POSE =
      new Pose2d(new Translation2d(Meter.of(16), Meter.of(4)), Rotation2d.fromDegrees(180));

  private final SwerveDrive swerveDrive;
  private final AntiTipController antiTip = new AntiTipController();

  private final SlewLimiter xLimiter = new SlewLimiter(3.0, Constants.LOOP_TIME);
  private final SlewLimiter yLimiter = new SlewLimiter(3.0, Constants.LOOP_TIME);

  private final StructArrayPublisher<SwerveModuleState> desiredPub;
  private final StructPublisher<Rotation2d> rotationPub;

  private final ProfiledPIDController headingPID =
      new ProfiledPIDController(
          2.0,
          0.0,
          0.2,
          new TrapezoidProfile.Constraints(
              Units.degreesToRadians(25),
              Units.degreesToRadians(60)));

  public SwerveSubsystem(File directory) {
    try {
      swerveDrive =
          new SwerveParser(directory).createSwerveDrive(Constants.MAX_SPEED, BLUE_START_POSE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    swerveDrive.setHeadingCorrection(false);
    swerveDrive.setCosineCompensator(false);
    swerveDrive.setAngularVelocityCompensation(false, false, 0.0);
    //swerveDrive.setModuleEncoderAutoSynchronize(true, 1);

    headingPID.enableContinuousInput(-Math.PI, Math.PI);

    setupPathPlanner();
    RobotModeTriggers.autonomous().onTrue(Commands.runOnce(this::zeroGyroWithAlliance));

    var nt = NetworkTableInstance.getDefault();
    desiredPub = nt.getStructArrayTopic("/Swerve/desired", SwerveModuleState.struct).publish();
    rotationPub = nt.getStructTopic("/Swerve/rotation", Rotation2d.struct).publish();
  }

  @Override
  public void periodic() {
    desiredPub.set(swerveDrive.getStates());
    rotationPub.set(swerveDrive.getPose().getRotation());

    Logger.recordOutput("swerve", swerveDrive.getPose());
    Logger.recordOutput("modules ", swerveDrive.getModulePositions());
  }

  public void drive(Translation2d translation, double rotation) {

    double x = xLimiter.calculate(translation.getX());
    double y = yLimiter.calculate(translation.getY());

    Translation2d limited = new Translation2d(x, y);

    double safety = antiTip.calculateSafetyFactor(getPitch(), getRoll());
    limited = limited.times(safety);
    rotation *= safety;

    swerveDrive.drive(limited, rotation, true, false);
  }

  public Command snapToAngleOnce(Rotation2d targetAngle) {
    return run(() -> {
          double rotOutput =
              headingPID.calculate(
                  getHeading().getRadians(),
                  targetAngle.getRadians());

          swerveDrive.drive(
              new Translation2d(0, 0),
              rotOutput,
              true,
              false);
        })
        .beforeStarting(() -> headingPID.reset(getHeading().getRadians()))
        .until(() -> Math.abs(getHeading().minus(targetAngle).getDegrees()) < 2.0);
  }

  public double getTotalRobotCurrent() {
    double sum = 0.0;

    for (var module : swerveDrive.getModules()) {
      Object drive = module.getDriveMotor().getMotor();
      Object angle = module.getAngleMotor().getMotor();

      if (RobotBase.isReal()) {
        if (drive instanceof SparkMax d) sum += d.getOutputCurrent();
        if (angle instanceof SparkMax a) sum += a.getOutputCurrent();
      }
    }
    return sum;
  }

  private void setupPathPlanner() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();

      AutoBuilder.configure(
          this::getPose,
          this::resetOdometry,
          this::getRobotVelocity,
          swerveDrive::setChassisSpeeds,
          new PPHolonomicDriveController(
              new PIDConstants(5.0, 0.0, 0.0),
              new PIDConstants(5.0, 0.0, 0.0)),
          config,
          () -> DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
              == DriverStation.Alliance.Red,
          this);

    } catch (Exception e) {
      e.printStackTrace();
    }

    PathfindingCommand.warmupCommand().schedule();
  }
  
  public Command driveFieldOriented(SwerveInputStream input) {
    return run(() -> swerveDrive.driveFieldOriented(input.get()));
  }

  public Command getAutonomousCommand(String pathName) {
    return new PathPlannerAuto(pathName);
  }

  public Pose2d getPose() {
    return swerveDrive.getPose();
  }

  public ChassisSpeeds getRobotVelocity() {
    return swerveDrive.getRobotVelocity();
  }

  public Rotation2d getHeading() {
    return getPose().getRotation();
  }

  public Rotation2d getPitch() {
    return swerveDrive.getPitch();
  }

  public Rotation2d getRoll() {
    return swerveDrive.getRoll();
  }

  public void resetOdometry(Pose2d pose) {
    swerveDrive.resetOdometry(pose);
  }

  public void zeroGyro() {
    swerveDrive.zeroGyro();
  }

  private void zeroGyroWithAlliance() {
    zeroGyro();
    resetOdometry(
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red
                ? RED_START_POSE
                : BLUE_START_POSE);
  }

  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }

public void setMotorBrake(boolean brake) {
  swerveDrive.setMotorIdleMode(brake);
}
}
