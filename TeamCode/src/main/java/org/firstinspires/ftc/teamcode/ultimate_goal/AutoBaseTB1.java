package org.firstinspires.ftc.teamcode.ultimate_goal;

//Imports


import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.EasyOpenCVExamples.WebcamExample;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;



public abstract class AutoBaseTB1 extends BaseClassTB1 {

    //Check for push and pull
    //Global variables
    double timeAtStop = 0;

    //Drives forward while correcting to face designated gyro heading
    public void driveForwardGyro(double power, double degree, double tolerance) {
        if (gyroZ + tolerance < degree) {
            mFL.setPower(power);
            mFR.setPower(power + 0.3);
            mBL.setPower(power);
            mBR.setPower(power + 0.3);
        } else if (gyroZ - tolerance > degree) {
            mFL.setPower(power + 0.3);
            mFR.setPower(power);
            mBL.setPower(power + 0.3);
            mBR.setPower(power);
        } else {
            driveForward(power);
        }
    }

    //Drives right while correcting to face designated gyro heading
    public void driveRightGyro(double power, double degree, double tolerance) {
        if (gyroZ + tolerance < degree) {
            mFL.setPower(power);
            mFR.setPower(-power + 0.1);
            mBL.setPower(-power);
            mBR.setPower(power + 0.1);
        } else if (gyroZ - tolerance > degree) {
            mFL.setPower(power + 0.1);
            mFR.setPower(-power);
            mBL.setPower(-power + 0.1);
            mBR.setPower(power);
        } else {
            driveRight(power);
        }
    }

    public void gyroAdjust(double power, double degree) {

        if (gyroZ > degree) {
            rotateClockwise(power);
        } else if (gyroZ < degree) {
            rotateCounterclockwise(power);
        } else {
            return;
        }
    }

    //Sigmoid function for rotating clockwise
    public void rotateSigmoid(double degree) {
        double power = (1 / (1 + Math.pow(Math.E, -(0.07 * (gyroZ - degree))))) - 0.5;
        if (power < 0.2 && power > 0) {
            power = 0.2;
        }
        if (power > -0.2 && power < 0) {
            power = -0.2;
        }
        rotateClockwise(power);
    }

    //Displays all sensor readings and important variables for debugging
    public void sensorTelemetry() {
        gyroUpdate();

        telemetry.addData("Gyro: ", gyroZ);
        telemetry.addData("Runtime: ", String.format("%.01f sec", runtime.seconds()));
        telemetry.addData("mBR ticks", mBR.getCurrentPosition());
        telemetry.update();
    }

    public void moveToPoseUpdates(double targetX, double targetY, double gyroLock, boolean drive) {

        double distanceX = (targetX - pose.x);
        double distanceY = (targetY - pose.y);
        double distanceTheta = gyroLock - gyroZ;

        double leftX = distanceX / 20;
        double leftY = -distanceY / 20;
        double rightX = distanceTheta / 180;

        double strafeAssist = 0;

        /*
        if(gyroZ > gyroLock + 2) {
            strafeAssist = (gyroZ - gyroLock) / 25;
        } else if(gyroZ < gyroLock - 2) {
            strafeAssist = (gyroZ - gyroLock) / 25;
        } else {
            strafeAssist = 0;
        }
        */

        double mFLPower = (leftX + leftY) * Math.sin(Math.toRadians(gyroZ) + (Math.PI / 4)) - rightX + strafeAssist;
        double mFRPower = (leftX - leftY) * Math.cos(Math.toRadians(gyroZ) + (Math.PI / 4)) + rightX - strafeAssist;
        double mBLPower = (leftX - leftY) * Math.cos(Math.toRadians(gyroZ) + (Math.PI / 4)) - rightX + strafeAssist;
        double mBRPower = (leftX + leftY) * Math.sin(Math.toRadians(gyroZ) + (Math.PI / 4)) + rightX - strafeAssist;

        telemetry.addData("mFL", mFLPower);
        telemetry.addData("mFR", mFRPower);
        telemetry.addData("mBL", mBLPower);
        telemetry.addData("mBR", mBRPower);
        telemetry.update();

        if (drive) {
            mFL.setPower(mFLPower);
            mFR.setPower(mFRPower);
            mBL.setPower(mBLPower);
            mBR.setPower(mBRPower);
        }
    }


    public void rotateToPose(double targetX, double targetY) {
        double distanceX = (targetX - pose.x);
        double distanceY = (targetY - pose.y);
        double tangentOf = (targetY - pose.y) / distanceX;

        double distanceToTheta = (Math.atan(tangentOf) - pose.theta);

        rotateSigmoid(Math.toDegrees(Math.atan(tangentOf)));

        telemetry.addData("Gyro", gyroZ);
        telemetry.addData("Target", Math.toDegrees(Math.atan(tangentOf)));
        telemetry.update();

    }

    //Shutdown all processes and stop drivetrain
    public void shutdown() {
        stopDriveTrain();
        stop();
    }

    public void changeStep() {
        runtime.reset();
        timeAtStop = stopTime.seconds();
        stopDriveTrain();
        isStartRecorded = false;
    }

    //Cases for auto steps
    public enum steps {
      /*  MOVE_TO_NE_CORNER,
        MOVE_TO_NW_CORNER,
        MOVE_TO_SW_CORNER,
        MOVE_TO_SE_CORNER,
        MOVE_TO_HOME,
        STOP
*/

        MOVE_TO_EAST,
        MOVE_TO_NE_CORNER,
       // MOVE_TO_EAST,
       // MOVE_TO_NE_CORNER,
       // PREP_TO_SHOOT,
        DELIVER_WOBBLE,
        GO_BACK_HOME,
        GO_BACK_HOME_SPIN,
        MOVE_TO_NW_CORNER,
        MOVE_TO_SW_CORNER,
        MOVE_TO_SE_CORNER,
        STOP



    }
}