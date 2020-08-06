/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.verifone.modelo;

import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import emv.BerTlvChain;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanBateria;
import ve.com.megasoft.pinpad.bean.BeanImpresora;
import ve.com.megasoft.pinpad.bean.BeanCalibracion;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.bean.BeanSerial;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.connection.WirelessConector;
import ve.com.megasoft.pinpad.connection.bluetooth.conector.BluetoothConector;
import ve.com.megasoft.pinpad.connection.bluetooth.conector.BluetoothServerConector;
import ve.com.megasoft.pinpad.modelo.ModeloPinpadBase;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.UtilField55;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.verifone.command.AidsDownloader;
import ve.com.megasoft.pinpad.verifone.command.CapksDownloader;
import ve.com.megasoft.pinpad.verifone.command.CommandBuilder;
import ve.com.megasoft.pinpad.verifone.command.EmvExecutor;
import ve.com.megasoft.pinpad.verifone.command.PinblockExecutor;
import ve.com.megasoft.pinpad.verifone.thread.ReadWriteThread;

/**
 * 
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * clase que entiende la comunicacion con el pinpad verifone e265, e355
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
@SuppressLint("HandlerLeak")
public class ModeloEWirelessSerie extends ModeloPinpadBase{

	//CONSTANTES
	private static final String TAG = ModeloEWirelessSerie.class.getName();
	
	//Atributos
		//Conectores
	private WirelessConector conector;
	
		//Handler's
			/*funciones basicas de PINPAD*/
	@SuppressLint("HandlerLeak")
	private Handler handlerSerial = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de serial");
			
			try{
				if(msg.what==0){
					//procesamos respuesta del serial
					
					String serial = (String) msg.obj;
					
					Log.d(TAG, "Serial recuperado: "+serial);
					
					BeanSerial bean = new BeanSerial();
					bean.setEstatus(OK);
					bean.setMensaje("Serial Recuperado");
					bean.setSerial(serial.substring(2,13));
					bean.setOs(serial.substring(13,21));
					bean.setApp(serial.substring(21,27));
					bean.setKernel(serial.substring(27,35));
					try{bean.setModelo(serial.substring(35));}
					catch(Exception e){
						Log.w(TAG,"pinpad no entrego modelo, usando valor por default");
						bean.setModelo("E265 - E355");
					}
					bean.setMarca("Verifone");
					//quemamos los valores de los filtros mientras tanto
					bean.setIsPrinter("false");
					bean.setIsICCard("true");
					bean.setIsMagCard("true");
					
					//generamos el JSON y lo guardamos en memoria
					Utils.saveTmpData(TAGSERIAL, bean.toJson().toString(), sp);
				}
				else{throw new Exception((String) msg.obj);}
			}
			catch(Exception e){
				try{
					BeanBase json = new BeanBase();
					json.setEstatus(NOK);
					json.setMensaje("No se pudo recuperar el serial del pinpad, error "+e.getMessage());
					Utils.saveTmpData(TAGSERIAL, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGSERIAL, IDERROR+":99:No se pudo obtener serial, "+e.getMessage(), sp);
				}
			}
