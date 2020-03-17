package org.deviceconnect.android.libmedia.streaming.sdp;

import java.util.ArrayList;
import java.util.List;

public class MediaDescription {
    private static final String NEW_LINE = "\r\n";

    private String mMedia;
    private int mPort;
    private String mProto;
    private int mPayloadType;

    private Information mInformation;
    private EncryptionKey mEncryptionKey;
    private List<Connection> mConnections = new ArrayList<>();
    private List<Attribute> mAttributes = new ArrayList<>();
    private List<Bandwidth> mBandwidths = new ArrayList<>();

    public MediaDescription(String media, int port, String proto, int payloadType) {
        mMedia = media;
        mPort = port;
        mProto = proto;
        mPayloadType = payloadType;
    }

    public MediaDescription(String line) {
        String[] params = line.split(" ");
        mMedia = params[0];
        mPort = Integer.parseInt(params[1]);
        mProto = params[2];
        mPayloadType = Integer.parseInt(params[3]);
    }

    public String getMedia() {
        return mMedia;
    }

    public void setMedia(String media) {
        mMedia = media;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public String getProto() {
        return mProto;
    }

    public void setProto(String proto) {
        mProto = proto;
    }

    public int getPayloadType() {
        return mPayloadType;
    }

    public void setPayloadType(int payloadType) {
        mPayloadType = payloadType;
    }

    public Information getInformation() {
        return mInformation;
    }

    public void setInformation(Information information) {
        mInformation = information;
    }

    public EncryptionKey getEncryptionKey() {
        return mEncryptionKey;
    }

    public void setEncryptionKey(EncryptionKey encryptionKey) {
        mEncryptionKey = encryptionKey;
    }

    public void addConnection(Connection connection) {
        mConnections.add(connection);
    }

    public List<Connection> getConnections() {
        return mConnections;
    }

    public List<Bandwidth> getBandwidths() {
        return mBandwidths;
    }

    public void addBandwidth(Bandwidth bandwidth) {
        mBandwidths.add(bandwidth);
    }

    public void addAttribute(Attribute attribute) {
        mAttributes.add(attribute);
    }

    public List<Attribute> getAttributes() {
        return mAttributes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("m=").append(mMedia).append(" ").append(mPort).append(" ").append(mProto).append(" ").append(mPayloadType).append(NEW_LINE);
        if (mInformation != null) {
            builder.append(mInformation.toString()).append(NEW_LINE);
        }
        if (mEncryptionKey != null) {
            builder.append(mEncryptionKey.toString()).append(NEW_LINE);
        }
        for (Connection c : mConnections) {
            builder.append(c.toString()).append(NEW_LINE);
        }
        for (Attribute b : mAttributes) {
            builder.append(b.toString()).append(NEW_LINE);
        }
        for (Bandwidth b : mBandwidths) {
            builder.append(b.toString()).append(NEW_LINE);
        }
        return builder.toString();
    }
}
