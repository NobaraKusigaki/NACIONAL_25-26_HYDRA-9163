package frc.robot.subsystems.Swervedrive;

import static edu.wpi.first.units.Units.Meter;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.math.MathUtil;
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
import java.util.function.BooleanSupplier;

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

  private final StructArrayPublisher<SwerveModuleState> desiredPub;
  private final StructPublisher<Rotation2d> rotationPub;

  // =========================================================
  // PID DE HEADING
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
  // LIMELIGHT 4
  // =========================================================
  private final NetworkTable limelight =
      NetworkTableInstance.getDefault().getTable("limelight-front");

  private double getLimelightTxRadians() {
    double txDeg = limelight.getEntry("tx").getDouble(0.0);
    return Units.degreesToRadians(txDeg);
  }

  // ================= LIMELIGHT 2 (NT3 - IA) =================
  private final NetworkTable limelight2 =
      NetworkTableInstance.getDefault().getTable("limelight-back");

  private double getLime2PieceTxRadians() {
    double txDeg = limelight2.getEntry("piece_tx").getDouble(0.0);
    return Units.degreesToRadians(txDeg);
  }


  // =========================================================
  // MODOS (HTML / CONTROLE)
  // =========================================================
  public enum AimLockMode {
    OFF(0),
    TAG(1);

    public final int code;
    AimLockMode(int code) { this.code = code; }
  }

  public enum AlignMode {
    OFF(0),
    ON(1),
    AUTO(2);

    public final int code;
    AlignMode(int code) { this.code = code; }
  }

  // ---------------- ESTADOS ----------------
  private AimLockMode aimLockLime4 = AimLockMode.OFF;
  private AimLockMode aimLockLime2 = AimLockMode.OFF;

  private AlignMode alignLime2 = AlignMode.OFF;
  private AlignMode alignLime2Persist = AlignMode.OFF;
  private boolean alignLime2HoldActive = false;
  private Command alignLime2HoldCmd = null;
  private static final double HOLD_TO_AUTO_S = 0.35;

  // ---------------- PUBLISHERS ----------------
  private final IntegerPublisher aimLockLime4Pub;
  private final IntegerPublisher aimLockLime2Pub;
  private final IntegerPublisher alignLime2Pub;

  private static final String TOPIC_AIMLOCK_LIME4 = "/Modes/AimLockLime4";
  private static final String TOPIC_AIMLOCK_LIME2 = "/Modes/AimLockLime2";
  private static final String TOPIC_ALIGN_LIME2   = "/Modes/AlignLime2";

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
    swerveDrive.setAngularVelocityCompensation(false, false, 0.0);

    headingPID.enableContinuousInput(-Math.PI, Math.PI);

    setupPathPlanner();
    RobotModeTriggers.autonomous().onTrue(Commands.runOnce(this::zeroGyroWithAlliance));

    var nt = NetworkTableInstance.getDefault();
    desiredPub = nt.getStructArrayTopic("/Swerve/desired", SwerveModuleState.struct).publish();
    rotationPub = nt.getStructTopic("/Swerve/rotation", Rotation2d.struct).publish();

    aimLockLime4Pub = nt.getIntegerTopic(TOPIC_AIMLOCK_LIME4).publish();
    aimLockLime2Pub = nt.getIntegerTopic(TOPIC_AIMLOCK_LIME2).publish();
    alignLime2Pub   = nt.getIntegerTopic(TOPIC_ALIGN_LIME2).publish();

    aimLockLime4Pub.set(aimLockLime4.code);
    aimLockLime2Pub.set(aimLockLime2.code);
    alignLime2Pub.set(alignLime2.code);
  }

  // =========================================================
  // PERIODIC
  // =========================================================
  @Override
  public void periodic() {
    desiredPub.set(swerveDrive.getStates());
    rotationPub.set(swerveDrive.getPose().getRotation());

    Logger.recordOutput("swerve", swerveDrive.getPose());
    Logger.recordOutput("modules", swerveDrive.getModulePositions());

    aimLockLime4Pub.set(aimLockLime4.code);
    aimLockLime2Pub.set(aimLockLime2.code);
    alignLime2Pub.set(alignLime2.code);
  }

  public void drive(Translation2d translation, double rotation) {

    double x = xLimiter.calculate(translation.getX());
    double y = yLimiter.calculate(translation.getY());
  
    if (Math.abs(x) < 0.05) x = 0.0;
    if (Math.abs(y) < 0.05) y = 0.0;

  // --------------------------------------
  // ALIGN LIMELIGHT 2 - AUTO (FOLLOW BALL)
  // --------------------------------------
  if (alignLime2 == AlignMode.AUTO) {

    boolean hasTarget = limelight2.getEntry("has_target").getBoolean(false);

    if (hasTarget) {

      // ---------- ROTATION ----------
      double yawError = -getLime2PieceTxRadians();
      double rot = headingPID.calculate(0.0, yawError);

      // ---------- FORWARD (X) ----------
      double ta = limelight2.getEntry("ta").getDouble(0.0);

      // quanto maior ta, mais perto
      double forward =
          Constants.K_AUTO_PIECE_FORWARD * (Constants.TA_TARGET - ta);

      forward = MathUtil.clamp(
          forward,
          -Constants.MAX_SPEED,
          Constants.MAX_SPEED
      );

    // Aplica controle automático
    Translation2d autoTranslation = new Translation2d(forward, 0.0);

    double safety = antiTip.calculateSafetyFactor(getPitch(), getRoll());
    autoTranslation = autoTranslation.times(safety);
    rot *= safety;

    swerveDrive.drive(autoTranslation, rot, false, false);
    return; 
  } else {
    swerveDrive.drive(new Translation2d(0, 0), 0.0, false, false);
    return;
  }
}

  
    Translation2d limited = new Translation2d(x, y);
  
    // -------------------------------
    // AIM LOCK 4 (TAG)
    // -------------------------------
    boolean rotationOverridden = false;
  
    if (aimLockLime4 == AimLockMode.TAG) {
  
      boolean hasTarget = limelight.getEntry("tv").getDouble(0) == 1;
  
      if (hasTarget) {
        double yawError = getLimelightTxRadians(); // sinal corrigido
        rotation = headingPID.calculate(0.0, yawError);
        rotationOverridden = true;
      }
  
    // -------------------------------
    // AIM LOCK 2+ (IA)
    // -------------------------------

    } else if (aimLockLime2 == AimLockMode.TAG) {
  
      boolean hasTarget = limelight2.getEntry("has_target").getBoolean(false);
  
      if (hasTarget) {
        double yawError = -getLime2PieceTxRadians();
        rotation = headingPID.calculate(0.0, yawError);
        rotationOverridden = true;
      }
    }
  
    
    double safety = antiTip.calculateSafetyFactor(getPitch(), getRoll());
    limited = limited.times(safety);
  
    if (rotationOverridden) {
      rotation *= safety;
    }
    
    swerveDrive.drive(limited, rotation, false, false);
  }
  
  public AimLockMode getAimLockLime4() { return aimLockLime4; }

  public void setAimLockLime4(AimLockMode mode) {
    aimLockLime4 = mode;

    if (mode == AimLockMode.TAG) {
      headingPID.reset(0.0);
    }

    aimLockLime4Pub.set(aimLockLime4.code);
  }

  public void toggleAimLockLime4() {
    setAimLockLime4(
        aimLockLime4 == AimLockMode.OFF ? AimLockMode.TAG : AimLockMode.OFF);
  }

  public Command toggleAimLockLime4Command() {
    return Commands.runOnce(this::toggleAimLockLime4, this);
  }

  public AimLockMode getAimLockLime2() { return aimLockLime2; }
  
  public void setAimLockLime2(AimLockMode mode) {
    aimLockLime2 = mode;

    if (mode == AimLockMode.TAG) {
      headingPID.reset(0.0);
    }

    aimLockLime2Pub.set(aimLockLime2.code);
  }
  public void toggleAimLockLime2() {
    setAimLockLime2(aimLockLime2 == AimLockMode.OFF ? AimLockMode.TAG : AimLockMode.OFF);
  }
  public Command toggleAimLockLime2Command() {
    return Commands.runOnce(this::toggleAimLockLime2, this);
  }

  public AlignMode getAlignLime2() { return alignLime2; }
  public void setAlignLime2(AlignMode mode) {
    alignLime2 = mode;
    alignLime2Pub.set(alignLime2.code);
  }

  private void setAlignLime2Persist(AlignMode mode) {
    alignLime2Persist = (mode == AlignMode.AUTO) ? AlignMode.ON : mode;
  }

  public void toggleAlignLime2OnOff() {
    AlignMode next = (alignLime2Persist == AlignMode.ON) ? AlignMode.OFF : AlignMode.ON;
    setAlignLime2Persist(next);
    if (!alignLime2HoldActive) setAlignLime2(next);
  }

  public void beginAlignLime2(BooleanSupplier stillPressed) {
    alignLime2HoldActive = false;

    if (alignLime2HoldCmd != null) {
      alignLime2HoldCmd.cancel();
      alignLime2HoldCmd = null;
    }

    alignLime2HoldCmd =
        Commands.waitSeconds(HOLD_TO_AUTO_S)
            .andThen(
                Commands.runOnce(
                    () -> {
                      if (stillPressed.getAsBoolean()) {
                        alignLime2HoldActive = true;
                        setAlignLime2(AlignMode.AUTO);
                      }
                    }));

    alignLime2HoldCmd.schedule();
  }

  public void endAlignLime2() {
    if (alignLime2HoldCmd != null) {
      alignLime2HoldCmd.cancel();
      alignLime2HoldCmd = null;
    }

    if (alignLime2HoldActive) {
      alignLime2HoldActive = false;
      setAlignLime2(alignLime2Persist);
    } else {
      toggleAlignLime2OnOff();
    }
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

  public Pose2d getPose() { return swerveDrive.getPose(); }
  public ChassisSpeeds getRobotVelocity() { return swerveDrive.getRobotVelocity(); }
  public Rotation2d getHeading() { return getPose().getRotation(); }
  public Rotation2d getPitch() { return swerveDrive.getPitch(); }
  public Rotation2d getRoll() { return swerveDrive.getRoll(); }

  public void resetOdometry(Pose2d pose) { swerveDrive.resetOdometry(pose); }
  public void zeroGyro() { swerveDrive.zeroGyro(); }

  private void zeroGyroWithAlliance() {
    zeroGyro();
    resetOdometry(
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red
                ? RED_START_POSE
                : BLUE_START_POSE);
  }

  public SwerveDrive getSwerveDrive() { return swerveDrive; }
  public void setMotorBrake(boolean brake) { swerveDrive.setMotorIdleMode(brake); }
}
