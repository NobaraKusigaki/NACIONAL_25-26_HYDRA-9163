package frc.robot.subsystems.ScoreSD.Intake;

import com.revrobotics.spark.*;
import com.revrobotics.spark.config.*;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class IntakeRollerSubsystem extends SubsystemBase {

    private final SparkMax leader =
        new SparkMax(Constants.IntakeConstants.INTAKE_LEADER_ID, MotorType.kBrushed);

    private final SparkMax follower =
        new SparkMax(Constants.IntakeConstants.INTAKE_FOLLOWER_ID, MotorType.kBrushed);

    public IntakeRollerSubsystem() {

        SparkMaxConfig leaderConfig = new SparkMaxConfig();
        leaderConfig
            .idleMode(IdleMode.kBrake)
            .smartCurrentLimit(40);

        SparkMaxConfig followerConfig = new SparkMaxConfig()
            .follow(leader, false)
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

    public void intake() {
        leader.set(0.6);
    }

    public void outtake() {
        leader.set(-0.6);
    }

    public void stop() {
        leader.stopMotor();
    }
}
