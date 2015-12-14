/*
 * Copyright (C) 2014 OMRON Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package omron.HVC;

import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;

/**
 * HVC object<br>
 * [Description]<br>
 * New class object definition HVC<br>
 */
public abstract class HVC {
    /**
     * Execution flag for Human Body Detection<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_BODY_DETECTION = 0x00000001;
    /**
     * Execution flag for Hand Detection<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_HAND_DETECTION = 0x00000002;
    /**
     * Execution flag for Face Detection<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_FACE_DETECTION = 0x00000004;
    /**
     * Execution flag for face direction estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_FACE_DIRECTION = 0x00000008;
    /**
     * Execution flag for Age Estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_AGE_ESTIMATION = 0x00000010;
    /**
     * Execution flag for Gender Estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_GENDER_ESTIMATION = 0x00000020;
    /**
     * Execution flag for Gaze Estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_GAZE_ESTIMATION = 0x00000040;
    /**
     * Execution flag for Blink Estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_BLINK_ESTIMATION = 0x00000080;
    /**
     * Execution flag for Expression Estimation<br>
     * Specify in inExec of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ACTIV_EXPRESSION_ESTIMATION = 0x00000100;

    /**
     * Normal end<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_NORMAL = 0;
    /**
     * Parameter error (invalid inExec specification)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_PARAMETER = -1;
    /**
     * Device error (device not found)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_NODEVICES = -2;
    /**
     * Connection error (cannot connect to HVC device)<br>
     * return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_DISCONNECTED = -3;
    /**
     * Input error (cannot re-input as HVC is already executing)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_BUSY = -4;
    /**
     * Send signal timeout error (timeout while sending command signal to HVC)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_SEND_DATA = -10;
    /**
     * Receive header signal timeout error (timeout while receiving header signal from HVC)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_HEADER_TIMEOUT = -20;
    /**
     * Invalid header error (invalid header data received from HVC)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_HEADER_INVALID = -21;
    /**
     * Receive data signal timeout error (timeout while receiving data signal from HVC)<br>
     * Return value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_ERROR_DATA_TIMEOUT = -22;

    /**
     * Normal end<br>
     * outStatus value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_STATUS_NORMAL = 0;
    /**
     * Unknown command<br>
     * outStatus value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_STATUS_UNKNOWN = -1;
    /**
     * Unexpected error<br>
     * outStatus value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_STATUS_VARIOUS = -2;
    /**
     * Invalid command<br>
     * outStatus value of Execute(int inExec, byte[] outStatus)<br>
     */
    public static final int HVC_STATUS_INVALID = -3;

    /**
     * Male<br>
     * Gender Estimation result value<br>
     */
    public static final int HVC_GEN_MALE = 1;
    /**
     * Female<br>
     * Gender Estimation result value<br>
     */
    public static final int HVC_GEN_FEMALE = 0;

    /**
     * Neutral<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_NEUTRAL = 1;
    /**
     * Happiness<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_HAPPINESS = 2;
    /**
     * Surprise<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_SURPRISE = 3;
    /**
     * Anger<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_ANGER = 4;
    /**
     * Sadness<br>
     * Expression Estimation result value<br>
     */
    public static final int HVC_EX_SADNESS = 5;

    /**
     * HVC constructor<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    protected HVC() {
    }

    /**
     * HVC finalize<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    protected abstract boolean IsBusy();
    protected abstract int execute(int inExec, HVC_RES res);
    protected abstract int setParam(HVC_PRM prm);
    protected abstract int getParam(HVC_PRM prm);
    protected abstract int getVersion(HVC_VER ver);

    /* Command number */
    private static final char HVC_COM_GET_VERSION = 0x00;
    private static final char HVC_COM_SET_CAMERA_ANGLE = 0x01;
    private static final char HVC_COM_GET_CAMERA_ANGLE = 0x02;
    private static final char HVC_COM_EXECUTE = 0x03;
    private static final char HVC_COM_SET_THRESHOLD = 0x05;
    private static final char HVC_COM_GET_THRESHOLD = 0x06;
    private static final char HVC_COM_SET_SIZE_RANGE = 0x07;
    private static final char HVC_COM_GET_SIZE_RANGE = 0x08;
    private static final char HVC_COM_SET_DETECTION_ANGLE = 0x09;
    private static final char HVC_COM_GET_DETECTION_ANGLE = 0x0A;

