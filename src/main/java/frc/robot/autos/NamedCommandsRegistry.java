package frc.robot.autos;

import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;
import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
import frc.robot.subsystems.ScoreSD.Spindexer.SpindexerManager;
import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.autonomous.Poses.FieldPoses;
import frc.robot.commands.auto_blocks.AutoGoAndAlign;
import frc.robot.commands.auto_blocks.AutoShootSequence;

public final class NamedCommandsRegistry {

   public static void registerAll(
        SwerveSubsystem swerve,
        ViewSubsystem vision,
        ShooterManager shooterManager,
        PreShooterManager preShooterManager,
        SpindexerManager spindexerManager) {

    NamedCommands.registerCommand(
        "AimAtTag",
        new AimAtTagCommand(swerve, vision)
    );

    NamedCommands.registerCommand(
        "GoAndAlignOutpost",
        new AutoGoAndAlign(
            swerve,
            vision,
            FieldPoses.BLUE_OUTPOST
        )
    );

    NamedCommands.registerCommand(
    "SpinUpShooter",
    Commands.runOnce(
        shooterManager::enableShooter,
        shooterManager
    )
);

NamedCommands.registerCommand(
    "GoToBestHubShot",
    swerve.goToBestHubShot()
);

NamedCommands.registerCommand(
    "AlignToHub",
    new AimAtTagCommand(swerve, vision)
);

NamedCommands.registerCommand(
    "AutoShoot",
    new AutoShootSequence(
        shooterManager,
        preShooterManager,
        spindexerManager
    )
);



}

}