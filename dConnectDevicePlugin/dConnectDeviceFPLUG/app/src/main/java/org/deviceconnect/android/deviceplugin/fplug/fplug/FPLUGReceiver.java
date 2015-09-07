/*
 FPLUGReceiver.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.fplug.fplug;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.fplug.BuildConfig;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class provides functions of receiving values from F-PLUG.
 *
 * @author NTT DOCOMO, INC.
 */
public class FPLUGReceiver extends Thread {

    public interface FPLUGReceiverEventListener {
        void onDisconnected();

        void onReceiveResponse(FPLUGResponse response);

        void onReceiveError(String message);
    }

    private final static String TAG = "FPLUGReceiver";

    private int BUFFER_SIZE = 16;
    private boolean mIsClose = false;
    private boolean isWattHour = false;
    private boolean isSuccessGetWattHour = false;
    private boolean isPastValues = false;
    private boolean isSuccessGetPastValues = false;
    private byte[] totalBytes = null;
    private int offset = 0;

    private BluetoothSocket mSocket;
    private String mAddress;
    private FPLUGReceiverEventListener mListener;

    public FPLUGReceiver(String address, BluetoothSocket socket, FPLUGReceiverEventListener listener) {
        mAddress = address;
        mSocket = socket;
        mListener = listener;
    }

