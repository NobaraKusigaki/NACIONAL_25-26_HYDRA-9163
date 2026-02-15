package frc.robot.subsystems.Swervedrive;

import static edu.wpi.first.units.Units.Meter;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.util.DriveFeedforwards;
import com.pathplanner.lib.util.swerve.SwerveSetpoint;
import com.pathplanner.lib.util.swerve.SwerveSetpointGenerator;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.*;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj2.command.*;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;

import frc.robot.Constants;
import frc.robot.Utils.DriveUtils.AntiTipController;
import frc.robot.Utils.DriveUtils.SlewLimiter;
import frc.robot.autonomous.Poses.FieldPoses;
import frc.robot.commands.drive.PathfindToPose; 

import java.io.File;
import java.util.Set;

import org.littletonrobotics.junction.Logger;

import swervelib.*;
import swervelib.parser.SwerveParser;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveSubsystem extends SubsystemBase {

  // =========================================================
  // POSES INICIAIS
  // =========================================================
  private static final Pose2d BLUE_START_POSE =
      new Pose2d(new Translation2d(Meter.of(1), Meter.of(4)), Rotation2d.fromDegrees(0));

  private static final Pose2d RED_START_POSE =
      new Pose2d(new Translation2d(Meter.of(16), Meter.of(4)), Rotation2d.fromDegrees(180));

  // =========================================================
  // SWERVE
  // =========================================================
  private final SwerveDrive swerveDrive;
  private final AntiTipController antiTip = new AntiTipController();

  private final SlewLimiter xLimiter = new SlewLimiter(3.0, Constants.LOOP_TIME);
  private final SlewLimiter yLimiter = new SlewLimiter(3.0, Constants.LOOP_TIME);
  private final SlewLimiter rotLimiter = new SlewLimiter(6.0, Constants.LOOP_TIME);

  private SwerveSetpointGenerator setpointGenerator;
  private SwerveSetpoint lastSetpoint;
  private double lastSetpointTime;
  
  private Translation2d lastTranslation = new Translation2d();


  private final StructArrayPublisher<SwerveModuleState> desiredPub;
  private final StructPublisher<Rotation2d> rotationPub;

  // =========================================================
  // PID (USADO POR COMMANDS)
  // =========================================================
  private final ProfiledPIDController headingPID =
      new ProfiledPIDController(
          2.0,
          0.0,
          0.2,
          new TrapezoidProfile.Constraints(
              Units.degreesToRadians(60),
              Units.degreesToRadians(100)));

  // =========================================================
  // CONSTRUTOR
  // =========================================================
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

    headingPID.enableContinuousInput(-Math.PI, Math.PI);

    try {
      setpointGenerator =
          new SwerveSetpointGenerator(
              RobotConfig.fromGUISettings(),
              swerveDrive.getMaximumChassisAngularVelocity());

      lastSetpoint =
          new SwerveSetpoint(
              new ChassisSpeeds(),
              swerveDrive.getStates(),
              DriveFeedforwards.zeros(swerveDrive.getModules().length));

      lastSetpointTime = Timer.getFPGATimestamp();
    } catch (Exception e) {
      DriverStation.reportError("Erro SetpointGenerator", e.getStackTrace());
    }

    setupPathPlanner();
    RobotModeTriggers.autonomous().onTrue(Commands.runOnce(this::zeroGyroWithAlliance));

    var nt = NetworkTableInstance.getDefault();
    desiredPub = nt.getStructArrayTopic("/Swerve/desired", SwerveModuleState.struct).publish();
    rotationPub = nt.getStructTopic("/Swerve/rotation", Rotation2d.struct).publish();
  }

  // =========================================================
  // PERIODIC
  // =========================================================
  @Override
  public void periodic() {
    desiredPub.set(swerveDrive.getStates());
    rotationPub.set(swerveDrive.getPose().getRotation());

    Logger.recordOutput("swerve/pose", swerveDrive.getPose());
    Logger.recordOutput("swerve/modules", swerveDrive.getModulePositions());
  }

  // =========================================================
  // DRIVE BASICO 
  // =========================================================
  public void drive(Translation2d translation, double rotation) {

    lastTranslation = translation;
  
    double x = xLimiter.calculate(translation.getX());
    double y = yLimiter.calculate(translation.getY());
    double rot = rotLimiter.calculate(rotation);
  
    if (Math.abs(x) < 0.05) x = 0.0;
    if (Math.abs(y) < 0.05) y = 0.0;
    if (Math.abs(rot) < 0.05) rot = 0.0;
  
    Translation2d limited = new Translation2d(x, y);
  
    double safety = antiTip.calculateSafetyFactor(getPitch(), getRoll());
    limited = limited.times(safety);
    rot *= safety;
  
    executeSetpoint(limited, rot);
  }
  

  // =========================================================
  // SETPOINT 
  // =========================================================
  public void executeSetpoint(Translation2d translation, double rotation) {

    ChassisSpeeds target =
        ChassisSpeeds.fromFieldRelativeSpeeds(
            translation.getX(),
            translation.getY(),
            rotation,
            getHeading());

    double now = Timer.getFPGATimestamp();
    double dt = now - lastSetpointTime;

    SwerveSetpoint next =
        setpointGenerator.generateSetpoint(lastSetpoint, target, dt);

    swerveDrive.drive(
        next.robotRelativeSpeeds(),
        next.moduleStates(),
        next.feedforwards().linearForces());

    lastSetpoint = next;
    lastSetpointTime = now;
  }

  // =========================================================
  // PATHPLANNER
  // =========================================================
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

  public Command getAutonomousCommand(String name) {
    return new PathPlannerAuto(name);
  }

  // =========================================================
  // GETTERS
  // =========================================================
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

  public ProfiledPIDController getHeadingPID() { 
    return headingPID; 
  }

  public Translation2d getLastTranslation() {
    return lastTranslation;
  }
  
  public void stop() {
    drive(new Translation2d(), 0.0);
  }

  // =========================================================
  // ODOMETRIA / GYRO
  // =========================================================
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

  // =========================================================
  // UTIL
  // =========================================================
  public void setMotorBrake(boolean brake) {
    swerveDrive.setMotorIdleMode(brake);
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


  // ================= HUB =================

private Pose2d getClosestHubPose() {

  Pose2d current = getPose();

  Pose2d[] options = {
          FieldPoses.BLUE_HUB_LEFT,
          FieldPoses.BLUE_HUB_CENTER,
          FieldPoses.BLUE_HUB_RIGHT
  };

  Pose2d closest = options[0];
  double minDistance = current.getTranslation()
          .getDistance(options[0].getTranslation());

  for (Pose2d pose : options) {

      double distance = current.getTranslation()
              .getDistance(pose.getTranslation());

      if (distance < minDistance) {
          minDistance = distance;
          closest = pose;
      }
  }

  return closest;
}

public Command goToBestHubShot() {
  return new PathfindToPose(this, getClosestHubPose());
}

public PathConstraints getPathConstraints() {
  return new PathConstraints(
          3.0, 
          3.0,  
          Math.PI, 
          Math.PI * 2  
  );
}

}