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
 * Version (GetVersion result)
 */
public class HVC_VER
{
    /**
     * Version string
     */
    public char[] str;
    /**
     * Major version number
     */
    public byte major;
    /**
     * Minor version number
     */
    public byte minor;
    /**
     * Release version number
     */
    public byte relese;
    /**
     * Revision number
     */
    public int  rev;

    /**
     * Constructor<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     */
    public HVC_VER()
    {
        str = new char[12];
        for(int i=0; i<str.length; i++) str[i]=0;
        major = 0;
        minor = 0;
        relese = 0;
        rev = 0;
    }

    /**
     * Get version struct size<br>
     * [Description]<br>
     * Return struct size when obtaining HVC version number<br>
     * @return int version struct size<br>
     */
    public int GetSize()
    {
        return str.length+3+4;
    }

    /**
     * Get version string<br>
     * [Description]<br>
     * Return HVC version number as a string<br>
     * @return String version number string<br>
     */
    public String GetString()
    {
        String strVer;
        strVer = Integer.toString(major)  + '.' +
                 Integer.toString(minor)  + '.' +
                 Integer.toString(relese) + '.' +
                 Integer.toString(rev)    + '[' + String.valueOf(str) + ']';
        return strVer;
    }
}