    @Override
    public void run() {
        while (!mIsClose) {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            try {
                len = mSocket.getInputStream().read(buf);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "len:" + len + " str:" + toHexString(buf));
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "read break e:" + e);
                }
                mListener.onDisconnected();
                break;
            }
            //handle multi read bytes
            if (isWattHour) {
                wattHourProcess(buf, len);
            } else if (isPastValues) {
                pastValuesProcess(buf, len);
            } else {
                parseCommand(buf);
            }
        }
    }

    public void close() {
        mIsClose = true;
    }

    private void parseCommand(byte[] buf) {
        int firstByte = buf[0] & 0xFF;
        //echonet-lite
        if (firstByte == 0x10) {
            int ehd2 = buf[1] & 0xFF;
            //specified
            if (ehd2 == 0x81) {
                int classCode = buf[5];
                if (classCode == 0x22) {
                    int serviceByte = buf[10];
                    if (serviceByte == 0x71 || serviceByte == 0x51) {
                        handlePlugInitResponse(buf);
                    } else if (serviceByte == 0x72 || serviceByte == 0x52) {
                        handleWattResponse(buf);
                    } else {
                        Log.e(TAG, "unknown parameter");
                    }
                } else if (classCode == 0x11) {
                    handleTemperature(buf);
                } else if (classCode == 0x12) {
                    handleHumidity(buf);
                } else if (classCode == 0x0D) {
                    handleIlluminance(buf);
                } else {
                    Log.e(TAG, "unknown parameter");
                }
            }
            //manual
            else if (ehd2 == 0x82) {
                int targetByte = buf[4] & 0xFF;
                if (targetByte == 0x91) {
                    handleWattHourResponse(buf);
                } else if (targetByte == 0x96) {
                    handlePastWattHourResponse(buf);
                } else if (targetByte == 0x97) {
                    handlePastValuesResponse(buf);
                } else {
                    Log.e(TAG, "unknown parameter");
                }
            } else {
                Log.e(TAG, "unknown parameter");
            }
        }
        //originals
        else if (firstByte == 0x86) {
            handlePairingResponse(buf);
        } else if (firstByte == 0x87) {
            handleSetDateResponse(buf);
        } else if (firstByte == 0x85) {
            handleLEDResponse(buf);
        }
        //unknown
        else {
            //When "init-plug" requested, 6 bytes by zero-filled comes after correct response.
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "unknown parameter");
            }
        }
    }

    private void wattHourProcess(byte[] buf, int len) {
        System.arraycopy(buf, 0, totalBytes, offset, len);
        offset += len;
        if (offset == 72) {
            isWattHour = false;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "str:" + toHexString(totalBytes));
            }
            ArrayList<WattHour> dataList = new ArrayList<>();
            int hoursAgo = 24;
            byte buf1 = 0;
            byte buf2 = 0;
            int counter = 0;
            for (byte bt : totalBytes) {
                if (counter == 2) {
                    WattHour data = new WattHour();
                    if ((bt & 0xff) == 0x00) {
                        data.setReliable(true);
                    } else if ((bt & 0xff) == 0x01) {
                        data.setReliable(false);
                    }
                    data.setHoursAgo(hoursAgo);
                    data.setWatt((((int) buf2) * 256 + (buf1 & 0xff)));
                    dataList.add(data);

                    hoursAgo--;
                    counter = 0;
                    continue;
                } else if (counter == 1) {
                    buf2 = bt;
                } else {
                    buf1 = bt;
                }
                counter++;
            }
            if (isSuccessGetWattHour) {
                FPLUGResponse response = new FPLUGResponse();
                response.setAddress(mAddress);
                response.setWattHourList(dataList);
                mListener.onReceiveResponse(response);
            } else {
                mListener.onReceiveError("get watt hour failed");
            }
        }
    }

    private void pastValuesProcess(byte[] buf, int len) {
        System.arraycopy(buf, 0, totalBytes, offset, len);
        offset += len;
        if (offset == 120) {
            isPastValues = false;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "str:" + toHexString(totalBytes));
            }
            ArrayList<PastValues> dataList = new ArrayList<>();

            int hoursAgo = 24;
            byte temperatureByte1 = 0;
            byte temperatureByte2 = 0;
            byte humidityByte = 0;
            byte illuminanceByte1 = 0;
            byte illuminanceByte2;
            int counter = 0;

            for (byte bt : totalBytes) {
                switch (counter) {
                    case 0:
                        temperatureByte1 = bt;
                        break;
                    case 1:
                        temperatureByte2 = bt;
                        break;
                    case 2:
                        humidityByte = bt;
                        break;
                    case 3:
                        illuminanceByte1 = bt;
                        break;
                    case 4:
                        illuminanceByte2 = bt;

                        PastValues data = new PastValues();
                        data.setHoursAgo(hoursAgo);

                        int temp = (((int) temperatureByte2) * 256 + (temperatureByte1 & 0xff));
                        double temperature = (double) temp / 10;
                        data.setTemperature(temperature);

                        int humidity = (humidityByte & 0xff);
                        data.setHumidity(humidity);

                        int illuminance = (illuminanceByte1 & 0xff);
                        illuminance += (illuminanceByte2 & 0xff) * 256;
                        data.setIlluminance(illuminance);

                        dataList.add(data);

                        hoursAgo--;
                        counter = 0;
                        continue;
                    default:
                        Log.e(TAG, "wrong index");
                }
                counter++;
            }
            if (isSuccessGetPastValues) {
                FPLUGResponse response = new FPLUGResponse();
                response.setAddress(mAddress);
                response.setPastValuesList(dataList);
                mListener.onReceiveResponse(response);
            } else {
                mListener.onReceiveError("get past values failed");
            }
        }
    }

    private void handlePlugInitResponse(byte[] buf) {
        int serviceByte = buf[10];
        if (serviceByte == 0x71) {
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            mListener.onReceiveResponse(response);
        } else if (serviceByte == 0x51) {
            mListener.onReceiveError("cancel pairing failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handlePairingResponse(byte[] buf) {
        int type = buf[1] & 0xFF;
        if (type == 0x00) {
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            mListener.onReceiveResponse(response);
        } else if (type == 0x01) {
            mListener.onReceiveError("cancel pairing failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handleWattHourResponse(byte[] buf) {
        isWattHour = true;
        totalBytes = new byte[72];
        System.arraycopy(buf, 6, totalBytes, 0, BUFFER_SIZE - 6);
        offset = 10;

        if ((buf[5] & 0xff) == 0x00) {
            isSuccessGetWattHour = true;
        } else if ((buf[5] & 0xff) == 0x01) {
            isSuccessGetWattHour = false;
        }
    }

    private void handleTemperature(byte[] buf) {
        int serviceByte = buf[10];
        if (serviceByte == 0x72) {
            int temp = (((int) buf[15]) * 256 + (buf[14] & 0xff));
            double temperature = (double) temp / 10;
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            response.setTemperature(temperature);
            mListener.onReceiveResponse(response);
        } else if (serviceByte == 0x52) {
            mListener.onReceiveError("get humidity failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handleHumidity(byte[] buf) {
        int serviceByte = buf[10];
        if (serviceByte == 0x72) {
            int humidity = (buf[14] & 0xff);
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            response.setHumidity(humidity);
            mListener.onReceiveResponse(response);
        } else if (serviceByte == 0x52) {
            mListener.onReceiveError("get humidity failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handleIlluminance(byte[] buf) {
        int serviceByte = buf[10];
        if (serviceByte == 0x72) {
            int illuminance = (buf[14] & 0xff);
            illuminance += (buf[15] & 0xff) * 256;
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            response.setIlluminance(illuminance);
            mListener.onReceiveResponse(response);
        } else if (serviceByte == 0x52) {
            mListener.onReceiveError("get illuminance failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handleWattResponse(byte[] buf) {
        int serviceByte = buf[10];
        if (serviceByte == 0x72) {
            int temp = (((int) buf[15]) * 256 + (buf[14] & 0xff));
            double watt = (double) temp / 10;

            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            response.setRealtimeWatt(watt);
            mListener.onReceiveResponse(response);
        } else if (serviceByte == 0x52) {
            mListener.onReceiveError("get realtime watt failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handlePastWattHourResponse(byte[] buf) {
        isWattHour = true;
        totalBytes = new byte[72];
        System.arraycopy(buf, 6, totalBytes, 0, BUFFER_SIZE - 6);
        offset = 10;

        if ((buf[5] & 0xff) == 0x00) {
            isSuccessGetWattHour = true;
        } else if ((buf[5] & 0xff) == 0x01) {
            isSuccessGetWattHour = false;
        }
    }

    private void handlePastValuesResponse(byte[] buf) {
        isPastValues = true;
        totalBytes = new byte[120];
        System.arraycopy(buf, 6, totalBytes, 0, BUFFER_SIZE - 6);
        offset = 10;

        if ((buf[5] & 0xff) == 0x00) {
            isSuccessGetPastValues = true;
        } else if ((buf[5] & 0xff) == 0x01) {
            isSuccessGetPastValues = false;
        }
    }

    private void handleSetDateResponse(byte[] buf) {
        int type = buf[1] & 0xFF;
        if (type == 0x00) {
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            mListener.onReceiveResponse(response);
        } else if (type == 0x01) {
            mListener.onReceiveError("set date failed");
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private void handleLEDResponse(byte[] buf) {
        int type = buf[1] & 0xFF;
        if (type == 0x00 || type == 0x01) {
            FPLUGResponse response = new FPLUGResponse();
            response.setAddress(mAddress);
            mListener.onReceiveResponse(response);
        } else {
            mListener.onReceiveError("unknown response parameter");
        }
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String s = String.format("%02x", b);
            sb.append(s).append(" ");
        }
        return sb.toString();
    }

}
