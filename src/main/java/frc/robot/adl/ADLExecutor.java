package frc.robot.adl;

import frc.robot.subsystems.ScoreSD.Intake.IntakeManager;
import frc.robot.subsystems.ScoreSD.Angular.IntakeAngleManager;
import frc.robot.subsystems.ScoreSD.Climb.ClimbManager;

public class ADLExecutor {

    private final IntakeManager intake;
    private final IntakeAngleManager intakeAngle;
    private final ClimbManager climb;

    public ADLExecutor(
        IntakeManager intake,
        IntakeAngleManager intakeAngle,
        ClimbManager climb
    ) {
        this.intake = intake;
        this.intakeAngle = intakeAngle;
        this.climb = climb;
    }

    public void execute(ADLState state) {

        switch (state) {

            case ACQUIRING:
                intake.armIntake();             
                intakeAngle.togglePosition();  
                break;

            case SCORING:
                intake.stop();
                break;

            case CLIMBING:
                intake.disable("Climb ativo");
                climb.togglePosition();
                break;

            case IDLE:
                intake.stop();
                break;

            case EMERGENCY:
                intake.disable("Emergência");
                climb.stop();
                break;

            default:
                break;
        }
    }
}