    /* Header for send signal data  */
    private static final int SEND_HEAD_SYNCBYTE = 0;
    private static final int SEND_HEAD_COMMANDNO = 1;
    private static final int SEND_HEAD_DATALENGTHLSB = 2;
    private static final int SEND_HEAD_DATALENGTHMSB = 3;
    private static final int SEND_HEAD_NUM = 4;

    /* Header for receive signal data */
    private static final int RECEIVE_HEAD_SYNCBYTE = 0;
    private static final int RECEIVE_HEAD_STATUS = 1;
    private static final int RECEIVE_HEAD_DATALENLL = 2;
    private static final int RECEIVE_HEAD_DATALENLM = 3;
    private static final int RECEIVE_HEAD_DATALENML = 4;
    private static final int RECEIVE_HEAD_DATALENMM = 5;
    private static final int RECEIVE_HEAD_NUM = 6;

    protected abstract int Send(byte[] inData);
    protected abstract int Receive(int inTimeOutTime, int inDataSize, byte[] outResult);

    /**
     * Send command signal<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inCommandNo command number<br>
     * @param inDataSize sending signal data size<br>
     * @param inData sending signal data<br>
     */
    protected int SendCommand(char inCommandNo, int inDataSize, byte[] inData) {
        int i;
        int nRet = 0;
        byte[] sendData;

        sendData = new byte[SEND_HEAD_NUM + inDataSize];

        /* Create header */
        sendData[SEND_HEAD_SYNCBYTE] = -2;
        sendData[SEND_HEAD_COMMANDNO] = (byte) inCommandNo;
        sendData[SEND_HEAD_DATALENGTHLSB] = (byte) (inDataSize & 0xff);
        sendData[SEND_HEAD_DATALENGTHMSB] = (byte) ((inDataSize >> 8) & 0xff);

        for (i = 0; i < inDataSize; i++) {
            sendData[SEND_HEAD_NUM + i] = (byte) (inData[i] & 0xff);
        }

        /* Send command signal */
        nRet = Send(sendData);
        if (nRet != SEND_HEAD_NUM + inDataSize) {
            return HVC_ERROR_SEND_DATA;
        }
        return HVC_NORMAL;
    }

    /**
     * Receive header<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outDataSize receive signal data length<br>
     * @param outStatus status<br>
     */
    protected int ReceiveHeader(int inTimeOutTime, int outDataSize[], byte[] outStatus) {
        int nRet = 0;
        byte[] headerData;

        headerData = new byte[32];

        /* Get header part */
        nRet = Receive(inTimeOutTime, RECEIVE_HEAD_NUM, headerData);
        if (nRet != RECEIVE_HEAD_NUM) {
            return HVC_ERROR_HEADER_TIMEOUT;
        } else if ( (headerData[RECEIVE_HEAD_SYNCBYTE] & 0xff) != 0xfe ) {
        /* Different value indicates an invalid result */
            return HVC_ERROR_HEADER_INVALID;
        }

        /* Get data length */
        outDataSize[0] = (headerData[RECEIVE_HEAD_DATALENLL] & 0xff) +
                ((headerData[RECEIVE_HEAD_DATALENLM] & 0xff) << 8) +
                ((headerData[RECEIVE_HEAD_DATALENML] & 0xff) << 16) +
                ((headerData[RECEIVE_HEAD_DATALENMM] & 0xff) << 24);

        /* Get command execution result */
        outStatus[0] = headerData[RECEIVE_HEAD_STATUS];
        return HVC_NORMAL;
    }

    /**
     * Receive data<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param inDataSize receive signal data size<br>
     * @param outResult receive signal data<br>
     */
    protected int ReceiveData(int inTimeOutTime, int inDataSize, byte[] outResult) {
        int nRet = 0;

        if (inDataSize <= 0) return HVC_NORMAL;

        /* Receive data */
        nRet = Receive(inTimeOutTime, inDataSize, outResult);
        if (nRet != inDataSize) {
            return HVC_ERROR_DATA_TIMEOUT;
        }
        return HVC_NORMAL;
    }

