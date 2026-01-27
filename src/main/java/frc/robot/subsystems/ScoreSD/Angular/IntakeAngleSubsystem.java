package frc.robot.subsystems.ScoreSD.Intake;

import com.revrobotics.spark.*;
import com.revrobotics.spark.config.*;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleSubsystem extends SubsystemBase {

    private final SparkMax angleMotor =
        new SparkMax(Constants.IntakeConstants.ANGLE_MOTOR_ID, MotorType.kBrushless);

    private final DutyCycleEncoder absEncoder =
        new DutyCycleEncoder(Constants.IntakeConstants.ANGLE_ENCODER_ID);

    private double encoderOffsetDeg = 0.0;

    public IntakeAngleSubsystem() {

        SparkMaxConfig cfg = new SparkMaxConfig()
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(30);

        angleMotor.configure(
            cfg,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );

        encoderOffsetDeg =
            Preferences.getDouble("Intake/AngleOffsetDeg", 0.0);
    }

    public double getAngleDeg() {
        double raw = absEncoder.get() * 360.0;
        double angle = raw + encoderOffsetDeg;
        return Math.max(
            Constants.IntakeConstants.MIN_ANGLE_DEG,
            Math.min(Constants.IntakeConstants.MAX_ANGLE_DEG, angle)
        );
    }

    public void setPower(double power) {
        angleMotor.set(power);
    }

    public void stop() {
        angleMotor.stopMotor();
    }

    public void calibrateZero() {
        encoderOffsetDeg = -absEncoder.get() * 360.0;
        Preferences.setDouble("Intake/AngleOffsetDeg", encoderOffsetDeg);
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("Intake/AngleDeg", getAngleDeg());
        SmartDashboard.putNumber("Intake/AngleOffset", encoderOffsetDeg);
    }
}
