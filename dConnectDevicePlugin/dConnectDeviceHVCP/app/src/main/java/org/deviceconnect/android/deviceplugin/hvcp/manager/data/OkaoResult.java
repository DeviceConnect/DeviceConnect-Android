/*
 OkaoResult
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvcp.manager.data;

/**
 * HVC Human Detect(body, hand, face) Result.
 * @author NTT DOCOMO, INC.
 */
public class OkaoResult {
    /** Body detect count. */
    private int mNumberOfBody;
    /** Hand detect count. */
    private int mNumberOfHand;
    /** Face detect count. */
    private int mNumberOfFace;
    // Body Detection.
    /** Body x position. */
    private long[] mBodyX;
    /** Body y position. */
    private long[] mBodyY;
    /** Body detect size. */
    private long[] mBodySize;
    /** Body detect confidence. */
    private long[] mBodyDetectConfidence;
    // Hand Detection.
    /** Hand x position. */
    private long[] mHandX;
    /** Hand y position. */
    private long[] mHandY;
    /** Hand detect size. */
    private long[] mHandSize;
    /** Hand detect confidence. */
    private long[] mHandDetectConfidence;
    // Face Detection.
    /** Face x position. */
    private long[] mFaceX;
    /** Face y position. */
    private long[] mFaceY;
    /** Face detect size. */
    private long[] mFaceSize;
    /** Face detect confidence. */
    private long[] mFaceDetectConfidence;
    // Face direction estimation.
    /** Face direction Left or Right. */
    private long[] mFaceDirectionLR;
    /** Face direction Up or Down. */
    private long[] mFaceDirectionUD;
    /** Face slope. */
    private long[] mFaceDirectionSlope;
    /** Face direction confidence. */
    private long[] mFaceDirectionConfidence;
    // Age estimation.
    /** Age. */
    private long[] mAge;
    /** Age confidence. */
    private long[] mAgeConfidence;
    // Gender estimation.
    /** Gender. */
    private long[] mGender;
    /** Gender confidence. */
    private long[] mGenderConfidence;
    // Gaze estimation
    /** Gaze Left or Right. */
    private long[] mGazeLR;
    /** Gaze Up or Down. */
    private long[] mGazeUD;
    // Blink estimation
    /** Blink Left. */
    private long[] mBlinkLeft;
    /** Blink Right. */
    private long[] mBlinkRight;
    // Expression estimation
    /** Expression Unknown.*/
    private long[] mExpressionUnknown;
    /** Expression Smile. */
    private long[] mExpressionSmile;
    /** Expression Surprise. */
    private long[] mExpressionSurprise;
    /** Expression Mad. */
    private long[] mExpressionMad;
    /** Expression Sad. */
    private long[] mExpressionSad;
    /** Expression confidence. */
    private long[] mExpressionConfidence;

    /**
     * Constructor.
     */
    public OkaoResult() {
        mNumberOfBody = 0;
        mNumberOfHand = 0;
        mNumberOfFace = 0;
        mBodyX=new long[35];
        mBodyY=new long[35];
        mBodySize=new long[35];
        mBodyDetectConfidence = new long[35];
        mHandX=new long[35];
        mHandY=new long[35];
        mHandSize=new long[35];
        mHandDetectConfidence = new long[35];
        mFaceX=new long[35];
        mFaceY=new long[35];
        mFaceSize=new long[35];
        mFaceDetectConfidence = new long[35];
        mFaceDirectionLR=new long[35];
        mFaceDirectionUD=new long[35];
        mFaceDirectionSlope = new long[35];
        mFaceDirectionConfidence=new long[35];
        mAge=new long[35];
        mAgeConfidence = new long[35];
        mGender=new long[35];
        mGenderConfidence = new long[35];
        mGazeLR=new long[35];
        mGazeUD=new long[35];
        mBlinkLeft=new long[35];
        mBlinkRight=new long[35];
        mExpressionUnknown=new long[35];
        mExpressionSmile=new long[35];
        mExpressionSurprise=new long[35];
        mExpressionMad=new long[35];
        mExpressionSad=new long[35];
        mExpressionConfidence=new long[35];
    }

    /**
     * Get body Count.
     * @return body count
     */
    public int getNumberOfBody() {
        return mNumberOfBody;
    }

    /**
     * Set body count.
     * @param numberOfBody body count
     */
    public void setNumberOfBody(final int numberOfBody) {
        this.mNumberOfBody = numberOfBody;
    }

    /**
     * Get hand count.
     * @return hand count
     */
    public int getNumberOfHand() {
        return mNumberOfHand;
    }

    /**
     * Set hand count.
     * @param numberOfHand hand count
     */
    public void setNumberOfHand(final int numberOfHand) {
        this.mNumberOfHand = numberOfHand;
    }

    /**
     * Get face count.
     * @return face count
     */
    public int getNumberOfFace() {
        return mNumberOfFace;
    }

    /**
     * Set face count.
     * @param numberOfFace face count
     */
    public void setNumberOfFace(final int numberOfFace) {
        this.mNumberOfFace = numberOfFace;
    }