//			finally{
//				try {disconnectDevice();} 
//				catch (Exception e) {Log.e(TAG, "Proceso Calibracion, Canal de comunicaciones no cerrado");}
//			}
		};
		
	};
			/*EMV*/
	private Handler handlerSegundoCertificado = new Handler(){
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			//estatus comando
			String commandStatus = "";
			
			try{
				if(msg.what==0){
					String respuesta = (String) msg.obj;
					if(respuesta.length()<3){throw new Exception("Repuesta entregada en formato no valido");}
					if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E03, respuesta.substring(3,5))){
						if(!respuesta.substring(0, 3).equals(CommandBuilder.COMANDO_RESPUESTA_E13)){
							throw new Exception("Segundo certificado fallido");
						}
						else{
							//recuperamos el campo 55 del segundo certificado
							BeanTarjeta bean = new BeanTarjeta(false);
							bean.setEstatus(OK);
							bean.setMensaje("Segundo Certificado exitoso");
							bean.setTlv(Tarjeta.obtenerCampo55(respuesta));
							
							//generamos y guardamos el json en memoria
							Utils.saveTmpData(TAGSEGUNDCERT, bean.toJson().toString(), sp);
						}
					}
					else if(CommandBuilder.isFail(CommandBuilder.COMANDO_E03, respuesta.substring(3, 5))){
						commandStatus = respuesta.substring(3, 5);
						throw new Exception(CommandBuilder.getFailMessage(commandStatus));
					}
					else{
						commandStatus = respuesta.substring(3, 5);
						throw new Exception(CommandBuilder.getFailMessage(commandStatus));
					}
					
				}
				else{throw new Exception((String) msg.obj);}
			}
			catch(Exception e){
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((!commandStatus.equals("")?commandStatus:NOK));
					json.setMensaje("No se pudo procesar el segundo certificado, error "+e.getMessage());
					Utils.saveTmpData(TAGSEGUNDCERT, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGSEGUNDCERT, IDERROR+":99:No se pudo obtener segundo certificado, "+e.getMessage(), sp);
				}
			}
			
		};
		
	};
			/*Cancelaciones*/
	private Handler handlerCancelarEmv = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			try{
				if(msg.what==0){
					String respuesta = (String) msg.obj;
					if(respuesta.length()<3){throw new Exception("Repuesta entregada en formato no valido");}
					if(CommandBuilder.isPositive(CommandBuilder.COMANDO_E03, respuesta.substring(3,5))){
						if(!respuesta.substring(0, 3).equals(CommandBuilder.COMANDO_RESPUESTA_E13)){
							throw new Exception("Fallo solicitando retiro de tarjeta");
						}
						else{
							//recuperamos el campo 55 del segundo certificado
							BeanBase bean = new BeanBase();
							bean.setEstatus(OK);
							bean.setMensaje("Transaccion EMV cancelada");
							
							//generamos y guardamos el json en memoria
							Utils.saveTmpData(TAGABORTEMV, bean.toJson().toString(), sp);
						}
					}
					else{throw new Exception("segundo certificado no positivo");}
					
				}
				else{throw new Exception((String) msg.obj);}
				
			}
			catch(Exception e){
				try{
					BeanBase json = new BeanBase();
					json.setEstatus(NOK);
					json.setMensaje("No se pudo procesar el segundo certificado, error "+e.getMessage());
					Utils.saveTmpData(TAGABORTEMV, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGABORTEMV, IDERROR+":99:No se pudo obtener segundo certificado, "+e.getMessage(), sp);
				}
			}
			
			
		};
		
	};
			/*Calibracion*/
	private Handler handlerCalibracion = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando dispositivo bluetooth seleccionado");
			
			try{
				BeanCalibracion bean = new BeanCalibracion();
				BluetoothDevice device;
				
				//verificamos la respuesta entregada
				if(msg.what==0){
					
					//recuperamos el dispositivo indicado
					Log.i(TAG, "Recuperando disp. Bluetooth seleccionado");
					device = (BluetoothDevice) msg.obj;
					
					//abrimos la coneccion al pinpad
					connectDevice(device);
					
					//creamos la respuesta exitosa de la calibracion
					bean = new BeanCalibracion();
					bean.setEstatus(OK);
					bean.setMensaje("Calibracion Exitosa");
					bean.setDeviceClass(ModeloEWirelessSerie.class.getName());
					bean.setDeviceName(device.getName());
					bean.setDeviceAddress(device.getAddress());
					
					//solicitamos los datos del equipo -- TODO
					
					//seteamos el texto en la pantalla del pinpad 
					//instanciamos el procesador de comandos
//					ReadWriteThread ejecutor = ReadWriteThread.getInstancia(conector, cordova.getThreadPool(),handlerSetIdleText);
//					
//					//colocamos el comando a ejecutar
//					ejecutor.setComando(CommandBuilder.getComandoZ8(configuracion.getPinpadIdleText()));
//					ejecutor.setWaitForAnswer(false);
//					
//					//solicitamos su ejecucion
//					cordova.getThreadPool().execute(ejecutor);
					
				}
				else{
					//en caso de no lograr recuperar el dispositivo bluetooth
					bean = new BeanCalibracion();
					bean.setEstatus(NOK);
					bean.setMensaje((String) msg.obj);
					bean.setDeviceClass(null);
					bean.setDeviceAddress(null);
					bean.setDeviceName(null);
				}
				
				//colocamos la respuesta en memoria
				Log.i(TAG, "Salida, Entregamos resultado de calibracion dispositivo a memoria");
				Utils.saveTmpData(TAGCALIBRACION, bean.toJson().toString(), sp);
			}
			catch(Exception e){
				try{
					BeanBase json = new BeanBase();
					json.setEstatus(NOK);
					json.setMensaje("Calibracion no exitosa, error "+e.getMessage());
					Utils.saveTmpData(TAGCALIBRACION, json.toJson().toString(), sp);
					
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGCALIBRACION, IDERROR+":99:No se pudo calibrar, "+e.getMessage(), sp);
				}
			}
