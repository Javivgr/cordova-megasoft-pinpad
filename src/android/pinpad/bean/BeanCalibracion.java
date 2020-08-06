/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * bean de respuesta para el proceso de calibracion de los pinpads
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanCalibracion extends BeanBase {

	//CONSTANTES
	/*TAGS*/
	private static final String TAGDEVICECLASS = "device_class";
	private static final String TAGDEVICENAME = "device_name";
	private static final String TAGDEVICEADDRESS = "device_address"; 
	
	//Atributos
	private String deviceClass;
	private String deviceName;
	private String deviceAddress;
	
	//Construvtor
	public BeanCalibracion(){}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGDEVICECLASS, deviceClass);
		json.put(TAGDEVICENAME, deviceName);
		json.put(TAGDEVICEADDRESS, deviceAddress);
		
		return json;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n deviceClass:["+deviceClass+"]");
		sb.append("\n deviceName:["+deviceName+"]");
		sb.append("\n deviceAddress:["+deviceAddress+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public String getDeviceClass() {
		return deviceClass;
	}

	public void setDeviceClass(String deviceClass) {
		this.deviceClass = deviceClass;
	}

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
