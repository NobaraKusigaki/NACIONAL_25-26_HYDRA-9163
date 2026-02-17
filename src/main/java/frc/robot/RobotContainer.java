// package frc.robot;

// import java.io.File;

// import com.pathplanner.lib.commands.PathPlannerAuto;

// import edu.wpi.first.math.MathUtil;
// import edu.wpi.first.math.geometry.Translation2d;
// import edu.wpi.first.networktables.NetworkTable;
// import edu.wpi.first.networktables.NetworkTableEntry;
// import edu.wpi.first.networktables.NetworkTableInstance;
// import edu.wpi.first.wpilibj.DriverStation;
// import edu.wpi.first.wpilibj.Filesystem;
// import edu.wpi.first.wpilibj2.command.*;
// import edu.wpi.first.wpilibj2.command.button.CommandPS5Controller;
// import edu.wpi.first.wpilibj2.command.button.Trigger;
// import frc.robot.Dashboards.RobotStress.DashboardPublisherStress;
// import frc.robot.Dashboards.RobotStress.RobotStressController;
// import frc.robot.Dashboards.RobotStress.RobotStressData;
// import frc.robot.Dashboards.RobotStress.RobotStressMonitor;
// import frc.robot.autos.NamedCommandsRegistry;
// import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleManager;
// import frc.robot.subsystems.ScoreSD.Angular.StreamDeckIntakeAngleController;
// import frc.robot.subsystems.ScoreSD.Climb.ClimbManager;
// import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;
// import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterSubsystem;
// import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
// import frc.robot.subsystems.ScoreSD.Shooter.ShooterSubsystem;
// import frc.robot.subsystems.Sensors.ViewSubsystem;
// import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
// import frc.robot.subsystems.ScoreSD.Spindexer.SpindexerSubsystem;
// import frc.robot.subsystems.ScoreSD.Spindexer.SpindexerManager;

// public class RobotContainer {

//   private final CommandPS5Controller driver = new CommandPS5Controller(0);

//   // ================= SUBSYSTEMS ATIVOS =================

//   private final SwerveSubsystem drivebase =
//       new SwerveSubsystem(
//           new File(Filesystem.getDeployDirectory(), "swerve/neo"));

//   private final ViewSubsystem vision = new ViewSubsystem(); 


//   private final ShooterSubsystem shooterSubsystem;
//   private final ShooterManager shooterManager;

//   private final PreShooterSubsystem preShooterSubsystem;
//   private final PreShooterManager preShooterManager;

//   private final SpindexerSubsystem spindexerSubsystem = new SpindexerSubsystem();
//   private final SpindexerManager spindexerManager = new SpindexerManager(spindexerSubsystem);

//   private final ClimbManager climbManager = new ClimbManager();


//   // ================= BOTÕES =================

//   Trigger povUp = driver.povUp();
//   Trigger longPress = povUp.debounce(1.0);

//   // ==================== STREAM DECK ==================== 

//   private final IntakeAngleManager intakeAngle = new IntakeAngleManager();
//   private final StreamDeckIntakeAngleController sdIntake =
//     new StreamDeckIntakeAngleController(intakeAngle);

  
//   // ================= ROBOT STRESS MONITORING =================
//   private final RobotStressMonitor stressMonitor =
//       new RobotStressMonitor();

//   private final RobotStressController stressController =
//       new RobotStressController();

//   private final DashboardPublisherStress dashboardPublisher =
//       new DashboardPublisherStress();

//   private final NetworkTable stressTable =
//       NetworkTableInstance.getDefault().getTable("RobotStress");

//   private final NetworkTableEntry stressSpeedScaleEntry =
//       stressTable.getEntry("speedScale");

//   private double driveSpeedScale = 1.0;


//   public RobotContainer() {

//     shooterSubsystem = new ShooterSubsystem();
//     shooterManager = new ShooterManager(shooterSubsystem);

//     preShooterSubsystem = new PreShooterSubsystem();

//     // Vision removido temporariamente → passar null
//     preShooterManager = new PreShooterManager(
//         preShooterSubsystem,
//         null,
//         shooterManager
//     );

//     NamedCommandsRegistry.registerAll(
//     drivebase,
//     vision,
//     shooterManager,
//     preShooterManager,
//     spindexerManager,
//     climbManager
// );

  
//     configureBindings();
//     configureDefaultCommands();
//     DriverStation.silenceJoystickConnectionWarning(true);
//   }

//   private void configureDefaultCommands() {

//     drivebase.setDefaultCommand(
//         Commands.run(() -> {

//           driveSpeedScale = MathUtil.clamp(
//               stressSpeedScaleEntry.getDouble(1.0),
//               0.3,
//               1.0
//           );

//           double speed = Constants.MAX_SPEED * driveSpeedScale;

//           drivebase.drive(
//               new Translation2d(
//                   driver.getLeftY() * speed,
//                   driver.getLeftX() * speed
//               ),
//               driver.getRightX() * speed
//           );

//         }, drivebase)
//     );
//   }

//   private void configureBindings() {

//     // ================= TOGGLE MODO (LONG PRESS) =================
//     longPress.onTrue(
//       Commands.runOnce(() -> preShooterManager.toggleMode())
//     );

//     // ================= TOGGLE FEED (SHORT PRESS) =================
//     driver.povDown().onTrue(
//       Commands.waitSeconds(0.5)
//           .unless(longPress)
//           .andThen(
//               Commands.runOnce(() -> preShooterManager.toggleManualFeed())
//           )
//     );

//     // ================= TOGGLE SHOOTER =================
//   //   driver.povUp().onTrue(
//   //     Commands.runOnce(() -> shooterManager.toggleShooter())
//   //   );
//   // 

// // ================= REFERÊNCIA SPINDEXER =================
// driver.triangle().onTrue(
//     Commands.runOnce(
//         () -> spindexerManager.saveReference(),
//         spindexerManager
//     )
// );

// // ================= VOLTAS =================
// driver.circle().onTrue(
//     Commands.runOnce(
//         () -> spindexerManager.spinThreeFromReference(),
//         spindexerManager
//     )
// );

//   }

//   public void updateDashboards() {

//     RobotStressData stressData =
//         stressMonitor.generateData(drivebase);

//     stressController.update(stressData);

//     double chassisSpeed =
//         Math.abs(drivebase.getRobotVelocity().vxMetersPerSecond);

//     dashboardPublisher.publish(
//         stressData,
//         driveSpeedScale,
//         chassisSpeed
//     );
//   }


//   public Command getAutonomousCommand() {
//     return new PathPlannerAuto("BlueAutoLeft");
// }


//   public void setMotorBrake(boolean brake) {
//     drivebase.setMotorBrake(brake);
//   }
  
// }
