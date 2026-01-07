package frc.robot.Utils;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

public class StreamDeckNT {

  private final GenericEntry[] buttons = new GenericEntry[15];

  public StreamDeckNT() {
    var tab = Shuffleboard.getTab("StreamDeck");

    for (int i = 0; i < 15; i++) {
      buttons[i] = tab.add("Button " + (i + 1), false).getEntry();
    }
  }

  public boolean button1Pressed()  { 
    return buttons[0].getBoolean(false); 
}
  public boolean button2Pressed()  { 
    return buttons[1].getBoolean(false); 
}
  public boolean button3Pressed()  { 
    return buttons[2].getBoolean(false); 
}
  public boolean button4Pressed()  { 
    return buttons[3].getBoolean(false); 
}
//   public boolean button5Pressed()  { 
//     return buttons[4].getBoolean(false); 
// }
//   public boolean button6Pressed()  { 
//     return buttons[5].getBoolean(false); 
// }
//   public boolean button7Pressed()  { 
//     return buttons[6].getBoolean(false); 
// }
//   public boolean button8Pressed()  { 
//     return buttons[7].getBoolean(false); 
// }
//   public boolean button9Pressed()  { 
//     return buttons[8].getBoolean(false); 
// }
//   public boolean button10Pressed() { 
//     return buttons[9].getBoolean(false); 
// }
//   public boolean button11Pressed() { 
//     return buttons[10].getBoolean(false); 
// }
//   public boolean button12Pressed() { 
//     return buttons[11].getBoolean(false); 
// }
//   public boolean button13Pressed() { 
//     return buttons[12].getBoolean(false);
//  }
//   public boolean button14Pressed() { 
//     return buttons[13].getBoolean(false); 
// }
//   public boolean button15Pressed() { 
//     return buttons[14].getBoolean(false); 
// }
}
