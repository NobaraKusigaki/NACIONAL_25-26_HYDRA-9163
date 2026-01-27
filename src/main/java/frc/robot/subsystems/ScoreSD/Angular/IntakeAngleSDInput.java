// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.ScoreSD.Angular;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class IntakeAngleSDInput extends SubsystemBase {

  private final IntakeAngleManager manager;
  private final StringSubscriber cmdSub;
  private String lastCmd = "";

  public IntakeAngleSDInput(IntakeAngleManager manager) {
      this.manager = manager;
      cmdSub = NetworkTableInstance.getDefault()
          .getStringTopic("/StreamDeck/Intake/Angle")
          .subscribe("");
  }

  @Override
  public void periodic() {
      String cmd = cmdSub.get();

      if (cmd.equals(lastCmd)) return;
      lastCmd = cmd;

      if (cmd.equals("TOGGLE")) {
          manager.togglePosition();
      }
  }
}
