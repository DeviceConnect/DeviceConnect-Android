package org.deviceconnect.android.deviceplugin.hvc.comm;

import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;

import omron.HVC.HVC;
import omron.HVC.HVCBleCallback;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

/**
 * HVC Detect thread.
 */
public class HvcDetectThread extends Thread {
    /**
     * Constructor.
     */
    private Context mContext;
    /**
     * Bluetooth Device.
     */
    private BluetoothDevice mDevice;
    /**
     * Use function bit flag.
     */
    private int mUseFunc;
    /**
     * HVC detect listener.
     */
    private HvcDetectListener mListener;
    
    /**
     * HVC BLE class.
     */
    HVC_BLE mHvcBle = new HVC_BLE();
    /**
     * HVC parameter class.
     */
    HVC_PRM mHvcPrm = new HVC_PRM();
    /**
     * HVC response class.
     */
    HVC_RES mHvcRes = new HVC_RES();
    
    /**
     * Constructor.
     * @param context Context
     * @param device bluetooth device
     * @param useFunc use function bit flag
     * @param params parameter
     * @param listener listener
     */
    public HvcDetectThread(final Context context, final BluetoothDevice device, final int useFunc,
            final HvcDetectRequestParams params, final HvcDetectListener listener) {
        super();
        mContext = context;
        mDevice = device;
        mUseFunc = useFunc;
        mHvcPrm = params.getHvcParams();
        mListener = listener;
    }
    
    @Override
    public void run() {
        
        // BLE initialize (GATT)
        mHvcBle.setCallBack(new HVCBleCallback() {
            @Override
            public void onDisconnected() {
                mListener.onDetectFaceDisconnected();
                super.onDisconnected();
            }
            @Override
            public void onPostExecute(final int nRet, final byte outStatus) {
                if (nRet != HVC.HVC_NORMAL || outStatus != 0) {
                    // Error processing
                } else {
                    mListener.onDetectFinished(mHvcRes);
                }
            }
        });
        mHvcBle.connect(mContext/*getApplicationContext()*/, mDevice);
        
    	/*
    	 * コマンド送信(スレッドを起動してすぐ応答を返す。スレッドが受信処理を行ってコールバックを実行する)
		 * SetCameraAngle()
		 *         nRet = SendCommand(HVC_COM_SET_CAMERA_ANGLE(0x01), 1, sendData);
		 *         nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);	※HVC_BLE#Receive()でinTimeOutTimeまで応答を待つ。
		 * SetThreshold()
		 *         nRet = SendCommand(HVC_COM_SET_THRESHOLD(0x05), 8, sendData);
		 *         nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);	※HVC_BLE#Receive()でinTimeOutTimeまで応答を待つ。
		 * SetSizeRange()
		 *         nRet = SendCommand(HVC_COM_SET_SIZE_RANGE(0x07), 12, sendData);
		 *         nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);	※HVC_BLE#Receive()でinTimeOutTimeまで応答を待つ。
		 * SetFaceDetectionAngle()
		 *         nRet = SendCommand(HVC_COM_SET_DETECTION_ANGLE(0x09), 2, sendData);
		 *         nRet = ReceiveHeader(inTimeOutTime, nSize, outStatus);	※HVC_BLE#Receive()でinTimeOutTimeまで応答を待つ。
		 * 
		 * 結果はコールバックで返す。
		 * mCallback.onPostSetParam()
		 */
    	mHvcPrm.face.MinSize = 60;
    	mHvcPrm.face.MaxSize = 240;
    	mHvcBle.setParam(mHvcPrm);
    	wait(10);				/* 結果を受信するまでwaitする(本来ならコールバック側に処理を書くべき) */

    	/*
    	 * 受信したときの処理？
		 * SendCommand(HVC_COM_EXECUTE, 3(送信するデータ(3: 検出実行)), sendData);
		 * ReceiveHeader(inTimeOutTime, nSize, outStatus);
		 * result data を解析してmHvcResに格納する。
    	 */
    	
    	/* 検出実行する機能を指定するビットフラグ */
//        int useFunc = HVC.HVC_ACTIV_BODY_DETECTION |	/* 0x01 */
//                   HVC.HVC_ACTIV_HAND_DETECTION |		/* 0x02 */
//                   HVC.HVC_ACTIV_FACE_DETECTION |		/* 0x04 */
//                   HVC.HVC_ACTIV_FACE_DIRECTION |		/* 0x08 */
//                   HVC.HVC_ACTIV_AGE_ESTIMATION |		/* 0x10 */
//                   HVC.HVC_ACTIV_GENDER_ESTIMATION |	/* 0x20 */
//                   HVC.HVC_ACTIV_GAZE_ESTIMATION |		/* 0x40 */
//                   HVC.HVC_ACTIV_BLINK_ESTIMATION |		/* 0x80 */
//                   HVC.HVC_ACTIV_EXPRESSION_ESTIMATION;	/* 0x100 */
		/*
		 * 検出実行(通信スレッドを起動してすぐ戻る)、30msec待ってループする
		 * ・通信スレッド リクエスト送信時に mStatus == STATE_BUSY を設定する。
		 * ・レスポンス受信時またはタイムアウト(30秒)時に、mStatus = STATE_CONNECTED を設定する。
		 *   ※タイムアウト通知がこないのでコールバックを追加する必要あり)
		 */
        mHvcBle.execute(mUseFunc, mHvcRes);
		wait(30);

    	mHvcBle.disconnect();
	}
	
    public void wait(int nWaitCount)
    {
        do {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if ( !mHvcBle.IsBusy() ) {
                return;
            }
            nWaitCount--;
        } while ( nWaitCount > 0 );
    }
}
