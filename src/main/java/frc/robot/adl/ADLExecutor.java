package frc.robot.adl;

import frc.robot.subsystems.ScoreSD.Intake.IntakeManager;
import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleManager;
import frc.robot.subsystems.ScoreSD.Climb.ClimbManager;
import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;

public class ADLExecutor {

    private final IntakeManager intake;
    private final IntakeAngleManager intakeAngle;
    private final ClimbManager climb;
    private final PreShooterManager preShooter;

    public ADLExecutor(
        IntakeManager intake,
        IntakeAngleManager intakeAngle,
        ClimbManager climb,
        PreShooterManager preShooter
    ) {
        this.intake = intake;
        this.intakeAngle = intakeAngle;
        this.climb = climb;
        this.preShooter = preShooter;
    }

    public void execute(ADLState state) {

        switch (state) {

            case ACQUIRING:
                intake.armIntake();
                intakeAngle.togglePosition();
                break;

            case SCORING:
                intake.stop();
                preShooter.arm();   
                break;

            case CLIMBING:
                intake.disable("Climb ativo");
                preShooter.disable("Climb ativo");
                climb.togglePosition();
                break;

            case EMERGENCY:
                intake.disable("Emergência");
                preShooter.disable("Emergência");
                climb.stop();
                break;

            case IDLE:
            default:
                preShooter.stop();
                intake.stop();
                break;
        }
    }
}
