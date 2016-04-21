/**************************************************************************************************
  Filename:       BleDeviceInfo.java
  Revised:        $Date: 2013-08-30 12:08:11 +0200 (fr, 30 aug 2013) $
  Revision:       $Revision: 27477 $

  Copyright (c) 2013 - 2014 Texas Instruments Incorporated

  All rights reserved not granted herein.
  Limited License. 

  Texas Instruments Incorporated grants a world-wide, royalty-free,
  non-exclusive license under copyrights and patents it now or hereafter
  owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
  this software subject to the terms herein.  With respect to the foregoing patent
  license, such license is granted  solely to the extent that any such patent is necessary
  to Utilize the software alone.  The patent license shall not apply to any combinations which
  include this software, other than combinations with devices manufactured by or for TI ('TI Devices'). 
  No hardware patent is licensed hereunder.

  Redistributions must preserve existing copyright notices and reproduce this license (including the
  above copyright notice and the disclaimer and (if applicable) source code license limitations below)
  in the documentation and/or other materials provided with the distribution

  Redistribution and use in binary form, without modification, are permitted provided that the following
  conditions are met:

    * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
      software provided in binary form.
    * any redistribution and use are licensed by TI for use only with TI Devices.
    * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

  If software source code is provided to you, modification and redistribution of the source code are permitted
  provided that the following conditions are met:

    * any redistribution and use of the source code, including any resulting derivative works, are licensed by
      TI for use only with TI Devices.
    * any redistribution and use of any object code compiled from the source code and any resulting derivative
      works, are licensed by TI for use only with TI Devices.

  Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
  promote products derived from this software without specific prior written permission.

  DISCLAIMER.

  THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package com.example.ti.ble.common;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BleDeviceInfo implements Parcelable {
  /**
   * Less than half a meter away
   */
  public static final int PROXIMITY_IMMEDIATE = 1;
  /**
   * More than half a meter away, but less than four meters away
   */
  public static final int PROXIMITY_NEAR = 2;
  /**
   * More than four meters away
   */
  public static final int PROXIMITY_FAR = 3;
  /**
   * No distance estimate was possible due to a bad RSSI value or measured TX power
   */
  public static final int PROXIMITY_UNKNOWN = 0;

  final private static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
  private static final String TAG = "IBeacon";
  // Data
  private BluetoothDevice mBtDevice;
  protected int mRssi;
  protected String mUUID1;
  protected int mmajor;
  protected int mminor;
  protected int mtxPower;
  protected double mdist;
  protected double runningAverageRssi;
  /**
   * An integer with four possible values representing a general idea of how far the iBeacon is away
   * @see #PROXIMITY_IMMEDIATE
   * @see #PROXIMITY_NEAR
   * @see #PROXIMITY_FAR
   * @see #PROXIMITY_UNKNOWN
   */
  protected Integer proximity;
  /**
   * A double that is an estimate of how far the iBeacon is away in meters.  This name is confusing, but is copied from
   * the iOS7 SDK terminology.   Note that this number fluctuates quite a bit with RSSI, so despite the name, it is not
   * super accurate.   It is recommended to instead use the proximity field, or your own bucketization of this value.
   */
  protected Double accuracy;

  public BleDeviceInfo(BluetoothDevice device, int rssi, int major,int minor, String UUID1,int txPower) {
    mBtDevice = device;
    mRssi = rssi;
    mUUID1=UUID1;
    mmajor=major;
    mminor=minor;
    mtxPower= txPower;
    runningAverageRssi = 0;
  }

  public BleDeviceInfo() {

  }


  protected BleDeviceInfo(Parcel in) {
    mBtDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
    mRssi = in.readInt();
    mUUID1 = in.readString();
    mmajor = in.readInt();
    mminor = in.readInt();
    mtxPower = in.readInt();
    mdist = in.readDouble();
    runningAverageRssi=in.readDouble();
  }

  public static final Creator<BleDeviceInfo> CREATOR = new Creator<BleDeviceInfo>() {
    @Override
    public BleDeviceInfo createFromParcel(Parcel in) {
      BleDeviceInfo Bdevice = new BleDeviceInfo();
      Bdevice.mmajor=in.readInt();
      Bdevice.mminor=in.readInt();
      Bdevice.mUUID1=in.readString();
      Bdevice.runningAverageRssi = in.readDouble();
      return Bdevice;
   //   return new BleDeviceInfo(in);
    }

    @Override
    public BleDeviceInfo[] newArray(int size) {
      return new BleDeviceInfo[size];
    }
  };

  public BluetoothDevice getBluetoothDevice() {
    return mBtDevice;
  }

  public double getRssi() {
    return  mRssi;
  }
  public void setAvaragedRssi(double mRunningAverageRssi) {
      runningAverageRssi=mRunningAverageRssi;
  }
  public double getAvaragedRssi() {
    return  runningAverageRssi;
  }
  public int getmajor() {
    return mmajor;
  }
  public int getminor() {
    return mminor;
  }
  public String getUUID1() {
    return mUUID1;
  }
  public int gettxPower() {
    return mtxPower;
  }
  public double getdistance() {
    return mdist;
  }


  public void updateRssi(int rssiValue) {
    mRssi = rssiValue;
  }

  public double getAccuracy() {
    if (accuracy == null) {
   //   accuracy = calculateAccuracy(mtxPower, runningAverageRssi != null ? runningAverageRssi : mRssi );
   //   accuracy= calcDistance(runningAverageRssi != null ? runningAverageRssi : mRssi );
      accuracy= calFeetToMeter(calcDistance(runningAverageRssi !=0 ? runningAverageRssi : mRssi ));
    }
    return accuracy;
  }

  static double calcDistance(double rssi) {
    double base = 10;
    double exponent = -(rssi + 51.504)/16.532;
    //double distance = Math.pow(base, exponent);
    //104.09004338 + 13.26842562x + 0.57250833x^2 + 0.00986120x^3 + 0.00006099x^4

    // SI NORTH THIRD FLOOR (room 3250)
//		  double distance = 104.09004338 + 13.26842562 * rssi + 0.57250833* Math.pow(rssi,2)
//		        + 0.00986120*Math.pow(rssi, 3) + 0.00006099 * Math.pow(rssi,4);

    // SI NORTH FIRST FLOOR
    // 0 degree
    //double distance = 3324.4981666 + 234.0366524 * rssi + 6.0593624* Math.pow(rssi,2)
    //  + 0.0683264*Math.pow(rssi, 3) + 0.0002843 * Math.pow(rssi,4);

    double distance = 730.24198315 + 52.33325511*rssi + 1.35152407*Math.pow(rssi, 2)
            + 0.01481265*Math.pow(rssi, 3) + 0.00005900*Math.pow(rssi, 4) + 0.00541703*180;


    //return (distance>0)?distance:rssi;
    return distance;
  }

  static double calFeetToMeter(double rssi) {
    return rssi*0.3048;
  }

  protected static double calculateAccuracy(int txPower, double rssi) {
    if (rssi == 0) {
      return -1.0; // if we cannot determine accuracy, return -1.
    }

   // Log.d(TAG, "calculating accuracy based on rssi of " + rssi);


    double ratio = rssi*1.0/txPower;
    if (ratio < 1.0) {
      return Math.pow(ratio,10);
    }
    else {
      double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
     // Log.d(TAG, " avg rssi: "+rssi+" accuracy: "+accuracy);
      return accuracy;
    }
  }

  protected static int calculateProximity(double accuracy) {
    if (accuracy < 0) {
      return PROXIMITY_UNKNOWN;
      // is this correct?  does proximity only show unknown when accuracy is negative?  I have seen cases where it returns unknown when
      // accuracy is -1;
    }
    if (accuracy < 0.5 ) {
      return PROXIMITY_IMMEDIATE;
    }
    // forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
    if (accuracy <= 4.0) {
      return PROXIMITY_NEAR;
    }
    // if it is > 4.0 meters, call it far
    return PROXIMITY_FAR;

  }

  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    int v;
    for ( int j = 0; j < bytes.length; j++ ) {
      v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

    dest.writeInt(mmajor);
    dest.writeInt(mminor);
    dest.writeString(mUUID1);
    dest.writeDouble(runningAverageRssi);

  }
  private void readFromParcel(Parcel in) {

       this.mmajor = in.readInt();
       this.mminor = in.readInt();
       this.mUUID1=in.readString();
       this.runningAverageRssi = in.readDouble();
  }


}
