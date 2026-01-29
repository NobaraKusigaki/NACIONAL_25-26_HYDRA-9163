package frc.robot.subsystems.ScoreSD.PreShooter;

import com.revrobotics.spark.*;
import com.revrobotics.spark.config.*;
import com.revrobotics.spark.SparkLowLevel.MotorType;
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
            .idleMode(SparkBaseConfig.IdleMode.kBrake)
            .smartCurrentLimit(40);

        SparkMaxConfig followerConfig = new SparkMaxConfig();
        followerConfig
            .follow(leader, false)
            .idleMode(SparkBaseConfig.IdleMode.kBrake)
            .smartCurrentLimit(40);

        leader.configure(leaderConfig,
            SparkBase.ResetMode.kResetSafeParameters,
            SparkBase.PersistMode.kPersistParameters);

        follower.configure(followerConfig,
            SparkBase.ResetMode.kResetSafeParameters,
            SparkBase.PersistMode.kPersistParameters);
    }

    public void feed() {
        leader.set(Constants.PreShooterConstants.FEED_POWER);
    }

    public void stop() {
        leader.stopMotor();
    }
}
