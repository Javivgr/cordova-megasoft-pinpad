/**
 * Copyright Mega Soft Computaci�n C.A. 
 */
package ve.com.megasoft.pinpad.connection.bluetooth.conector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;
import ve.com.megasoft.pinpad.connection.WirelessConector;

/**
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * implementacion bluetooth de la interfaz de comunicacion con pinpad's
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 *	TODO - modificar para que reciba el perfil bluetooth que usara para establecer la conexion
 *
 */
public class BluetoothServerConector implements WirelessConector {

	//CONSTANTES
	private static final String TAG = BluetoothServerConector.class.getName();
	private static final UUID appUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	//timeout
	private static final int TIMEOUTCONEX = 30000;
	/*private static final int TIMEOUTCONEX = 15000;*/
	
	//Atributos
		//coneccion dispositivo
	private BluetoothServerSocket serverSocket;
	private BluetoothSocket socket;
		//lectura-escritura
	private InputStream reads;
	private OutputStream writes;
		//flags
	private boolean openComChannelFlag = false;
	
	//Metodos Sobre Escritos
	@Override
	public void openConection(BluetoothDevice device) throws Exception {
		Log.i(TAG, "Aperturando socket bluetooth para comunicaci�n");
		Log.d(TAG, "Device: "+device+" modo servidor");
		
		//Modo Servicio
		Log.i(TAG, "Levantando servicio de escucha");
		serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("Servicio Pinpad", appUUID);
		
		//abrimos la coneccion
		Log.i(TAG, "Aceptando coneccion");
		socket = serverSocket.accept(TIMEOUTCONEX);
		
		if(!socket.getRemoteDevice().getAddress().equals(device.getAddress())){
			throw new Exception("Dispositivo no calibrado");
		}
		
		//coneccion aceptada cerrando y destruyendo servicio
		serverSocket.close();
		serverSocket = null;
		
		//recuperamos los canales de comunicacion
		writes = socket.getOutputStream();
		reads = socket.getInputStream();
		
		//indicamos que el canal esta abierto
		openComChannelFlag = true;
	}

	@Override
	public void closeConection() throws IOException {
		//verificamos la apertura de la coneccion y la cerramos 
		if(socket!=null && socket.isConnected()){
			socket.close();
		}
		
		openComChannelFlag = false;
	}

	@Override
	public boolean checkConectionCapabilities() throws Exception {
		
		//instanciamos el adapter 
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		//instaciamos el metodo
		Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
		
		//recuperamos los uuids de los perfiles de bluetooth
		ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
		
		//debug
		for(ParcelUuid uuid : uuids){
			Log.d(TAG, "UUID: "+uuid.getUuid().toString());
		}
		
		return true;
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

	
	
	
}
