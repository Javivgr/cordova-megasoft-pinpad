/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.connection;

import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothDevice;

/**
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * interfaz de Comunicacion pinpad
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public interface WirelessConector {

	//Metodos publicos
		/*Coneccion*/
	public void openConection(BluetoothDevice device) throws Exception;
	
	public void closeConection() throws Exception;

		/*Canales*/
	public InputStream getInputStream();
	
	public OutputStream getOutputStream();
	
		/*Verificacion*/
	public boolean isOpenComChannel();
	
	public boolean checkConectionCapabilities() throws Exception;
	
}
