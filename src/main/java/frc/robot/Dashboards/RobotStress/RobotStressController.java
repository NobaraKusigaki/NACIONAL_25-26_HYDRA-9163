package frc.robot.Dashboards.RobotStress;
public class RobotStressController {

    private double maxAllowedSpeedMps = 3.6; // default (12 ft/s)
    private boolean reduced = false;

    public void update(RobotStressData data) {

        reduced = false;

        double voltage = data.batteryVoltage;

        if (voltage >= 11.0) {
            maxAllowedSpeedMps = 3.6;
        } else if (voltage >= 10.0) {
            maxAllowedSpeedMps = 2.8;
            reduced = true;
        } else if (voltage >= 9.0) {
            maxAllowedSpeedMps = 2.2;
            reduced = true;
        } else {
            maxAllowedSpeedMps = 1.8;
            reduced = true;
        }
    }

    public double getMaxAllowedSpeedMps() {
        return maxAllowedSpeedMps;
    }

    public boolean isSpeedReduced() {
        return reduced;
    }
}
