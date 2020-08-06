/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.bean.BeanTelephoneData;
import ve.com.megasoft.pinpad.connection.bluetooth.bean.BeanBluetoothDevice;
import ve.com.megasoft.pinpad.connection.bluetooth.ui.UIDeviceDialog;
import ve.com.megasoft.pinpad.connection.configuracion.Configuracion;
import ve.com.megasoft.pinpad.util.Utils;
import ve.com.megasoft.pinpad.util.WinUtils;

import ve.com.megasoft.pinpad.n58.modelo.ModeloN58;
import ve.com.megasoft.pinpad.newland.modelo.ModeloNewland;
import ve.com.megasoft.pinpad.verifone.modelo.ModeloEWirelessSerie;

/**
 * 
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * clase maestra para todos los modelos del pinpad 
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public abstract class ModeloPinpadBase extends ModeloBase {

	//CONSTANTES
	private static final String TAG = ModeloPinpadBase.class.getName();
	protected static final String EXECMODE = "exec";
	protected static final String GETMODE = "get";
	protected static boolean BLUETOOTHUSER = true;
	
	//datos
	protected static final String NOTFOUND = "404";
	protected static final String CALIBRACIONSTILLOK="02";
	
	/*TAGS*/
		/*funciones basicas de PINPAD*/
	protected static final String TAGFECHA = "sp_cordova_pinpad_tag_fecha_pinpad";
	protected static final String TAGBATERIA = "sp_cordova_pinpad_tag_bateria_pinpad";
	protected static final String TAGIMPRESORA = "sp_cordova_pinpad_tag_impresora_pinpad";
	protected static final String TAGSERIAL = "sp_cordova_pinpad_tag_serial_eletronico";
		/*Banda magnetica y pin block*/	
	protected static final String TAGPINBLOCK = "sp_cordova_pinpad_tag_pinblock";
		/*EMV*/
	protected static final String TAGPRIMERCERT = "sp_cordova_pinpad_tag_primer_certificado";
	protected static final String TAGSEGUNDCERT = "sp_cordova_pinpad_tag_segundo_certificado";
		/*Anulacion*/
	protected static final String TAGANULACION = "sp_cordova_pinpad_tag_anulacion";
		/*Cancelacion*/
	protected static final String TAGABORTEMV = "sp_cordova_pinpad_tag_cancel_emv";
	protected static final String TAGCANCELOP = "sp_cordova_pinpad_tag_cancel_op";
		/*Calibracion*/
	protected static final String TAGCALIBRACION = "sp_cordova_pinpad_tag_calibracion";
		/*Carga AIDS y llaves EMV*/
	protected static final String TAGDOWNLOADAIDS = "sp_cordova_pinpad_tag_download_aids";
	protected static final String TAGDOWNLIADEMVKEYS = "sp_cordova_pinpad_tag_download_emv_keys";
		/*Indicador*/
	protected static final String IDERROR = "!ERROR!";
		/*Active Statuss*/
	protected static final String TAGACTIVESTATUS = "sp_cordova_pinpad_tag_active_status";
	
	//Atributos
	//Singleton 
	private static ModeloPinpadBase instancia;
	private static Date fechaInstancia;
	
	//Configuraciones
	protected Configuracion configuracion;
	protected String mode;
	
	//recursos
	protected String packageName;
	protected Resources resources;
	
	//UI
	protected Dialog connectDialog;
	protected Dialog progressDialog;
	protected UIDeviceDialog deviceDialog;
	
	//Data
	protected SharedPreferences sp;
	protected TelephonyManager tm;
	protected BeanTelephoneData btd;
	
	//Metodos Privados
	/**
 	 * funcion que lista los dispositivos bluetooth registrados 
 	 * en el dispositivo
 	 * @return (List[BeanBluetoothDevice]) dispositivos bluetooth recuperados
 	 */
 	private List<BeanBluetoothDevice> getBluetoothDevices (){
 		Log.i(TAG, "Recuperando lista de dispositivos bluetooth");
 		
 		//inicializamos la lista entregar
 		List<BeanBluetoothDevice> list = new ArrayList<BeanBluetoothDevice>();
 		list.clear();
 		
 		//recuperamos los dispositivos emparejados
 		if(BluetoothAdapter.getDefaultAdapter().getBondedDevices().size()>0){
 			for(BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()){
 				BeanBluetoothDevice bbd = new BeanBluetoothDevice();
 				bbd.setEstatus(OK);
 				bbd.setMensaje("");
 				bbd.setDeviceName(device.getName());
 				bbd.setDeviceAddress(device.getAddress());
 				list.add(bbd);
 			}
 		}
 		
 		Log.d(TAG, "Dispositivos Bluetooth: "+list);
 		return list;
 	}
	
	//Metodos protegidos
 	/**
 	 * procedimiento que levanta interfaz de seleccion de dispositivo.
 	 * @param responseHandler (Handler) quien manipulara la respuesta del dialogo
 	 */
 	protected void openSelectDeviceDialog(Handler responseHandler){
 		Log.i(TAG, "Levantando dialogo de seleccion de bluetooth");
 		
 		//instanciamos el dialogo de seleccion de dispositico
		deviceDialog = new UIDeviceDialog(
			cordova.getActivity(),  
			resources.getIdentifier("MyDialogStyle", "style", packageName),
			this.getBluetoothDevices(),
			responseHandler
		);
		
		//mostramos el cuadro de dialogo
		deviceDialog.show();
 		
 		//actualizamos listado de dispositivos
		Log.i(TAG, "Actualizando listado de dispositivos Bluetooth");
 		Message msg = new Message();
 		msg.obj = this.getBluetoothDevices();
 		deviceDialog.getDeviceChangeHandler().sendMessage(msg);
 		
 		//iniciamos el thread de timeout con la configuracion establecida TODO
 		//Destruir pantalla de dialogo por timeout -- TODO
 	}
 	
 	/**
 	 *  proceso invocado para cerra ventana de dialogo en caso de una excepcion.
 	 */
 	protected void closeSelectDeviceDialog(){
 		if(deviceDialog!=null && deviceDialog.isShowing()){
 			deviceDialog.dismiss();
 			deviceDialog.setStopThread(true);
 			deviceDialog = null;
 		}
 		
 		if(deviceDialog!=null){deviceDialog = null;}
 	}
 	
 	/**
 	 * muestra un dialogo de progreso 
 	 * @param xmlStringTag (String) nombre del recurso a mostrar
 	 * @param data (String[]) informacion a colocar en los comodines
 	 */
 	protected void showProgressDialog(String xmlStringTag, String[] data){
 		//generamos nuevo dialogo en caso de no tenerlo
 		if(progressDialog==null){progressDialog=WinUtils.getMyDialog("TEST", cordova.getActivity());}
 		
 		//recuperamos el mensaje a mostrar
 		String msg = resources.getString(resources.getIdentifier(xmlStringTag, "string", packageName));
 		if(data!=null && data.length>0){
 			//sustituimos la informacion en los comodines --TODO
 		}
 		
 		//seteamos el mensaje
 		((ProgressDialog)progressDialog).setMessage(msg);
 		
 		//mostramos el dialogo
 		progressDialog.show();
 	}
 	
 	/**
 	 * muestra un dialogo de progreso 
 	 * @param xmlStringTag (String) nombre del recurso a mostrar
 	 */
 	protected void showProgressDialog(String xmlStringTag){showProgressDialog(xmlStringTag, null);}
 	
 	/**
 	 * oculta dialogo de progreso
 	 */
 	protected void dismissProgressDialog(){
 		if(progressDialog!=null){
 			if(progressDialog.isShowing()){
 				progressDialog.dismiss();
 			}
 		}
 	}
 	
 	/**
	 * funcion que realiza la consulta del resultado de la ejecucion del comando
	 * @param tag (String) identificador de la data segun el comando ejecutado
	 * @param callbackContext (CallbackContext) callback donde se enviara la respuesta
	 * @return (boolean) true para todo ok, false en caso contrario.
	 * @throws JSONException
	 */
	protected boolean getModeExecute(String tag, CallbackContext callbackContext) throws JSONException{
		//recuperamos el resultado de la ejecucion de comando solicitado
		String status = Utils.getTmpData(TAGACTIVESTATUS, sp);
		String response = Utils.getTmpData(tag, sp);
		
		//verificamos la informacion recuperada
		if(response==null || response.equals("")){
			//camino en caso de no recibir un valor
			Log.i(TAG, "No a culminado el comando");
			BeanBase notFound = new BeanBase();
			notFound.setEstatus(NOTFOUND);
			if(status!=null){
				Utils.removeTmpData(TAGACTIVESTATUS, sp);
				notFound.setMensaje(status);	
			}
			callbackContext.error(notFound.toJson());
			return false;
		}
		else{
			//en caso de recibir respuesta del comando
			if(response.contains(IDERROR)){
				Log.e(TAG, "Se produjo un error al recuperar la respuesta");
				
				//creamos el json de error 
				String[] data = response.split(":");
				BeanBase bean = new BeanBase();
				bean.setEstatus(data[1]);
				bean.setMensaje(data[2]);
				
				//verificamos si hay dialogo abierto
				dismissProgressDialog();
				
				//entregamos la respuesta
				callbackContext.error(bean.toJson());
				return false;
			}
			//en caso de una respuesta afirmativa
			else{
				Log.e(TAG, "Respuesta recuperada, realizando post procesamiento");
				
				//limpiamos la data de la memoria
				Utils.clearTmpData(sp);
				
				//verificamos si hay dialogo abierto
				dismissProgressDialog();
				
				//entregamos la respuesta
				callbackContext.success(new JSONObject(response));
				return true;
			}
		}
	}
 	
 	//Metodos Publicos
 	/**
 	 * funcion que inicializa los datos de la clase de pinpad a usar
 	 * @param mode (String) modo de trabajo, exec - ejecucion, get - obtencion
 	 * @param configuracion (BeanConfiguracion) configuracion de pinpad a usar
 	 * @param cordova (CordovaInterface) Interfaz cordova utilizada para la comunicacion
 	 * @throws Exception - validacion de la exitencia y activacion de radio bluetooth en caso que aplique
 	 */
	public void init(Configuracion configuracion, CordovaInterface cordova) throws Exception{
		
		//colocamos configuracion, e interfaz cordova indicados
		this.configuracion = configuracion;
		this.cordova = cordova;
		fechaInstancia = new Date();
		
		//inicializamos los datos compartidos
		sp = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
		
		//recuperamos los recursos de la aplicacion
		packageName = this.cordova.getActivity().getPackageName();
		resources = this.cordova.getActivity().getResources();
		
		//generamos un nueva instancia de dialogos
		progressDialog = WinUtils.getMyDialog("TEST", cordova.getActivity());
		
		//obtenemos los datos del telefono
		/*tm = (TelephonyManager) this.cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);*/
		
		//recuperamos el imei
		btd = new BeanTelephoneData();
		btd.setImei("000000000000000");
		btd.setMcc(0);
		btd.setMnc(0);
		btd.setCid(0);
		btd.setLoc(0);

		//Old Way
		/*if(tm.getDeviceId()!=null){btd.setImei(tm.getDeviceId());}
		else{btd.setImei("000000000000000");}
		
		//recuperamos los datos del operador
		String operador = tm.getNetworkOperator();
		if(operador!=null && operador.length()>=5){
			btd.setMcc(Integer.valueOf(operador.substring(0,3)));
			btd.setMnc(Integer.valueOf(operador.substring(3,5)));
		}
		else{
			btd.setMcc(0);
			btd.setMnc(0);
		}
		
		//identificamos el canal de comunicacion y obtenemos sus datos
		if(tm.getCellLocation()!=null && tm.getCellLocation().getClass().equals(GsmCellLocation.class)){
			GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
			btd.setCid(loc.getCid());
			btd.setLoc(loc.getLac());
		}
		else if(tm.getCellLocation()!=null && tm.getCellLocation().getClass().equals(CdmaCellLocation.class)){
			CdmaCellLocation loc = (CdmaCellLocation) tm.getCellLocation();
			btd.setCid(loc.getBaseStationId());
			btd.setLoc(loc.getNetworkId());
		}
		else{
			btd.setCid(0);
			btd.setLoc(0);
		}*/
		
	}
 	
	//Metodos Publicos Estaticos
	/**
	 * Funcion estatica para la obtencion de una instancia del modelo de pinpad a trabajar
	 * @param config (Configuracion) configuracion a usar por el modelo
	 * @param modo (String) modo de trabajo
	 * @param cordova (CordovaInterface) interfaz cordova para la comunicacion
	 * @return (ModeloPinpadBase) instacia del modelo de pinpad a trabajar
	 * @throws Exception - todas las posibles excepciones producto de la instanciacion o inicalizacion
	 */
	public static ModeloPinpadBase getInstance(Configuracion config, String modo, CordovaInterface cordova) throws Exception{
		Log.i(TAG, "Recuperando Instancia de Modelo");
		
		//Verificamos que tenemos una instancia y que esta corresponda a la entregada
		if(instancia==null){
			Log.i(TAG, "Generando nueva instancia");
			Log.i(TAG, "class name: " + ModeloPinpadBase.class.getName());
			Log.i(TAG, "n58: " + ModeloN58.class.getName());
			Log.i(TAG, "Verifone: "+ModeloEWirelessSerie.class.getName());
			Log.i(TAG, "N910: " + ModeloNewland.class.getName());
			Class<?> clazz = Class.forName(config.getModeloPinpad());
			instancia = (ModeloPinpadBase) clazz.newInstance();
			instancia.init(config, cordova);
		}
		
		//verifica si la clase seleccionada usa bluetooth
		if(BLUETOOTHUSER){
			//Preguntar si para la clase seleccionada el bluetooth esta prendido
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if(adapter==null){throw new Exception("El Dispositivo Movil no soporta / posee Bluetooth");}
			if(!adapter.isEnabled()){throw new Exception("Debe encender el bluetooth del dispositivo para acceder a las funciones del pinpad");}
		}
		
		Log.i(TAG, "Fecha de Instancia: "+Utils.dateToString(fechaInstancia, Utils.DATETIME_FORMAT));
		
		//seteamos el modo de trabajo
		instancia.mode = modo;
		instancia.cordova = cordova;
		instancia.configuracion = config;
		
		//retornamos la instancia
		return instancia;
	}
	
	/**
	 * procedimiento que permite la destruccion de la instancia activa en el aplicativo movil
	 */
	public static void destroyInstance(){
		//nulificamos los parametros de la instancia
		instancia.mode = null;
		instancia.cordova = null;
		instancia.configuracion = null;
		
		//limpiamos la informacion temporal
		Utils.clearTmpData(instancia.sp);
		
		//borramos de memoria los recursos de la aplicacion
		instancia.packageName = null;
		instancia.resources = null;
		
		//eliminamos el dialogo de seleccion de dispositivo
		instancia.dismissProgressDialog();
		instancia.progressDialog = null;
		
		//elimianmos de memoria los datos del telefono
		instancia.tm  = null;
		instancia.btd = null;
		
		//destruimos la instancia 
		instancia = null;
		
	}
	
	//Metodos Abstratos
	/*Apertura y Cierra de canales de comunicaci�n*/
	/**
	 * funcion que abre el canal de comunicacion con el pinpad 
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean openComChannel(CallbackContext callbackContext);

	/**
	 * funcion que cierra el canal de comunicaci�n con el pinpad 
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean closeComChannel(CallbackContext callbackContext);
	
	/*funciones basicas de PINPAD*/
	/**
	 * funcion que consulta la fecha y hora presentes en el pinpad
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getFecha(CallbackContext callbackContext);
	
	/**
	 * funcion que consulta el estado de la bateria presente en el dispositivo
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getBateria(CallbackContext callbackContext);

	/**
	 * funcion con la se envia el comando para realizar la impresion de voucher
	 * @param args (JSONArray) contiene la data requerida para dicho proceso
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean printer(JSONArray args, CallbackContext callbackContext);

	/**
	 * funcion que consulta el estado del dispositivo (tamper) para saber si fue alterado
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getTamper(CallbackContext callbackContext);
	
	/**
	 * funcion que consulta los datos de fabricacion del dispositivo (Serial)
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getSerial(CallbackContext callbackContext);

	/*Banda magnetica y pin block*/
	/**
	 * funcion que activa el lector de banda magnetica para procesar una tarjeta financiera
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getBandaMagnetica(CallbackContext callbackContext);

	/**
	 * funcion que activa el teclado del pinpad para captura de pinblock
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getPinblock(JSONArray args, CallbackContext callbackContext);

	/*EMV*/
	/**
	 * funcion con la que se envia el comando para iniciar el proceso EMV en el pinpad
	 * @param args (JSONArray) argumentos de la transaccion
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getEmvTrans(JSONArray args, CallbackContext callbackContext);
	
	/**
	 * funcion con la qeu ejecuta el comando de segundo certificado en la tarjeta ingresada
	 * @param args (JSONArray) argumentos de la transaccion
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getEmvSegundoCertificado(JSONArray args, CallbackContext callbackContext);
		
	/*cancelaciones*/
	/**
	 * funcion que iniciar el proceso de cancelacion del a ultima operacion ejecutada
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getAbortOperation(CallbackContext callbackContext);
	
	/**
	 * funcion que inicia el proceso de cancelacion de la ultima operacion emv iniciada
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getAbortOperationEmv(CallbackContext callbackContext);
	
	/*Calibracion*/
	/**
	 * funcion que inicia el proceso de calibracion del dispositivo con el pinpad
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean getCalibracionDispositivo(CallbackContext callbackContext);

	/*Carga de AID y CAPK*/
	/**
	 * funcion con la se envia el comando para realizar la carga de aids entregados por el servicio web
	 * @param args (JSONArray) contiene la data requerida para dicho proceso
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean downloadAids(JSONArray args, CallbackContext callbackContext);
	
	/**
	 * funcion con la se envia el comando para realizar la carga de llaves EMV entregados por el servicio web
	 * @param args (JSONArray) contiene la data requerida para dicho proceso
	 * @param callbackContext (CallbackContext) medio por el cual se enviara la respuesta
	 * @return (Boolean) true o false dependiendo de la respuesta del pinpad
	 */
	public abstract boolean downloadCapks(JSONArray args, CallbackContext callbackContext);



	
}
