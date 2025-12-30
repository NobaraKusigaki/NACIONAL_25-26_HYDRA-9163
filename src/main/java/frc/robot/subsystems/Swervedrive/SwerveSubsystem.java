package frc.robot.subsystems.Swervedrive;

import static edu.wpi.first.units.Units.Meter;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import frc.robot.Constants;
import frc.robot.utils.DriveUtils.AntiTipController;
import frc.robot.utils.DriveUtils.SlewLimiter;

import java.io.File;
import swervelib.SwerveController;
import swervelib.SwerveDrive;
import swervelib.parser.SwerveControllerConfiguration;
import swervelib.parser.SwerveDriveConfiguration;
import swervelib.parser.SwerveParser;
import org.littletonrobotics.junction.Logger;

public class SwerveSubsystem extends SubsystemBase {

  private final SwerveDrive swerveDrive;
  private final AntiTipController antiTip = new AntiTipController();

  private final SlewLimiter xLimiter = new SlewLimiter(3.5, Constants.LOOP_TIME);
  private final SlewLimiter yLimiter = new SlewLimiter(3.5, Constants.LOOP_TIME);
  private final SlewLimiter rotLimiter = new SlewLimiter(6.0, Constants.LOOP_TIME);

  private static final Pose2d BLUE_START_POSE =
      new Pose2d(new Translation2d(Meter.of(1), Meter.of(4)), Rotation2d.fromDegrees(0));

  private static final Pose2d RED_START_POSE =
      new Pose2d(new Translation2d(Meter.of(16), Meter.of(4)), Rotation2d.fromDegrees(180));

  public SwerveSubsystem(File directory) {
    try {
      swerveDrive =
          new SwerveParser(directory).createSwerveDrive(Constants.MAX_SPEED, BLUE_START_POSE);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    swerveDrive.setHeadingCorrection(false);
    swerveDrive.setCosineCompensator(false);
    swerveDrive.setAngularVelocityCompensation(true, true, 0.1);
    swerveDrive.setModuleEncoderAutoSynchronize(true, 1);

    setupPathPlanner();
    RobotModeTriggers.autonomous().onTrue(Commands.runOnce(this::zeroGyroWithAlliance));
  }

  public SwerveSubsystem(
      SwerveDriveConfiguration driveCfg, SwerveControllerConfiguration controllerCfg) {
    swerveDrive =
        new SwerveDrive(
            driveCfg,
            controllerCfg,
            Constants.MAX_SPEED,
            new Pose2d(new Translation2d(Meter.of(2), Meter.of(0)), Rotation2d.fromDegrees(0)));
  }

  @Override
  public void periodic() {
    super.periodic();
  
    Pose2d pose = swerveDrive.getPose();
  
    Logger.recordOutput("Swerve/Pose", pose);
    Logger.recordOutput("Field/Robot", pose);
  
    Logger.recordOutput("Swerve/ChassisSpeeds", swerveDrive.getRobotVelocity());
    Logger.recordOutput("Swerve/FieldSpeeds", swerveDrive.getFieldVelocity());

    Logger.recordOutput("Swerve/Gyro/YawDeg", getHeading().getDegrees());
    Logger.recordOutput("Swerve/Gyro/PitchDeg", getPitch().getDegrees());
    Logger.recordOutput("Swerve/Gyro/RollDeg", getRoll().getDegrees());
  
    var modules = swerveDrive.getModules();
    for (int i = 0; i < modules.length; i++) {
      Logger.recordOutput(
          "Swerve/Modules/" + i + "/AngleDeg",
          modules[i].getState().angle.getDegrees());
  
      Logger.recordOutput(
          "Swerve/Modules/" + i + "/SpeedMPS",
          modules[i].getState().speedMetersPerSecond);
    }
  }

  public void drive(Translation2d translation, double rotation, boolean fieldRelative) {

    double x = xLimiter.calculate(translation.getX());
    double y = yLimiter.calculate(translation.getY());
    double rot = rotLimiter.calculate(rotation);

    Translation2d limited = new Translation2d(x, y);

    double safety = antiTip.calculateSafetyFactor(getPitch(), getRoll());
    limited = new Translation2d(limited.getX() * safety, limited.getY() * safety);
    rot *= safety;

    swerveDrive.drive(limited, rot, fieldRelative, false);
  }

  public void driveFieldOriented(ChassisSpeeds velocity) {
    swerveDrive.driveFieldOriented(velocity);
  }

  public Command driveFieldOriented(java.util.function.Supplier<ChassisSpeeds> velocity) {
    return run(() -> swerveDrive.driveFieldOriented(velocity.get()));
  }

  public void drive(ChassisSpeeds velocity) {
    swerveDrive.drive(velocity);
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
              new PIDConstants(5.0, 0.0, 0.0), new PIDConstants(5.0, 0.0, 0.0)),
          config,
          () ->
              DriverStation.getAlliance().isPresent()
                  && DriverStation.getAlliance().get() == DriverStation.Alliance.Red,
          this);

    } catch (Exception e) {
      e.printStackTrace();
    }

    PathfindingCommand.warmupCommand().schedule();
  }

  public Command getAutonomousCommand(String pathName) {
    return new PathPlannerAuto(pathName);
  }

  public Command driveToPose(Pose2d pose) {
    PathConstraints constraints =
        new PathConstraints(
            swerveDrive.getMaximumChassisVelocity(),
            4.0,
            swerveDrive.getMaximumChassisAngularVelocity(),
            Units.degreesToRadians(720));

    return AutoBuilder.pathfindToPose(
        pose, constraints, edu.wpi.first.units.Units.MetersPerSecond.of(0));
  }


  public void resetOdometry(Pose2d pose) {
    swerveDrive.resetOdometry(pose);
  }

  public Pose2d getPose() {
    return swerveDrive.getPose();
  }

  public ChassisSpeeds getRobotVelocity() {
    return swerveDrive.getRobotVelocity();
  }

  public ChassisSpeeds getFieldVelocity() {
    return swerveDrive.getFieldVelocity();
  }

  public Rotation2d getHeading() {
    return getPose().getRotation();
  }

  public void zeroGyro() {
    swerveDrive.zeroGyro();
  }

  private Pose2d getStartingPoseForAlliance() {
    var alliance = DriverStation.getAlliance();
    if (alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red) {
      return RED_START_POSE;
    }
    return BLUE_START_POSE;
  }

  public void zeroGyroWithAlliance() {
    zeroGyro();
    resetOdometry(getStartingPoseForAlliance());
  }

  public void setMotorBrake(boolean brake) {
    swerveDrive.setMotorIdleMode(brake);
  }

  public void lock() {
    swerveDrive.lockPose();
  }

  public Rotation2d getPitch() {
    return swerveDrive.getPitch();
  }

  public Rotation2d getRoll() {
    return swerveDrive.getRoll();
  }

  public void replaceSwerveModuleFeedforward(double kS, double kV, double kA) {
    swerveDrive.replaceSwerveModuleFeedforward(new SimpleMotorFeedforward(kS, kV, kA));
  }

  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }

  public SwerveDriveKinematics getKinematics() {
    return swerveDrive.kinematics;
  }

  public SwerveController getSwerveController() {
    return swerveDrive.swerveController;
  }

  public SwerveDriveConfiguration getSwerveDriveConfiguration() {
    return swerveDrive.swerveDriveConfiguration;
  }
}
