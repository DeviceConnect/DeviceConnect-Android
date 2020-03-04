package org.deviceconnect.android.libsrt;

/**
 * SRT 通信の統計情報.
 */
public class SRTStats {

    /**
     * SRT開始からの経過時間. 単位はミリ秒.
     */
    private long msTimeStamp;

    /**
     * 送信したパケットの個数の合計.
     */
    private long pktSentTotal;

    /**
     * 再送したパケットの個数の合計.
     */
    private int pktRetransTotal;

    /**
     * ロスが検出されたパケットの個数の合計.
     */
    private int pktSndLossTotal;

    /**
     * 送信側によってドロップされたパケットの個数の合計.
     */
    private int pktSndDropTotal;

    /**
     * 送信側の帯域幅の上限値. 単位は Mbps.
     */
    private double mbpsMaxBW;

    /**
     * 送信バッファの使用可能領域. 単位はバイト.
     */
    private int byteAvailSndBuf;

    /**
     * ラウンドトリップタイム (RTT). 単位はミリ秒.
     */
    private double msRTT;

    /**
     * ネットワークリンクの帯域幅の推定値. 単位は Mbps.
     */
    private double mbpsBandwidth;

    public long msTimeStamp() {
        return msTimeStamp;
    }

    void msTimeStamp(long msTimeStamp) {
        this.msTimeStamp = msTimeStamp;
    }

    public long pktSentTotal() {
        return pktSentTotal;
    }

    void pktSentTotal(long pktSentTotal) {
        this.pktSentTotal = pktSentTotal;
    }

    public int pktRetransTotal() {
        return pktRetransTotal;
    }

    void pktRetransTotal(int pktRetransTotal) {
        this.pktRetransTotal = pktRetransTotal;
    }

    public int pktSndLossTotal() {
        return pktSndLossTotal;
    }

    void pktSndLossTotal(int pktSndLossTotal) {
        this.pktSndLossTotal = pktSndLossTotal;
    }

    public int pktSndDropTotal() {
        return pktSndDropTotal;
    }

    void pktSndDropTotal(int pktSndDropTotal) {
        this.pktSndDropTotal = pktSndDropTotal;
    }

    public double mbpsMaxBW() {
        return mbpsMaxBW;
    }

    void mbpsMaxBW(double mbpsMaxBW) {
        this.mbpsMaxBW = mbpsMaxBW;
    }

    public int byteAvailSndBuf() {
        return byteAvailSndBuf;
    }

    void byteAvailSndBuf(int byteAvailSndBuf) {
        this.byteAvailSndBuf = byteAvailSndBuf;
    }

    public double msRTT() {
        return msRTT;
    }

    void msRTT(double msRTT) {
        this.msRTT = msRTT;
    }

    public double mbpsBandwidth() {
        return mbpsBandwidth;
    }

    void mbpsBandwidth(double mbpsBandWidth) {
        mbpsBandwidth = mbpsBandWidth;
    }

    @Override
    public String toString() {
        return "SRTStats{" +
                "msTimeStamp=" + msTimeStamp +
                ", pktSentTotal=" + pktSentTotal +
                ", pktRetransTotal=" + pktRetransTotal +
                ", pktSndLossTotal=" + pktSndLossTotal +
                ", pktSndDropTotal=" + pktSndDropTotal +
                ", mbpsMaxBW=" + mbpsMaxBW +
                ", byteAvailSndBuf=" + byteAvailSndBuf +
                ", msRTT=" + msRTT +
                ", mbpsBandwidth=" + mbpsBandwidth +
                '}';
    }
}
