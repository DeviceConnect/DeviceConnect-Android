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

interface BleCallbackInterface {
	abstract void onConnected();
	abstract void onDisconnected();
	abstract void onPostGetDeviceName(byte[] value);
}

public class HVCBleCallback extends HVCCallback implements BleCallbackInterface {
	@Override
	public void onPostSetParam(int nRet, byte outStatus) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPostGetParam(int nRet, byte outStatus) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPostGetVersion(int nRet, byte outStatus) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPostExecute(int nRet, byte outStatus) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPostGetDeviceName(byte[] value) {
		// TODO Auto-generated method stub
	}
}
