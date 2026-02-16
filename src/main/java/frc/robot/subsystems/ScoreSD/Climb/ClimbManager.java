package frc.robot.subsystems.ScoreSD.Climb;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ClimbManager extends SubsystemBase {

    public enum PositionState { RETRACTED, EXTENDED }
    public enum ControlMode { MANUAL, AUTOMATIC, DISABLED }

    private final ClimbSubsystem io = new ClimbSubsystem();

    private final PIDController pid =
        new PIDController(
            Constants.Climb.KP,
            Constants.Climb.KI,
            Constants.Climb.KD
        );

    private ControlMode mode = ControlMode.DISABLED;
    private PositionState lastPosition = PositionState.RETRACTED;

    private double targetPosition = 0.0;

    private double minPos;
    private double maxPos;

    public ClimbManager() {

        pid.setTolerance(Constants.Climb.TOLERANCE);

        minPos = Preferences.getDouble(Constants.Climb.MIN_POS_KEY, 0.0);
        maxPos = Preferences.getDouble(Constants.Climb.MAX_POS_KEY, 0.0);

        SmartDashboard.putString("Climb/State", lastPosition.name());
    }

    // ================= MANUAL =================

    public void manualUp() {
        mode = ControlMode.MANUAL;
        io.setPower(0.4);
    }

    public void manualDown() {
        mode = ControlMode.MANUAL;
        io.setPower(-0.4);
    }

    public void stop() {
        io.stop();
        mode = ControlMode.DISABLED;
    }

    // ================= CALIBRATION =================

    public void calibrateRetracted() {
        minPos = io.getPosition();
        Preferences.setDouble(Constants.Climb.MIN_POS_KEY, minPos);
    }

    public void calibrateExtended() {
        maxPos = io.getPosition();
        Preferences.setDouble(Constants.Climb.MAX_POS_KEY, maxPos);
    }

    // ================= TOGGLE =================

    public void togglePosition() {

        mode = ControlMode.AUTOMATIC;

        if (lastPosition == PositionState.RETRACTED) {
            targetPosition = maxPos;
            lastPosition = PositionState.EXTENDED;
        } else {
            targetPosition = minPos;
            lastPosition = PositionState.RETRACTED;
        }

        SmartDashboard.putString("Climb/State", lastPosition.name());
    }

    public void moveToExtended() {
        targetPosition = maxPos;
        lastPosition = PositionState.EXTENDED;
        mode = ControlMode.AUTOMATIC;
    }

    public void moveToRetracted() {
        targetPosition = minPos;
        lastPosition = PositionState.RETRACTED;
        mode = ControlMode.AUTOMATIC;
    }

    public boolean atTarget() {
        return pid.atSetpoint();
    }

    // ================= PERIODIC =================

    @Override
    public void periodic() {
    
        if (mode != ControlMode.AUTOMATIC) return;
    
        double current = io.getPosition();
    
       
        if (current >= maxPos && targetPosition > maxPos) {
            io.stop();
            mode = ControlMode.DISABLED;
            return;
        }
    
        // 🔒 PROTEÇÃO DE LIMITE INFERIOR
        if (current <= minPos && targetPosition < minPos) {
            io.stop();
            mode = ControlMode.DISABLED;
            return;
        }
    
        double output = pid.calculate(current, targetPosition);
    
        output = Math.max(
            Math.min(output, Constants.Climb.MAX_OUTPUT),
            -Constants.Climb.MAX_OUTPUT
        );
    
        io.setPower(output);
    
        SmartDashboard.putNumber("Climb/Current", current);
        SmartDashboard.putNumber("Climb/Target", targetPosition);
    
        if (pid.atSetpoint()) {
            io.stop();
            mode = ControlMode.DISABLED;
        }
    }
    
}
