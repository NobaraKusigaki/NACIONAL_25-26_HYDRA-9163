package frc.robot.dashboards;

public class RobotStressController {

    private double speedScale = 1.0;
    private boolean critical = false;

    public void update(RobotStressData data) {

        critical = false;

        switch (data.stressLevel) {

            case "LOW":
                speedScale = 1.0;
                break;

            case "MEDIUM":
                speedScale = 0.9;
                break;

            case "HIGH":
                speedScale = 0.75;
                break;

            case "CRITICAL":
                speedScale = 0.6;
                critical = true;
                break;

            default:
                speedScale = 0.45;
                critical = true;
                break;
        }
    }

    public double getSpeedScale() {
        return speedScale;
    }

    public boolean isCritical() {
        return critical;
    }
}
