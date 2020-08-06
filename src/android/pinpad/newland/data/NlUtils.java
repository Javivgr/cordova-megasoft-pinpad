/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.newland.data;

import ve.com.megasoft.pinpad.newland.modelo.ModeloNewland;

import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.app.Activity;
import java.util.List;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import android.content.SharedPreferences;

import com.newland.me.DeviceManager;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.util.ISOUtils;

import com.newland.mtype.ModuleType;

import android.util.Log;

/**
 * 
 * clase encargada de construir, instanciar y mantener los modulos
 * y objetos necesarios para procesar una transacción EMV, esto, segun el
 * formato requerido por el Newland N910
 * 
 * @author Javier Vicente González Rodríguez / Jesús Alberto Jiménez Garizao / Adrian Jesus Silva Simoes 
 *
 */
public class NlUtils{

	//Atributos
	private static CordovaPlugin cordovaPlugin;
	private static SharedPreferences sharedPreferences;
	private static CallbackContext callbackContext;
	private static String workingKey;

	//Metodos Publicos
	/**
	 * funcion que obtiene e inicializa módulo EMV para su posterior
	 * uso en transacciones por contacto
	 * @return (EmvModule) módulo emv
	 * @throws Exception - no logró obtener el módulo emv
	 */
	public static EmvModule getEmvModule() throws Exception
	{
		try{
			EmvModule emvModule=(EmvModule) ModeloNewland.getDevice().getExModule("EMV_INNERLEVEL2");
            emvModule.initEmvModule(cordovaPlugin.cordova.getActivity().getApplicationContext());
			return emvModule;
		}
		catch(Exception e){
			throw new Exception("Error al obtener modulo emv",e);
		}
	}

	//Getter's && Setter's
	/**
	 * funcion que setea el cordova plugin de la aplicación para
	 * su posterior uso en los distintos métodos
	 * @param cordovaPlugin (CordovaPlugin) activity a setear 
	 */
	public static void setCordovaPlugin (CordovaPlugin cordovaPlugin){
		NlUtils.cordovaPlugin=cordovaPlugin;
	}

	/**
	 * funcion que obtiene el cordova plugin de la aplicación
	 * @return cordovaPlugin (CordovaPlugin)
	 */
	public static CordovaPlugin getCordovaPlugin (){
		return cordovaPlugin;
	}

	public static void setSharedPreferences (SharedPreferences sharedPreferences){
		NlUtils.sharedPreferences=sharedPreferences;
	}

	public static SharedPreferences getSharedPreferences (){
		return sharedPreferences;
	}

	public static void setCallbackContext (CallbackContext callbackContext){
		NlUtils.callbackContext=callbackContext;
	}

	public static CallbackContext getCallbackContext (){
		return callbackContext;
	}

	public static void setWorkingKey (String workingKey){
		NlUtils.workingKey=workingKey;
	}

	public static String getWorkingKey (){
		return workingKey;
	}
}