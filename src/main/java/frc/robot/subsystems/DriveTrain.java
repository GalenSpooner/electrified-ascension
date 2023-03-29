// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;
import com.swervedrivespecialties.swervelib.Mk4SwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SwerveModule;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.DriveConstants;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.interfaces.Accelerometer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
public class DriveTrain extends SubsystemBase {
  /** Creates a new ExampleSubsystem. */
  public final DriveModule frontright;
  public final DriveModule frontleft;
  public final DriveModule backright;
  public final DriveModule backleft;
  DoubleLogEntry encoderLog;
  private ChassisSpeeds chassisSpeeds;

  public boolean autoMode = false;
  public int hatValue = 0;
  public AHRS NAVX = new AHRS(I2C.Port.kOnboard);

  public Accelerometer accelerometer = new BuiltInAccelerometer();
  private final SwerveDriveKinematics kinematics = new SwerveDriveKinematics(
      new Translation2d(-DriveConstants.CHASSIS_WIDTH / 2,DriveConstants.CHASSIS_LENGTH / 2), 
      new Translation2d(-DriveConstants.CHASSIS_WIDTH/2,-DriveConstants.CHASSIS_LENGTH/2),
      new Translation2d(DriveConstants.CHASSIS_WIDTH / 2,DriveConstants.CHASSIS_LENGTH / 2), 
      new Translation2d(DriveConstants.CHASSIS_WIDTH,-DriveConstants.CHASSIS_LENGTH / 2)
  );
  SwerveDriveOdometry m_odometry;
  public DriveTrain() {
    frontright = new DriveModule(
      DriveConstants.FRONTRIGHT_MODULE_DRIVE_CAN,
      DriveConstants.FRONTRIGHT_MODULE_STEER_CAN,
      DriveConstants.FRONTRIGHT_MODULE_ENCODER,
      DriveConstants.FRONTRIGHT_MODULE_OFFSET
    );
    frontleft = new DriveModule(
      DriveConstants.FRONTLEFT_MODULE_DRIVE_CAN,
      DriveConstants.FRONTLEFT_MODULE_STEER_CAN,
      DriveConstants.FRONTLEFT_MODULE_ENCODER,
      DriveConstants.FRONTLEFT_MODULE_OFFSET
    );
    backright = new DriveModule(
      DriveConstants.BACKRIGHT_MODULE_DRIVE_CAN,
      DriveConstants.BACKRIGHT_MODULE_STEER_CAN,
      DriveConstants.BACKRIGHT_MODULE_ENCODER,
      DriveConstants.BACKRIGHT_MODULE_OFFSET
    );
    backleft = new DriveModule(
      DriveConstants.BACKLEFT_MODULE_DRIVE_CAN,
      DriveConstants.BACKLEFT_MODULE_STEER_CAN,
      DriveConstants.BACKLEFT_MODULE_ENCODER,
      DriveConstants.BACKLEFT_MODULE_OFFSET
    );
    m_odometry = new SwerveDriveOdometry(kinematics, new Rotation2d(), 
    
    new SwerveModulePosition[] {
      new SwerveModulePosition(frontright.getDriveEncoder(), new Rotation2d()),
      new SwerveModulePosition(frontleft.getDriveEncoder(), new Rotation2d()),
      new SwerveModulePosition(backright.getDriveEncoder(), new Rotation2d()),
      new SwerveModulePosition(backleft.getDriveEncoder(), new Rotation2d())
    }, new Pose2d(0,0, new Rotation2d())
  );
      chassisSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);


      
      DataLogManager.start();
      DriverStation.startDataLog(DataLogManager.getLog());
      DataLog log = DataLogManager.getLog();
      encoderLog = new DoubleLogEntry(log, "encoder values");
  }
  public void drive(double x, double y, double z, int hat) {
    if (autoMode) {return;}
    if (Math.abs(x) < 0.07) {x = 0;}
    if (Math.abs(y) < 0.07) {y = 0;}
    if (Math.abs(z) < 0.07) {z = 0;}
    chassisSpeeds.vxMetersPerSecond = y;
    chassisSpeeds.vyMetersPerSecond = x;
    chassisSpeeds = new ChassisSpeeds(y, x, z);
    hatValue = hat;
    // chassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(chassisSpeeds, NAVX.getRotation2d());
  }
  public void driveAuto(double x, double y, double z) {
    chassisSpeeds = new ChassisSpeeds(y, x, z);
  }
  public double clampToAngle(double i) {
    if (i > 360.0) {
      i-=360.0;
    }else if (i < 0.0) {
      i+=360.0;
    }
    return i;
  }
  public double getContinousDist(double a1, double a2) {
    double error1 = (a1-a2+360.0);
    double error2 = (a1-a2);

    

    if (Math.abs(error1) < Math.abs(error2)) {
        return (error1);
    }else {
        return (error2);
    }
    
  }

  public SwerveModuleState optimize(SwerveModuleState desiredState, double angle) { //degrees
    double targetSpeed = desiredState.speedMetersPerSecond;
    double targetAngle = desiredState.angle.getRadians() * (180.0/Math.PI);

    double dist = Math.abs(targetAngle - angle);
    if (angle>90.0f) {
      targetAngle += 180.0;
      targetSpeed = -targetSpeed;
      System.out.println("Swapped at delta: " + dist);
    }
    targetAngle = clampToAngle(targetAngle);
    return new SwerveModuleState(targetSpeed, Rotation2d.fromDegrees(targetAngle));
  }
  public Translation3d getAccelerometer() {
    return new Translation3d(accelerometer.getX(), accelerometer.getY(), accelerometer.getZ());
  }
  @Override
  public void periodic() {
    
    SwerveModuleState[] state = kinematics.toSwerveModuleStates(chassisSpeeds);
    // state[0] = SwerveModuleState.optimize(state[0], new Rotation2d(backright.getNEOEncoder() * (3.14/180.0)));
    // state[1] = SwerveModuleState.optimize(state[1], new Rotation2d(backleft.getNEOEncoder() * (3.14/180.0)));
    // state[2] = SwerveModuleState.optimize(state[2], new Rotation2d(-frontright.getNEOEncoder() * (3.14/180.0)));
    // state[3] = SwerveModuleState.optimize(state[3], new Rotation2d(-frontleft.getNEOEncoder() * (3.14/180.0)));
    // if (Math.abs(chassisSpeeds.vxMetersPerSecond) < 0.01 &&
    //     Math.abs(chassisSpeeds.vyMetersPerSecond) < 0.01 && 
    //     Math.abs(chassisSpeeds.omegaRadiansPerSecond) < 0.01) {
    //       frontright.stop();
    //       frontleft.stop();
    //       backright.stop();
    //       backleft.stop();
    //       return;
          
    //     }
    SmartDashboard.putNumber("frontright speed: ", state[0].speedMetersPerSecond);
    SmartDashboard.putNumber("frontright angle: ", state[0].angle.getDegrees());
    SmartDashboard.putNumber("frontright encoder: ", frontright.getDriveEncoder());
    SmartDashboard.putNumber("frontright current", frontright.drive.getOutputCurrent());

    SmartDashboard.putNumber("frontleft speed: ", state[1].speedMetersPerSecond);
    SmartDashboard.putNumber("frontleft angle: ", state[1].angle.getDegrees());
    SmartDashboard.putNumber("frontleft encoder: ", -frontleft.getDriveEncoder());
    SmartDashboard.putNumber("frontleft current", frontleft.drive.getOutputCurrent());

    SmartDashboard.putNumber("backright speed: ", state[2].speedMetersPerSecond);
    SmartDashboard.putNumber("backright angle: ", state[2].angle.getDegrees());
    SmartDashboard.putNumber("backright encoder: ", backright.getDriveEncoder());
    SmartDashboard.putNumber("backright current", backright.drive.getOutputCurrent());

    SmartDashboard.putNumber("backleft speed: ", state[3].speedMetersPerSecond);
    SmartDashboard.putNumber("backleft angle: ", state[3].angle.getDegrees());
    SmartDashboard.putNumber("backleft encoder: ", backleft.getDriveEncoder());
    SmartDashboard.putNumber("backleft current", backleft.drive.getOutputCurrent());

    SmartDashboard.putNumber("ACCELEROMETER X", accelerometer.getX());
    SmartDashboard.putNumber("ACCELEROMETER Y", accelerometer.getY());
    SmartDashboard.putNumber("ACCELEROMETER Z", accelerometer.getZ());

    SmartDashboard.putNumber("NAVX Angle", NAVX.getAngle());
    if (autoMode) {
      return;
    }
    if (hatValue != -1) {
      frontright.set(-Constants.DriveConstants.CREEP_SPEED, hatValue, true);
      frontleft.set(Constants.DriveConstants.CREEP_SPEED, hatValue, false);
      backright.set(-Constants.DriveConstants.CREEP_SPEED, hatValue, false);
      backleft.set(-Constants.DriveConstants.CREEP_SPEED, hatValue, true);
  
    }else {
      frontright.set(state[0].speedMetersPerSecond * Constants.DriveConstants.MAX_SPEED, state[0].angle.getDegrees(), true);
      frontleft.set(-state[1].speedMetersPerSecond * Constants.DriveConstants.MAX_SPEED, state[1].angle.getDegrees(), false);
      backright.set(state[2].speedMetersPerSecond * Constants.DriveConstants.MAX_SPEED, state[2].angle.getDegrees(), false);
      backleft.set(state[3].speedMetersPerSecond * Constants.DriveConstants.MAX_SPEED, state[3].angle.getDegrees(), true);
  
    }
    
    Pose2d pose = m_odometry.update(new Rotation2d(), 
    new SwerveModulePosition[] {
      new SwerveModulePosition(frontright.getDriveEncoder()*0.1156, new Rotation2d(frontright.getEncoder() * (3.14/180.0))),
      new SwerveModulePosition(frontleft.getDriveEncoder()*0.1156, new Rotation2d(frontleft.getEncoder() * (3.14/180.0))),
      new SwerveModulePosition(backright.getDriveEncoder()*0.1156, new Rotation2d(backright.getEncoder() * (3.14/180.0))),
      new SwerveModulePosition(backleft.getDriveEncoder()*0.1156, new Rotation2d(backleft.getEncoder() * (3.14/180.0)))
    }
    );
    SmartDashboard.putNumber("HAT VALUE", hatValue);
    SmartDashboard.putNumber("odometry x", pose.getX());
    SmartDashboard.putNumber("odometry y", pose.getY());
    
    //t.set(0.5);
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