    /**
     * GetVersion<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int GetVersion(int inTimeOutTime, byte[] outStatus, HVC_VER ver) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        /* Send GetVersion command signal*/
        nRet = SendCommand(HVC_COM_GET_VERSION, 0, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        if (nSize[0] > ver.GetSize()) {
            nSize[0] = ver.GetSize();
        }

        /* Receive data */
        nRet = ReceiveData(inTimeOutTime, nSize[0], recvData);
        for (int i = 0; i < ver.str.length; i++) ver.str[i] = (char) recvData[i];
        ver.major = recvData[ver.str.length];
        ver.minor = recvData[ver.str.length + 1];
        ver.relese = recvData[ver.str.length + 2];
        ver.rev = recvData[ver.str.length + 3] + (recvData[ver.str.length + 4] << 8) + (recvData[ver.str.length + 5] << 16) + (recvData[ver.str.length + 6] << 24);
        return nRet;
    }

    /**
     * SetCameraAngle<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int SetCameraAngle(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;

        sendData = new byte[32];

        sendData[0] = (byte) (param.CameraAngle & 0xff);
        /* Send SetCameraAngle command signal */
        nRet = SendCommand(HVC_COM_SET_CAMERA_ANGLE, 1, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;
        return HVC_NORMAL;
    }

    /**
     * GetCameraAngle<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int GetCameraAngle(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        /* Send GetCameraAngle command signal */
        nRet = SendCommand(HVC_COM_GET_CAMERA_ANGLE, 0, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        if (nSize[0] > 1) {
            nSize[0] = 1;
        }

        /* Receive data */
        nRet = ReceiveData(inTimeOutTime, nSize[0], recvData);
        param.CameraAngle = recvData[0];
        return nRet;
    }

    /**
     * SetThreshold<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int SetThreshold(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;

        sendData = new byte[32];

        sendData[0] = (byte) (param.body.Threshold & 0xff);
        sendData[1] = (byte) ((param.body.Threshold >> 8) & 0xff);
        sendData[2] = (byte) (param.hand.Threshold & 0xff);
        sendData[3] = (byte) ((param.hand.Threshold >> 8) & 0xff);
        sendData[4] = (byte) (param.face.Threshold & 0xff);
        sendData[5] = (byte) ((param.face.Threshold >> 8) & 0xff);
        sendData[6] = 0;
        sendData[7] = 0;
        /* Send SetThreshold command signal */
        nRet = SendCommand(HVC_COM_SET_THRESHOLD, 8, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;
        return HVC_NORMAL;
    }

    /**
     * GetThreshold<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int GetThreshold(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        /* Send GetThreshold command signal */
        nRet = SendCommand(HVC_COM_GET_THRESHOLD, 0, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        if (nSize[0] > 8) {
            nSize[0] = 8;
        }

        /* Receive data */
        nRet = ReceiveData(inTimeOutTime, nSize[0], recvData);
        param.body.Threshold = recvData[0] + (recvData[1] << 8);
        param.hand.Threshold = recvData[2] + (recvData[3] << 8);
        param.face.Threshold = recvData[4] + (recvData[5] << 8);
        return nRet;
    }

    /**
     * SetSizeRange<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int SetSizeRange(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;

        sendData = new byte[32];

        sendData[0] = (byte) (param.body.MinSize & 0xff);
        sendData[1] = (byte) ((param.body.MinSize >> 8) & 0xff);
        sendData[2] = (byte) (param.body.MaxSize & 0xff);
        sendData[3] = (byte) ((param.body.MaxSize >> 8) & 0xff);
        sendData[4] = (byte) (param.hand.MinSize & 0xff);
        sendData[5] = (byte) ((param.hand.MinSize >> 8) & 0xff);
        sendData[6] = (byte) (param.hand.MaxSize & 0xff);
        sendData[7] = (byte) ((param.hand.MaxSize >> 8) & 0xff);
        sendData[8] = (byte) (param.face.MinSize & 0xff);
        sendData[9] = (byte) ((param.face.MinSize >> 8) & 0xff);
        sendData[10] = (byte) (param.face.MaxSize & 0xff);
        sendData[11] = (byte) ((param.face.MaxSize >> 8) & 0xff);
        /* Send SetSizeRange command signal */
        nRet = SendCommand(HVC_COM_SET_SIZE_RANGE, 12, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;
        return HVC_NORMAL;
    }

