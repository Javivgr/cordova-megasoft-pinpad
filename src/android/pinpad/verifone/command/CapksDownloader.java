/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.verifone.command;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.thread.ReadWriteThread;

/**
 * 
 * clase encargada de realizar la descaga de llaves EMV al dispositivo
 * 
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class CapksDownloader extends Thread {

	
	//CONSTANTES
	private static final String TAG = CapksDownloader.class.getName();
	
	//Response
	private static final String OK = "00";
	private static final String NOK = "99";
	
	/*ERRORES*/
	private static final String IDERROR = "!ERROR!";
	
	/*Commandos*/
	private static final int COMZ2 = 0;
	private static final int COME07 = 1;
	
	//Atributos
	//Memoria
	private String tag;
	private SharedPreferences sp;
	
	//datos de coneccion
	private WirelessConector conector; 
	private ReadWriteThread thread;
	
	//manejador de hilos
	private ExecutorService threadPool;
	
	//aids
	private int count = 1;
	private String capksCount;
	private String capksTotal;
	private String texto = "Act. Llaves";
	private BeanEmvKeyInfo lastCapks;
	private List<BeanEmvKeyInfo> capks;
	private Iterator<BeanEmvKeyInfo> iterator;
	
	//Handler's
	@SuppressLint("HandlerLeak")
	private Handler processHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			String commandStatus = "";
			
			try{
				if(msg.what==0){
					String respuesta = (String) msg.obj;
					if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E06, respuesta.substring(3,5))){
						if(iterator.hasNext()){
							
							//Incrementamos el contador
							count++;
							
							//actualizamos el texto de la pantalla -- TODO
							
							//tiempo de ajuste del pinpad 
							sleep(500);
							
							//cargamos la siguiente llave
							downloadCapk();
						}
						else{
							//construimos la respuesta exitosa a la carga de aids
							BeanBase bean = new BeanBase();
							bean.setEstatus(OK);
							bean.setMensaje("Llaves EMV Cargadas de forma exitosa");
							
							//entregamos la respuesta
							Utils.saveTmpData(tag, bean.toJson().toString(), sp);
						}
					}
					else{
						commandStatus = respuesta.substring(3, 5);
						throw new Exception(CommandBuilder.getFailMessage(commandStatus));
					}
				}
				else{throw new Exception((String) msg.obj);}
				
			}
			catch(Exception e){
				Log.e(TAG, "Fallo en el proceso de carga de llave EMV: "+lastCapks+", error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((!commandStatus.equals(""))?commandStatus:NOK);
					json.setMensaje("No se pudo procesar el listado de Llaves EMV, error "+e.getMessage());
					Utils.saveTmpData(tag, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(tag, IDERROR+":99:No se pudo procesar el listado de Llaves EMV, "+e.getMessage(), sp);
				}
			}
		}
	};
	
	//Metodos Privados
	public void setScreenText(){}
	
	private void downloadCapk() throws ParseException{
		//recuperamos la llave a descargar al pinpad
		lastCapks = iterator.next();
		
		Log.i(TAG, "Llave emv a cargar: "+lastCapks);
		
		//colocamos el comando a ejecutar
		byte[] e06 = CommandBuilder.getComandoE06(lastCapks);
		if(e06 != null){
			thread.setComando(e06);
			thread.setCommandInit(CommandBuilder.STX);
			thread.setCommandEnd(CommandBuilder.ETX);
			
			Log.i(TAG, "Comando Montado, Iniciando Ejecucion");
			
			//iniciamos el procesamiento del comando
			threadPool.execute(thread);
		}
		else{
			Log.w(TAG, "llave no cargada, supera longitud permitida");
			
			Message msg = new Message();
			msg.what = 0;
			msg.obj = "E1600";
			
			processHandler.sendMessage(msg);
		}
	}
	
	//Constructor
	public CapksDownloader (SharedPreferences sp, WirelessConector conector, ExecutorService threadPool, List<BeanEmvKeyInfo> capks, String tag){
		
		super();
		this.sp = sp;
		this.conector = conector;
		this.threadPool = threadPool;
		this.tag = tag;
		this.capks = capks;
		
		this.iterator = this.capks.iterator();
		
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		
		//Solicitamos el tamano de la lista -- TODO
		
		//creamos el hilo de ejecucion
		thread = ReadWriteThread.getInstancia(conector, threadPool, processHandler);
		
		//iniciamos la descarga de llave publicas emv al dispositivo
		try {downloadCapk();} 
		catch (ParseException e) {
			Log.e(TAG, "No se pudo iniciar la carga de llaves EMV");
			Message msg = new Message();
			msg.what = 1;
			msg.obj = "No se pudo iniciar la carga de llaves EMV, Error: "+e.getMessage();
			processHandler.sendMessage(msg);
		}
		
	}
}
