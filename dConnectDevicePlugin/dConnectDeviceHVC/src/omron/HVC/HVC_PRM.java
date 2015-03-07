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

/**
 * HVC parameters
 */
public class HVC_PRM
{
    /**
     * Detection parameters
     */
    public class DetectionParam
    {
        /**
         * Minimum detection size
         */
        public int MinSize;
        /**
         * Maximum detection size
         */
        public int MaxSize;
        /**
         * Degree of confidence
         */
        public int Threshold;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public DetectionParam()
        {
            MinSize = 40;
            MaxSize = 480;
            Threshold = 500;
        }
        
        @Override
        public boolean equals(final Object o) {
            DetectionParam compare = (DetectionParam) o;
            
            if (MinSize == compare.MinSize
            &&  MaxSize == compare.MaxSize
            &&  Threshold == compare.Threshold) {
                return true;
            }
            return false;
        }
    }

    /**
     * Face Detection parameters
     */
    public class FaceParam extends DetectionParam
    {
        /**
         * Facial pose
         */
        public int Pose;
        /**
         * Roll angle
         */
        public int Angle;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public FaceParam()
        {
            Pose = 40;
            Angle = 480;
        }
        
        @Override
        public boolean equals(Object o) {
            FaceParam compare = (FaceParam) o;
            if (super.equals(compare)
            &&  Pose == compare.Pose
            &&  Angle == compare.Angle) {
                return true;
            }
            return false;
        }
    }

	/**
     * Camera angle
     */
    public int CameraAngle;
    /**
     * Human Body Detection parameters
     */
    public DetectionParam body;
    /**
     * Hand Detection parameters
     */
    public DetectionParam hand;
    /**
     * Face Detection parameters
     */
    public FaceParam face;

    /**
     * Constructor<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    public HVC_PRM()
    {
    	CameraAngle = 0;
        body = new DetectionParam();
        hand = new DetectionParam();
        face = new FaceParam();
    }
    
    
    @Override
    public boolean equals(Object o) {
        HVC_PRM compare = (HVC_PRM) o;
        if (CameraAngle == compare.CameraAngle
        &&  body.equals(compare.body)
        &&  hand.equals(compare.hand)
        &&  face.equals(compare.face)) {
            return true;
        }
        return false;
    }
}