    /**
     * GetSizeRange<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int GetSizeRange(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        /* Send GetSizeRange command signal */
        nRet = SendCommand(HVC_COM_GET_SIZE_RANGE, 0, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        if (nSize[0] > 12) {
            nSize[0] = 12;
        }

        /* Receive data */
        nRet = ReceiveData(inTimeOutTime, nSize[0], recvData);
        param.body.MinSize = recvData[0] + (recvData[1] << 8);
        param.body.MaxSize = recvData[2] + (recvData[3] << 8);
        param.hand.MinSize = recvData[4] + (recvData[5] << 8);
        param.hand.MaxSize = recvData[6] + (recvData[7] << 8);
        param.face.MinSize = recvData[8] + (recvData[9] << 8);
        param.face.MaxSize = recvData[10] + (recvData[11] << 8);
        return nRet;
    }

    /**
     * SetFaceDetectionAngle<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int SetFaceDetectionAngle(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;

        sendData = new byte[32];

        sendData[0] = (byte) (param.face.Pose & 0xff);
        sendData[1] = (byte) (param.face.Angle & 0xff);
        /* Send SetFaceDetectionAngle command signal */
        nRet = SendCommand(HVC_COM_SET_DETECTION_ANGLE, 2, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;
        return HVC_NORMAL;
    }

