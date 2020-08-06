/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.connection.bluetooth.bean;

import org.json.JSONException;
import org.json.JSONObject;

import ve.com.megasoft.pinpad.bean.BeanBase;

/**
 * 
 * bean con la representacion basica de los dispositivos bluetooth
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanBluetoothDevice extends BeanBase{

	//CONSTANTES
//	private static final String TAG = BeanBluetoothDevice.class.getName();
	
	/*TAGS*/
	private static final String TAGDEVICENAME = "bluetooth_device_name";
	private static final String TAGDEVICEADDRESS = "bluetooth_device_address";
	
	//Atributos
	private String deviceName;
	private String deviceAddress;
	
	//Constructor
	public BeanBluetoothDevice() {}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGDEVICENAME, deviceName);
		json.put(TAGDEVICEADDRESS, deviceAddress);
		
		return json;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n deviceName:["+deviceName+"]");
		sb.append("\n deviceAddress:["+deviceAddress+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public String getDeviceName() {
		return deviceName;
	}
	
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	public String getDeviceAddress() {
		return deviceAddress;
	}
	
	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
	
	
}
