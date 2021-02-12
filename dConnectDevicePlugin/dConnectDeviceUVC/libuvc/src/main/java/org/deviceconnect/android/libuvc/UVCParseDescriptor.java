/*
 UVCParseDescriptor.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.libuvc;

import org.deviceconnect.android.libusb.descriptor.Descriptor;
import org.deviceconnect.android.libusb.descriptor.Scanner;

import java.io.IOException;

/**
 * UVC のディスクリプタを解析します.
 *
 * @author NTT DOCOMO, INC.
 */
class UVCParseDescriptor implements Descriptor {

    private static final int CC_VIDEO = 0x0E;

    private static final int SC_VIDEOCONTROL = 0x01;
    private static final int SC_VIDEOSTREAMING = 0x02;

    private static final int VC_HEADER = 0x01;
    private static final int VC_INPUT_TERMINAL = 0x02;
    private static final int VC_OUTPUT_TERMINAL = 0x03;
    private static final int VC_SELECTOR_UNIT = 0x04;
    private static final int VC_PROCESSING_UNIT = 0x05;
    private static final int VC_EXTENSION_UNIT = 0x06;
    private static final int VC_ENCODING_UNIT = 0x07;

    private UVCParseDescriptor() {}

    /**
     * アクティブにする Configuration の ID を取得します.
     * <p>
     * bcdUVC (UVCバージョン)が一番大きい値を格納しているConfigurationのIDを取得します。
     * </p>
     * <p>
     * THETA S では、bcdUVC が 0x0110 と 0x0150 の2つを持っているので、
     * 0x0150の Configuration を取得するようにしています。
     * </p>
     * @param data ディスクリプタ
     * @return ConfigurationのID
     * @throws IOException 解析に失敗した場合に発生
     */
    static int getConfigId(final byte[] data) throws IOException {
        if (data == null) {
            throw new IOException("data is null.");
        }

        int activeConfigId = 0;

        int _bConfigurationValue = 0;
        int _bcdUVC = 0;

        Scanner scanner = new Scanner(data);
        while (!scanner.isFinish()) {
            int bLength = scanner.readByte();
            byte bDescriptorType = scanner.readByte();
            switch (bDescriptorType) {
                case CONFIGURATION:
                    int wTotalLength = scanner.readShort();
                    int bNumInterfaces = scanner.readByte();
                    int bConfigurationValue = scanner.readByte();
                    int iConfiguration = scanner.readByte();
                    int bmAttributes = scanner.readByte();
                    int bMaxPower = scanner.readByte();
                    _bConfigurationValue = bConfigurationValue;
                    break;

                case INTERFACE:
                    int bInterfaceNumber = scanner.readByte();
                    int bAlternateSetting = scanner.readByte();
                    int bNumEndpoints = scanner.readByte();
                    int bInterfaceClass = scanner.readByte();
                    int bInterfaceSubClass = scanner.readByte();
                    int bInterfaceProtocol = scanner.readByte();
                    int iInterface = scanner.readByte();

                    if (bInterfaceClass == CC_VIDEO) {
                        switch (bInterfaceSubClass) {
                            case SC_VIDEOCONTROL:
                                int bcdUVC = parseVC(scanner);
                                if (bcdUVC > _bcdUVC) {
                                    _bcdUVC = bcdUVC;
                                    activeConfigId = _bConfigurationValue;
                                }
                                break;

                            case SC_VIDEOSTREAMING:
                            default:
                                break;
                        }
                    }
                    break;

                default:
                    scanner.skip(bLength - 2);
                    break;
            }
        }
        return activeConfigId;
    }

    /**
     * Video Control から bcdUVC の値を取得します.
     *
     * @param scanner ディスクリプタのスキャナ
     * @return bcdUVCの値
     * @throws IOException スキャナの読み込みに失敗した場合に発生
     */
    private static int parseVC(final Scanner scanner) throws IOException {
        int bcdUVC = 0;
        while (!scanner.isFinish()) {
            int bLength = scanner.readByte();
            byte bDescriptorType = scanner.readByte();
            switch (bDescriptorType) {
                case INTERFACE:
                    scanner.skip(bLength - 2);
                    return bcdUVC;

                case CS_INTERFACE: {
                    int bDescriptorSubtype = scanner.readByte();
                    switch (bDescriptorSubtype) {
                        case VC_HEADER:
                            bcdUVC = scanner.readShort();
                            int wTotalLength = scanner.readShort();
                            int dwClockFrequency = scanner.readInt();
                            int bInCollection = scanner.readByte();
                            for (int i = 0;i < bInCollection; i++) {
                                scanner.readByte();
                            }
                            break;
                        case VC_INPUT_TERMINAL:
                        case VC_OUTPUT_TERMINAL:
                        case VC_SELECTOR_UNIT:
                        case VC_PROCESSING_UNIT:
                        case VC_EXTENSION_UNIT:
                        case VC_ENCODING_UNIT:
                        default:
                            scanner.skip(bLength - 3);
                            break;
                    }
                }   break;

                case ENDPOINT:
                case CS_ENDPOINT:
                default:
                    scanner.skip(bLength - 2);
                    break;
            }
        }
        return bcdUVC;
    }
}
