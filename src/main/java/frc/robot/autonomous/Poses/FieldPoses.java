package frc.robot.autonomous.Poses;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public final class FieldPoses {

  private FieldPoses() {}

  //======== Aliança azul ========
  public static final Pose2d BLUE_OUTPOST =
      new Pose2d(
          new Translation2d(0.480, 0.645),   
          Rotation2d.fromDegrees(180)
      );

  public static final Pose2d BLUE_TOWER =
      new Pose2d(
          new Translation2d(1.700, 3.592),
          Rotation2d.fromDegrees(0.0)
      );

      //======== Aliança vermelha ========
  public static final Pose2d RED_OUTPOST =
      new Pose2d(
          new Translation2d(16.002, 7.375),  
          Rotation2d.fromDegrees(90)
      );

  public static final Pose2d RED_TOWER =
      new Pose2d(
          new Translation2d(14.818, 4.216),
          Rotation2d.fromDegrees(0)
      );

// ======== BLUE HUB SHOOTING POSITIONS ========

public static final Pose2d BLUE_HUB_LEFT =
new Pose2d(
    new Translation2d(3.009, 2.513),        //ESQUERDAAAAAAAAAA
    Rotation2d.fromDegrees(42.797)
);

public static final Pose2d BLUE_HUB_CENTER = //CENTROOOOOOOOO
new Pose2d(
    new Translation2d(3.041, 3.987),
    Rotation2d.fromDegrees(0.0)      
);

public static final Pose2d BLUE_HUB_RIGHT =
new Pose2d(
    new Translation2d(3.020, 5.052),
    Rotation2d.fromDegrees(-45.000));   //DIREITAAAAAAAAAAAAA


// ======== RED HUB SHOOTING POSITIONS ========

// public static final Pose2d RED_HUB_LEFT =
//     new Pose2d(
//         new Translation2d(7.2, 5.1),
//         Rotation2d.fromDegrees(210)
//     );

// public static final Pose2d RED_HUB_CENTER =
//     new Pose2d(
//         new Translation2d(8.0, 4.0),
//         Rotation2d.fromDegrees(180)
//     );

// public static final Pose2d RED_HUB_RIGHT =
//     new Pose2d(
//         new Translation2d(7.3, 2.9),
//         Rotation2d.fromDegrees(150));    

}
