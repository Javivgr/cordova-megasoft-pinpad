/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.connection.bluetooth.conector;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.cordova.LOG;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import ve.com.megasoft.pinpad.connection.WirelessConector;

/**
 * 
 * clase que se encarga de realziar las coneccion hacia dispositivos bluetooth bajo la modalidad de cliente
 * 
 * @author adrian Jesus Silva Simoes 
 *
 */
public class BluetoothConector implements WirelessConector {

	//CONSTANTES
	private static final String TAG = BluetoothConector.class.getName();
	private static final UUID appUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	//timeout
	private static final int TIMEOUTCONEX = 30000;
	
	//Atributos
		//conexion dispositivo
	private BluetoothSocket socket;
		//lectura-escritura
	private InputStream reads;
	private OutputStream writes;
		//flags
	private boolean openComChannelFlag = false;
	
	@Override
	public void openConection(BluetoothDevice device) throws Exception {
		Log.i(TAG,"Aperturando socket bluetooth para comucación");
		Log.d(TAG, "Device: "+device+", modo cliente");
		
		//solicitamos la coneccion
		socket = device.createRfcommSocketToServiceRecord(appUUID);
		
		//recuperamos los canales de comunicación
		writes = socket.getOutputStream();
		reads = socket.getInputStream();
		
		//indicamos que el canal esta abierto
		openComChannelFlag = true;
		
	}

	@Override
	public void closeConection() throws Exception {
		Log.i(TAG, "Cerrando canal de comunicaciones");
		
		if(socket!=null && socket.isConnected()){
			socket.close();
		}
		
		openComChannelFlag = false;
	}

	@Override
	public InputStream getInputStream() {
		return reads;
	}

	@Override
	public OutputStream getOutputStream() {
		return writes;
	}

	@Override
	public boolean isOpenComChannel() {
		return openComChannelFlag;
	}

	@Override
	public boolean checkConectionCapabilities() throws Exception {
		return true;
	}

}
