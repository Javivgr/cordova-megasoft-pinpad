/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.connection.bluetooth.thread;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * Hilo de coneccion dispositivos bluetooth en modo cliente
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class ServerConectorThread extends Thread {

	//CONSTANTES
	private static final String TAG = ServerConectorThread.class.getName();
	private static final UUID appUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String BTSERVICENAME = "Mobile_POS_Bluetooth_Server";
	
	//Atributos
	//primitivas
	private boolean running;
	
	//bluetooth
	private BluetoothDevice device;
	private BluetoothServerSocket serverSocket;
	
	//Handler
	private Handler socketHandler;
//	private handler cancelHandler;
	
	//Constructor
	/**
	 * constructor del conecctor en modo servicio
	 * @throws Exception
	 */
	public ServerConectorThread(BluetoothDevice device, Handler socketHandler) throws Exception {
		Log.i(TAG, "Instanciado hilo de servicio de coneccion");
		Log.d(TAG, "Device: "+device+" Handler: "+socketHandler);
		
		//indicamos cual es el handler que entregara el socket
		if(socketHandler==null){throw new Exception("No se entrego el Handler para recuperar la conexión");}
		this.socketHandler = socketHandler;
		
		//creamo un serversocket temporal
		BluetoothServerSocket tmp = null;
		try{
			tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(BTSERVICENAME, appUUID);
			serverSocket = tmp;
			Log.i(TAG, "Servicio "+BTSERVICENAME+" establecido con el UUID "+appUUID);
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo crear el servicio, error: ",e);
			throw e;
		}
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		Log.i(TAG, "Iniciando servicio de coneccion bluetooth "+BTSERVICENAME);
		BluetoothSocket tmpSocket = null;
		BluetoothDevice tmpDevice = null;
		running = true;
		Message msg = new Message();
		
		//nos mantenemos a la escucha hasta que ocurra una excepcion o recuperemos el socket
		//TODO - entregar el timeout de coneccion
		while(running){
			try{
				//iniciamos el servicio y aceptamos la coneccion
				tmpSocket = serverSocket.accept();
				
				//si se acepto una coneccion
				if(tmpSocket!=null ){
					//recuperamos el device conectado
					tmpDevice = tmpSocket.getRemoteDevice();
					
					Log.i(TAG, "Coneccion Aceptada, verificando Dispositivo");
					Log.d(TAG, "Esperado: "+device.getAddress()+", Recibido: "+tmpDevice.getAddress());
					
					if(tmpDevice.getAddress() == device.getAddress()){
						Log.i(TAG, "Dispositivo Verificando, recuperando Socket de coneccion, entregando respuesta");
						
						//enviamos un mensaje al handler para indicarle que recuperamos el socket
						msg.what = 0;
						msg.obj = tmpSocket;
						socketHandler.sendMessage(msg);
						
						//cerramos el serversocket y terminamos el ciclo
						Log.i(TAG, "Cerrando servicio");
						serverSocket.close();
						running = false;
						break;
					}
				}
			}
			catch(Exception e){
				Log.e(TAG, "No se establecio la conexión, error: ",e);
				try {cancel();} 
				catch (IOException e1) {Log.e(TAG, "No se pudo cerrar el servicio bluetooth");}
				msg.what = 1;
				msg.obj = e;
				socketHandler.sendMessage(msg);
				running = false;
				break;
			}
		}
	}

	//Metodos Publicos
	/**
	 * procedimiento que cierra el servicio
	 * @throws IOException
	 */
	public void cancel() throws IOException{
		if(running){running=false;}
		if(serverSocket!=null){serverSocket.close();}
	}

	//Getter's && Setter's
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
