// package frc.robot.commands.auto_blocks;

// import edu.wpi.first.wpilibj2.command.*;
// import frc.robot.subsystems.ScoreSD.PreShooter.PreShooterManager;
// import frc.robot.subsystems.ScoreSD.Shooter.ShooterManager;
// import frc.robot.subsystems.ScoreSD.Spindexer.SpindexerManager;

// public class AutoShootSequence extends SequentialCommandGroup {

//     public AutoShootSequence(
//         ShooterManager shooterManager,
//         PreShooterManager preShooterManager,
//         SpindexerManager spindexerManager
//     ) {

//         addCommands(

//             // Ativa modo automático de distância
//             Commands.runOnce(
//                 preShooterManager::enableAutoDistanceMode,
//                 preShooterManager
//             ),

//             // Espera PreShooter começar a alimentar
//             Commands.waitUntil(() ->
//             preShooterManager.getState() ==
//             PreShooterManager.PreShooterState.AUTO_FEEDING)
//             .withTimeout(2.0),
        

//             // Gira spindexer 3 voltas
//             Commands.runOnce(
//                 spindexerManager::spinThreeFromReference,
//                 spindexerManager
//             ),

//             // Espera terminar as 3 voltas
//             Commands.waitUntil(() ->
//             spindexerManager.getState() ==
//             SpindexerManager.SpindexerState.HOLDING)
//             .withTimeout(3.0),


//             // Desliga tudo
//             Commands.runOnce(() -> {
//                 preShooterManager.forceStop();
//                 shooterManager.disableShooter();
//                 spindexerManager.stop();
//             })
//         );
//     }
// }
