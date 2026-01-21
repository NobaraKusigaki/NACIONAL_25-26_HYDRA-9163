package frc.robot;

import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import swervelib.math.Matter;

public final class Constants {

  public static final double ROBOT_MASS = 51.25 * 0.453592;
  public static final Matter CHASSIS = new Matter(new Translation3d(0, 0, Units.inchesToMeters(8)), ROBOT_MASS);
  public static final double LOOP_TIME = 0.13;
  public static final double MAX_SPEED = Units.feetToMeters(12);
    public static final double K_AUTO_PIECE_FORWARD = -0.1;
  public static final double TA_TARGET = 5;

  public static final class DrivebaseConstants {
    public static final double WHEEL_LOCK_TIME = 10;
  }

  public static class OperatorConstants {
    public static final double DEADBAND = 0.05;
    public static final double LEFT_Y_DEADBAND = 0.05;
    public static final double RIGHT_X_DEADBAND = 0.05;
    public static final double TURN_CONSTANT = 2;
  }

  public static class IntakeConstants {
    public static final int INTAKE_LEADER_ID = 0;
    public static final int INTAKE_FOLLOWER_ID = 0;
  }

  public static class PreShooterConstants{

    public static final int LEADER_ID = 0;
    public static final int FOLLOWER_ID = 0;
    
  }
  public static class ADLManager{
    public static final double MIN_DECISION_INTERVAL = 0.15;
  }
  

}
