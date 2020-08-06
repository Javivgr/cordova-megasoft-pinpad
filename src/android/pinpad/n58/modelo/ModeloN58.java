/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.n58.modelo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.newpos.app.cmd.Instruction.Code;
import com.newpos.app.entity.DataResponse;
import com.newpos.app.function.MisFunction;
import com.newpos.app.function.OperateMPOS;
import com.newpos.app.function.SysFunction;
import com.newpos.mpos.iInterface.ICommunication;
import com.newpos.mpos.iInterface.IDevice;
import com.newpos.mpos.iInterface.IDevice.CommunicationMode;
import com.newpos.mpos.protocol.bluetooth.BTController;
import com.newpos.mpos.protocol.bluetooth.BtTransfer;
import com.newpos.mpos.tools.BCDUtils;
import com.newpos.mpos.tools.BaseUtils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanBateria;
import ve.com.megasoft.pinpad.bean.BeanImpresora;
import ve.com.megasoft.pinpad.bean.BeanCalibracion;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.bean.BeanFecha;
import ve.com.megasoft.pinpad.bean.BeanPinblock;
import ve.com.megasoft.pinpad.bean.BeanSerial;
import ve.com.megasoft.pinpad.bean.BeanTarjeta;
import ve.com.megasoft.pinpad.modelo.ModeloPinpadBase;
import ve.com.megasoft.pinpad.n58.data.AidsCapksData;
import ve.com.megasoft.pinpad.n58.data.TransactionData;
import ve.com.megasoft.pinpad.util.Tarjeta;
import ve.com.megasoft.pinpad.util.Utils;

