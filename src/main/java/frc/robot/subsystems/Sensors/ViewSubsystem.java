package frc.robot.subsystems.Sensors;

import java.util.Set;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ViewSubsystem extends SubsystemBase {

  // private Set<Integer> frontAllowedTags = Set.of(0, 1, 2); // AINDA NÃO UTILIZADO

  private final NetworkTable limeFront =
      NetworkTableInstance.getDefault().getTable("limelight-front");

  private final NetworkTable limeBack =
      NetworkTableInstance.getDefault().getTable("limelight-back");


  // ================= LIMELIGHT FRONT =================
  public boolean hasFrontTarget() {
    return limeFront.getEntry("tv").getDouble(0) == 1;
  }

  public int getDetectedTagId() {
    if (!hasFrontTarget()) return -1;

    return (int) limeFront.getEntry("tid").getDouble(-1);
  }


  public double getDistanceToTag() {

    if (!hasFrontTarget()) return Double.MAX_VALUE;

    double tyDegrees = limeFront.getEntry("ty").getDouble(0.0);
    double tyRadians = Units.degreesToRadians(tyDegrees);
    double angle = Constants.LimelightConstants.LIMELIGHT_ANGLE + tyRadians;

    if (Math.abs(Math.tan(angle)) < 1e-3) {
      return Double.MAX_VALUE;
    }

    return (Constants.LimelightConstants.TAG_HEIGHT - Constants.LimelightConstants.LIMELIGHT_HEIGHT) / Math.tan(angle);
  }


  public double getFrontTxRad() {
    return Units.degreesToRadians(
        limeFront.getEntry("tx").getDouble(0.0));
  }

  public double getFrontTa() {
    return limeFront.getEntry("ta").getDouble(0.0);
  }

  // ================= LIMELIGHT "BACK" =================
  public boolean hasBackTarget() {
    return limeBack.getEntry("has_target").getBoolean(true);
  }

  public double getBackPieceTxRad() {
    return Units.degreesToRadians(
        limeBack.getEntry("piece_tx").getDouble(0.0));
  }

  public double getBackTa() {
    return limeBack.getEntry("ta").getDouble(0.0);
  }
 // ================= CONFIGURAÇÃO  =================
  // public void setFrontAllowedTags(Set<Integer> ids) {
  //   frontAllowedTags = ids;
  // }
}