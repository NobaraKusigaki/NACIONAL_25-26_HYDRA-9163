package frc.robot.dashboards;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;

public class RobotStressMonitor {

    private final PowerDistribution pdh;

    public RobotStressMonitor(int pdhID) {
        pdh = new PowerDistribution(pdhID, PowerDistribution.ModuleType.kRev);
    }

    public double getBatteryVoltage() {
        return RobotController.getBatteryVoltage();
    }

    public double getTotalCurrent() {
        return pdh.getTotalCurrent();
    }

    public double getDrivetrainCurrent(int[] channels) {
        double sum = 0;
        for (int c : channels) {
            sum += pdh.getCurrent(c);
        }
        return sum;
    }

    private double batteryStress(double voltage) {
        if (voltage >= 11.0) return 0;
        if (voltage >= 10.0) return 30;
        if (voltage >= 9.0)  return 70;
        if (voltage >= 8.0)  return 90;
        return 100;
    }

    public double calculateStressScore(double drivetrainCurrent) {
        double voltage = getBatteryVoltage();
        double totalCurrent = getTotalCurrent();

        double voltageStress = batteryStress(voltage);
        double drivetrainStress = drivetrainCurrent * 0.15;
        double systemStress = totalCurrent * 0.05;

        double score = voltageStress + drivetrainStress + systemStress;
        return Math.min(score, 100);
    }

    public String getStressLevel(double score) {
        if (score < 20) return "LOW";
        if (score < 45) return "MEDIUM";
        if (score < 70) return "HIGH";
        return "CRITICAL";
    }
    

    public RobotStressData generateData(int[] drivetrainChannels) {
        double voltage = getBatteryVoltage();
        double totalCurrent = getTotalCurrent();
        double drivetrainCurrent = getDrivetrainCurrent(drivetrainChannels);
    
        double score = calculateStressScore(drivetrainCurrent);
        String level = getStressLevel(score);
    
        return new RobotStressData(
                voltage,
                totalCurrent,
                drivetrainCurrent,
                score,
                level
        );
    }
    
}
