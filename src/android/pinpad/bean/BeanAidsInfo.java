/**
 * Copyright Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * 
 * bean para el envio de la informacion de los aids a los pinpads
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanAidsInfo {

	//CONSTANTES
	private static final String TAG = BeanAidsInfo.class.getName();
		/*Aids-Schema*/
	public static Map<String, String> aidsSchema;
		/*TAGS*/
	private static final String TAGRID                     = "rid";
	private static final String TAGAID                     = "aid";
	private static final String TAGVERSIONAPP              = "versionApp";
	private static final String TAGTIPOSELECCION           = "tipoSeleccion";
	private static final String TAGTACDEFAULT              = "tacDefault";
	private static final String TAGTACDENIAL               = "tacDenial";
	private static final String TAGONLINE                  = "tacOnline";
	private static final String TAGFLOORLIMIT              = "floorLimit";
	private static final String TAGTHRESHOLDVALUE          = "thresholdValue";
	private static final String TAGTARGETPERCENTAGE        = "targetPercentage";
	private static final String TAGMAXIMUMTARGETPERCENTAGE = "maximumTargetPercentage";
	private static final String TAGDDOL                    = "ddol";
	private static final String TAGBONLINEPIN              = "bOnlinePinblock";
	
	//Atributos
	/** Registered Application Provider Identifier. Es el identificador de la franquicia. */
	private String rid;
	/** Identificador de la aplicación. */
	private String aid;
	/** Application version number. */
	private String versionApp;
	/** 
	 * Tipo de selección. Si el valor es 1 no permitira selección parcial, si es 2 permitirá selección parcial. 
	 * Este valor es usado en dispositivos Verifone. 
	 */
	private String tipoSeleccion;
	/** Terminal Action Code Default. */
	private String TACDefault;
	/** Terminal Action Code Denial. */
	private String TACDenial;
	/** Terminal Action Code Online. */
	private String TACOnline;
	
	private String floorLimit;
	private String thresholdValue;
	private String targetPercentage;
	private String maximumTargetPercentage;
	private String DDOL;
	private String bOnlinePinblock;
	
	//Metodos Privados
	/**
	 * procedimiento que inicializa el mapa con los RID de las franquisias
	 */
	private static void initAidsSchema(){
		if(aidsSchema==null){
			aidsSchema = new HashMap<String, String>();
			aidsSchema.put("A000000003","VISA");
			aidsSchema.put("A000000004","MASTERCARD");
			aidsSchema.put("A000000025","AMEX");
			aidsSchema.put("A000000152","DINERS");
		}
	}
	
	//Constructor
	/**
	 * constructor generico con datos en blanco
	 */
	public BeanAidsInfo() {
		//inicializamos el mapa de schema
		initAidsSchema();
		
		rid = "";
		aid = "";
		versionApp = "";
		tipoSeleccion = "";
		TACDefault ="";
		TACDenial = "";
		TACOnline = "";
		floorLimit = "";
		thresholdValue = "";
		targetPercentage = "";
		maximumTargetPercentage = "";
		DDOL = "";
		bOnlinePinblock = "";
		
	}
	
	/**
	 * constructor con los valores recibidos atraves de una representacion json del mismo
	 * @param json (JSONObject) representacion json del objeto
	 * @throws JSONException - en caso de no poder recuperar los datos
	 */
	public BeanAidsInfo(JSONObject json) throws JSONException{
		
		//inicializamos el mapa de schema
		initAidsSchema();
		
		Log.d(TAG, "Aid a procesar: "+json.toString());
		
		//recuperamos la data del json
		rid = json.getString(TAGRID);
		aid = json.getString(TAGAID);
		versionApp = json.getString(TAGVERSIONAPP);
		tipoSeleccion = json.getString(TAGTIPOSELECCION);
		TACDefault = json.getString(TAGTACDEFAULT);
		TACDenial = json.getString(TAGTACDENIAL);
		TACOnline = json.getString(TAGONLINE);
		floorLimit = json.getString(TAGFLOORLIMIT);
		thresholdValue = json.getString(TAGTHRESHOLDVALUE);
		targetPercentage = json.getString(TAGTARGETPERCENTAGE);
		maximumTargetPercentage = json.getString(TAGMAXIMUMTARGETPERCENTAGE);
		DDOL = json.getString(TAGDDOL);
//		bOnlinePinblock = json.getString(TAGBONLINEPIN);
	}
	
	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n rid ["+rid+"]");
		sb.append("\n aid ["+aid+"]");
		sb.append("\n versionApp ["+versionApp+"]");
		sb.append("\n tipoSeleccion ["+tipoSeleccion+"]");
		sb.append("\n TACDefault ["+TACDefault+"]");
		sb.append("\n TACDenial ["+TACDenial+"]");
		sb.append("\n TACOnline ["+TACOnline+"]");
		sb.append("\n floorLimit ["+floorLimit+"]");
		sb.append("\n thresholdValue ["+thresholdValue+"]");
		sb.append("\n targetPercentage ["+targetPercentage+"]");
		sb.append("\n maximumTargetPercentage ["+maximumTargetPercentage+"]");
		sb.append("\n DDOL ["+DDOL+"]");
		sb.append("\n bOnlinePinblock ["+bOnlinePinblock+"]");
		
		return sb.toString();
	}
	
	/**
	 * funcion que retorna la representacion JSON del bean
	 * @return (JSONObject) representacion json 
	 * @throws JSONException - en caso de que no pueda construir la representacion
	 */
	public JSONObject toJson() throws JSONException{
		
		JSONObject json = new JSONObject();
		
		json.put(TAGRID, rid);
		json.put(TAGAID, aid);
		json.put(TAGVERSIONAPP, versionApp);
		json.put(TAGTIPOSELECCION, tipoSeleccion);
		json.put(TAGTACDEFAULT, TACDefault);
		json.put(TAGTACDENIAL, TACDenial);
		json.put(TAGONLINE, TACOnline);
		json.put(TAGFLOORLIMIT, floorLimit);
		json.put(TAGTHRESHOLDVALUE, thresholdValue);
		json.put(TAGTARGETPERCENTAGE, targetPercentage);
		json.put(TAGMAXIMUMTARGETPERCENTAGE, maximumTargetPercentage);
		json.put(TAGDDOL, DDOL);
		json.put(TAGBONLINEPIN, bOnlinePinblock);
		
		return json;
	}
	
	//Getter's && Setter's
	public String getRid() {
		return rid;
	}
	
	public void setRid(String rid) {
		this.rid = rid;
	}
	
	public String getAid() {
		return aid;
	}
	
	public void setAid(String aid) {
		this.aid = aid;
	}
	
	public String getVersionApp() {
		return versionApp;
	}
	
	public void setVersionApp(String versionApp) {
		this.versionApp = versionApp;
	}
	
	public String getTipoSeleccion() {
		return tipoSeleccion;
	}
	
	public void setTipoSeleccion(String tipoSeleccion) {
		this.tipoSeleccion = tipoSeleccion;
	}
	
	public String getTACDefault() {
		return TACDefault;
	}
	
	public void setTACDefault(String tACDefault) {
		TACDefault = tACDefault;
	}
	
	public String getTACDenial() {
		return TACDenial;
	}
	
	public void setTACDenial(String tACDenial) {
		TACDenial = tACDenial;
	}
	
	public String getTACOnline() {
		return TACOnline;
	}
	
	public void setTACOnline(String tACOnline) {
		TACOnline = tACOnline;
	}

	public String getFloorLimit() {
		return floorLimit;
	}

	public void setFloorLimit(String floorLimit) {
		this.floorLimit = floorLimit;
	}

	public String getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(String thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public String getTargetPercentage() {
		return targetPercentage;
	}

	public void setTargetPercentage(String targetPercentage) {
		this.targetPercentage = targetPercentage;
	}

	public String getMaximumTargetPercentage() {
		return maximumTargetPercentage;
	}

	public void setMaximumTargetPercentage(String maximumTargetPercentage) {
		this.maximumTargetPercentage = maximumTargetPercentage;
	}

	public String getDDOL() {
		return DDOL;
	}

	public void setDDOL(String dDOL) {
		DDOL = dDOL;
	}

	
	public String getbOnlinePinblock() {
		return bOnlinePinblock;
	}

	public void setbOnlinePinblock(String bOnlinePinblock) {
		this.bOnlinePinblock = bOnlinePinblock;
	}
	
}
