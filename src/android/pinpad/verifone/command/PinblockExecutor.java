/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.verifone.command;

import java.util.concurrent.ExecutorService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanPinblock;
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.connection.configuracion.Configuracion;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.thread.ReadWriteThread;

/**
 * 
 * clase encargada de realizar todo el procesamiento requerido
 * para la captura de un pinblock (MSR)
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class PinblockExecutor extends Thread {

	//CONSTANTES
	private static final String TAG = PinblockExecutor.class.getName();
	
	//anulacion
	private static final String checkAnulacion = "000";
	
	//Response
	private static final String OK = "00";
	private static final String NOK = "99";
	
	/*ERRORES*/
	private static final String IDERROR = "!ERROR!";
	private static final String CANCEL = "CANCEL_BY_USER";
	
	/*Commandos*/
	private static final int COM08 = 0;
	private static final int COMZ62 = 1;
	
	//Atributos
	//Memoria
	private String tag;
	private SharedPreferences sp;
	
	//Comando en ejecucion
	private int command = COM08;
	
	//datos de la transaccion
	private String numeroTarjeta;
	private String montoTransaccion;
	
	//datos de coneccion
	private Configuracion configuracion;
	private WirelessConector conector; 
	private ReadWriteThread thread;
	
	//manejador de hilos
	private ExecutorService threadPool;
	
	//Handler
	@SuppressLint("HandlerLeak")
	private Handler proccessHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			//DEBUG
			Log.e(tag,"Salida obj: "+(String) msg.obj);
			Log.e(tag,"Salida what: "+msg.what);
			
			try{
				if(msg.what==0){
					switch (command) {
						case COM08:{processCom08(); break;}
						case COMZ62:{processComZ62((String) msg.obj); break;}
						default:{throw new Exception("no se esta evaluando un comando valido");}
					}
				}
				else{throw new Exception((String) msg.obj);}
			}
			catch(Exception e){
				try{
					BeanBase json = new BeanBase();
					if(((String)msg.obj).equals(CANCEL)){
						json.setEstatus("05");
						json.setMensaje("cancelado por el usuario");
					}
					else{
						json.setEstatus(NOK);
						json.setMensaje(e.getMessage());
					}
					Utils.saveTmpData(tag, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(tag, IDERROR+":99:No se pudo procesar la tarjeta, "+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
	//Metodos Privaos 
	/**
	 * procedimiento que procesa la respuesta de la ejecucion del comando 08
	 */
	private void processCom08(){
		Log.i(TAG, "llave maestra seleccionada, ejecutando comando de solicitud de pinblock");
		
		//indicamos el comando a procesar 
		this.command = COMZ62;
		
		//colocamos los datos requeridos 
		thread.setComando(CommandBuilder.getComandoZ62(configuracion.getPinpadWorkingKey(), numeroTarjeta, (!montoTransaccion.equals(checkAnulacion))?montoTransaccion:null));
		thread.setCommandInit(CommandBuilder.STX);
		thread.setCommandEnd(CommandBuilder.ETX);
		thread.setWaitForAnswer(true);
		
		//ejecutamos el comando
		threadPool.execute(thread);
		
	}
	
	/**
	 * procedimiento que procesa la respuesta del comando de obtencion de pinblock
	 * @param respuesta (String) respuesta recibida por la ejecucion del comando 
	 * @throws Exception - las posibles excepciones del procesamiento de la respuesta
	 */
	private void processComZ62(String respuesta) throws Exception{
		if(respuesta.length()<3){throw new Exception("");}
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_Z62, respuesta.substring(0,2))){
			
			//generamos el objeto de respuseta
			BeanPinblock pinblock = new BeanPinblock();
			pinblock.setEstatus(OK);
			pinblock.setMensaje("Pinblock Generado");
			pinblock.setPinblockData(respuesta.substring(8, 24));
			
			//lo guardamos a memoria
			Utils.saveTmpData(tag, pinblock.toJson().toString(), sp);
			
		}
		else{throw new Exception("error en la captura de PIN");}
		
	}
	
	//Constructor
	/**
	 * constructor de la clase para la ejecucion de solicitud de pinblock
	 * @param sp (SharedPreferences) memoria compartida de la aplicacion
	 * @param numeroTarjeta (String) numero de tarjeta (leida por banda)
	 * @param montoTransaccion (String) monto de transaccion 
	 * @param conector (WirelessConector) via de comunicacion contra el pinpad
	 * @param threadPool (ExecutorService) ejecutor de hilos
	 */
	public PinblockExecutor(Configuracion configuracion, SharedPreferences sp, String numeroTarjeta, String montoTransaccion, WirelessConector conector, ExecutorService threadPool, String tag) {
		this.sp = sp;
		this.tag = tag;
		this.numeroTarjeta = numeroTarjeta;
		this.montoTransaccion = montoTransaccion;
		this.conector = conector;
		this.threadPool = threadPool;
		this.configuracion = configuracion;
	};
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		
		//indicamos el comando a ejecutar
		command = COM08;
		
		//creamos el hilo de ejecucion de comando
		thread = ReadWriteThread.getInstancia(conector, threadPool, proccessHandler);
		
		//colocamos el comando inicial 
		thread.setComando(CommandBuilder.getComando08(configuracion.getPinpadIndiceWk()));
		thread.setWaitForAnswer(false);
		
		//iniciamos el procesamiento del comando
		threadPool.execute(thread);
		
	}

	//Metodos Publicos
	/**
	 * procedimiento que permite setear el mensaje que se desea mostrar
	 * @param mensaje (String) mensaje que se desea setear
	 */
	public void setMensaje(String mensaje){CommandBuilder.setMessage(mensaje);}
	
}
