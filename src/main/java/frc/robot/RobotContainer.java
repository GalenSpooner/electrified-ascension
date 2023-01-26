// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.commands.ArmBackward;
import frc.robot.commands.ArmForward;
import frc.robot.commands.DriveCommand;
import frc.robot.commands.TurnWrist;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.Wrist;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveTrain drivetrain = new DriveTrain();
  //private final Wrist wrist = new Wrist();
  private final Arm arm = new Arm();
  private final Joystick joystick;
  //private final JoystickButton b_turnWrist;
  private final JoystickButton arm_backwards;
  private final JoystickButton arm_forwards;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    joystick = new Joystick(0);
    //b_turnWrist = new JoystickButton(joystick, Constants.ButtonConstants.BUTTON_WRIST);
    arm_backwards = new JoystickButton(joystick, Constants.ButtonConstants.BUTTON_ARMBACKWARD);
    arm_forwards = new JoystickButton(joystick, Constants.ButtonConstants.BUTTON_ARMFORWARD);
    
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    //b_turnWrist.onTrue(new TurnWrist(wrist));
    arm_backwards.whileTrue( (Command) new ArmBackward(arm));
    arm_forwards.whileTrue( (Command) new ArmForward(arm));
    drivetrain.setDefaultCommand(new DriveCommand(drivetrain, joystick));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return null;
  }
}