    /**
     * Get body x position array.
     * @return body's x position array
     */
    public long[] getBodyX() {
        return mBodyX;
    }

    /**
     * Set body x position array.
     * @param bodyX body's x position array
     */
    public void setBodyX(final long[] bodyX) {
        this.mBodyX = bodyX;
    }

    /**
     * Get body y position array.
     * @return body's y position array
     */
    public long[] getBodyY() {
        return mBodyY;
    }

    /**
     * Set body y position array.
     * @param bodyY body's y position array
     */
    public void setBodyY(final long[] bodyY) {
        this.mBodyY = bodyY;
    }

    /**
     * Get body size array.
     * @return body size array
     */
    public long[] getBodySize() {
        return mBodySize;
    }

    /**
     * Set body size array.
     * @param bodySize body size array
     */
    public void setBodySize(final long[] bodySize) {
        this.mBodySize = bodySize;
    }

    /**
     * Get body detect confidence array.
     * @return body detect confidence array
     */
    public long[] getBodyDetectConfidence() {
        return mBodyDetectConfidence;
    }

    /**
     * Set body detect confidence array.
     * @param bodyDetectConfidence body detect confidence array
     */
    public void setBodyDetectConfidence(final long[] bodyDetectConfidence) {
        this.mBodyDetectConfidence = bodyDetectConfidence;
    }

    /**
     * Get hand x position array.
     * @return hand x position array
     */
    public long[] getHandX() {
        return mHandX;
    }

    /**
     * Set hand x position array.
     * @param handX hand x position array
     */
    public void setHandX(final long[] handX) {
        this.mHandX = handX;
    }

    /**
     * Get hand y position array.
     * @return hand y position array
     */
    public long[] getHandY() {
        return mHandY;
    }

    /**
     * Set hand y position array.
     * @param handY hand y position array
     */
    public void setHandY(final long[] handY) {
        this.mHandY = handY;
    }

    /**
     * Get hand size array.
     * @return hand size array
     */
    public long[] getHandSize() {
        return mHandSize;
    }

    /**
     * Set hand size array.
     * @param handSize hand size array
     */
    public void setHandSize(final long[] handSize) {
        this.mHandSize = handSize;
    }

    /**
     * Get hand detect confidence array.
     * @return hand detect confidence
     */
    public long[] getHandDetectConfidence() {
        return mHandDetectConfidence;
    }

    /**
     * Set hand detect confidence array.
     * @param handDetectConfidence hand detect confidence array
     */
    public void setHandDetectConfidence(final long[] handDetectConfidence) {
        this.mHandDetectConfidence = handDetectConfidence;
    }

    /**
     * Get face x position array.
     * @return face x position array
     */
    public long[] getFaceX() {
        return mFaceX;
    }

    /**
     * Set face x position array.
     * @param faceX face x position array
     */
    public void setFaceX(final long[] faceX) {
        this.mFaceX = faceX;
    }

    /**
     * Get face y position array.
     * @return face y position array
     */
    public long[] getFaceY() {
        return mFaceY;
    }

    /**
     * Set face y position array.
     * @param faceY face y position array
     */
    public void setFaceY(final long[] faceY) {
        this.mFaceY = faceY;
    }

    /**
     * Get face size.
     * @return face size
     */
    public long[] getFaceSize() {
        return mFaceSize;
    }

    /**
     * Set face size.
     * @param faceSize face size
     */
    public void setFaceSize(final long[] faceSize) {
        this.mFaceSize = faceSize;
    }

    /**
     * Get face detect confidence.
     * @return face detect confidence
     */
    public long[] getFaceDetectConfidence() {
        return mFaceDetectConfidence;
    }

    /**
     * Set face detect confidence
     * @param faceDetectConfidence face detect confidence
     */
    public void setFaceDetectConfidence(final long[] faceDetectConfidence) {
        this.mFaceDetectConfidence = faceDetectConfidence;
    }

    /**
     * Get face direction Left or Right array.
     * @return face direction left or right array
     */
    public long[] getFaceDirectionLR() {
        return mFaceDirectionLR;
    }

    /**
     * Set face direction left or right array
     * @param faceDirectionLR face direction left or right array
     */
    public void setFaceDirectionLR(final long[] faceDirectionLR) {
        this.mFaceDirectionLR = faceDirectionLR;
    }

    /**
     * Get face direction Up or down array.
     * @return face direction up or down array
     */
    public long[] getFaceDirectionUD() {
        return mFaceDirectionUD;
    }

    /**
     * Set face direction Up or down array.
     * @param faceDirectionUD face direction up or down array
     */
    public void setFaceDirectionUD(final long[] faceDirectionUD) {
        this.mFaceDirectionUD = faceDirectionUD;
    }

    /**
     * Get face direction slope array.
     * @return face direction slope array
     */
    public long[] getFaceDirectionSlope() {
        return mFaceDirectionSlope;
    }

    /**
     * Set face direction slope array.
     * @param faceDirectionSlope face direction slope array
     */
    public void setFaceDirectionSlope(final long[] faceDirectionSlope) {
        this.mFaceDirectionSlope = faceDirectionSlope;
    }