/**
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * clase que entiende la comunicacion con el pinpad N58
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
@SuppressLint("HandlerLeak")
public class ModeloN58 extends ModeloPinpadBase{

	//CONSTANTES
	private static final String TAG = ModeloN58.class.getName();
	
	//Atributos
	//canal de comunicación
	private IDevice btController;
	private ICommunication btCanal;
	
	//working key
	private String workingKey = "";
	private JSONArray args;
	
	//Operadores
	private SysFunction sistema;
	private MisFunction transaccional;
	
	//Global info
	private Iterator<byte[]> capks;
	private byte[] aids;
	private boolean clearData = false;
	private String statusCode = null;
	
	//Handler's
		/*funciones basicas de PINPAD*/
	/**
	 * handler encargado de realizar el procesamiento de la solicitud de la fecha del dispositivo
	 */
	private Handler handlerGetFecha = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de fecha y hora");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					
					//armamos la respuesta de la fecha 
					BeanFecha bean = new BeanFecha();
					bean.setEstatus(OK);
					bean.setMensaje("Fecha Recuperada");
					bean.setFecha(
						BaseUtils.stringPattern(
							BaseUtils.byteArr2HexStr(response.getDataContent()), 
								Utils.DATETIME_FORMAT_WITHOUT_SEPARATOR, 
									Utils.DATETIME_FORMAT
						)
					);
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "--------------- DATOS FECHA RECIBIDOS ------------------");
					Log.d(TAG, "Bean Fecha: "+bean.toJson().toString());
					Log.d(TAG, "--------------------------------------------------------");
					Utils.saveTmpData(TAGFECHA, bean.toJson().toString(), sp);
				}
				else{
					throw new Exception("fecha no recuperada, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo recuperar la fecha del dispositivo, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGFECHA, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e1);
					Utils.saveTmpData(TAGFECHA, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
	
	};
	
	/**
	 * handler encargado de realizar el procesamiento de la solicitud de estado de bateria
	 */
	private Handler handlerBateria = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de estado de bateria");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos la informacion
					int porcentaje = response.getDataContent()[0];
					int estado = response.getDataContent()[1];
					
					//armamos la respuesta de la bateria
					BeanBateria bean = new BeanBateria();
					bean.setEstatus(OK);
					bean.setMensaje("Bateria Recuperada");
					bean.setBateria(Integer.toString(porcentaje));
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS BATERIA RECIBIDOS -----------------");
					Log.d(TAG, "Bean Bateria: "+bean.toJson().toString());
					Log.d(TAG, "--------------------------------------------------------");
					Utils.saveTmpData(TAGBATERIA, bean.toJson().toString(), sp);
				}
				else{
					throw new Exception("estado de bater&#237;a no recuperada, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo recuperar el estado de la bateria, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGBATERIA, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e1);
					Utils.saveTmpData(TAGBATERIA, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};

	//no habilitado printer
	private Handler handlerImpresora = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de estado de impresora");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos la informacion
					int porcentaje = response.getDataContent()[0];
					int estado = response.getDataContent()[1];
					
					//armamos la respuesta de la bateria
					BeanImpresora bean = new BeanImpresora();
					bean.setEstatus(OK);
					bean.setMensaje("Impresora Recuperada");
					bean.setImpresora(Integer.toString(porcentaje));
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS Impresora RECIBIDOS -----------------");
					Log.d(TAG, "Bean Impresora: "+bean.toJson().toString());
					Log.d(TAG, "--------------------------------------------------------");
					Utils.saveTmpData(TAGIMPRESORA, bean.toJson().toString(), sp);
				}
				else{
					throw new Exception("estado de impresora no recuperada, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo recuperar el estado de la impresora, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGIMPRESORA, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e1);
					Utils.saveTmpData(TAGIMPRESORA, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
	/**
	 * handler encargado de realizar el procesamiento de la solicitud de serial del dispositivo
	 */
	private Handler handlerSerial = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de serial");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos el tipo de comando ejecutado 
//					SYS sys = SYS.getInstance(msg.what); TODO - usarlo para identificar el comando ejecutado
					
					String sn = BCDArr2String(response.getDataContent());
					sistema.setMposSN(sn);
					
					Log.d(TAG, "Salida serial: "+sn);
					
					//separamos cada uno de los elementos de la respuesta
					String[] data = sn.split(",");
					
					//armamos el objeto de respuesta de serial
					BeanSerial bean = new BeanSerial();
					bean.setEstatus(OK);
					bean.setMensaje("Serial Recuperado");
					bean.setSerial(data[0]);
					bean.setKernel(data[1]);
					bean.setOs(data[2]);
					bean.setApp(data[3]);
					bean.setMarca(data[4]);
					bean.setModelo(data[5]);
					//quemamos los valores de los filtros mientras tanto
					bean.setIsPrinter("false");
					bean.setIsICCard("true");
					bean.setIsMagCard("true");
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS SERIAL RECIBIDOS -----------------");
					Log.d(TAG, "Bean Serial: "+bean.toJson().toString());
					Utils.saveTmpData(TAGSERIAL, bean.toJson().toString(), sp);
					Log.d(TAG, "-------------------------------------------------------");
				}
				else{
					throw new Exception("Serial no recuperado, "+getFailCodeMessage(response.getRspResult()));
				}
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo recuperar el serial, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGSERIAL, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGSERIAL, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
		
	};
	
		/*Banda magnetica y pin block*/
	/**
	 * handler encargado de realizar el procesamiento de la respuesta de la captura 
	 * de pin y generacion de pinblock
	 */
	private Handler handlerPinblock = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando respuesta de peticion de captura de pin y generacion de pinblock");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					Log.i(TAG, "Captura de pinblock completada");
					
					//generamos el objeto de respuesta
					BeanPinblock pinblock = new BeanPinblock();
					pinblock.setEstatus(OK);
					pinblock.setMensaje("Pinblock Generado");
					pinblock.setPinblockData(Utils.bytesToHex(response.getDataContent()));
					
					//lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS PINBLOCK -----------------");
					Log.d(TAG, "Bean Pinblock: "+pinblock.toJson().toString());
					Log.d(TAG, "-----------------------------------------------------");
					Utils.saveTmpData(TAGPINBLOCK, pinblock.toJson().toString(), sp);
					
				}
				else{
					throw new Exception("pinblock no generado, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGPINBLOCK, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGPINBLOCK, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
	};
	
		/*EMV*/
	/**
	 * handler encargado de realizar el procesamiento de la respuesta de la injeccion de la 
	 * working key para la generacion de pinblocks
	 */
	private Handler handlerInjectWk = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando respueta de peticion de injeccion de Working Key");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					Log.i(TAG, "Carga de llave exitoso reanunando transaccion");
					
					//reanudamos la transaccion 
					ejecutarEmvTrans(args);
					
				}
				else{
					//TODO - mostrar el mensaje correspondiente al codigo
					throw new Exception("transacci&#243;n no iniciada, "+getFailCodeMessage(response.getRspResult()));
				}
			}
			catch(Exception e){
				Log.e(TAG, "Working key no injectada, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGPRIMERCERT, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGPRIMERCERT, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
		
	};
	
	/**
	 * handler encargado de realizar el procesamiento el inicio de una transaccion EMV
	 */
	private Handler handlerStartEMV = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando peticion de transacción EMV");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos la data 
					byte[] content = response.getDataContent();
					
					Log.d(TAG, Utils.bytesToHex(content));
					
					//parceamos el resultado obtenido
					TransactionData data = new TransactionData();
					data.parseTLVData(content);
					BeanTarjeta tarjeta = new BeanTarjeta(true);
					BeanPinblock pinblock = null;
					tarjeta.setEstatus(OK);
					
					//recuperamos el pan ofuscado
					tarjeta.setObfuscatedPan(data.getCardId());
					
					//extraemos el nombre del tarjetahabiente
					if(data.getTrack1()!=null && !data.getTrack1().equals("")){
						String track1 = Utils.hexToString(data.getTrack1());
						if(track1.indexOf('^')>0){
							int start = track1.indexOf('^');
							int end = track1.lastIndexOf('^');
							tarjeta.setCardholderName(track1.substring(start+1, end));
						}
						else{tarjeta.setCardholderName("");}
					}
					else{tarjeta.setCardholderName("");}
					
					//analizamos el modo de lectura de la tarjeta
					if(data.getPosEntryMode().equals("02") || data.getPosEntryMode().equals("90")){tarjeta.setExtrationMode("B");}
					else if(data.getPosEntryMode().equals("05") || data.getPosEntryMode().equals("95")){tarjeta.setExtrationMode("E");}
					else if(data.getPosEntryMode().equals("79") || data.getPosEntryMode().equals("80")){tarjeta.setExtrationMode("F");}
					else{tarjeta.setExtrationMode("N//A");}
					
					//procesamos el track 2 de la tarjeta 
					tarjeta.setTrack2Data(data.getTrack2());
					tarjeta.setTrack2Ksn("");
					tarjeta.setServiceCode(Tarjeta.extraerServiceCode(tarjeta.getTrack2Data()));
					
					//si se inicio una transaccion EMV colocamos el campo 55 segun el listado de tags
					if(tarjeta.getExtrationMode().equals("E")){tarjeta.setTlv(Utils.bytesToHex(content));}
					
					//recuperamos el pinblock si este fue capturado
					if(tarjeta.getExtrationMode().equals("E") && !data.getPin().equals("") && !data.getPin().equals("0000000000000000")){
						pinblock = new BeanPinblock();
						pinblock.setPinblockKsn("");
						pinblock.setPinblockData(data.getPin());
					}
					
					//procesamos la respuesta para entregarla
					JSONObject finalJson = tarjeta.toJson();
					if(pinblock!=null){
						String key;
						JSONObject pinblockJson = pinblock.toJson();
						@SuppressWarnings("rawtypes")
						Iterator i = pinblockJson.keys();
						while(i.hasNext()){
							key = (String) i.next();
							finalJson.put(key, pinblockJson.get(key));
						}
						
					}
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS EMV RECIBIDOS -----------------");
					Log.d(TAG, "Bean Primer Certificado: "+tarjeta.toJson().toString());
					Log.d(TAG, "-----------------------------------------------------");
					Utils.saveTmpData(TAGPRIMERCERT, finalJson.toString(), sp);
				}
				else{
					throw new Exception("Transacci&#243;n EMV no iniciada, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGPRIMERCERT, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGPRIMERCERT, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
	/**
	 * handler encargado de realizar el procesamiento de segundo certificado para finalizar una transaccion emv
	 */
	private Handler handlerFinishEMV = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				Log.d(TAG, "Respuesta recibida: "+msg.obj);
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos la data 
					byte[] content = response.getDataContent();
					
					//procesamos el resultado recibido del pinpad
					BeanTarjeta bean = new BeanTarjeta(false);
					bean.setEstatus(OK);
					bean.setMensaje("Segundo Certificado exitoso");
					bean.setTlv(Utils.bytesToHex(content));
					
					//generamos y guardamos el json en memoria
					Log.d(TAG, "-------------- DATOS EMV RECIBIDOS -----------------");
					Log.d(TAG, "Bean Segundo Certificado: "+bean.toJson().toString());
					Log.d(TAG, "-----------------------------------------------------");
					Utils.saveTmpData(TAGSEGUNDCERT, bean.toJson().toString(), sp);
					
				}
				else{
					//TODO - mostrar el mensaje correspondiente al codigo
					throw new Exception("Transacci&#243;n EMV finalizada de forma incorrecta, "+getFailCodeMessage(response.getRspResult()));
				}
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo culminar la transaccion EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGSEGUNDCERT, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGSEGUNDCERT, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
	};
	
		/*Abortos*/
	private Handler handlerAbortEMV = new Handler(){
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				Log.d(TAG, "Respuesta recibida: "+msg.obj);
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				if(response.getRspResult() == Code.INS_SUCCESS){
					//recuperamos el campo 55 del segundo certificado
					BeanBase bean = new BeanBase();
					bean.setEstatus(OK);
					bean.setMensaje("Transaccion EMV cancelada");
					
					//generamos y guardamos el json en memoria
					Utils.saveTmpData(TAGABORTEMV, bean.toJson().toString(), sp);
				}
				else{throw new Exception("Transacci&#243;n EMV finalizada de forma incorrecta, "+getFailCodeMessage(response.getRspResult()));}
				
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo culminar la transaccion EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGABORTEMV, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGABORTEMV, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
		/*Cancelaciones*/
	private Handler handlerCancelOperation = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando cancelacion de la transaccion EMV");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la respuesta
				checkRequestStatus(msg);
				
				//verificamos si la ejecucion se realizo con exito
				if(response.getRspResult() == Code.INS_SUCCESS){
					//armamos el objeto de respuesta
					BeanBase bean = new BeanBase();
					bean.setEstatus(OK);
					bean.setMensaje("Operacion Cancelada EMV");
					
					//generamos el JSON y lo guardamos en memoria
					Log.d(TAG, "-------------- DATOS CANCELACION RECIBIDOS -----------------");
					Log.d(TAG, "Bean Cancelacion: "+bean.toJson().toString());
					Log.d(TAG, "------------------------------------------------------------");
					Utils.saveTmpData(TAGCANCELOP, bean.toJson().toString(), sp);
				}
				else{
					//TODO - mostrar el mensaje correspondiente al codigo
					throw new Exception("Cancelaci&#243;n de operacion no realizada, "+getFailCodeMessage(response.getRspResult()));
				}
				
			}
			catch(Exception e){
				Log.e(TAG, "Error cancelando transaccion EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGCANCELOP, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGCANCELOP, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
		};
		
	};
	
		/*Calibracion*/
	/**
	 * handler en cargado de realizar el proceso de calibracion 
	 * con el pinpad N58
	 */
	private Handler handlerCalibracion = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando dispositivo bluetooth selecionado");
			
			try{
				BeanCalibracion bean;
				//verificamos la respuesta entregada
				if(msg.what==0){
					//recuperamos el dispositivo indicado
					Log.i(TAG, "Recuperando disp. Bluetooth seleccionado");
					BluetoothDevice device = (BluetoothDevice) msg.obj;
					
					//Establecemos la coneccion
					Log.i(TAG, "Realizando conexión con disp. Bluetooth seleccionado asumiendo que es un N58");
					connectDevice(device);
					
					//indicamos el mensaje de salida 
					bean = new BeanCalibracion();
					bean.setEstatus(OK);
					bean.setMensaje("Calibracion Exitosa");
					bean.setDeviceClass(ModeloN58.class.getName());
					bean.setDeviceName(device.getName());
					bean.setDeviceAddress(device.getAddress());
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
				Log.d(TAG, "-------------- DATOS CALIBRACION RECIBIDOS -----------------");
				Log.d(TAG, "Bean Calibración: "+bean.toJson().toString());
				Log.d(TAG, "------------------------------------------------------------");
				Utils.saveTmpData(TAGCALIBRACION, bean.toJson().toString(), sp);
				
			}
			catch(Exception e){
				Log.e(TAG, "Error intentando de realziar la calibración, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus(NOK);
					json.setMensaje("Calibraci&#243;n no exitosa, error "+e.getMessage());
					Utils.saveTmpData(TAGCALIBRACION, json.toJson().toString(), sp);
					
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGCALIBRACION, IDERROR+":99:No se pudo calibrar, "+e.getMessage(), sp);
				}
			}
			finally{disconectDevice();}
		}
	
	};
	
		/*Carga de AID y CAPK*/
	/**
	 * handler encargado de procesar la respuesta a la carga de aids en el pinpad.
	 */
	private Handler handlerCargaAids = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando respuesta de carga de AIDS en el N58");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				if(response.getRspResult() == Code.INS_SUCCESS){
					if(!clearData){
						clearData = true;
						ejecutarAids();
					}
					else{
						//construimos la respuesta exitosa
						BeanBase bean = new BeanBase();
						bean.setEstatus(OK);
						bean.setMensaje("Aids Cargados de forma exitosa");
						
						//generamos el JSON y lo guardamos en memoria
						Log.d(TAG, "-------------- DATOS AIDS CARGADOS -----------------");
						Log.d(TAG, "Bean AIDS: "+bean.toJson().toString());
						Log.d(TAG, "----------------------------------------------------");
						Utils.saveTmpData(TAGDOWNLOADAIDS, bean.toJson().toString(), sp);
					}
				}
				else{
					//TODO - mostrar el mensaje correspondiente al codigo
					throw new Exception("no se pudo cargar los aids, "+getFailCodeMessage(response.getRspResult()));
				}
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGDOWNLOADAIDS, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGDOWNLOADAIDS, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
	/**
	 * handler encargado de procesar la respuesta a la carga de llaves emv en el pinpad
	 */
	private Handler handlerCargaCapks = new Handler(){
		
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			Log.i(TAG, "Procesando respuesta de carga de llaves emv en el N58");
			
			try{
				//recuperamos la respuesta del pinpad 
				DataResponse response = (DataResponse) msg.obj;
				
				//verificamos que se pudo leer la respuesta
				if(response==null){
					statusCode = "91";
					throw new Exception("error de lectura");
				}
				
				//verificamos el estado de la peticion
				checkRequestStatus(msg);
				
				//verificamos el estado de la respuesta
				checkResponseStatus(msg);
				
				//verficamos lo recivido en data response
				checkDataResponse(response);
				
				if(response.getRspResult() == Code.INS_SUCCESS){
					if(capks.hasNext()){
						Thread.sleep(1000);
						ejecutarCargaCapks();	
					}
					else{
						//construimos la respuesta exitosa
						BeanBase bean = new BeanBase();
						bean.setEstatus(OK);
						bean.setMensaje("Llaves EMV Cargadas de forma exitosa");
						
						//generamos el JSON y lo guardamos en memoria
						Log.d(TAG, "-------------- DATOS CAPKS CARGADOS -----------------");
						Log.d(TAG, "Bean LlavesEMV: "+bean.toJson().toString());
						Log.d(TAG, "-----------------------------------------------------");
						Utils.saveTmpData(TAGDOWNLIADEMVKEYS, bean.toJson().toString(), sp);
					}
				}
				else{
					Log.e(TAG, "Llave no cargada");
					throw new Exception("no se pudo cargar las llaves emv, "+getFailCodeMessage(response.getRspResult()));
				}
			}
			catch(Exception e){
				Log.e(TAG, "no se pudo iniciar la transacción EMV, error: ",e);
				try{
					BeanBase json = new BeanBase();
					json.setEstatus((statusCode!=null)?statusCode:NOK);
					json.setMensaje(e.getMessage());
					Utils.saveTmpData(TAGDOWNLIADEMVKEYS, json.toJson().toString(), sp);
				}
				catch(Exception e1){
					//en caso de que no pueda trabajar el JSON
					Log.e(TAG, "No se pudo generar el json de respuesta, error: ",e);
					Utils.saveTmpData(TAGDOWNLIADEMVKEYS, IDERROR+":99:"+e.getMessage(), sp);
				}
			}
			
		};
		
	};
	
	//Constructor
	public ModeloN58() {BLUETOOTHUSER = true;}
	
	//Metodos Privados
	/**
	 * procedimiento que apertura el canal de comunicacion con el pinpad
	 * @param device (BluetoothDevice) dispositivo bluetooth a usar 
	 * @throws Exception - en caso de no poder aperturar el canal
	 */
	private void connectDevice(BluetoothDevice device) throws Exception{
		Log.i(TAG, "Aperturando canal de comunicacion con N58");
		Log.d(TAG, "Bluetooth Device: "+device);
		
		//instanciamos un controlador con el dispositivio
		btController = BTController.getInstance(device);
		
		//abrimos el canal de comunicacion
		int response = btController.open(CommunicationMode.BT_SPP);
		
		if(response<0){throw new Exception("No es posible abrir canal de comunicación");}
		else{
			//establecemos el canal de comunicacion y continuamos con las peticiones
			Log.i(TAG, "Canal de comunicaciones establesido");
			btCanal = BtTransfer.getInstance();
		}
	}
	
	/**
	 * procedimiento que cierra el canal de comunicación
	 */
	private void disconectDevice(){
		Log.i(TAG, "Cerrando canal de comunicacion");
		
		//cerramos el canal de comunicacion
		if(btCanal!=null){
			btCanal.release();
			btCanal = null;
		}
		
		if(btController!=null){
			btController.close();
			btController = null;
		}
		
	}
	
	/**
	 * procedimiento usado para verificar el canal de comunicacion
	 * @throws Exception
	 */
	private void checkComChannel() throws Exception{
		if(btController==null){throw new Exception("Canal de comunicacion cerrado, debe abrirlo antes de ejecutar comandos");}
		if(btCanal==null){throw new Exception("Canal de comunicacion cerrado, debe abrirlo antes de ejecutar comandos");}
	}
	
	/**
	 * procedimiento que chequea la respuesta recibida del pinpad
	 * @param msg (Message) respuesta entregada por el pinpad
	 * @throws Exception - las posibles excepciones probocadas por el pinpad.
	 */
	private void checkRequestStatus(Message msg) throws Exception{
		Log.i(TAG, "Verificando resultado de ejecucion de request de comando");
		
		//fallo enviando el comando
		if(msg.arg1==ICommunication.SEND_FAILURE){
			this.statusCode = "92";
			throw new Exception("No se envio el comando");
		}
		
		//fallo por alcanzar timeout
		if(msg.arg1==ICommunication.OPER_TIMEOUT){
			this.statusCode = "93";
			throw new Exception("Tiempo maximo de espera alcanzado");
		}
	}
	
	/**
	 * procedimiento que chequea la respuesta del request enviado
	 * @param msg (Message) respuesta entregada por el pinpad
	 * @throws Exception - las posibles excepciones probocadas por el pinpad.
	 */
	private void checkResponseStatus(Message msg) throws Exception{
		Log.i(TAG, "Verificando resultado de ejecucion de request de comando");
		
		if(msg.arg2 == ICommunication.OPER_TIMEOUT){
			this.statusCode = "94";
			throw new Exception("Tiempo maximo de espera por respuesta alcanzado");
		}
	}
	
	/**
	 * procedimiento que verifica la situacion recibida en la respuesta
	 * @param response (DataResponse) datos de respuesta de la peticion
	 * @throws Exception las posibles excepciones a la validacion 
	 */
	private void checkDataResponse(DataResponse response) throws Exception{
		Log.i(TAG, "Verificando resultado entregados por el comando");
		
		switch(response.getExceptionCode()){
			case OperateMPOS.INTERNET_EXCP:{
				this.statusCode = "95";
				sistema.cancelAction();
				throw new Exception("error de conexi&#243;n a internet");
			}
			case OperateMPOS.UNKNOW_HOST_EXCP:{
				this.statusCode = "96";
				sistema.cancelAction();
				throw new Exception("error de conexi&#243;n, host desconocido");
			}
			case OperateMPOS.CONNECT_EXCP:{
				this.statusCode = "97";
				sistema.cancelAction();
				throw new Exception("falla de conexi&#243;n al servidor");
			}
		}
	}
	
	/**
	 * funcion que dato un codigo retorna el mensaje asociado al codigo
	 * @param code (int) codigo a verificar
	 * @return (String) mensaje asociado al codigo
	 */
	private String getFailCodeMessage(int code){
		Log.e(TAG,"Se produjo un error en el pinpad, retornando el mensaje asociado");
		
		switch(code){
			case 5:{
				this.statusCode = "05";
				return "acceso denegado";
			}
			case 6:{
				this.statusCode = "06";
				return "llave de sesi&#243;n no valida";
			}
			case 7:{
				this.statusCode = "07";
				return "versi&#243;n no soportada";
			}
			case 9:{
				this.statusCode = "09";
				return "fallo de comunicaci&#243;n";
			}
			case 10:{
				this.statusCode = "10";
				return "terminal no registrado";
			}
			case 11:{
				this.statusCode = "11";
				return "terminal no inicializado";
			}
			case 12:{
				this.statusCode = "12";
				return "terminal no inicializado";
			}
			case 15:{
				this.statusCode = "15";
				return "transacci&#243;n cancelada";	
			}
			default:{
				this.statusCode = "98";
				return "error interno del pinpad";
			}
		
		}
		
	}
	
	/**
	 * funcion que arma la informacion del dispositivo y lugar de 
	 * origen de la transaccion.
	 * @return (byte[]) 
	 */
 	private byte[] getInfo(){
		//recuperamos el imei en bytes
		byte[] imei = btd.getImei().getBytes();
		
		//preparamos la informacion de la locacion
		byte[] mcc = BaseUtils.int2ByteArr(btd.getMcc());
		byte[] mnc = BaseUtils.int2ByteArr(btd.getMnc());
		byte[] cid = BaseUtils.int2ByteArr(btd.getCid());
		byte[] loc = BaseUtils.int2ByteArr(btd.getLoc());
		
		//armamos la informacion de la estacion base (MCC, MNC, CID, LOC)
		byte[] locationInfo = new byte[2+1+4+4];
		System.arraycopy(mcc, 2, locationInfo, 0, 2);
		System.arraycopy(mnc, 3, locationInfo, 2, 1);
		System.arraycopy(cid, 0, locationInfo, 2+1, 4);
		System.arraycopy(loc, 0, locationInfo, 2+1+4, 4);
		
		//aramamos la informacion final
		byte[] info = new byte[imei.length+locationInfo.length];
		System.arraycopy(imei, 0, info, 0, imei.length);
		System.arraycopy(locationInfo, 0, info, imei.length, locationInfo.length);
		
		//entregamos lo solicitado
		return info;
		
	}
	
	/**
	 * funcion que realiza la conversion de los datos recibidos del pinpad 
	 * a texto claro usando el charset definido por el pinpad
	 * @param dataResp (byte[]) cadena de bytes a restaurar
	 * @return (String) el dato en claro.
	 * @throws UnsupportedEncodingException 
	 */
	private String BCDArr2String(byte[] dataResp) throws UnsupportedEncodingException {
        String str = null;
        try {str = new String(dataResp, OperateMPOS.CHARSET);} 
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "No se pudo recuperar String de la cadena de bytes");
            throw e;
        }
        return str;
    }
	
	/**
	 * procedimiento que realiza la injeccion de la working key 
	 * en el pinpad para posteriormenta realizar ejecucion de la lectura 
	 * de la tarjeta
	 * @param args (JSONArray) - parametros requeridos para la lectura de la tarjeta
	 */
	private void injectarWk(JSONArray args){
		Log.i(TAG, "Iniciando proceso de injeccion de la working key");
		
		//indicamos el indice de workingkey y recuperamos la working key
		Log.i(TAG, "preparando working key");
		byte[] index = new byte[]{0x01};
		byte[] wk = Utils.hexToBytes(configuracion.getPinpadWorkingKey());
		
		//armamos la data como el comando requiere
		Log.i(TAG, "Preparando data final");
//		byte[] reqData = new byte[17];
		byte[] reqData = new byte[index.length+wk.length];
		System.arraycopy(index, 0, reqData, 0, 1);
		System.arraycopy(wk, 0, reqData, 1, wk.length);
		
		//instanciamos y ejecutamos el comando
		Log.i(TAG, "Injectando working key");
		sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
		transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerInjectWk);
		
		Log.d(TAG, "---------- PARAMETROS A ENVIAR AL COMANDO WORKING KEY ----------");
		
		Log.d(TAG, "reqData (HEX): "+Utils.bytesToHex(reqData));
		
		Log.d(TAG, "----------------------------------------------------------------");
		
		transaccional.injecWKkey(reqData);
	}
	
	/**
	 * procedimiento que realiza la ejecucion de la lectura de la tarjeta
	 * @param args (JSONArray) - parametros requeridos para la lectura de la tarjeta
	 * @throws JSONException
	 */
	private void ejecutarEmvTrans(JSONArray args) throws JSONException{
		//recuperamos la info
		byte[] info = getInfo();
		
		//procesamos los datos recibidos
		JSONArray parametros = args.getJSONArray(0);
		String monto = parametros.getString(0);
		String cashback = parametros.getString(1);
		String tipoTransaccion = parametros.getString(2);
		
		//debug
		Log.d(TAG, "---------- DATOS PARA INICIAR TRANSACCION EMV ----------");
		
		Log.d(TAG, "Monto : "+monto);
		Log.d(TAG, "Avance : "+cashback);
		Log.d(TAG, "Tipo Transaccion: "+tipoTransaccion);
		
		Log.d(TAG, "--------------------------------------------------------");
		
		//ajustamos el monto en caso de identificar que es una anulacion
		if(monto.equals("000")){monto="100";}
		
		byte[] data = new byte[7];
		byte[] montoBCD = BCDUtils.str2Bcd(monto);
		if(montoBCD.length<=6){System.arraycopy(montoBCD, 0, data, 6-montoBCD.length, montoBCD.length);}
		data[6] = (byte) 0xFC;
		
		//colocamos la informacion en el request de la transaccion
		byte[] reqData = new byte[info.length+data.length];
		System.arraycopy(info, 0, reqData, 0, info.length);
		System.arraycopy(data, 0, reqData, info.length, data.length);
		
		//instanciamos el ejecutor requerido para la transaccion EMV
		sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
		transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerStartEMV);
		
		//Debug
		Log.d(TAG, "---------- DATOS HEX ENVIADOS AL COMANDO PRIMER CERTIFICADO ----------");
		
		Log.d(TAG, "Datos : "+Utils.bytesToHex(reqData));
		
		Log.d(TAG, "----------------------------------------------------------------------");
		
		//ejecutamos la accion y entregamos la respuesta
		transaccional.consume(reqData);
	}
	
	/**
	 * procedimiento que realiza la limpieza de aids y capks para
	 * la posterior carga de los aids entregados
	 * @throws Exception
	 */
	private void ejecutarAids() throws Exception{
		Log.i(TAG, "Realizando carga de AIDS");
		
		if(!clearData){transaccional.clearData((byte)0x03);}
		else{
			try{
				//debug
				Log.d(TAG, "---------- PARAMETROS A ENVIAR AL COMANDO downloadICCardAID ----------");
				
				Log.d(TAG, "reqData (HEX): "+Utils.bytesToHex(aids));
				
				Log.d(TAG, "----------------------------------------------------------------------");
				
				transaccional.downloadICCardAID(aids);
			}
			catch(Exception e){throw new Exception("Aids no cargados, error: ",e);}
		}
	}
	
	/**
	 * procedimiento que realiza la carga de la siguiente llave en la lista
	 * @throws Exception
	 */
	private void ejecutarCargaCapks()throws Exception{
		Log.i(TAG, "Realizando carga de llave");
		if(capks.hasNext()){
			byte[] llave;
			try{
				llave = (byte[]) capks.next();
				
				//debug
				Log.d(TAG, "---------- PARAMETROS A ENVIAR AL COMANDO downloadICCardPUK ----------");
				
				Log.d(TAG, "reqData (HEX): "+Utils.bytesToHex(llave));
				
				Log.d(TAG, "----------------------------------------------------------------------");
				
				transaccional.downloadICCardPUK(llave);
			}
			catch(Exception e){ throw new Exception("Llave no cargada, error: ",e); }
		}
		else{ throw new Exception("No hay mas llaves que cargar"); }
	}
	
	//Metodos Sobre Escritos
	/*Apertura y Cierra de canales de comunicación*/
	@Override
	public boolean openComChannel(CallbackContext callbackContext) {
		try{
			BeanBase bean = new BeanBase();
			if(btController==null && btCanal==null){
				//ubicamos el dispositivo y abrimos el canal de comunicacion
				connectDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(configuracion.getPinpadDirecc()));
				
				//indicamos que tenemos el puerto aperturado
				bean.setEstatus(OK);
				bean.setMensaje("Canal de comunicacion aperturado");
			}
			else{
				bean.setEstatus(OK);
				bean.setMensaje("Canal de comunicacion ya abierto");
			}
			
			//armamos el bean de respuesta
			callbackContext.success(bean.toJson());
			return true;
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo establecer el canal de comunicacion con el pinpad", e);
			disconectDevice();
			errorCallback("No se pudo establecer el canal de comunicacion con el pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean closeComChannel(CallbackContext callbackContext) {
		try{
			//cerramos el canal de comunicacion del pinpad 
			disconectDevice();
			
			BeanBase bean = new BeanBase();
			bean.setEstatus(OK);
			bean.setMensaje("Canal de comunicacion cerrado");
			
			//armamos el bean de respuesta
			callbackContext.success(bean.toJson());
			return true;
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo cerrar el canal de comunicacion con el pinpad", e);
			errorCallback("No se pudo cerrar el canal de comunicacion con el pinpad", e, callbackContext);
			return false;
		}
	}
	
	/*funciones basicas de PINPAD*/
	@Override
	public boolean getFecha(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "ejecutando consulta de fecha y hora del pinpad");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//instanciamos el ejecutor requerido para el serial 
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerGetFecha);
				
				//ejecutamos la accion y entregamos la respuesta
				sistema.toGetPOSTime();
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de proceso de fecha");
				return getModeExecute(TAGFECHA, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar la fecha y hora del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo recuperar la fecha y hora del pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getBateria(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "ejecutando consulta de estado de bateria del pinpad");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//instanciamos el ejecutor requerido para el serial 
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerBateria);
				
				//ejecutamos la accion y entregamos la respuesta
				sistema.toGetBattery();
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de proceso de bateria");
				return getModeExecute(TAGBATERIA, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el estado de la bateria del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo recuperar el estado de la bateria del pinpad", e, callbackContext);
			return false;
		}
	}

	//no habilitado
	@Override
	public boolean printer(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				/*Log.i(TAG, "ejecutando consulta de estado de impresora del pinpad");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//instanciamos el ejecutor requerido para el serial 
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerBateria);
				
				//ejecutamos la accion y entregamos la respuesta
				sistema.toGetBattery();
				callbackContext.success();
				return true;*/
				throw new Exception("modo de trabajo no valido");
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de proceso de bateria");
				return getModeExecute(TAGBATERIA, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
			
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el estado de la bateria del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo recuperar el estado de la bateria del pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getTamper(CallbackContext callbackContext) {
		try{
			BeanBase bean = new BeanBase();
			bean.setEstatus(NOK);
			bean.setMensaje("no implementado, no disponible");
			callbackContext.error(bean.toJson());
			return false;
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar el estado fisico del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo recuperar el estado fisico del pinpad", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getSerial(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando consulta del serial del pinpad");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//instanciamos el ejecutor requerido para el serial 
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerSerial);
				
				//abrimos un dialogo para mostrar el progreso solicitando serial
				showProgressDialog("pd_pinpad_data");
				
				//ejecutamos la accion y entregamos la respuesta
				sistema.toGetSN();
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de proceso de calibracion");
				return getModeExecute(TAGSERIAL, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar los datos del pinpad", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo recuperar los datos del pinpad", e, callbackContext);
			return false;
		}
	}

	/*Banda magnetica y pin block*/
	@Override
	public boolean getBandaMagnetica(CallbackContext callbackContext) {
		try{
			BeanBase bean = new BeanBase();
			bean.setEstatus(NOK);
			bean.setMensaje("no implementado");
			callbackContext.error(bean.toJson());
			return false;
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo Activar el lector de banda magenetica", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo Activar el lector de banda magenetica", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getPinblock(JSONArray args, CallbackContext callbackContext) {
		try{
			
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando solicitud de captura de pin y generacion de pinblock");
				
				//recuperamos la data requerida
				JSONArray parametros = args.getJSONArray(0);
				String track2 = parametros.getString(0);
				String montoTransaccion = parametros.getString(1);
				
				//debug
				Log.d(TAG, "---------- DATOS PARA SOLICITUD PINBLOCK ----------");
				
				Log.d(TAG, "track2 : "+track2);
				Log.d(TAG, "montoTransaccion : "+montoTransaccion);
				
				Log.d(TAG, "---------------------------------------------------");
				
				//preparamos la informacion solicitar pinblock
				byte[] index = new byte[]{0x01};
				byte[] pan = Tarjeta.extraerDatosTrack2(track2)[0].getBytes();
				byte[] info = new byte[1+pan.length];
				System.arraycopy(index, 0, info, 0, 1);
				System.arraycopy(pan, 0, info, 1, pan.length);
				
				//instanciamos el ejecutor requerido para la captura de pinblock
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
				transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerPinblock);
				
				//ejecutamos la solicitud de pinblock
				transaccional.getPinblock(info);
				
				//entregamos la respuesta
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de comando Pinblock");
				return getModeExecute(TAGPINBLOCK, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo Activar el lector de banda magenetica", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo Activar el lector de banda magenetica", e, callbackContext);
			return false;
		}
	}

	/*EMV*/
	@Override
	public boolean getEmvTrans(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "ejecutando comando de inicio transaccion EMV");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				this.args=args;
				
				//verificamos si debemos injectar la working key en el pinpad 
				if(workingKey.equals("")||!workingKey.equals(configuracion.getPinpadWorkingKey())){
					//injectamos la working key y solicitamos la ejecucion de la lectura
					workingKey = configuracion.getPinpadWorkingKey();
					injectarWk(args);
				}
				else{
					//ejecutamos la lectura de la tarjeta
					ejecutarEmvTrans(args);
				}
				
				//entregamos la respuesta
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de comando EMV");
				return getModeExecute(TAGPRIMERCERT, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo iniciar transaccion emv", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo iniciar transaccion emv", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getEmvSegundoCertificado(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "ejecutando comando de segundo certificado");
				
				//Verificamos que el canal de comunicacion esta abierto
				checkComChannel();
				
				//limpiamos los tmp
				Utils.clearTmpData(sp);
				
				//extraemos los parametros a usar
				JSONArray parametros = args.getJSONArray(0);
				
				//recuperamos los argumentos requeridos
				String tag39 = parametros.getString(0);
				String estatus = parametros.getString(1);
				String tag71 = parametros.getString(2);
				String tag72 = parametros.getString(3);
				String tag91 = parametros.getString(4);
				
				//debug
				Log.d(TAG, "---------- DATOS PARA INICIAR SEGUNDO CERTIFICADO ----------");
				
				Log.d(TAG, "tag39 : "+tag39);
				Log.d(TAG, "estatus : "+estatus);
				Log.d(TAG, "tag71: "+tag71);
				Log.d(TAG, "tag72 : "+tag72);
				Log.d(TAG, "tag91: "+tag91);
				
				Log.d(TAG, "------------------------------------------------------------");
				
				//preparamos la informacion del segundo certificado
				byte[] byteTag39 = Utils.hexToBytes(tag39);
				byte[] byteTag91 = Utils.hexToBytes(tag91);
				byte[] byteTag72 = Utils.hexToBytes(tag72);
				byte[] byteTag71 = Utils.hexToBytes(tag71);
				
				byte[] reqData = new byte[byteTag39.length + byteTag91.length + byteTag72.length + byteTag71.length];
				
				System.arraycopy(byteTag39, 0, reqData, 0, byteTag39.length);
				System.arraycopy(byteTag91, 0, reqData, byteTag39.length, byteTag91.length);
				System.arraycopy(byteTag72, 0, reqData, byteTag39.length+byteTag91.length, byteTag72.length);
				System.arraycopy(byteTag71, 0, reqData, byteTag39.length + byteTag91.length + byteTag72.length, byteTag71.length);
				
				//instanciamos el ejecutor requerido para la transaccion EMV
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
				transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerFinishEMV);
				
				//debug
				Log.d(TAG, "---------- PARAMETROS A ENVIAR AL COMANDO 2DO CERTIFICADO ----------");
				
				Log.d(TAG, "reqData (HEX): "+Utils.bytesToHex(reqData));
				
				Log.d(TAG, "--------------------------------------------------------------------");
				
				//ejecutamos el comando de segundo certificado
				transaccional.onlineData(reqData);
				
				callbackContext.success();
				return true;
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de comando EMV");
				return getModeExecute(TAGSEGUNDCERT, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo finalizar la transacción EMV, segundo certificado no ejecutado", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo finalizar la transacción EMV, segundo certificado no ejecutado", e, callbackContext);
			return false;
		}
	}
	
	/*cancelaciones*/
	@Override
	public boolean getAbortOperation(CallbackContext callbackContext) {
		try{
			BeanBase bean = new BeanBase();
			bean.setEstatus(NOK);
			bean.setMensaje("no implementado");
			callbackContext.error(bean.toJson());
			return false;
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo cancelar la operacion", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo cancelar la operacion", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean getAbortOperationEmv(CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				Log.i(TAG, "Ejecutando cancelacion de transaccion EMV");
				
				//verificamos que el canal de comunicaciones este abierto
				checkComChannel();
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//preparamos la informacion para cancelar la transaccion
				byte[] byteTag39 = Utils.hexToBytes("FF");
				byte[] byteTag91 = Utils.hexToBytes("910100");
				byte[] byteTag72 = Utils.hexToBytes("720100");
				byte[] byteTag71 = Utils.hexToBytes("710100");
				
				byte[] reqData = new byte[byteTag39.length + byteTag91.length + byteTag72.length + byteTag71.length];
				
				System.arraycopy(byteTag39, 0, reqData, 0, byteTag39.length);
				System.arraycopy(byteTag91, 0, reqData, byteTag39.length, byteTag91.length);
				System.arraycopy(byteTag72, 0, reqData, byteTag39.length+byteTag91.length, byteTag72.length);
				System.arraycopy(byteTag71, 0, reqData, byteTag39.length + byteTag91.length + byteTag72.length, byteTag71.length);
				
				//instanciamos el ejecutor requerido para la transaccion EMV
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
				transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerAbortEMV);
				
				//debug
				Log.d(TAG, "---------- PARAMETROS A ENVIAR AL COMANDO CANCELAR EMV ----------");
				
				Log.d(TAG, "reqData (HEX): "+Utils.bytesToHex(reqData));
				
				Log.d(TAG, "-----------------------------------------------------------------");
				
				//ejecutamos el comando de segundo certificado
				transaccional.onlineData(reqData);
				
				callbackContext.success();
				return true;
				
			}
			else if (mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de segundo certificado");
				return getModeExecute(TAGABORTEMV, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo cancelar la operacion emv", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo cancelar la operacion emv", e, callbackContext);
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
				
				Log.i(TAG, "Ejecutando solicitud de carga de AIDS en el N58");
				
				//recuperamos la data requerida
				JSONArray aidsJson = args.getJSONArray(0);
				
				//recuperamos el listado de aids
				List<BeanAidsInfo> listAids = new ArrayList<BeanAidsInfo>();
				for(int i=0; i<aidsJson.length(); i++){listAids.add(new BeanAidsInfo(aidsJson.getJSONObject(i)));}
				
				//debug
				Log.d(TAG, "---------------- DATOS AIDS RECIBIDOS ------------------");
				int count = 0;
				for(BeanAidsInfo info : listAids){
					Log.d(TAG, "AID "+count+" "+info);
					count++;
				}
				Log.d(TAG, "--------------------------------------------------------");
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//instanciamos el ejecutor requerido para la carga de AIDS
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
				transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerCargaAids);
				
				//ejecutamos la accion y entregamos la respuesta
				aids = AidsCapksData.buildAidsInfo(listAids);
				
				//iniciamos el proceso de carga
				this.ejecutarAids();
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de carga de AIDS en el N58");
				return getModeExecute(TAGDOWNLOADAIDS, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la carga de AIDS", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo realizar la carga de AIDS", e, callbackContext);
			return false;
		}
	}

	@Override
	public boolean downloadCapks(JSONArray args, CallbackContext callbackContext) {
		try{
			if(mode.equals(EXECMODE)){
				
				Log.i(TAG, "Ejecutando solicitud de carga de Llaves EMV en el N58");
				
				//recuperamos la data requerida
				JSONArray aidsJson = args.getJSONArray(0);
				
				//recuperamos el listado de aids
				List<BeanEmvKeyInfo> capks = new ArrayList<BeanEmvKeyInfo>();
				for(int i=0; i<aidsJson.length(); i++){capks.add(new BeanEmvKeyInfo(aidsJson.getJSONObject(i)));}
				
				//debug
				Log.d(TAG, "---------------- DATOS CAPKS RECIBIDOS ------------------");
				int count = 0;
				for(BeanEmvKeyInfo info : capks){
					Log.d(TAG, "CAPKS "+count+" "+info);
					count++;
				}
				Log.d(TAG, "--------------------------------------------------------");
				
				//limpiamos los TMP
				Utils.clearTmpData(sp);
				
				//verificar si tenemos una coneccion establecida por en el pinpad
				checkComChannel();
				
				//instanciamos el ejecutor requerido para la carga de llaves emv
				sistema = new SysFunction(cordova.getActivity(), btCanal, handlerCancelOperation);
				transaccional = new MisFunction(cordova.getActivity(), btCanal, handlerCargaCapks);
				
				//ejecutamos la accion y entregamos la respuesta
				this.capks = AidsCapksData.buildCapksInfo(capks).iterator();
				
				//iniciamos el proceso de carga TODO captura el posible error 
				this.ejecutarCargaCapks();
				callbackContext.success();
				return true;
				
			}
			else if(mode.equals(GETMODE)){
				Log.i(TAG, "Consultando estado de ejecucion de carga de llaves emv en el N58");
				return getModeExecute(TAGDOWNLIADEMVKEYS, callbackContext);
			}
			else{throw new Exception("modo de trabajo no valido");}
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo realizar la carga de las llaves emv", e);
			Utils.clearTmpData(sp);
			errorCallback("No se pudo realizar la carga de las llaves emv", e, callbackContext);
			return false;
		}
	}
}
