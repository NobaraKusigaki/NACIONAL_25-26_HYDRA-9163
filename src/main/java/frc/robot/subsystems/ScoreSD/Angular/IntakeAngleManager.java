package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleManager extends SubsystemBase {

    public enum IntakeAnglePosition {
        HOME,    
        INTAKE  
    }

    private final IntakeAngleSubsystem subsystem;

    private final PIDController pid;
    private final ArmFeedforward ff;

    private IntakeAnglePosition currentPosition = IntakeAnglePosition.HOME;

    private double targetAngleDeg = 0.0;
    private boolean moving = false;
    private double lastOutput = 0.0;

    public IntakeAngleManager() {

        subsystem = new IntakeAngleSubsystem();

        pid = new PIDController(
            Constants.IntakeConstants.ANGLE_KP,
            Constants.IntakeConstants.ANGLE_KI,
            Constants.IntakeConstants.ANGLE_KD
        );
        pid.setTolerance(Constants.IntakeConstants.ANGLE_TOLERANCE_DEG);

        ff = new ArmFeedforward(
            Constants.IntakeConstants.ANGLE_KS,
            Constants.IntakeConstants.ANGLE_KG,
            Constants.IntakeConstants.ANGLE_KV,
            Constants.IntakeConstants.ANGLE_KA
        );

        double offset =
            Preferences.getDouble(Constants.IntakeConstants.PREF_ENCODER_OFFSET, 0.0);
        subsystem.setEncoderOffset(offset);

        SmartDashboard.putString(
            "IntakeAngle/Position",
            currentPosition.name()
        );
    }

    public void togglePosition() {

        if (currentPosition == IntakeAnglePosition.HOME) {
            moveTo(Constants.IntakeConstants.INTAKE_ANGLE_DEG);
            currentPosition = IntakeAnglePosition.INTAKE;
        } else {
            moveTo(Constants.IntakeConstants.HOME_ANGLE_DEG);
            currentPosition = IntakeAnglePosition.HOME;
        }

        SmartDashboard.putString(
            "IntakeAngle/Position",
            currentPosition.name()
        );
    }

    public IntakeAnglePosition getCurrentPosition() {
        return currentPosition;
    }

    public void stop() {
        moving = false;
        subsystem.stop();
    }

    private void moveTo(double angleDeg) {
        targetAngleDeg = angleDeg;
        moving = true;
    }

    @Override
    public void periodic() {

        if (!moving) return;

        double currentAngle = subsystem.getAngleDeg();

        double error =
            ((targetAngleDeg - currentAngle + 540.0) % 360.0) - 180.0;

        double pidOut = pid.calculate(error, 0.0);
        double ffOut  = ff.calculate(Math.toRadians(currentAngle), 0.0);

        double output = pidOut + ffOut;

        output = Math.max(
            Math.min(output, Constants.IntakeConstants.ANGLE_MAX_OUTPUT),
            -Constants.IntakeConstants.ANGLE_MAX_OUTPUT
        );

        // rampa simples (protege mecânica)
        double ramp = 0.03;
        output = lastOutput +
            Math.max(Math.min(output - lastOutput, ramp), -ramp);

        lastOutput = output;

        subsystem.setPower(output);

        SmartDashboard.putNumber("IntakeAngle/TargetDeg", targetAngleDeg);
        SmartDashboard.putNumber("IntakeAngle/ErrorDeg", error);
        SmartDashboard.putNumber("IntakeAngle/Output", output);

        if (Math.abs(error) < Constants.IntakeConstants.ANGLE_TOLERANCE_DEG) {
            subsystem.stop();
            moving = false;
        }
    }

    public void calibrateZero() {
        double raw = subsystem.getAngleDeg();
        double newOffset = -raw;

        subsystem.setEncoderOffset(newOffset);
        Preferences.setDouble(
            Constants.IntakeConstants.PREF_ENCODER_OFFSET,
            newOffset
        );

        SmartDashboard.putString(
            "IntakeAngle/Calibration",
            "ZERO CALIBRADO"
        );
    }
}