    /**
     * GetFaceDetectionAngle<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param outStatus response code<br>
     */
    protected int GetFaceDetectionAngle(int inTimeOutTime, byte[] outStatus, HVC_PRM param) {
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        /* Send GetFaceDetectionAngle signal command */
        nRet = SendCommand(HVC_COM_GET_DETECTION_ANGLE, 0, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        if (nSize[0] > 2) {
            nSize[0] = 2;
        }

        /* Receive data */
        nRet = ReceiveData(inTimeOutTime, nSize[0], recvData);
        param.face.Pose = recvData[0];
        param.face.Angle = recvData[1];
        return nRet;
    }

    /**
     * Execute<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inTimeOutTime timeout time<br>
     * @param inExec executable function<br>
     * @param outStatus response code<br>
     */
    protected int Execute(int inTimeOutTime, int inExec, byte[] outStatus, HVC_RES result) {
        int i;
        int nRet = 0;
        int nSize[] = {0};
        byte[] sendData;
        byte[] recvData;

        sendData = new byte[32];
        recvData = new byte[32];

        result.executedFunc = inExec;
        result.body.removeAll(result.body);
        result.hand.removeAll(result.hand);
        result.face.removeAll(result.face);

        /* Send Execute command signal */
        sendData[0] = (byte) (inExec & 0xff);
        sendData[1] = (byte) ((inExec >> 8) & 0xff);
        sendData[2] = 0;
        nRet = SendCommand(HVC_COM_EXECUTE, 3, sendData);
        if (nRet != 0) return nRet;

        /* Receive header */
        nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);
        if (nRet != 0) return nRet;

        int numBody = 0;
        int numHand = 0;
        int numFace = 0;
        /* Receive result data */
        if (nSize[0] >= 4) {
            nRet = ReceiveData(inTimeOutTime, 4, recvData);
            numBody = (recvData[0] & 0xff);
            numHand = (recvData[1] & 0xff);
            numFace = (recvData[2] & 0xff);
            if (nRet != 0) return nRet;
            nSize[0] -= 4;
        }

        /* Get Human Body Detection result */
        for (i = 0; i < numBody; i++) {
        	DetectionResult body = new HVC_RES().new DetectionResult();

            if (nSize[0] >= 8) {
                nRet = ReceiveData(inTimeOutTime, 8, recvData);
                body.posX = ((recvData[0] & 0xff) + (recvData[1] << 8));
                body.posY = ((recvData[2] & 0xff) + (recvData[3] << 8));
                body.size = ((recvData[4] & 0xff) + (recvData[5] << 8));
                body.confidence = ((recvData[6] & 0xff) + (recvData[7] << 8));
                if (nRet != 0) return nRet;
                nSize[0] -= 8;
            }

            result.body.add(body);
        }

        /* Get Hand Detection result */
        for (i = 0; i < numHand; i++) {
        	DetectionResult hand = new HVC_RES().new DetectionResult();

            if (nSize[0] >= 8) {
                nRet = ReceiveData(inTimeOutTime, 8, recvData);
                hand.posX = ((recvData[0] & 0xff) + (recvData[1] << 8));
                hand.posY = ((recvData[2] & 0xff) + (recvData[3] << 8));
                hand.size = ((recvData[4] & 0xff) + (recvData[5] << 8));
                hand.confidence = ((recvData[6] & 0xff) + (recvData[7] << 8));
                if (nRet != 0) return nRet;
                nSize[0] -= 8;
            }

            result.hand.add(hand);
        }

        /* Face-related results */
        for (i = 0; i < numFace; i++) {
            FaceResult face = new HVC_RES().new FaceResult();

            /* Face Detection result */
            if (0 != (result.executedFunc & HVC_ACTIV_FACE_DETECTION)) {
                if (nSize[0] >= 8) {
                    nRet = ReceiveData(inTimeOutTime, 8, recvData);
                    face.posX = ((recvData[0] & 0xff) + (recvData[1] << 8));
                    face.posY = ((recvData[2] & 0xff) + (recvData[3] << 8));
                    face.size = ((recvData[4] & 0xff) + (recvData[5] << 8));
                    face.confidence = ((recvData[6] & 0xff) + (recvData[7] << 8));
                    if (nRet != 0) return nRet;
                    nSize[0] -= 8;
                }
            }

            /* Face direction */
            if (0 != (result.executedFunc & HVC_ACTIV_FACE_DIRECTION)) {
                if (nSize[0] >= 8) {
                    nRet = ReceiveData(inTimeOutTime, 8, recvData);
                    face.dir.yaw = (short) ((recvData[0] & 0xff) + (recvData[1] << 8));
                    face.dir.pitch = (short) ((recvData[2] & 0xff) + (recvData[3] << 8));
                    face.dir.roll = (short) ((recvData[4] & 0xff) + (recvData[5] << 8));
                    face.dir.confidence = (short) ((recvData[6] & 0xff) + (recvData[7] << 8));
                    if (nRet != 0) return nRet;
                    nSize[0] -= 8;
                }
            }

            /* Age */
            if (0 != (result.executedFunc & HVC_ACTIV_AGE_ESTIMATION)) {
                if (nSize[0] >= 3) {
                    nRet = ReceiveData(inTimeOutTime, 3, recvData);
                    face.age.age = recvData[0];
                    face.age.confidence = (short) ((recvData[1] & 0xff) + (recvData[2] << 8));
                    if (nRet != 0) return nRet;
                    nSize[0] -= 3;
                }
            }

            /* Gender */
            if (0 != (result.executedFunc & HVC_ACTIV_GENDER_ESTIMATION)) {
                if (nSize[0] >= 3) {
                    nRet = ReceiveData(inTimeOutTime, 3, recvData);
                    face.gen.gender = recvData[0];
                    face.gen.confidence = (short) ((recvData[1] & 0xff) + (recvData[2] << 8));
                    if (nRet != 0) return nRet;
                    nSize[0] -= 3;
                }
            }

            /* Gaze */
            if (0 != (result.executedFunc & HVC_ACTIV_GAZE_ESTIMATION)) {
                if (nSize[0] >= 2) {
                    nRet = ReceiveData(inTimeOutTime, 2, recvData);
                    face.gaze.gazeLR = recvData[0];
                    face.gaze.gazeUD = recvData[1];
                    if (nRet != 0) return nRet;
                    nSize[0] -= 2;
                }
            }

            /* Blink */
            if (0 != (result.executedFunc & HVC_ACTIV_BLINK_ESTIMATION)) {
                if (nSize[0] >= 4) {
                    nRet = ReceiveData(inTimeOutTime, 4, recvData);
                    face.blink.ratioL = (short) ((recvData[0] & 0xff) + (recvData[1] << 8));
                    face.blink.ratioR = (short) ((recvData[2] & 0xff) + (recvData[3] << 8));
                    if (nRet != 0) return nRet;
                    nSize[0] -= 4;
                }
            }

            /* Expression */
            if (0 != (result.executedFunc & HVC_ACTIV_EXPRESSION_ESTIMATION)) {
                if (nSize[0] >= 3) {
                    nRet = ReceiveData(inTimeOutTime, 3, recvData);
                    face.exp.expression = recvData[0];
                    face.exp.score = recvData[1];
                    face.exp.degree = recvData[2];
                    if (nRet != 0) return nRet;
                    nSize[0] -= 3;
                }
            }

            result.face.add(face);
        }

        return HVC_NORMAL;
    }
}
