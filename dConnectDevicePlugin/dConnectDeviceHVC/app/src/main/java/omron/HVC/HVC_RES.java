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

import java.util.ArrayList;

/**
 * HVC execution result 
 */
public class HVC_RES
{
    /**
     * Detection result
     */
    public class DetectionResult
    {
        /**
         * Center x-coordinate
         */
        public int posX;
        /**c
         * Center y-coordinate
         */
        public int posY;
        /**
         * Size
         */
        public int size;
        /**
         * Degree of confidence
         */
        public int confidence;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public DetectionResult()
        {
            posX = -1;
            posY = -1;
            size = -1;
            confidence = -1;
        }
    }

    /**
     * Face Detection & Estimations results
     */
    public class FaceResult extends DetectionResult
    {
        /**
         * Face direction
         */
        public class DirResult
        {
            /**
             * Yaw angle
             */
            public int yaw;
            /**
             * Pitch angle
             */
            public int pitch;
            /**
             * Roll angle
             */
            public int roll;
            /**
             * Degree of confidence
             */
            public int confidence;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public DirResult()
            {
                yaw = -1;
                pitch = -1;
                roll = -1;
                confidence = -1;
            }
        }

        /**
         * Age
         */
        public class AgeResult
        {
            /**
             * Age
             */
            public int age;
            /**
             * Degree of confidence
             */
            public int confidence;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public AgeResult()
            {
                age = -1;
                confidence = -1;
            }
        }

        /**
         * Gender
         */
        public class GenResult
        {
            /**
             * Gender
             */
            public int gender;
            /**
             * Degree of confidence
             */
            public int confidence;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public GenResult()
            {
                gender = -1;
                confidence = -1;
            }
        }

        /**
         * Gaze
         */
        public class GazeResult
        {
            /**
             * Yaw angle
             */
            public int gazeLR;
            /**
             * Pitch angle
             */
            public int gazeUD;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public GazeResult()
            {
                gazeLR = -1;
                gazeUD = -1;
            }
        }

        /**
         * Blink
         */
        public class BlinkResult
        {
            /**
             * Left eye blink result
             */
            public int ratioL;
            /**
             * Right eye blink result
             */
            public int ratioR;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public BlinkResult()
            {
                ratioL = -1;
                ratioR = -1;
            }
        }

        /**
         * Expression
         */
        public class ExpResult
        {
            /**
             * Expression
             */
            public int expression;
            /**
             * Score
             */
            public int score;
            /**
             * Negative-positive degree
             */
            public int degree;

            /**
             * Constructor<br>
             * [Description]<br>
             * none<br>
             * [Notes]<br>
             */
            public ExpResult()
            {
                expression = -1;
                score = -1;
                degree = -1;
            }
        }

        /**
         * Face direction estimation result
         */
        public DirResult dir;
        /**
         * Age Estimation result
         */
        public AgeResult age;
        /**
         * Gender Estimation result
         */
        public GenResult gen;
        /**
         * Gaze Estimation result
         */
        public GazeResult gaze;
        /**
         * Blink Estimation result
         */
        public BlinkResult blink;
        /**
         * Expression Estimation result
         */
        public ExpResult exp;

        /**
         * Constructor<br>
         * [Description]<br>
         * none<br>
         * [Notes]<br>
         */
        public FaceResult()
        {
            dir = new DirResult();
            age = new AgeResult();
            gen = new GenResult();
            gaze = new GazeResult();
            blink = new BlinkResult();
            exp = new ExpResult();
        }
    }

    /**
     * Execution flag (nUseFuncの値(HVC.HVC_ACTIV_BODY_DETECTION(=0x01)等)を格納する)
     */
    public int executedFunc;
    /**
     * Human Body Detection results
     */
    public ArrayList<DetectionResult> body;
    /**
     * Hand Detection results
     */
    public ArrayList<DetectionResult> hand;
    /**
     * Face Detection, Estimations results
     */
    public ArrayList<FaceResult> face;
    
    /**
     * Constructor<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    public HVC_RES()
    {
        body = new ArrayList<DetectionResult>();
        hand = new ArrayList<DetectionResult>();
        face = new ArrayList<FaceResult>();
    }
}
