/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.modelo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import android.util.Log;
import ve.com.megasoft.pinpad.bean.BeanBase;
import ve.com.megasoft.pinpad.util.WinUtils;

/**
 * 
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * clase maestra para todos los modelos a usar
 * 
 * @author Adrian Jesus Silva Simoes 
 * 
 * 
 *
 */
public class ModeloBase extends CordovaPlugin{
	
	//CONSTANTES
	/*privadas*/
	private static final String TAG = ModeloBase.class.getName();
	
	/*protegidos*/
		/*Acciones*/
	protected static final int ACTIONECHO = 1;
	
		/*Estados*/
	protected static final String OK = "00";
	protected static final String NOK = "99";
	protected static final String TIMEOUT = "98";
	
	//Metodos Protegidos
	/**
	 * funcion de prueba de comunicacion con el core java
	 * @param mensaje (String) mensaje a hacer echo
	 * @param callbackContext (CallbackContext) - por donde se enviaran las respuesta
	 * @return (boolean) true - siempre es true.
	 */
	protected boolean echo(String mensaje, CallbackContext callbackContext){
		//analizamos el mensaje recibido
		String respuesta = "Estoy Activo";
		if(mensaje!=null && !mensaje.equals("")){
			respuesta = respuesta+", mensaje recibido: "+mensaje;
			WinUtils.makeToast(cordova.getActivity().getApplicationContext(), mensaje, true);
		}
		
		//entregamos la respuesta
		try{
			BeanBase json = new BeanBase();
			json.setEstatus(OK);
			json.setMensaje(respuesta);
			callbackContext.success(json.toJson());
		}
		catch(Exception e){
			Log.e(TAG, "no se pudo crear la respuesta",e);
			callbackContext.error(NOK);
			return false;
		}
		
		return true;
	}
	
	/**
	 * funcion que genera el objeto de respuesta de error comun para todas las funciones 
	 * @param mensaje (String) mensaje de error a plasmar
	 * @param e (Exception) la excepcion generada (si es que se genera)
	 * @param callbackContext (CallbackContext) medio para enviar la respuesta
	 */
 	protected void errorCallback(String mensaje, Exception e, CallbackContext callbackContext){
		if(e!=null){Log.e(TAG, mensaje,e);}
		else{Log.e(TAG, mensaje);}
		try{
			BeanBase json = new BeanBase();
			json.setEstatus(NOK);
			if(e!=null){json.setMensaje(mensaje+", Error: "+e.getMessage());}
			else{json.setMensaje(mensaje);}
			callbackContext.error(json.toJson());
		}
		catch(Exception e1){
			Log.e(TAG, "no se logro armar una respuesta", e1);
			callbackContext.error(NOK);
		}
	}
	
}
