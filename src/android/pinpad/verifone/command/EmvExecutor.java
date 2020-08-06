/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.verifone.command;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import emv.BerTlvChain;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanPinblock;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.connection.configuracion.Configuracion;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.UtilField55;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.thread.ReadWriteThread;

/**
 * 
 * clase encargada de realizar la ejecucion y obtencion de la 
 * peticion de primer certificado.
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class EmvExecutor extends Thread {

	//CONSTANTES
	private static final String TAG = EmvExecutor.class.getName();
	
	//anulacion
	private static final String checkAnulacion = "000";
	
	//Response
	private static final String OK = "00";
	private static final String NOK = "99";
	
	/*ERRORES*/
	private static final String IDERROR = "!ERROR!";
	
	/*Comandos*/
	private static final int COMZ9030 = 0;
	private static final int COMZ9033 = 1;
	private static final int COMZ9230 = 2;
//	private static final int COMZ9233 = 3;
	private static final int COME01 = 4;
	private static final int COME02 = 5;
	private static final int COME03 = 6;
	
	//Atributos
	//Memoria
	private String tag;
	private String statusTag;
	private SharedPreferences sp;
	
	//Comando en ejecucion
	private int command = COMZ9033;
	
	//estatus comando ejecutado
	private String commandStatus = "";
	
	//datos de la transaccion
	private String tipoTransEmv;
	private String monto;
	private String cashback;
	
	//datos de las llaves 
	private Configuracion configuracion;
	
	//datos del procesamiento pinpad
		//indicadores
	private boolean retireTarjeta = true;
	private int pinOnline = 0;
	
		//datos de la transaccion
	private BeanTarjeta tarjeta;
	private BeanPinblock pinblock;
	
	//datos de coneccion
	private WirelessConector conector; 
	private ReadWriteThread thread;
	
	//manejador de hilos
	private ExecutorService threadPool;
	
	//Handler's
	@SuppressLint("HandlerLeak")
	private Handler proccessHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			try{
				if(msg.what==0){
					switch (command) {
						case COMZ9030:{processZ9030((String) msg.obj); break;}
						case COMZ9033:{processZ9033((String) msg.obj); break;}
						case COMZ9230:{break;}
//						case COMZ9233:{processZ9233((String) msg.obj); break;}
						case COME01:{processE01((String) msg.obj); break;}
						case COME02:{processE02((String) msg.obj); break;}
						case COME03:{break;}
						default:{throw new Exception("no se esta evaluando un comando valido");}
					}
				}
				else{throw new Exception((String) msg.obj);}
			}
			catch(Exception e){
				try{
					Log.e(TAG, "tarjeta / pin no procesados, error: ",e);
					BeanBase json = new BeanBase();
					json.setEstatus((!commandStatus.equals(""))?commandStatus:NOK);
					json.setMensaje(e.getMessage());
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
	
	//Metodos Privados
	/**
	 * Funcion que procesa la tarjeta si esta fue leida pro banda magnetica
	 * @param respuesta (String) datos de origen para la extraccion
	 * @return (BeanTarjeta) la respuesta esperada por la aplicacion 
	 * @throws Exception - problemas de procesamiento
	 */
	private BeanTarjeta obtenerTarjetaBanda(String respuesta) throws Exception{
		
		BeanTarjeta tarjeta = new BeanTarjeta(true);
		tarjeta.setExtrationMode("B");
		
		//validamos el formato de la respuesta
		if(respuesta.indexOf(';')==-1){
			Log.e(TAG, "Error en lectura banda, formato de respuesta no es el esperado");
			commandStatus = "98";
			throw new Exception("error procesando tarjeta");
		}
		
		//Obtenemos el track2
		String aux = respuesta.substring(respuesta.indexOf(";"));
		tarjeta.setTrack2Data(aux.substring(1,aux.indexOf("?")));
		
		//Extraemos la informacion del  track2 
		String[] camposTrack2 = Tarjeta.extraerDatosTrack2(tarjeta.getTrack2Data());
		tarjeta.setObfuscatedPan(Tarjeta.obtenerPanEnmascarado(camposTrack2[0]));
		
		//Obtenemos el service code
		tarjeta.setServiceCode(Tarjeta.extraerServiceCode(tarjeta.getTrack2Data()));
		
		//Obtenemos el track 1 y extraemos el nombre del tarjetahabiente de ser posible
		String track1 = Tarjeta.obtenerTrack1(respuesta);
		tarjeta.setCardholderName(Tarjeta.extrarDatosTrack1(track1));
		
		//indicamos que el procesamiento fue correcto
		tarjeta.setEstatus(OK);
		tarjeta.setMensaje("Lectura Banda");
		
		//entregamos la respuesta
		return tarjeta;
	}
	
	/*
	private BeanTarjeta obtenerTarjetaBandaEncriptada(String respuesta) throws Exception{
		BeanTarjeta tarjeta = new BeanTarjeta(true);
		tarjeta.setExtrationMode("B");
		
		//validamos el formato de la respuesta
		if(respuesta.indexOf(';')==-1){
			Log.e(TAG, "Error en lectura banda, formato de respuesta no es el esperado");
			throw new Exception("Error de lectura banda");
		}
		
		return tarjeta;
		
	}
	*/
	
	/**
	 * funcion que recupera los datos de la tarjeta y los coloca en memoria
	 * @param respuesta (String) respuesta recibida por la ejecucion del comando
	 * @return (BeanTarjeta) datos de la tarjeta recuperados
	 * @throws Exception - las posibles excepciones del analisis
	 */
	private BeanTarjeta obtenerTarjetaChip(String respuesta) throws Exception{
		BeanTarjeta tarjeta = new BeanTarjeta(true);
		
		tarjeta.setExtrationMode("E");
		
		//Obtenemos el campo 55
		tarjeta.setTlv(Tarjeta.obtenerCampo55(respuesta));
		BerTlvChain tlv;
		try{tlv = Tarjeta.obtenerTlv(respuesta,CommandBuilder.emuladorTAGCero);}
		catch(Exception e){
			Log.e(TAG, "Error en lectura chip, procesando campo 55, error: ",e);
			commandStatus = "98";
			throw new Exception("error procesando tarjeta");
		}
		
		//Obtenemos el track2 
		tarjeta.setTrack2Data(Tarjeta.obtenerTrack2(tlv));
		
		//Extraemos la informacion del  track2 
		String[] camposTrack2 = Tarjeta.extraerDatosTrack2(tarjeta.getTrack2Data());
		tarjeta.setObfuscatedPan(Tarjeta.obtenerPanEnmascarado(camposTrack2[0]));
		
		//Obtenemos el service code
		tarjeta.setServiceCode(Tarjeta.extraerServiceCode(tarjeta.getTrack2Data()));
		
		//Obtenemos el track 1 y extraemos el nombre del tarjetahabiente de ser posible
		String track1 = Tarjeta.obtenerTrack1(respuesta);
		tarjeta.setCardholderName(Tarjeta.extrarDatosTrack1(track1));
		
		//indicamos que el procesamiento fue correcto
		tarjeta.setEstatus(OK);
		tarjeta.setMensaje("Lectura Chip");
		
		//entregamos la respuesta
		return tarjeta;
	}
	
	/**
	 * funcion que recupera los datos de la tarjeta si esta produjo un fallback y se leyo por banda
	 * @param respuesta (String) respuestad del comando
	 * @return (BeanTarjeta) tarjeta recuperada
	 * @throws Exception - las posibles excepciones producto del procesamiento
	 */
	private BeanTarjeta obtenerTarjetaFallback(String respuesta) throws Exception{
		
		BeanTarjeta tarjeta = obtenerTarjetaBanda(respuesta);
		
		tarjeta.setExtrationMode("F");
		
		return tarjeta;
		
	}
	
	//procesamiento de comandos
	/**
	 * funcion que procesa la respuesta en caso de que se produjera un fallback en el lector
	 * @param respuesta (String) respuesta del pinpad 
	 * @throws Exception - las posibles excepciones encontradas
	 */
	private void processZ9030(String respuesta) throws Exception{
		
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_Z9030, respuesta.substring(3,5))){
			//recuperamos y entregamos los datos de la tarjeta
			Log.i(TAG, "Tarjeta leida por banda Magnetica, recuperando datos");
			tarjeta = obtenerTarjetaFallback(respuesta);
			Utils.saveTmpData(tag, tarjeta.toJson().toString(), sp);
		}
		else{
			commandStatus = respuesta.substring(3,5);
			throw new Exception("fallback no procesado, "+CommandBuilder.getFailMessage(commandStatus));	
		}
		
	}
	
	/**
	 * procesa el resultado del comando Z9033
	 * @param respuesta (String) respuesta entregada por el comando 
	 * @throws Exception - en caso de un fallo de lectura
	 */
	private void processZ9033(String respuesta) throws Exception{
		Log.i(TAG, "Procesando respuesta de lectura de tarjeta");
		
		//verificamos el estado de la lectura realizada por el pinpad
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_Z9033, respuesta.substring(3,5))){
			
			//realizamos el procesamiento de la tarjeta segun el medio de captura
			int medio = Integer.parseInt(respuesta.substring(3,5));
			switch(medio){
				case CommandBuilder.COMANDO_RESPUESTA_CHIP:{
					Log.i(TAG, "Leyendo datos de tarjeta chip");
					
					//indicamos que estamos procesando
					Utils.saveTmpData(statusTag, "Procesado Información", sp);
					
					//indicamos el numero comando a procesar
					this.command = COME01;
					
					//colocamos los datos requeridos para la ejecucion del comando
					thread.setComando(CommandBuilder.getComandoE01(tipoTransEmv, (!monto.equals(checkAnulacion))?monto:"100", cashback));
					thread.setCommandInit(CommandBuilder.STX);
					thread.setCommandEnd(CommandBuilder.ETX);
					
					//ejecutamos el comando
					threadPool.execute(thread);
					
					break;	
				}
				case CommandBuilder.COMANDO_RESPUESTA_BANDA:{
					retireTarjeta = false;
					
					//recuperamos y entregamos los datos de la tarjeta
					Log.i(TAG, "Tarjeta leida por banda Magnetica, recuperando datos");
					tarjeta = obtenerTarjetaBanda(respuesta);
					Utils.saveTmpData(tag, tarjeta.toJson().toString(), sp);
					break;
				}
				default:{
					
					if(retireTarjeta){
						//Solicitamos el retiro de tarjeta
						BerTlvChain tlv = null;
						tlv = UtilField55.createEmptyField55(tlv);
						
						//indicamos el comando a procesar
						this.command = COME03;
						
						//colocamos el comando a ejecutar
						thread.setComando(CommandBuilder.getComandoE03("FF", "1", tlv.getString(true)));
						thread.setCommandInit(CommandBuilder.STX);
						thread.setCommandEnd(CommandBuilder.ETX);
						
						//ejecutamos el comando
						threadPool.execute(thread);
					}
					
					//enviamos el error
					throw new Exception("tarjeta no procesada");
				}
			}
		}
		else if(CommandBuilder.isFail(CommandBuilder.COMANDO_Z9033, respuesta.substring(3,5))){
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
		else{
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
	}
	
	/**
	 * TODO - PENDIENTE
	 * @param respuesta
	 * @throws Exception
	 */
/*
	private void processZ9233(String respuesta) throws Exception{
		Log.i(TAG, "Procesando respuesta de lectura de tarjeta encriptada");
		
		//verificamos el estado de la lectura realizada por el pinpad
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_Z9033, respuesta.substring(3,5))){
			//realizamos el procesamiento de la tarjeta segun el medio de captura
			int medio = Integer.parseInt(respuesta.substring(3,5));
			switch(medio){
				case CommandBuilder.COMANDO_RESPUESTA_CHIP:{
					Log.i(TAG, "Leyendo datos de tarjeta chip");
					
					//indicamos el numero comando a procesar
					this.command = COME01;
					
					//colocamos los datos requeridos para la ejecucion del comando
					thread.setComando(CommandBuilder.getComandoE01(tipoTransEmv, monto, cashback));
					thread.setCommandInit(CommandBuilder.STX);
					thread.setCommandEnd(CommandBuilder.ETX);
					
					//ejecutamos el comando
					threadPool.execute(thread);
					
					break;	
				}
				case CommandBuilder.COMANDO_RESPUESTA_BANDA:{
					retireTarjeta = false;
					
					//recuperamos y entregamos los datos de la tarjeta
					Log.i(TAG, "Tarjeta leida por banda Magnetica, recuperando datos");
					
					//TODO
//					tarjeta = obtenerTarjetaBandaEncriptada(respuesta);
//					Utils.saveTmpData(tag, tarjeta.toJson().toString(), sp);
					
					
					throw new Exception("Prueba de tarjeta encriptada");
					
//					break;
				}
				default:{
					
					if(retireTarjeta){
						//Solicitamos el retiro de tarjeta
						BerTlvChain tlv = null;
						tlv = UtilField55.createEmptyField55(tlv);
						
						//indicamos el comando a procesar
						this.command = COME03;
						
						//colocamos el comando a ejecutar
						thread.setComando(CommandBuilder.getComandoE03("FF", "1", tlv.getString(true)));
						thread.setCommandInit(CommandBuilder.STX);
						thread.setCommandEnd(CommandBuilder.ETX);
						
						//ejecutamos el comando
						threadPool.execute(thread);
					}
					
					//enviamos el error
					throw new Exception("Tarjeta no procesada");
				}
			}
		}
		else if(CommandBuilder.isFail(CommandBuilder.COMANDO_Z9033, respuesta.substring(3,5))){
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("Tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
		else{
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("Tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
	}
	*/

	/**
	 * procesa el resultado del comando E01
	 * @param respuesta (String) respuesta entregada por el comando 
	 * @throws Exception - en caso de un fallo de lectura
	 */
	private void processE01(String respuesta) throws Exception{
		
		//analizamos la respuesta recibida
		String status = respuesta.substring(3, 5);
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E01, status)){
			String comandoResp = respuesta.substring(0,3);
			if(comandoResp.equals(CommandBuilder.COMANDO_RESPUESTA_E10)){pinOnline = 0;}
			else if(comandoResp.equals(CommandBuilder.COMANDO_RESPUESTA_E1A)){pinOnline = 1;}
			else{
				pinOnline = -1;
				throw new Exception("no se identifico si la tarjeta pide pinblock o no");
			}
			
			//recuperamos la data chip
			Log.i(TAG, "Tarjeta leida por lector chip");
			tarjeta = obtenerTarjetaChip(respuesta);
			
			//verificamos si debemos pedir pinblock
			if(pinOnline == 1){
				//Solicitamos y ejecutamos el comando de pinonline
				Log.i(TAG, "Solicitando pinblock");
				
				//Mostramos el dialogo de solicitud de pin
				Utils.saveTmpData(statusTag, "ingrese PIN", sp);
				
				//indicamos el numero de comando a procesar
				this.command = COME02;
				
				//colocamos los datos requeridos para la ejecucion del comando
				thread.setComando(CommandBuilder.getComandoE02(configuracion.getPinpadIndiceWk(), configuracion.getPinpadWorkingKey(), (!monto.equals(checkAnulacion))?monto:null));
				thread.setCommandInit(CommandBuilder.STX);
				thread.setCommandEnd(CommandBuilder.ETX);
				
				//ejecutamos el comando
				threadPool.execute(thread);
			}
			//en caso de ser un tarejta de credito se entrega sus datos
			else{Utils.saveTmpData(tag, tarjeta.toJson().toString(), sp);}
			
		}
		else if(CommandBuilder.isFallback(CommandBuilder.COMANDO_E01, status)){
			Log.i(TAG, "Se produjo un fallback, activando lector de banda magnerica");
			
			//Mostramos el mensaje de deslice tarjeta
			Utils.saveTmpData(statusTag, "Error en chip, deslice Tarjeta", sp);
			
			//indicamos el numero de comando a procesar
			this.command = COMZ9030;
			
			//preparamos el comando de lectura por banda Z9030
			thread.setComando(CommandBuilder.getComandoZ9030());
			thread.setCommandInit(CommandBuilder.STX);
			thread.setCommandEnd(CommandBuilder.ETX);
			
			//ejecutamos el comando
			threadPool.execute(thread);
		}
		else if(CommandBuilder.isFail(CommandBuilder.COMANDO_E01, status)){
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
		else{
			commandStatus = respuesta.substring(3, 5);
			throw new Exception("tarjeta no procesada, "+CommandBuilder.getFailMessage(commandStatus));
		}
	}
	
	/**
	 * analiza la respuesta de una tarjeta que posee pin online
	 * @param respuesta (String) respuesta del pinpad 
	 * @throws Exception - las posibles exceptionnes producto del analisis
	 */
	private void processE02(String respuesta) throws Exception{
		Log.i(TAG, "Procesando informacion de tarjeta con pin online");
		
		//Analizamos la respuesta recibida
		if(respuesta.length()<3){throw new Exception("Error en obtencion de PIN");}
		if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E02, respuesta.substring(3,5))){
			String pinblock;
			String comandoResp = respuesta.substring(0,3);
			if(!comandoResp.equals(CommandBuilder.COMANDO_RESPUESTA_E12)){throw new Exception("Error en obtencion de PIN");}
			else{
				int index = Integer.valueOf(respuesta.substring(5,8)).intValue();
				pinblock = respuesta.substring(12+index,28+index);
				
				//creamos el bean de pinblock
				this.pinblock = new BeanPinblock();
				this.pinblock.setPinblockData(pinblock);
				this.pinblock.setPinblockKsn("");
			}
		}
		else{
			//capturamos el estado de la transaccion
			commandStatus = respuesta.substring(3, 5);
			
			//enviamos el error
			throw new Exception("error en obtencion de PIN, "+CommandBuilder.getFailMessage(commandStatus));
			
		}
		
		//actualizamos el campo 55 de la tarejta
		tarjeta.setTlv(Tarjeta.obtenerCampo55(respuesta));
		
		//procesamos la respuesta para entregarla segun lo solicitado
		String key;
		JSONObject json = tarjeta.toJson();
		JSONObject pinblockJson = pinblock.toJson();
		@SuppressWarnings("rawtypes")
		Iterator i = pinblockJson.keys();
		while(i.hasNext()){
			key = (String) i.next();
			json.put(key, pinblockJson.get(key));
		}
		
		Utils.saveTmpData(tag, json.toString(), sp);
	}
	
	//Constructor
	//TODO - agregar parametro de modalidad de trabajo encriptado o no
	/**
	 * constructor de la clase
	 * @param tag (String) identificador con el se enviara la respuesta
	 * @param conector (WirelessConector) conector con el pinpad 
	 * @param sp (SharedPreferences) lugar donde se almacenara la data
	 * @param threadPool (ExecutorService) ejecutor de hilos 
	 * @param configuracion (Configuracion) configuracion a usar
	 * @param tipoTransaccion (String) tipo de transaccion a ejecutar
	 * @param monto (String) monto de la transaccion de compra
	 * @param avance (String) monto del avance / retiro
	 */
	public EmvExecutor(String tag, String statusTag, WirelessConector conector, SharedPreferences sp, ExecutorService threadPool, Configuracion configuracion,String tipoTransaccion, String monto, String avance){
		this.tag = tag;
		this.statusTag = statusTag;
		this.conector = conector;
		this.sp = sp;
		this.threadPool = threadPool;
		this.configuracion = configuracion;
		this.tipoTransEmv = tipoTransaccion;
		this.monto = monto;
		this.cashback = avance;
	}
	
	//Metodos Sobre Escritos
	@Override
	public void run() {
		
		//mostramos el dialogo
		Utils.saveTmpData(statusTag, "Inserte o deslice Tarjeta", sp);
		
		//indicamos el comando de arranque
//		command = COMZ9233;
		command = COMZ9033;
		
		//creamos el hilo de ejecucion de comandos
		thread = ReadWriteThread.getInstancia(conector, threadPool, proccessHandler);
		
		//Colocamos el comando inicial
		thread.setComando(CommandBuilder.getComandoZ9033());
//		thread.setComando(CommandBuilder.getComandoZ9233());
		thread.setCommandInit(CommandBuilder.STX);
		thread.setCommandEnd(CommandBuilder.ETX);
		
		//iniciamos el procesamiento
		threadPool.execute(thread);
	}

	//Metodos Publicos
	/**
	 * procedimiento que permite setear el mensaje que se desea mostrar
	 * @param mensaje (String) mensaje que se desea setear
	 */
	public void setMensaje(String mensaje){CommandBuilder.setMessage(mensaje);}
	
}
