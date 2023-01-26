package frc.robot.commands;
import frc.robot.subsystems.*;
import edu.wpi.first.wpilibj.Timer;
public class ArmBackward {
    
    private double speed;
    private Arm armMotor;
    Timer timer = new Timer();
    public ArmBackward(Arm a){
        armMotor = a;
    }
    public void initialize(){timer.start();}

    public void execute(){
        armMotor.setMotor(-0.3);
    }
    public void end(boolean interrupted){
        armMotor.setMotor(0);
        timer.reset();
    }
    public boolean isFinished(){return (timer.get() > 3);}
}
