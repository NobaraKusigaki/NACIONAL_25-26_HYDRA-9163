package frc.robot.Utils;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

public class StreamDeckNT {

  private final GenericEntry b1;
  private final GenericEntry b2;
  private final GenericEntry b3;
  private final GenericEntry b4;

  public StreamDeckNT() {
    var tab = Shuffleboard.getTab("StreamDeck");

    b1 = tab.add("Button 1", false).getEntry();
    b2 = tab.add("Button 2", false).getEntry();
    b3 = tab.add("Button 3", false).getEntry();
    b4 = tab.add("Button 4", false).getEntry();
  }

  public boolean b1() { return b1.getBoolean(false); }
  public boolean b2() { return b2.getBoolean(false); }
  public boolean b3() { return b3.getBoolean(false); }
  public boolean b4() { return b4.getBoolean(false); }
}
