package org.deviceconnect.android.deviceplugin.awsiot.udt;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.UUID;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class StunClient {

    private static final boolean DEBUG = false;
    private static final String TAG = "UDT";

    private String mStunServer = "stun1.l.google.com";
    private int mPort = 19302;

    private int mTimeout = 30000;
    private byte[] mUniqueId;

    private MappedAddress mMappedAddress;

    public StunClient() {
        mUniqueId = generateUUID();
    }

    public void setStunServer(final String server, final int port) {
        mStunServer = server;
        mPort = port;
    }

    public void setTimeout(final int timeout) {
        mTimeout = timeout;
    }

    public boolean bindingRequest() {
        return bindingRequest(-1);
    }

    public boolean bindingRequest(final int port) {
        DatagramSocket socket = null;
        try {
            if (port <= 0) {
                socket = new DatagramSocket();
            } else {
                socket = new DatagramSocket(port);
            }
            socket.connect(InetAddress.getByName(mStunServer), mPort);
            socket.setSoTimeout(mTimeout);
            return bindingCommunicationInitialSocket(socket);
        } catch (IOException | UtilityException | MessageHeaderParsingException | MessageAttributeParsingException e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to binding Request." + e.getMessage(), e);
            }
           return false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public String getMappedAddress() {
        if (mMappedAddress == null) {
            return null;
        }
        return mMappedAddress.getAddress().toString();
    }

    public int getMappedPort() {
        if (mMappedAddress == null) {
            return -1;
        }
        return mMappedAddress.getPort();
    }

    private boolean bindingCommunicationInitialSocket(final DatagramSocket socket) throws UtilityException, IOException, MessageHeaderParsingException, MessageAttributeParsingException {
        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        sendMH.setTransactionID(mUniqueId);
        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);
        byte[] data = sendMH.getBytes();

        DatagramPacket send = new DatagramPacket(data, data.length, InetAddress.getByName(mStunServer), mPort);
        socket.send(send);
        if (DEBUG) {
            Log.i(TAG, "Binding Request sent.");
        }

        final byte[] buf = new byte[256];
        MessageHeader receiveMH = new MessageHeader();
        while (!(receiveMH.equalTransactionID(sendMH))) {
            DatagramPacket receive = new DatagramPacket(buf, buf.length);
            socket.receive(receive);
            receiveMH = MessageHeader.parseHeader(receive.getData());
            receiveMH.parseAttributes(receive.getData());
        }

        mMappedAddress = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
        if (ec != null) {
            if (DEBUG) {
                Log.e(TAG, "Message header contains an ErrorCode message attribute. ErrorCode=" + ec);
            }
            return false;
        }
        if (mMappedAddress == null) {
            if (DEBUG) {
                Log.e(TAG, "Response does not contain a Mapped Address message attribute.");
            }
            return false;
        }

        if (DEBUG) {
            Log.i(TAG, "Address: " + mMappedAddress.getAddress().toString());
            Log.i(TAG, "Port: " + mMappedAddress.getPort());
        }
        return true;
    }

    private byte[] generateUUID() {
        byte[] uniqueId = new byte[16];
        UUID uuid = UUID.randomUUID();
        byte[] m = fromLong(uuid.getMostSignificantBits());
        byte[] l = fromLong(uuid.getLeastSignificantBits());
        System.arraycopy(m, 0, uniqueId, 0, m.length);
        System.arraycopy(l, 0, uniqueId, m.length, l.length);
        return uniqueId;
    }

    private byte[] fromLong(long value) {
        int arraySize = Long.SIZE / Byte.SIZE;
        ByteBuffer buffer = ByteBuffer.allocate(arraySize);
        return buffer.putLong(value).array();
    }
}
