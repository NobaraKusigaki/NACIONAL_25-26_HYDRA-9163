package frc.robot.commands.auto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Swervedrive.SwerveSubsystem;


public class AutoBalanceCommand extends Command {

  private final SwerveSubsystem swerveSubsystem;
  private final PIDController controller;

  public AutoBalanceCommand(SwerveSubsystem swerveSubsystem)
  {
    this.swerveSubsystem = swerveSubsystem;
    controller = new PIDController(1.0, 0.0, 0.0);
    controller.setTolerance(1);
    controller.setSetpoint(0.0);
    
    addRequirements(this.swerveSubsystem);
  }

  @Override
  public void initialize(){

  }

  @Override
  public void execute()
  {
    SmartDashboard.putBoolean("At Tolerance", controller.atSetpoint());

    double translationVal = MathUtil.clamp(controller.calculate(swerveSubsystem.getPitch().getDegrees(), 0.0), -0.5,
                                           0.5);
    swerveSubsystem.drive(new Translation2d(translationVal, 0.0), 0.0);
  }

  @Override
  public boolean isFinished()
  {
    return controller.atSetpoint();
  }

  @Override
  public void end(boolean interrupted){
   
  }
}
