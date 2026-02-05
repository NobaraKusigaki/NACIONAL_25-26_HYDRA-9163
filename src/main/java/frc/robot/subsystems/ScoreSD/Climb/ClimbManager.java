package frc.robot.subsystems.ScoreSD.Climb;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ClimbManager extends SubsystemBase {

    public enum ClimbPosition {
        RETRACTED,
        EXTENDED
    }

    private final ClimbSubsystem subsystem;
    private final PIDController pid;

    private ClimbPosition currentPosition = ClimbPosition.RETRACTED;
    private boolean moving = false;

    private double minPos;
    private double maxPos;
    private double targetPos;

    public ClimbManager() {

        subsystem = new ClimbSubsystem();

        pid = new PIDController(
            Constants.Climb.KP,
            Constants.Climb.KI,
            Constants.Climb.KD
        );
        pid.setTolerance(Constants.Climb.TOLERANCE);

        minPos = Preferences.getDouble(Constants.Climb.MIN_POS_KEY, 0.0);
        maxPos = Preferences.getDouble(Constants.Climb.MAX_POS_KEY, 0.0);

        SmartDashboard.putString("Climb/Position", currentPosition.name());
    }

    public void togglePosition() {

        if (currentPosition == ClimbPosition.RETRACTED) {
            moveTo(maxPos);
            currentPosition = ClimbPosition.EXTENDED;
        } else {
            moveTo(minPos);
            currentPosition = ClimbPosition.RETRACTED;
        }

        SmartDashboard.putString("Climb/Position", currentPosition.name());
    }

    private void moveTo(double pos) {
        targetPos = pos;
        moving = true;
    }

    public void stop() {
        moving = false;
        subsystem.stop();
    }

    public void calibrateRetracted() {
        minPos = subsystem.getPosition();
        Preferences.setDouble(Constants.Climb.MIN_POS_KEY, minPos);
        SmartDashboard.putString("Climb/Calibration", "RETRACTED calibrado");
    }

    public void calibrateExtended() {
        maxPos = subsystem.getPosition();
        Preferences.setDouble(Constants.Climb.MAX_POS_KEY, maxPos);
        SmartDashboard.putString("Climb/Calibration", "EXTENDED calibrado");
    }

    @Override
    public void periodic() {

        if (!moving) return;

        double current = subsystem.getPosition();
        double output = pid.calculate(current, targetPos);

        output = Math.max(
            Math.min(output, Constants.Climb.MAX_OUTPUT),
            -Constants.Climb.MAX_OUTPUT
        );

        subsystem.setPower(output);

        SmartDashboard.putNumber("Climb/Current", current);
        SmartDashboard.putNumber("Climb/Target", targetPos);
        SmartDashboard.putNumber("Climb/Output", output);

        if (pid.atSetpoint()) {
            subsystem.stop();
            moving = false;
        }
    }
}
