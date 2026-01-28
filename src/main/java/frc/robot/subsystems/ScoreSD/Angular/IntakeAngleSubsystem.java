package frc.robot.subsystems.ScoreSD.Angular;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.DutyCycleEncoder;
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

        SparkMaxConfig cfg = new SparkMaxConfig();
        cfg.idleMode(IdleMode.kBrake)
           .smartCurrentLimit(30);

        angleMotor.configure(
            cfg,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );
    }

   
    public double getAngleDeg() {
        double raw = absEncoder.get(); 
        double angle = raw * 360.0 + encoderOffsetDeg;
        return ((angle % 360.0) + 360.0) % 360.0;
    }

    public void setEncoderOffset(double offsetDeg) {
        encoderOffsetDeg = offsetDeg;
    }

    public double getEncoderOffset() {
        return encoderOffsetDeg;
    }

    public void setPower(double power) {
        angleMotor.set(power);
    }

    public void stop() {
        angleMotor.stopMotor();
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("IntakeAngle/AngleDeg", getAngleDeg());
        SmartDashboard.putNumber("IntakeAngle/OffsetDeg", encoderOffsetDeg);
    }
}
