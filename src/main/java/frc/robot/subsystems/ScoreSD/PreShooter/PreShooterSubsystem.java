package frc.robot.subsystems.ScoreSD.PreShooter;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class PreShooterSubsystem extends SubsystemBase {

    private final SparkMax leader =
        new SparkMax(Constants.PreShooterConstants.LEADER_ID, MotorType.kBrushed);

    private final SparkMax follower =
        new SparkMax(Constants.PreShooterConstants.FOLLOWER_ID, MotorType.kBrushed);

    public PreShooterSubsystem() {

        SparkMaxConfig leaderConfig = new SparkMaxConfig();
        leaderConfig
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        SparkMaxConfig followerConfig = new SparkMaxConfig();
        followerConfig
            .follow(leader, false) // se girar errado, muda pra true
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        leader.configure(
            leaderConfig,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );

        follower.configure(
            followerConfig,
            ResetMode.kResetSafeParameters,
            PersistMode.kPersistParameters
        );
    }

    public void setPower(double power) {
        leader.set(power);
    }

    public void stop() {
        leader.stopMotor();
    }
}
