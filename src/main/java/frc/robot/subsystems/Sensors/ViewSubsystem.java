package frc.robot.subsystems.Sensors;

import java.util.Set;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ViewSubsystem extends SubsystemBase {

  private Set<Integer> frontAllowedTags = Set.of(0, 0, 0); //OLHA NO MANUAL PRA DEFINIIRRR
  private Set<Integer> backAllowedTags  = Set.of(0, 0, 0);


  private final NetworkTable limeFront =
      NetworkTableInstance.getDefault().getTable("limelight-front");

  private final NetworkTable limeBack =
      NetworkTableInstance.getDefault().getTable("limelight-back");


  // ================= LIMELIGHT FRONT =================
  public boolean hasFrontTarget() {
    return limeFront.getEntry("tv").getDouble(0) == 1;
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
    return limeBack.getEntry("has_target").getBoolean(false);
  }

  public double getBackPieceTxRad() {
    return Units.degreesToRadians(
        limeBack.getEntry("piece_tx").getDouble(0.0));
  }

  public double getBackTa() {
    return limeBack.getEntry("ta").getDouble(0.0);
  }
 // ================= CONFIGURAÇÃO  =================
  public void setFrontAllowedTags(Set<Integer> ids) {
    frontAllowedTags = ids;
  }

  public void setBackAllowedTags(Set<Integer> ids) {
    backAllowedTags = ids;
  }
}

