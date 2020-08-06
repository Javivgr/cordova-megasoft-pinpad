/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.verifone.thread;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.exception.SerialConnectionException;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.command.CommandBuilder;

/**
 * 
 * hilo para la escritura lectura de comandos del pinpad
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class ReadWriteThread extends Thread {

	//CONSTANTES
	private static final String TAG = ReadWriteThread.class.getName();
	private static final int RETARDOLECTURA = 1;
	
	//private static final int TIMEOUTEJECUCION = 60;
	private static final int TIMEOUTEJECUCION = 30;
	
	/*TAGS*/
	public static final int TAGOK = 0;
	private static final String TAGNORESPONSE = "NO_RESPONSE";
	private static final String TAGCANCEL = "CANCEL_BY_USER";
	
	//Atributos
	//Singleton
	private static ReadWriteThread instancia;
	
	//threadpool
	private ExecutorService threadPool;
	
	//canales
	private DataInputStream reads;
	private DataOutputStream writes;
	
	//comando
	private byte[] comando;
	private byte commandInit;
	private byte commandEnd;
	private boolean waitForAnswer;
	
	//handlers
	private Handler handler;
	
	//Thread
	private TimeoutThread timeout;
	
	//Metodos privados
	/**
	 * funcion que realiza la lectura de los datos recibidos por el pinpad 
	 * @param commandInit (byte) byte de inicio de respuesta
	 * @param commandEnd (byte) byte de fin de respuesta
	 * @return (String) la respuesta entregada por el pinpad 
	 * @throws InterruptedException - en caso que se detenga el hilo de la aplicacion
	 * @throws IOException - en caso de que haya un problema de lectura o de escritura
	 * @throws SerialConnectionException - en caso que se genere un error procesando la respuesta
	 */
 	private String leerRespuesta(byte commandInit, byte commandEnd) throws InterruptedException, IOException, SerialConnectionException{
		Log.i(TAG, "Ejecutando proceso de lectura de respuesta");
		
		byte[] respuesta = new byte[2048];
		int reintentos = 2;
		int cInit = -1;
		int pos = -1;
		
		//agregar verificacion del timeout en el ciclo
		do{
			//Esperamos un poco por el pinpad
			Thread.sleep(RETARDOLECTURA);
			
			//buscamos el caracter de inicio STX o SI
			Log.e(TAG, "Buscando caracter de inicio");
			do{
				if(reads.available()!=0){cInit = reads.read();}
			}while(cInit!=commandInit && cInit!=CommandBuilder.EOT && cInit!=CommandBuilder.CAN && timeout.corriendo);
			
			//verfificamos si dio Timeout o un EOT
			if(cInit==CommandBuilder.EOT || !timeout.corriendo){
				Log.e(TAG, "[0] Excepcion sin respuesta del pinpad");
				if(!timeout.corriendo){writes.write(CommandBuilder.EOT);}
				throw new SerialConnectionException(TAGNORESPONSE);
				
			}//TODO - Colocar en los recursos aplicacion
			
			if(cInit==CommandBuilder.CAN){
				Log.w(TAG,"Usuario cancelo por el boton cancelar");
				throw new SerialConnectionException(TAGCANCEL);
			}

			//leemos el resto de la trama de respuesta
			Log.e(TAG, "leemos trama de respuesta");
			pos = 0;
			int data = -1;
			do{
				if(reads.available()>0){
					data = (byte) reads.read();
					respuesta[pos++] = (byte) data;
				}
			}while(data != commandEnd && timeout.corriendo);
			
			Log.d(TAG, "Respuesta Obtenida: "+Utils.bytesToHex(respuesta));
			
			//verficamos si tenemos timeout
			if(!timeout.corriendo){
				Log.e(TAG, "[1] Excepcion sin respuesta del pinpad, TIMEOUT");
				writes.write(CommandBuilder.EOT);
				throw new SerialConnectionException("tiempo expirado para recibir respuesta del pinpad"); //TODO - Colocar en los recursos aplicacion
			}
			
			//leemos el caracter LRC
			while(reads.available() == 0){
				if(!timeout.corriendo){
					Log.e(TAG, "[2] Excepcion sin respuesta del pinpad, TIMEOUT");
					writes.write(CommandBuilder.EOT);
					throw new SerialConnectionException("tiempo expirado para recibir respuesta del pinpad"); //TODO - Colocar en los recursos aplicacion
				}
				ReadWriteThread.sleep(RETARDOLECTURA);
			}
			
			//verificamos timeout
			if(!timeout.corriendo){
				Log.e(TAG, "[3] Excepcion sin respuesta del pinpad, TIMEOUT");
				writes.write(CommandBuilder.EOT);
				throw new SerialConnectionException("tiempo expirado para recibir respuesta del pinpad"); //TODO - Colocar en los recursos aplicacion
			}
			
			//realizamos el calculo del LRC para verificar la respuesta entregada
			int lrc = reads.read();
			byte[] msgCalLcr = new byte[pos];
			for(int j=0; j < pos; j++){msgCalLcr[j] = respuesta[j];}
			
			//verificamos el mensaje tiene su lrc valido
			if(CommandBuilder.verificarLrc(msgCalLcr, lrc)){
				writes.write(CommandBuilder.ACK);
				break;
			}
			else{
				if(reintentos >= 0){writes.write(CommandBuilder.NAK);}
				else{
					Log.e(TAG, "enviando un EOT al pinpad");
					Log.e(TAG, "[4] Excepcion sin respuesta del pinpad, TIMEOUT");
					writes.write(CommandBuilder.EOT);
					throw new SerialConnectionException("error Obteniendo Datos pinpad"); //TODO - Colocar en los recursos aplicacion
				}
			}
			
			//en caso de no tener exito se realiza un nuevo reintento
			reintentos--;
			
		}while(reintentos > 0 && timeout.corriendo);
		
		//verificamos que el timeout y que se verifica que se recibio un respuesta
		if(!timeout.corriendo){
			Log.e(TAG, "[5] Excepcion sin respuesta del pinpad, TIMEOUT");
			writes.write(CommandBuilder.EOT);
			throw new SerialConnectionException("timeout alcanzado");
		}
		if(pos<1){
			Log.e(TAG, "[6] Excepcion sin respuesta del pinpad, BN");
			throw new SerialConnectionException("BN");
		}
		
		StringBuffer sb = new StringBuffer();
		for(int k=0; k<pos-1; k++){sb.append((char)respuesta[k]);}
		
		return sb.toString();
	}
	
	/**
	 * funcion que realiza la lectura de un EOT para comandos que no entregan respuesta
	 * @return (boolean) true si el comando entrega un EOT, false en caso contrario 
	 * @throws InterruptedException - en caso que se detenga el hilo de la aplicacion
	 * @throws IOException - en caso de que haya un problema de lectura o de escritura
	 * @throws SerialConnectionException - en caso que se genere un error procesando la respuesta
	 */
	private void leerRespuesta() throws InterruptedException, IOException, SerialConnectionException{
		int initChar=0;
		int reintentos = 2;
		
		do{
			//Esperamos un poco por el pinpad
			Thread.sleep(RETARDOLECTURA);
			
			//buscamos el caracter de la trama de respuesta
			do{
				if(reads.available()!=0){initChar = reads.read();}
			}while(initChar!=CommandBuilder.EOT && timeout.corriendo);
			
			//verificamos si es un EOT
			if(initChar == CommandBuilder.EOT && timeout.corriendo){return;}
			
			reintentos-=1;
			
		}while(reintentos > 0 && timeout.corriendo);
		
		//verificamos si se agotaron los reintentos o se alcanso el timeout
		if(reintentos<=0 || !timeout.corriendo){throw new SerialConnectionException("tiempo expirado para recibir respuesta del pinpad");}
	}

	/**
	 * funcion que envia el comando a ejecutar al pinpad a traves del canal de comunicación
	 * @param comando (byte[]) comando a ejecutar en el pinpad 
	 * @return (Boolean) true el comando fue y recibido por el pinpad, false en caso contrario
	 * @throws IOException - problemas escribiendo el comando 
	 * @throws InterruptedException - problemas con el hilo del aplicativo
	 */
	private boolean enviarComando(byte[] comando) throws IOException, InterruptedException{
		Log.i(TAG, "Ejecutando comando: "+Utils.bytesToHex(comando));
		int reintentos = 2;
		int respuesta;
		do{
			//escribimos el comando a ejecutar por el pinpad
			for(byte data : comando){writes.write(data);}
			Thread.sleep(RETARDOLECTURA);
			Log.i(TAG, "Comando Enviado");
			
			//esperamos un ACK por parte del pipad
			while(reads.available() == 0){
				if(!timeout.corriendo){
					Log.w(TAG, "tiempo expirado para recibir respuesta del pinpad");
					writes.write(CommandBuilder.EOT);
					return false;
				}
				Thread.sleep(RETARDOLECTURA);
			}
			
			//recuperamos la respuesta del pinpad 
			respuesta = reads.read();
			Log.d(TAG, "Respuesta envio comando: "+respuesta);
			
			//si es un ACK todo esta bien continuamos con el proceso de lectura
			if(respuesta == CommandBuilder.ACK){
				Log.i(TAG, "Ejecucion Exitosa de comando, esperando respuesta");
				return true;
			}
			
			//volvemos a reintetar 
			reintentos--;
			
		}while(reintentos > 0);
		
		//enviar EOT -- TODO 
		
		Log.i(TAG, "Ejecucion no exitosa de comando, cancelando proceso");
		return false;
	}
	
	/**
	 * funcion que ejecuta el comando indicado y obtiene la respuesta
	 * @param comando (byte[]) comando a ejecutar
	 * @param commandInit (byte) caracter de inicio
	 * @param commandEnd (byte) caracter de fin
	 */
	private void ejecutarComando(byte[] comando, byte commandInit, byte commandEnd){
		//enviamos el comando
		Message msg = new Message();
		String respuesta;
		try {
			if(enviarComando(comando)){
				if(waitForAnswer){
					respuesta = leerRespuesta(commandInit, commandEnd);
					if(respuesta!=null){
						Log.i(TAG, "Respuesta de ejecucion de comando recibida");
						Log.d(TAG, "Respuesta: "+respuesta);
						msg.what=0;
						msg.obj= respuesta;
					}
					else{throw new SerialConnectionException("error obteniendo datos pinpad");}
				}
				else{
					leerRespuesta();
					Log.i(TAG,"Comando Ejecutado de forma exitosa");
					msg.what = 0;
				}
			}
			else{throw new SerialConnectionException("error obteniendo datos pinpad");}
		} 
		catch (Exception e) {
			Log.e(TAG, "No se pudo ejecutar el comando: "+Utils.bytesToHex(comando)+", error: ",e);
			//respuesta = e.getMessage();
			msg.what = 1;
			//msg.obj = respuesta;
			if(e.getMessage().contains(TAGCANCEL)){msg.obj=TAGCANCEL;}
			else{msg.obj = "comunicación con el PINPad perdida";}
		}
		finally{
			//detenemos el timeout
			timeout.interrupted=true;
			timeout.corriendo = false;
			
			//invocamos el handler y entregamos la respuesta de la ejecucion del comando
			handler.sendMessage(msg);
		}
	}
	
	//Constructor
	/**
	 * Constructor del objecto de escritura y lectura 
	 * @param conector (WirelessConector) conector que tiene el canal de comunicacion
	 */
	private ReadWriteThread(WirelessConector conector) {
		
		//indicamos que se espera una respuesta
		this.waitForAnswer = true;
		
		//Recuperamos los canales de comunicacion 
		reads = new DataInputStream(conector.getInputStream());
		writes = new DataOutputStream(conector.getOutputStream());
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		//delay antes de ejecutar comando
		try {sleep(300);} 
		catch (InterruptedException e) {
			Log.w(TAG, "error en delay, error: ",e);
			Log.w(TAG, "Ejecutando sin esperar");
		}
		
		//iniciamos hilo de timeout TODO
		timeout = new TimeoutThread(TIMEOUTEJECUCION);
		threadPool.execute(timeout);
		
		//ejecutamos el comando
		ejecutarComando(comando, commandInit, commandEnd);
	}
	
	//Metodos Publicos
	/**
	 * funcion que crea y/o entrega una instancia
	 * @param conector (WirelessConector) interfaz de coneccion con el pinpad 
	 * @param handler (Handler) quien procesa el resultado de la transaccion
	 * @return (ReadWriteThread) la instancia solicitada
	 */
	public static ReadWriteThread getInstancia(WirelessConector conector, ExecutorService threadPool, Handler handler){
		if(instancia==null){instancia = new ReadWriteThread(conector);}
		
		instancia.threadPool = threadPool;
		instancia.handler = handler;
		
		return instancia;
	}
	
	/**
	 * procedimiento que destruye la instacia actual
	 * @throws IOException 
	 */
	public static void destroyInstance () throws IOException{
		if(instancia!=null){
			instancia.comando = null;
			instancia.handler = null;
			
			//cerramos canal de escritura
			if(instancia.writes!=null){	
				instancia.writes.close();
				instancia.writes = null;
			}
			
			//cerramos canal de lectura
			if(instancia.reads!=null){
				instancia.reads.close();
				instancia.reads = null;
			}
			
			instancia = null;
		}
	}
	
	//Getter's && Setter's
	public byte[] getComando() {
		return comando;
	}

	public void setComando(byte[] comando) {
		this.comando = comando;
	}

	public byte getCommandInit() {
		return commandInit;
	}

	public void setCommandInit(byte commandInit) {
		this.commandInit = commandInit;
	}

	public byte getCommandEnd() {
		return commandEnd;
	}

	public void setCommandEnd(byte commandEnd) {
		this.commandEnd = commandEnd;
	}

	public boolean isWaitForAnswer() {
		return waitForAnswer;
	}

	public void setWaitForAnswer(boolean waitForAnswer) {
		this.waitForAnswer = waitForAnswer;
	}
	
}