    /**
     * Get face direction confidence array.
     * @return face direction confidence array
     */
    public long[] getFaceDirectionConfidence() {
        return mFaceDirectionConfidence;
    }

    /**
     * Set face direction confidence array.
     * @param faceDirectionConfidence face direction confidence array
     */
    public void setFaceDirectionConfidence(final long[] faceDirectionConfidence) {
        this.mFaceDirectionConfidence = faceDirectionConfidence;
    }

    /**
     * Get age array.
     * @return age array
     */
    public long[] getAge() {
        return mAge;
    }

    /**
     * Set age array.
     * @param age age array
     */
    public void setAge(final long[] age) {
        this.mAge = age;
    }

    /**
     * Get age confidence array.
     * @return age confidence array
     */
    public long[] getAgeConfidence() {
        return mAgeConfidence;
    }

    /**
     * Set age confidence array.
     * @param ageConfidence age confidence array
     */
    public void setAgeConfidence(final long[] ageConfidence) {
        this.mAgeConfidence = ageConfidence;
    }

    /**
     * Get gender array.
     * @return gender array
     */
    public long[] getGender() {
        return mGender;
    }

    /**
     * Set gender array.
     * @param gender gender array
     */
    public void setGender(final long[] gender) {
        this.mGender = gender;
    }

    /**
     * Get gender confidence array.
     * @return gender confidence array
     */
    public long[] getGenderConfidence() {
        return mGenderConfidence;
    }

    /**
     * Set gender confidence array.
     * @param genderConfidence gender confidence array
     */
    public void setGenderConfidence(final long[] genderConfidence) {
        this.mGenderConfidence = genderConfidence;
    }

    /**
     * Get gaze left or right array.
     * @return gaze left or right array
     */
    public long[] getGazeLR() {
        return mGazeLR;
    }

    /**
     * Set gaze left or right array.
     * @param gazeLR left or right array
     */
    public void setGazeLR(final long[] gazeLR) {
        this.mGazeLR = gazeLR;
    }

    /**
     * Get gaze up or down array.
     * @return gaze up or down array
     */
    public long[] getGazeUD() {
        return mGazeUD;
    }

    /**
     * Set gaze up or down array.
     * @param gazeUD gaze up or down array
     */
    public void setGazeUD(final long[] gazeUD) {
        this.mGazeUD = gazeUD;
    }

    /**
     * Get blink left array.
     * @return blink left array
     */
    public long[] getBlinkLeft() {
        return mBlinkLeft;
    }

    /**
     * Set blink left array.
     * @param blinkLeft blink left array
     */
    public void setBlinkLeft(final long[] blinkLeft) {
        this.mBlinkLeft = blinkLeft;
    }

    /**
     * Get blink right array.
     * @return blink right array
     */
    public long[] getBlinkRight() {
        return mBlinkRight;
    }

    /**
     * Set blink right array.
     * @param blinkRight blink right array
     */
    public void setBlinkRight(final long[] blinkRight) {
        this.mBlinkRight = blinkRight;
    }

    /**
     * Get expression unknown array.
     * @return expression unknown array
     */
    public long[] getExpressionUnknown() {
        return mExpressionUnknown;
    }

    /**
     * Set expression unknown array.
     * @param expressionUnknown expression unknown array
     */
    public void setExpressionUnknown(final long[] expressionUnknown) {
        this.mExpressionUnknown = expressionUnknown;
    }

    /**
     * Get expression smile array.
     * @return expression smile array
     */
    public long[] getExpressionSmile() {
        return mExpressionSmile;
    }

    /**
     * Set expression smile array.
     * @param expressionSmile expression smile array
     */
    public void setExpressionSmile(final long[] expressionSmile) {
        this.mExpressionSmile = expressionSmile;
    }

    /**
     * Get expression surprise array.
     * @return expression surprise array
     */
    public long[] getExpressionSurprise() {
        return mExpressionSurprise;
    }

    /**
     * Set expression surprise array.
     * @param expressionSurprise expression surprise array
     */
    public void setExpressionSurprise(final long[] expressionSurprise) {
        this.mExpressionSurprise = expressionSurprise;
    }

    /**
     * Get expression mad array.
     * @return expression mad array
     */
    public long[] getExpressionMad() {
        return mExpressionMad;
    }

    /**
     * Set expression mad array.
     * @param expressionMad expression mad array
     */
    public void setExpressionMad(final long[] expressionMad) {
        this.mExpressionMad = expressionMad;
    }

    /**
     * Get expression sad array.
     * @return expression sad
     */
    public long[] getExpressionSad() {
        return mExpressionSad;
    }

    /**
     * Set expression sad array.
     * @param expressionSad expression sad array
     */
    public void setExpressionSad(final long[] expressionSad) {
        this.mExpressionSad = expressionSad;
    }

    /**
     * Get expression confidence array.
     * @return expression confidence array
     */
    public long[] getExpressionConfidence() {
        return mExpressionConfidence;
    }

    /**
     * Set expression confidence array.
     * @param expressionConfidence expression confidence array
     */
    public void setExpressionConfidence(final long[] expressionConfidence) {
        this.mExpressionConfidence = expressionConfidence;
    }
}