//			finally{
//				try {disconnectDevice();} 
//				catch (Exception e) {Log.e(TAG, "Proceso Calibracion, Canal de comunicaciones no cerrado");}
//			}
		};
		
	};
		/*Otros*/
	@SuppressWarnings("unused")
	private Handler handlerSetIdleText = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			if(msg.what==0){Log.i(TAG, "Mensaje en IDLE seteado");}
			else{Log.w(TAG, "Mensaje en IDLE no seteado");}
			
			
			try {disconnectDevice();} 
			catch (Exception e) {Log.e(TAG, "Proceso Calibracion, Canal de comunicaciones no cerrado");}
			
		};
		
	};
	
	
	//Metodos Privado
	/**
	 * funcion que establece el canal de comunicaciones
	 * @param device (BluetoothDevice) representacion del dispositivo bluetooth que es el pinpad
	 * @throws Exception
	 */
	private void connectDevice(BluetoothDevice device) throws Exception{
		Log.i(TAG, "Aperturando canal de comunicacion con Verifone");
		
		conector = new BluetoothServerConector();
//		conector = new BluetoothConector();
		conector.openConection(device);
	}
	
	/**
	 * procedimiento que cierra el canal de comunicacionescon el pinpad
	 * @throws Exception
	 */
	private void disconnectDevice() throws Exception{
		Log.i(TAG, "Cerrando canal de comunicaci�n");
		ReadWriteThread.destroyInstance();
		if(conector!=null){conector.closeConection();}
	}
	
	private void checkComChannel() throws Exception{
		Log.i(TAG, "Verificando estado de canal de comunicaci�n");
		if(conector==null){throw new Exception("clase conector no instanciada");}
		else if(!conector.isOpenComChannel()){throw new Exception("canal de comunicacion cerrado, debe abrirlo antes de ejecutar comandos");}
	}
	
	//Constructor
	public ModeloEWirelessSerie() {BLUETOOTHUSER = true;}
	
	//Metodos Sobre Escritos
	/*Apertura y Cierra de canales de comunicaci�n*/
	@Override
	public boolean openComChannel(CallbackContext callbackContext) {
		Log.i(TAG, "Abriendo Canal de comunicaci�n");
		try{
			//abrimos un dialogo para mostrar el progreso solicitando serial
//			((ProgressDialog) progressDialog).setMessage("Estableciendo conexi�n con el pinpad");
//			progressDialog.show();
			
			BeanBase bean = new BeanBase();
			if(conector==null){
				connectDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(configuracion.getPinpadDirecc()));
				bean.setEstatus(OK);
				bean.setMensaje("Canal de comunicacion aperturado");
			}
			else if(conector!=null && !conector.isOpenComChannel()){
				conector.openConection(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(configuracion.getPinpadDirecc()));
				bean.setEstatus(OK);
				bean.setMensaje("Canal de comunicacion aperturado");
			}
			else{
				bean.setEstatus(OK);
				bean.setMensaje("Canal de comunicacion ya abierto");
			}
			
//			progressDialog.dismiss();
			
			//armamos el bean de respuesta
			callbackContext.success(bean.toJson());
			return true;
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo establecer el canal de comunicacion con el pinpad", e);
//			progressDialog.dismiss();
			try {disconnectDevice();} 
			catch (Exception e1) {Log.wtf(TAG, "No se pudo cerrar el canal");}
			errorCallback("no se pudo establecer el canal de comunicacion con el pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean closeComChannel(CallbackContext callbackContext) {
		Log.i(TAG, "Cerrando Canal de comunicacion");
		try{
			//cerramos el canal de comunicacion
			disconnectDevice();
			
			//armamos el bean de respuesta
			BeanBase bean = new BeanBase();
			bean.setEstatus(OK);
			bean.setMensaje("Canal de comunicacion cerrado");
			callbackContext.success(bean.toJson());
			return true;
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo cerrar el canal de comunicacion con el pinpad", e);
			errorCallback("no se pudo cerrar el canal de comunicacion con el pinpad", e, callbackContext);
			return false;
		}
	};
	
	/*funciones basicas de PINPAD*/
	@Override
	public boolean getFecha(CallbackContext callbackContext) {
		
		errorCallback("Funcionalidad no disponible", new Exception("Funcionalidad no disponible"), callbackContext);
		
		return false;
	}

	@Override
	public boolean getBateria(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				//armamos la respuesta de la bateria
				BeanBateria bean = new BeanBateria();
				bean.setEstatus(OK);
				bean.setMensaje("Funcionalidad no disponible");
				bean.setBateria("Funcionalidad no disponible");
				
				//generamos el JSON y lo guardamos en memoria
				Utils.saveTmpData(TAGBATERIA, bean.toJson().toString(), sp);
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de solicitud de serial");
				return getModeExecute(TAGBATERIA, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el serial del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo recuperar el serial del pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean printer(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				/*//armamos la respuesta de la bateria
				BeanBateria bean = new BeanBateria();
				bean.setEstatus(OK);
				bean.setMensaje("Funcionalidad no disponible");
				bean.setBateria("Funcionalidad no disponible");
				
				//generamos el JSON y lo guardamos en memoria
				Utils.saveTmpData(TAGBATERIA, bean.toJson().toString(), sp);
				callbackContext.success();
				return true;*/
				throw new Exception("modo de trabajo no valido");
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de solicitud de serial");
				return getModeExecute(TAGIMPRESORA, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el serial del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo recuperar el serial del pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getTamper(CallbackContext callbackContext) {
		errorCallback("Funcionalidad no disponible", new Exception("Funcionalidad no disponible"), callbackContext);
		
		return false;
	}

	@Override
	public boolean getSerial(CallbackContext callbackContext) {
		
		Log.i(TAG, "Iniciando proceso de solicitud de serial");
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando solicitud de serial del dispositivo");
				
				//abrimos un dialogo para mostrar el progreso solicitando serial
				showProgressDialog("pd_pinpad_data");
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//instanciamos el procesador de comandos
				ReadWriteThread ejecutor = ReadWriteThread.getInstancia(conector, cordova.getThreadPool(),handlerSerial);
				
				//entregamos la informacion del comando a ejecutar 
				ejecutor.setComando(CommandBuilder.getComando06());
				ejecutor.setCommandInit(CommandBuilder.SI);
				ejecutor.setCommandEnd(CommandBuilder.SO);
				
				//esperamos un tiempo prudencial
//				Thread.sleep(300);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(ejecutor);
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de solicitud de serial");
				return getModeExecute(TAGSERIAL, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el serial del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo recuperar el serial del pinpad", e, callbackContext);
			return false;
		}
	}

	/*Banda magnetica y pin block*/
	@Override
	public boolean getBandaMagnetica(CallbackContext callbackContext) {
		errorCallback("Funcionalidad no disponible", new Exception("Funcionalidad no disponible"), callbackContext);
		
		return false;
	}

	@Override
	public boolean getPinblock(JSONArray args, CallbackContext callbackContext) {
		Log.i(TAG, "Inicio de solicitud de pinblock");
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando solicitud de captura de pin y generacion de pinblock");
				
				//recuperamos la data requerida
				JSONArray parametros = args.getJSONArray(0);
				String track2 = parametros.getString(0);
				String montoTransaccion = parametros.getString(1);
				String mensaje = null;
				if(parametros.length()==3){mensaje = parametros.getString(2);}
				String[] data = track2.split("=");
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//Instanciamos el ejecutor de pinblock
				PinblockExecutor executor = new PinblockExecutor(configuracion, sp, data[0], montoTransaccion, conector, cordova.getThreadPool(), TAGPINBLOCK);
				executor.setMensaje(mensaje);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(executor);
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion inicio de transaccion EMV");
				return getModeExecute(TAGPINBLOCK, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la calibracion", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo solicitar captura de PIN", e, callbackContext);
			return false;
		}
		
	}

	/*EMV*/
	@Override
	public boolean getEmvTrans(JSONArray args, CallbackContext callbackContext) {
		Log.i(TAG, "Iniciando Transaccion EMV");
		try{
			if(mode.equals(EXECMODE)){
				
				Log.i(TAG, "Ejecutando solicitud de inicio de transaccion EMV");
				
				//recuperamos la data requerida
				JSONArray parametros = args.getJSONArray(0);
				String monto = parametros.getString(0);
				String cashback = parametros.getString(1);
				String tipoTransaccion = parametros.getString(2);
				String mensaje = null;
				if(parametros.length()==4){mensaje=parametros.getString(3);}
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//Instanciamos el procesador de EMV
				EmvExecutor executor = new EmvExecutor(TAGPRIMERCERT, TAGACTIVESTATUS, conector, sp, cordova.getThreadPool(), configuracion, tipoTransaccion, monto, cashback);
				executor.setMensaje(mensaje);
				
				//esperamos un tiempo prudencial
				Thread.sleep(300);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(executor);
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion inicio de transaccion EMV");
				return getModeExecute(TAGPRIMERCERT, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo iniciar transaccion EMV", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo iniciar transaccion EMV", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getEmvSegundoCertificado(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando Segundo Certificado");
				
				//extraemos los parametros a usar
				JSONArray parametros = args.getJSONArray(0);
				
				//recuperamos los argumentos requeridos
				String tag39 = parametros.getString(0);
				String estatus = parametros.getString(1);
				String tag71 = parametros.getString(2);
				String tag72 = parametros.getString(3);
				String tag91 = parametros.getString(4);
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//procesamos la infoamcion para generar un tlv
				BerTlvChain tlv = UtilField55.createField55(tag91+tag71+tag72);
				String[] campos = UtilField55.getFinishField55(tlv, true);
				String campo55 = campos[0]+campos[1]+campos[2];
				
				//instanciamos el procesador de comandos
				ReadWriteThread ejecutor = ReadWriteThread.getInstancia(conector, cordova.getThreadPool(),handlerSegundoCertificado);
				
				//entregamos la informacion del comando a ejecutar 
				ejecutor.setComando(CommandBuilder.getComandoE03(tag39, estatus, campo55));
				ejecutor.setCommandInit(CommandBuilder.STX);
				ejecutor.setCommandEnd(CommandBuilder.ETX);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(ejecutor);
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de segundo certificado");
				return getModeExecute(TAGSEGUNDCERT, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la ejecucion del segundo certificado", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo realizar la ejecucion del segundo certificado", e, callbackContext);
			return false;
		}
		
	}

	/*cancelaciones*/
	@Override
	public boolean getAbortOperation(CallbackContext callbackContext) {
		errorCallback("Funcionalidad no disponible", new Exception("Funcionalidad no disponible"), callbackContext);
		
		return false;
	}

	@Override
	public boolean getAbortOperationEmv(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//creamos el comando de solicitud de retiro de tarjeta
				//Solicitamos el retiro de tarjeta
				BerTlvChain tlv = null;
				tlv = UtilField55.createEmptyField55(tlv);
				
				//instanciamos el procesador de comandos
				ReadWriteThread ejecutor = ReadWriteThread.getInstancia(conector, cordova.getThreadPool(),handlerCancelarEmv);
				
				//entregamos la informacion del comando a ejecutar 
				ejecutor.setComando(CommandBuilder.getComandoE03("FF", "1", tlv.getString(true)));
				ejecutor.setCommandInit(CommandBuilder.STX);
				ejecutor.setCommandEnd(CommandBuilder.ETX);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(ejecutor);
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de segundo certificado");
				return getModeExecute(TAGABORTEMV, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la ejecucion del segundo certificado", e);
			Utils.clearTmpData(sp);
			errorCallback("no se pudo realizar la ejecucion del segundo certificado", e, callbackContext);
			return false;
		}
	}

	/*Calibracion*/
	@Override
	public boolean getCalibracionDispositivo(CallbackContext callbackContext) {
		Log.i(TAG, "Iniciando proceso de calibracion pinpad - dispositivo");
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando proceso de calibracion");
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//levantamos la interfaz de seleccion de dispositivo
				this.openSelectDeviceDialog(this.handlerCalibracion);
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de proceso de calibracion");
				return getModeExecute(TAGCALIBRACION, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la calibracion", e);
			closeSelectDeviceDialog();
			Utils.clearTmpData(sp);
			errorCallback("No se pudo realizar la calibracion", e, callbackContext);
			return false;
		}
	}

	/*Carga de AID y CAPK*/
	@Override
	public boolean downloadAids(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando solicitud de carga de AIDS en el Verifone");
				
				//recuperamos la data requerida
				JSONArray aidsJson = args.getJSONArray(0);
				
				//recuperamos el listado de aids
				List<BeanAidsInfo> aids = new ArrayList<BeanAidsInfo>();
				for(int i=0; i<aidsJson.length(); i++){aids.add(new BeanAidsInfo(aidsJson.getJSONObject(i)));}
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//Instanciamos el procesador de AIDS
				AidsDownloader downloader = new AidsDownloader(sp, conector, cordova.getThreadPool(), aids, TAGDOWNLOADAIDS, TAGACTIVESTATUS);
				
				//esperamos un tiempo prudencial
				Thread.sleep(300);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(downloader);
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de carga de AIDS en el Verifone E355 - E265");
				return getModeExecute(TAGDOWNLOADAIDS, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se realizar la carga de AIDS en el pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se realizar la carga de las aplicaci�nes en el pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean downloadCapks(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando solicitud de carga de Llaves publicas EMV en el Verifone");
				
				//recuperamos la data requerida
				JSONArray capksJson = args.getJSONArray(0);
				
				//recuperamos el listado de llaves 
				List<BeanEmvKeyInfo> capks = new ArrayList<BeanEmvKeyInfo>();
				for(int i=0; i<capksJson.length(); i++){capks.add(new BeanEmvKeyInfo(capksJson.getJSONObject(i)));}
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//instanciamos el procesador de CAPKS
				CapksDownloader downloader = new CapksDownloader(sp, conector, cordova.getThreadPool(), capks, TAGDOWNLIADEMVKEYS);
				
				//ejecutamos la accion y entregamos la respuesta
				cordova.getThreadPool().execute(downloader);
				callbackContext.success();
				return true;
			}	
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de carga de AIDS en el Verifone E355 - E265");
				return getModeExecute(TAGDOWNLIADEMVKEYS, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se realizar la carga de AIDS en el pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("no se realizar la carga de llaves en el pinpad", e, callbackContext);
			return false;
		}
	}

}
