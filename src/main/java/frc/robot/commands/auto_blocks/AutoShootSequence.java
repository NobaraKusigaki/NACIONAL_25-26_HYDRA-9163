package frc.robot.commands.auto_blocks;

import edu.wpi.first.wpilibj2.command.*;
import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
import frc.robot.subsystems.ScoreSD.Spindexer.SpindexerManager;

public class AutoShootSequence extends SequentialCommandGroup {

    public AutoShootSequence(
        ShooterManager shooterManager,
        PreShooterManager preShooterManager,
        SpindexerManager spindexerManager
    ) {

        addCommands(

    Commands.runOnce(
        shooterManager::enable,
        shooterManager
    ),

    Commands.runOnce(
        preShooterManager::enableAuto,
        preShooterManager
    ),

    Commands.waitUntil(shooterManager::isAtSpeed)
        .withTimeout(2.5),

    Commands.waitUntil(() ->
        preShooterManager.getState() ==
        PreShooterManager.State.AUTO_FEEDING
    ).withTimeout(2.0),

    Commands.runOnce(
        spindexerManager::spinThreeFromReference,
        spindexerManager
    ),

    Commands.waitUntil(() ->
        spindexerManager.getState() ==
        SpindexerManager.SpindexerState.HOLDING
    ).withTimeout(3.0),

    Commands.runOnce(() -> {
        preShooterManager.stop();
        shooterManager.disable();
        spindexerManager.stop();
    })
);

}
}
