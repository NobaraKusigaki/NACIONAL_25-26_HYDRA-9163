package frc.robot.Utils.DriveUtils;

public class SlewLimiter {

    private double lastValue = 0.0;
    private final double maxAccel;
    private final double dt;

    public SlewLimiter(double maxAccel, double dtSeconds) {
        this.maxAccel = maxAccel;
        this.dt = dtSeconds;
    }

    public double calculate(double input) {
        double delta = input - lastValue;
        double maxDelta = maxAccel * dt;

        if (delta > maxDelta) delta = maxDelta;
        else if (delta < -maxDelta) delta = -maxDelta;

        lastValue += delta;
        return lastValue;
    }

    public void reset(double value) {
        lastValue = value;
    }
}
