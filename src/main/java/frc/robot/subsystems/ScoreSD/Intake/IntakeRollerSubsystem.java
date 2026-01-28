package frc.robot.subsystems.ScoreSD.Intake;

import com.revrobotics.spark.*;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.*;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeRollerSubsystem extends SubsystemBase {

    private final SparkMax leader =
        new SparkMax(Constants.IntakeConstants.INTAKE_LEADER_ID, MotorType.kBrushed);

    public IntakeRollerSubsystem() {

        SparkMaxConfig config = new SparkMaxConfig();
        config
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        leader.configure(
            config,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );
    }

    public void intake() {
        leader.set(Constants.IntakeConstants.INTAKE_POWER);
    }

    public void outtake() {
        leader.set(Constants.IntakeConstants.OUTTAKE_POWER);
    }

    public void stop() {
        leader.stopMotor();
    }
}
