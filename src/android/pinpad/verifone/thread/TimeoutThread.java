/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.verifone.thread;

import java.util.Calendar;

import android.util.Log;

/**
 * 
 * Thread para el manejo de timeouts
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class TimeoutThread extends Thread {

	//CONSTANTES 
	private static final String TAG = TimeoutThread.class.getName();
	
	
	//Atributos
	public boolean corriendo;
	public boolean interrupted;
	private int timeoutEjecucion;
	
	//Constructor
	/**
	 * constructor de la instacia
	 * @param timeoutEjecucion (int) duracion en segundos antes de que se produsca un timeout
	 */
	public TimeoutThread(int timeoutEjecucion) {
		this.timeoutEjecucion = timeoutEjecucion;
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		setName("Read_Write_timeout_thread");
		
		//indicadores de ejecucion 
		corriendo = true;
		interrupted = false;
		
		//manejo de fecha 
		Calendar ahora = Calendar.getInstance();
		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.SECOND, timeoutEjecucion);
		
		//ejecutamos ciclo de timeout
		while (ahora.before(timeout)&&corriendo){
			try {
				Thread.sleep(300);
				ahora = Calendar.getInstance();
			} 
			catch (InterruptedException e) {
				Log.w(TAG, "Hilo de timeout interrumpido");
			}
		}
		
		corriendo = false;
		
		if(!interrupted){Log.w(TAG, "Timeout Alcanzado");}
		
	}
	
	
	
}
