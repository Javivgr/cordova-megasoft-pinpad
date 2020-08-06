/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.verifone.command;

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
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.thread.ReadWriteThread;

/**
 * 
 * executor encargado de realzar la carga de aids en el pinpad
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class AidsDownloader extends Thread {

	//CONSTANTES
	private static final String TAG = AidsDownloader.class.getName();
	
	//Response
	private static final String OK = "00";
	private static final String NOK = "99";
	
	/*ERRORES*/
	private static final String IDERROR = "!ERROR!";
	
	/*Commandos*/
	private static final int COMZ1 = 0;
	private static final int COMZ2 = 1;
	private static final int COME07 = 2;
	
	/*String TAGS*/
	private static final String TAGAHORA = "@ahora@";
	private static final String TAGTOTAL = "@total@";
	
	//Atributos
	//Memoria
	private String tag;
	private String updateTag;
	private SharedPreferences sp;
	
	//datos de coneccion
	private WirelessConector conector; 
	private ReadWriteThread thread;
	
	//manejador de hilos
	private ExecutorService threadPool;
	
	//aids
	private int command;
	
	private int count = 1;
	private String aidCount;
	private String aidTotal;
	
	private String texto = "Act Aids @ahora@-@total@";
	
	private BeanAidsInfo lastAid;
	private List<BeanAidsInfo> aids;
	private Iterator<BeanAidsInfo> iterator;

	//Handler
	@SuppressLint("HandlerLeak")
	private Handler processHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			String commandStatus = "";
			
			try{
				if(msg.what==0){
					switch (command) {
						case COMZ1:{break;} 
						case COMZ2:{downloadAid(); break;}
						case COME07:{
							//recuperamos la respuesta de la ejecucion del comando
							String respuesta = (String) msg.obj;
							if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E07, respuesta.substring(3, 5))){
								Log.i(TAG, "AID instalado");
								
								//verificamos si tenemos aids por procesar
								if(iterator.hasNext()){
									//incrementamos el contador
									count++;
									
									//actualizamos el texto de la pantalla
									//setScreenText();
									
									//tiempo de ajuste del pinpad 
									sleep(500);
									
									//cargamos la siguiente aid
									downloadAid();
								}
								else{
									Log.i(TAG, "AIDS instalados");
									Utils.saveTmpData(updateTag, "AIDS instalados", sp);
									
									//construimos la respuesta exitosa a la carga de aids
									BeanBase bean = new BeanBase();
									bean.setEstatus(OK);
									bean.setMensaje("Aids Cargados de forma exitosa");
									
									//entregamos la respuesta
									Utils.saveTmpData(tag, bean.toJson().toString(), sp);
									
									//ejecutamos el comando de retorno a estado idle
									//returnToIdle();
								}
							}
							else{
								commandStatus = respuesta.substring(3, 5);
								throw new Exception(CommandBuilder.getFailMessage(commandStatus));
							}
							break;
						}
					}
				}
				else{throw new Exception((String) msg.obj);}
			}
			catch(Exception e){
				//returnToIdle();
				try{
					Log.e(TAG, "Fallo en el proceso de carga de AID: "+lastAid.getAid()+", error: ",e);
					BeanBase json = new BeanBase();
					json.setEstatus((!commandStatus.equals(""))?commandStatus:NOK);
					json.setMensaje("No se pudo procesar el listado de aids, error "+e.getMessage());
					Utils.saveTmpData(tag, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(tag, IDERROR+":99:No se pudo procesar el listado de aids, "+e.getMessage(), sp);
				}
			}
		};
		
	};
	
	//Metodos privados
	/**
	 * procedimiento que le indica al pinpad que retorne a su estado idle
	 */
	private void returnToIdle(){
		Log.i(TAG, "Retornando el pinpad a idle");
		
		//creamos el comando para retornar a idle
		thread.setComando(CommandBuilder.getComandoZ1());
		thread.setCommandInit(CommandBuilder.STX);
		thread.setCommandEnd(CommandBuilder.ETX);
		
		//indicamos que estamos esperando una respuesta
		thread.setWaitForAnswer(false);
		
		//iniciamos el procesamiento del comando
		threadPool.execute(thread);
	}
	
	/**
	 * proceso que actualiza la pantalla del pinpad y coloca un mensaje de actualizacion en memoria
	 */
	private void setScreenText(){
		Log.i(TAG, "Actualizando texto en el pinpad");
		
		//solicitamos el tamano del iterator
		aidTotal = Integer.toString(aids.size());
		if(aidTotal.length()==1){aidTotal = "0"+aidTotal;}
		
		//solicitamos que aid esta cargandose
		aidCount = Integer.toString(count);
		if(aidCount.length()==1){aidCount = "0"+aidCount;}
		
		//realizamos la sustitucion en el texto base
		String msg = texto.replace(TAGTOTAL, aidTotal).replace(TAGAHORA, aidCount);
		
		//colocamos el mensaje en memoria
		Utils.saveTmpData(updateTag, msg, sp);
		
		//tratamos de ejecutar el comando de texto
		try {
			//informamos del comando en ejecucion
			command = COMZ2;
			
			//colocamos el comando a ejecutar
			thread.setComando(CommandBuilder.getComandoZ2(msg));
			thread.setCommandInit(CommandBuilder.STX);
			thread.setCommandEnd(CommandBuilder.ETX);
			
			//indicamos que no estamos esperando por una respuesta
			thread.setWaitForAnswer(false);
			
			//ejecutamos el comando
			threadPool.execute(thread);
			
		} catch (Exception e) {
			Log.w(TAG, "No se pudo actualizar el texto del pinpad, error: ",e);
			
			//creamos el mensaje al handler para que continue
			Message message = new Message();
			message.what = 0;
			message.obj = "No se Actualizo el texto en el pinpad";
			
			//enviamos el mensaje
			processHandler.sendMessage(message);
		}
		
		
	}
	
	/**
	 * proceso que inicia la descarga de aid en el pinpad 
	 */
	private void downloadAid(){
		//recuperamos el aid a descargar al pinpad 
		lastAid = iterator.next();
		
		Log.i(TAG, "AID a carga: "+lastAid);
		
		//informamos del comando en ejecucion
		command = COME07;
		
		//colocamos el comando a ejecutar
		thread.setComando(CommandBuilder.getComandoE07(lastAid));
		thread.setCommandInit(CommandBuilder.STX);
		thread.setCommandEnd(CommandBuilder.ETX);
		
		//indicamos que estamos esperando una respuesta
		thread.setWaitForAnswer(true);
		
		//iniciamos el procesamiento del comando
		threadPool.execute(thread);
	}
	
	//Constructor
	public AidsDownloader(SharedPreferences sp, WirelessConector conector, ExecutorService threadPool, List<BeanAidsInfo> aids, String tag, String updateTag) {
		super();
		this.sp = sp;
		this.conector = conector;
		this.threadPool = threadPool;
		this.tag = tag;
		this.updateTag = updateTag;
		this.aids = aids;
		iterator = this.aids.iterator();
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		
		//creamos el hilo de ejecucion de comando
		thread = ReadWriteThread.getInstancia(conector, threadPool, processHandler);
		
		//iniciamos el proceso de instalacion de aids
//		setScreenText();
		downloadAid();
	}
	
	
}
