package frc.robot.autos;

import com.pathplanner.lib.auto.NamedCommands;

import frc.robot.subsystems.Sensors.ViewSubsystem;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;
import frc.robot.commands.vision.AimAtTagCommand;
import frc.robot.autonomous.Poses.FieldPoses;
import frc.robot.commands.auto_blocks.AutoGoAndAlign;

public final class NamedCommandsRegistry {

   public static void registerAll(
        SwerveSubsystem swerve,
        ViewSubsystem vision) {

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
        "GoToBestHubShot",
        swerve.goToBestHubShot()
    );
}

}
