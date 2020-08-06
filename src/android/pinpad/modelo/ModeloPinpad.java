/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.modelo;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.connection.configuracion.Configuracion;

/**
 * 
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * interfaz de ejecucion/Comunicacion 
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class ModeloPinpad extends ModeloBase{

	//CONSTANTES
	private static final String TAG = ModeloPinpad.class.getName();
	
	/*Mode*/
	
	/*ACTIONS*/
	/*configuracion*/
	private static final int ACCIONCONFIGURACION = 2;
	
	/*funciones de apertura y cierre de canalde comunicacion*/
	private static final int ACCIONOPENCOMCHANNEL = 3;
	private static final int ACCIONCLOSECOMCHANNEL = 4;
	
	/*funciones basicas de PINPAD*/
	public static final int ACCIONGETFECHA = 10;
	public static final int ACCIONGETBATERIA = 11;
	public static final int ACCIONGETTAMPER = 12;
	public static final int ACCIONGETSERIAL = 13;
	
	/*Banda magnetica y pin block*/
	public static final int ACCIONGETMSR = 14;
	public static final int ACCIONGETPINBLOCK = 15;
	
	/*EMV*/
	public static final int ACCIONGETEMVTRANS = 16;
	public static final int ACCIONGETEMVSEGCER = 17;
	
	/*cancelaciones*/
	public static final int ACCIONABORT = 18;
	public static final int ACCIONABORTEMV = 19;
	
	/*Calibracion*/
	public static final int ACCIONCALIBRACION = 20;
	
	/*Carga de AID y CAPK*/
	public static final int ACCIONAIDS = 21;
	public static final int ACCIONLLAVESEMV = 22;

	/* Impresora */
	public static final int ACCIONIMPRESORA = 24;
	
	//Atributos
	private ModeloPinpadBase modelo;
	private Configuracion configuracion = new Configuracion();
	private String mode;
	
	//Metodos Privados
	/**
	 * funcion que retorna la configuracion default
	 * @param callbackContext
	 * @return
	 */
	private boolean getConfiguracion(CallbackContext callbackContext){
		Log.i(TAG, "Recuperando configuracion activa");
		try{
			Configuracion config = new Configuracion();
			callbackContext.success(config.toJson());
			return true;
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo recuperar la configuracion activa", e);
			try {
				BeanBase json = new BeanBase();
				json.setEstatus(NOK);
				json.setMensaje("datos solicitados no recuperados, Error: "+e.getMessage());
				callbackContext.error(json.toJson());
			} 
			catch (JSONException e1) {
				Log.e(TAG, "no se logro armar una respuesta", e1);
				callbackContext.error(NOK);
			}
			return false;
		}
	}
	
	//Metodos Sobre Escritos
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		//obtenemos la accion indicada a ejecutar
		int accion = Integer.parseInt(action);
		
		//cargamos la configuracion recibida o por defecto e instanciamos la clase de pinpad a usar
		if(accion != ACCIONCONFIGURACION && accion != ACTIONECHO){
			//extraemos la configuracion a usar segun el servicio
			try{
				JSONObject conf = args.getJSONObject(args.length()-1);
				if(conf!=null){
					Log.d(TAG, "Configuracion Recibida: "+conf.toString());
					configuracion = new Configuracion(conf);
					
				}
				Log.i(TAG, "usando configuracion recibida");
				
				//recuperamos el modo de extraccion
				mode = args.getString(args.length()-2);
				Log.i(TAG, "Modo de accion solicitado: "+((mode.equals("exec"))?"Ejecucion":"Obtencion")+", "+mode);
				
				modelo = ModeloPinpadBase.getInstance(configuracion, mode, this.cordova);		
			}
			catch(Exception e){
				Log.e(TAG, "No se pudo instanciar el modelo de pinpad indicado", e);
				errorCallback("Pinpad no inicializado", e, callbackContext);
				return false;
			}
		}
		
		try{
			Log.i(TAG, "Ejecutando la accion solicitada");
			switch(accion){
				/*Funciones de los modelos*/
				case ACTIONECHO:{return echo(args.getString(0),callbackContext);}
				case ACCIONCONFIGURACION:{return getConfiguracion(callbackContext);} //eliminar
				/*funciones basicas de PINPAD*/
				case ACCIONOPENCOMCHANNEL:{return modelo.openComChannel(callbackContext);}
				case ACCIONCLOSECOMCHANNEL:{return modelo.closeComChannel(callbackContext);}
				/*funciones basicas de PINPAD*/
				case ACCIONGETFECHA:{return modelo.getFecha(callbackContext);} //eliminar??
				case ACCIONGETBATERIA:{return modelo.getBateria(callbackContext);}
				case ACCIONGETTAMPER:{return modelo.getTamper(callbackContext);} //eliminar
				case ACCIONGETSERIAL:{return modelo.getSerial(callbackContext);}
				/*Banda magnetica y pin block*/
				case ACCIONGETMSR:{return modelo.getBandaMagnetica(callbackContext);}//eliminar ??
				case ACCIONGETPINBLOCK:{return modelo.getPinblock(args, callbackContext);}
				/*EMV*/
				case ACCIONGETEMVTRANS:{return modelo.getEmvTrans(args, callbackContext);}
				case ACCIONGETEMVSEGCER:{return modelo.getEmvSegundoCertificado(args, callbackContext);}
				/*cancelaciones*/
				case ACCIONABORT:{return modelo.getAbortOperation(callbackContext);}
				case ACCIONABORTEMV:{return modelo.getAbortOperationEmv(callbackContext);}
				/*calibracion*/
				case ACCIONCALIBRACION:{return modelo.getCalibracionDispositivo(callbackContext);}
				/*Carga de AID y CAPK*/
				case ACCIONAIDS:{return modelo.downloadAids(args, callbackContext);}
				case ACCIONLLAVESEMV:{return modelo.downloadCapks(args, callbackContext);}
				/* Impresora */
				case ACCIONIMPRESORA:{return modelo.printer(args, callbackContext);}
				//case ACCIONIMPRESORA:{return modelo.printer(callbackContext);}
				default:{return false;}
			}
		}
		catch(JSONException e){
			Log.e(TAG, "No se pudo ejecutar la accion solicitada", e);
			throw e;
		}
		finally{
			if(accion == ACCIONCLOSECOMCHANNEL){
				//si se esta cerrando la coneccion se destruye la instancia utilizada
				Log.i(TAG, "Eliminando instancia del pinpad");
				ModeloPinpadBase.destroyInstance();
				modelo = null;
			}
		}
	}
	
}
