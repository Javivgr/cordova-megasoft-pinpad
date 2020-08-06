/**
 * Copyright Mega Soft Configuracion C.A.
 */
package ve.com.megasoft.pinpad.connection.configuracion;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cordova Java Part Plugin Project
 * 
 * bean que contiene la configuracion requerida por los pinpads 
 * implementados en el plugin, ademas de poseer una configuracion
 * por defecto
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class Configuracion {

	//CONSTANTES 
	/*Tags*/
		/*Modelo Pinpad*/
	private static final String TAGMODELOPINPAD = "modelo_pinpad";
	private static final String TAGPINPADNOMBRE = "pinpad_nombre";
	private static final String TAGPINPADDIRECC = "pinpad_direcc";
	private static final String TAGPINPADINDICEWK = "pinpad_indice_wk";
	private static final String TAGPINPADWORKINGKEY = "pinpad_working_key";
	private static final String TAGPINPADIDLETEXT = "pinpad_idle_text";
	
	/*Timeout's */
	public static final String TIMEOUTHEX = "0A";
	public static final int TIMEOUTINT = 10;
	public static final int TIMEOUTINTSELECTAPP = 15;
	public static final int TIMEOUTRESPONSE = 60;

	//Atributos
		//Datos del pinpad y su modelo
	/** string que contiene el nombre de la clase del pinpad a usar */
	private String modeloPinpad;
	/** string que contiene el nombre del pinpad a usar, inalambrico */
	private String pinpadNombre;
	/** string que contiene la direccion (MAC/ip) del pinpad a usar, inalambrico */
	private String pinpadDirecc;
	/** String qeu tiene el indice de la working key */
	private String pinpadIndiceWk;
	/** String que contiene la working a usar el pin */
	private String pinpadWorkingKey;
	/** texto a mostrar en la pantalla del pinpad mientras no realize ninguna operacion */
	private String pinpadIdleText;

	//Constructor
	/**
	 * constructor generico con la configuracion default
	 */
	public Configuracion() {
		this.modeloPinpad = "";
		this.pinpadNombre = "";
		this.pinpadDirecc = "";
		this.pinpadIndiceWk = "";
		this.pinpadWorkingKey = "";
		this.pinpadIdleText = "";
	}
	
	/**
	 * constructor con los valores recibidos por parte del app 
	 * @param json (JSONObject) Json con los parametros requeridos 
	 * @throws JSONException - la excepcion pertinente a la extracion de los datos de los parametros
	 */
	public Configuracion(JSONObject json) throws JSONException{
		this.modeloPinpad = json.getString(TAGMODELOPINPAD);
		this.pinpadNombre = json.getString(TAGPINPADNOMBRE);
		this.pinpadDirecc = json.getString(TAGPINPADDIRECC);
		this.pinpadIndiceWk = json.getString(TAGPINPADINDICEWK);
		this.pinpadWorkingKey = json.getString(TAGPINPADWORKINGKEY);
		this.pinpadIdleText = json.getString(TAGPINPADIDLETEXT);
	}
	
	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("/n [modeloPinpad] :"+modeloPinpad);
		sb.append("/n [pinpadNombre] :"+pinpadNombre);
		sb.append("/n [pinpadDirecc] :"+pinpadDirecc);
		
		sb.append("/n [pinpadIndiceWk] :"+pinpadIndiceWk);
		sb.append("/n [pinpadWorkingKey] :"+pinpadWorkingKey);
		
		return sb.toString();
	}

	//Metodos Publicos
	/**
	 * funcion que retorna los parametros actuales de la instancia
	 * @return (JSONObject) json con los parametros usados por los pinpad's
	 * @throws JSONException - las posibles excepciones producto de la ejecucion
	 */
	public JSONObject toJson() throws JSONException{
		
		JSONObject parametros = new JSONObject();
		
		parametros.put(TAGMODELOPINPAD, modeloPinpad);
		parametros.put(TAGPINPADNOMBRE, pinpadNombre);
		parametros.put(TAGPINPADDIRECC, pinpadDirecc);
		
		return parametros;
	}
	
	//Getter's && Setter's
	public String getModeloPinpad() {
		return modeloPinpad;
	}

	public void setModeloPinpad(String modeloPinpad) {
		this.modeloPinpad = modeloPinpad;
	}
	
	public String getPinpadNombre() {
		return pinpadNombre;
	}

	public void setPinpadNombre(String pinpadNombre) {
		this.pinpadNombre = pinpadNombre;
	}

	public String getPinpadDirecc() {
		return pinpadDirecc;
	}

	public void setPinpadDirecc(String pinpadDirecc) {
		this.pinpadDirecc = pinpadDirecc;
	}

	public String getPinpadIndiceWk() {
		return pinpadIndiceWk;
	}

	public void setPinpadIndiceWk(String pinpadIndiceWk) {
		this.pinpadIndiceWk = pinpadIndiceWk;
	}

	public String getPinpadWorkingKey() {
		return pinpadWorkingKey;
	}

	public void setPinpadWorkingKey(String pinpadWorkingKey) {
		this.pinpadWorkingKey = pinpadWorkingKey;
	}

	
	public String getPinpadIdleText() {
		return pinpadIdleText;
	}

	public void setPinpadIdleText(String pinpadIdleText) {
		this.pinpadIdleText = pinpadIdleText;
	}
	
}
