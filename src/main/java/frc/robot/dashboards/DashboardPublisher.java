package frc.robot.dashboards;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class DashboardPublisher {

    private final NetworkTable stressTable;

    public DashboardPublisher() {
        stressTable = NetworkTableInstance.getDefault()
                .getTable("RobotStress");
    }

    public void publish(
            double voltage,
            double totalCurrent,
            double drivetrainCurrent,
            double stressScore,
            String stressLevel
    ) {
        stressTable.getEntry("batteryVoltage").setDouble(voltage);
        stressTable.getEntry("totalCurrent").setDouble(totalCurrent);
        stressTable.getEntry("drivetrainCurrent").setDouble(drivetrainCurrent);
        stressTable.getEntry("stressScore").setDouble(stressScore);
        stressTable.getEntry("stressLevel").setString(stressLevel);
    }
}
