// package frc.robot.subsystems.ScoreSD.PreShooter;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import frc.robot.Constants;
// import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
// import frc.robot.subsystems.Sensors.ViewSubsystem;

// public class PreShooterManager extends SubsystemBase {

//     public enum State {
//         IDLE,
//         MANUAL_ARMED,
//         AUTO_FEEDING,
//         DISABLED
//     }

//     private State state = State.IDLE;

//     private final PreShooterSubsystem preShooter;
//     private final ViewSubsystem vision;
//     private final ShooterManager shooter;

//     public PreShooterManager(
//         PreShooterSubsystem preShooter,
//         ViewSubsystem vision,
//         ShooterManager shooter
//     ) {
//         this.preShooter = preShooter;
//         this.vision = vision;
//         this.shooter = shooter;
//     }

//     // ================= TELEOP =================

//     public void toggleMode() {
//         if (state == State.MANUAL_ARMED) {
//             state = State.IDLE;
//         } else {
//             state = State.MANUAL_ARMED;
//         }
//     }

//     public void toggleManualFeed() {
//         if (state == State.MANUAL_ARMED) {
//             state = State.IDLE;
//         } else {
//             state = State.MANUAL_ARMED;
//         }
//     }

//     // ================= AUTO =================

//     public void enableAuto() {
//         state = State.IDLE; 
//     }

//     public void stop() {
//         state = State.IDLE;
//         preShooter.stop();
//     }

//     public State getState() {
//         return state;
//     }

//     // ================= PERIODIC =================

//     @Override
//     public void periodic() {

//         if (state == State.DISABLED) {
//             preShooter.stop();
//             return;
//         }

//         // ===== MANUAL MODE =====
//         if (state == State.MANUAL_ARMED) {
//             preShooter.feed();
//             return;
//         }

//         // ===== AUTO MODE =====
//         boolean tagValid = vision.hasValidFrontTarget();
//         boolean aligned =
//             Math.abs(vision.getFrontTxRad())
//             < Math.toRadians(1.2);

//         double distance = vision.getDistanceToTag();

//         boolean validDistance =
//             distance != Double.MAX_VALUE &&
//             Math.abs(distance -
//             Constants.LimelightConstants.distance4Shoot)
//             < 0.20;

//         boolean shooterReady = shooter.isAtSpeed();

//         if (tagValid && aligned && shooterReady && validDistance) {
//             state = State.AUTO_FEEDING;
//         } else {
//             state = State.IDLE;
//         }

//         if (state == State.AUTO_FEEDING) {
//             preShooter.feed();
//         } else {
//             preShooter.stop();
//         }

//         SmartDashboard.putString("PreShooter/State", state.name());
//     }
// }
