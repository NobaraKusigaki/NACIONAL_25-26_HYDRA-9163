package frc.robot.subsystems.ScoreSD.Angular;

import com.revrobotics.spark.*;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.*;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeAngleSubsystem extends SubsystemBase {

    private final SparkMax motor =
        new SparkMax(Constants.IntakeConstants.ANGLE_MOTOR_ID, MotorType.kBrushless);

    private final DutyCycleEncoder absEncoder =
        new DutyCycleEncoder(Constants.IntakeConstants.ANGLE_ENCODER_ID);

    // Offset absoluto redefinível
    private double zeroOffsetDeg = 0.0;

    public IntakeAngleSubsystem() {
        SparkMaxConfig cfg = new SparkMaxConfig();
        cfg.idleMode(IdleMode.kBrake).smartCurrentLimit(30);

        motor.configure(
            cfg,
            SparkBase.ResetMode.kResetSafeParameters,
            SparkBase.PersistMode.kPersistParameters
        );
    }

    /** Ângulo bruto do encoder (invertido se necessário) */
    private double getRawAngleDeg() {
        return (1.0 - absEncoder.get()) * 360.0;
    }

    /** Ângulo CONTÍNUO relativo ao último ZERO */
    public double getAngleDeg() {
        return getRawAngleDeg() + zeroOffsetDeg;
    }

    public void loadZero() {
        zeroOffsetDeg = Preferences.getDouble("IntakeAngleZero", 0.0);
    }

    /** Faz a posição atual virar 0° */
    public void recalibrateZero() {
        zeroOffsetDeg = -getRawAngleDeg();
    }

    public double getZeroOffset() {
        return zeroOffsetDeg;
    }

    public void setPower(double power) {
        motor.set(power);
    }

    public void stop() {
        motor.stopMotor();
    }
}
