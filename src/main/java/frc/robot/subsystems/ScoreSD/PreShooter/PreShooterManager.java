// package frc.robot.subsystems.ScoreSD.PreShooter;

// import edu.wpi.first.math.MathUtil;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import frc.robot.Constants;
// import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
// import frc.robot.subsystems.Sensors.ViewSubsystem;

// public class PreShooterManager extends SubsystemBase {

//     public enum State {
//         IDLE,
//         AUTO_FEEDING,
//         MANUAL_FEED,
//         DISABLED
//     }

//     private State state = State.IDLE;

//     private final PreShooterSubsystem preShooter;
//     private final ViewSubsystem vision;
//     private final ShooterManager shooter;

//     private int lastTag = -1;

//     public PreShooterManager(
//         PreShooterSubsystem preShooter,
//         ViewSubsystem vision,
//         ShooterManager shooter
//     ) {
//         this.preShooter = preShooter;
//         this.vision = vision;
//         this.shooter = shooter;
//     }

//     public void enableAuto() {
//         state = State.IDLE;
//     }

//     public void manualFeed() {
//         state = State.MANUAL_FEED;
//     }

//     public void stop() {
//         state = State.IDLE;
//     }

//     @Override
//     public void periodic() {

//         if (state == State.DISABLED) {
//             preShooter.stop();
//             return;
//         }

//         int detected = vision.getDetectedTagId();
//         if (detected != -1)
//             lastTag = detected;

//         // boolean tagValid =
//         //     vision.getFrontAllowedTags().contains(lastTag);

//         boolean aligned =
//             Math.abs(vision.getFrontTxRad())
//             < Math.toRadians(1.2);

//         double distance = vision.getDistanceToTag();

//         boolean validDistance =
//             distance > 0.1 &&
//             Math.abs(distance - Constants.LimelightConstants.distance4Shoot)
//             < 0.20;

//         boolean shooterReady = shooter.isAtSpeed();

//         if (state == State.MANUAL_FEED) {
//             preShooter.feed();
//             return;
//         }

//         // if (tagValid && aligned && shooterReady && validDistance) {
//         //     state = State.AUTO_FEEDING;
//         } else {
//             state = State.IDLE;
//         }

//         switch (state) {
//             case AUTO_FEEDING:
//                 preShooter.feed();
//                 break;
//             default:
//                 preShooter.stop();
//                 break;
//         }

//         SmartDashboard.putString("PreShooter/State", state.name());
//     }
// }
