/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import org.json.JSONException;
import org.json.JSONObject;

import ve.com.megasoft.pinpad.verifone.modelo.ModeloE105;

/**
 * Cordova Java Part Plugin Project
 * 
 * bean que contiene la configuracion requerida por los pinpads 
 * implementados en el plugin, ademas de poseer una configuracion
 * por defecto
 * 
 * @author Adrian Jesus Silva Simeos 
 *
 */
public class BeanConfiguration{
	//CONSTANTES
	/*atributos-Timeout*/
	private static final String TIMEOUTRESPONSE = "timeout_response";
	private static final String TIMEOUTHEX = "timeout_hex";
	private static final String TIMEOUTINT = "timeout_int";
	private static final String TIMEOUTINTSELECTAPP = "timeout_int_select_app"; 
	private static final String TIMEOURINTCALIBRACION = "timeout_int_calibracion";
	
	/*atributos-Intervalos*/
	private static final String INTSEGUNDO = "int_segundo";
	
	/*atributos-Titulos*/
	private static final String TITULOBADWRONGPINBLOCK = "titulo_bad_wrong_pinblock";
	private static final String TITULOSELECTAPP = "titulo_select_app";
	private static final String TITULOFALLBACK = "titulo_fallback";
	
	/*atributos-Textos*/
	private static final String TEXTOAMBOSLECTORES = "texto_ambos_lectores";
	private static final String TEXTOLECTORBANDA = "texto_lector_banda";
	private static final String TEXTOLECTORCHIP = "texto_lector_chip";
	private static final String TEXTOENTERPINBLOCK = "texto_enter_pinblock";
	private static final String TEXTOREENTERPINBLOCK = "texto_re_enter_pinblock";
	private static final String TEXTOBADWRONGPINBLOCK = "texto_bad_wrong_pinblock";
	private static final String TEXTOSELECTAPPCANCELOPTION ="texto_select_app_cancel_option";
	private static final String TEXTOFALLBACK = "texto_fallback";
	
	/*Modelo Pinpad*/
	private static final String MODELOPINPAD = "modelo_pinpad";
	private static final String PINPADNOMBRE = "pinpad_nombre";
	private static final String PINPADDIRECC = "pinpad_direcc";
	
	//Atributos
	/*Timeout*/
	/**tiempo maximo por respuesta del pinpad.*/
	private int timeoutResponse;
	/**timeout para las operaciones internas del pinpad.*/
	private String timeoutHex;
	/**timeout para las operaciones internas del pinpad.*/
	private int timeoutInt;
	/**timeout para la seleccion de una app de la tarjeta.*/
	private int timeoutIntSelectApp;
	/**timeout para la calibracion de un dispositivo con un pinpad.;*/
	private int timeoutIntCalibracion;
	
	/*Intervalos*/
	/**cuando dura un segundo o cada ciclo de consulta, en milisegundos*/
	private int intSegundo;
	
	/*Titulos*/
		//PinBlock
	/**titulo a mostrar cuando el usuario se equipoca ingresando el pinblock*/
	private String tituloBadWrongpinblock;
		//Card Internal Apps
	/**titulo a mostrar cuando el usuario debe seleccionar una aplicacion de la tarjeta*/
	private String tituloSelectApp;
		//fallback
	/**texto a mostrar cuando se produce un error de lectura de la tarjeta chip (fallback)*/
	private String tituloFallback;
	
	/*Textos*/
		//Lectores
	/**texto que se muestra cuando se activan ambos lectores*/
	private String textoAmbosLectores;//texto que se muestra cuando se activan ambos lectores
	/**texto qeu se muestra cuando solo esta activo el lecto de banda magnetica*/
	private String textoLectorBanda; //texto qeu se muestra cuando solo esta activo el lecto de banda magnetica
	/**texto que se muestra cuando solo esta activo el lector chip*/
	private String textoLectorChip; //texto que se muestra cuando solo esta activo el lector chip
		//PinBlock
	/**texto que se muestra cuando se solicita el pinblock del tarjeta habiente*/
	private String textoEnterPinblock;
	/**texto que se muestra cuando se solicita al tarjeta habiente que reingrese su pinblock,
	 * por colocar uno incorrecto*/
	private String textoReEnterPinblock;
	/**texto que se muestra cuando se le indica al tarjeta habiente que su tarjeta fue bloqueada 
	 * por ingresar un pinblock incorrecto mas de 3 veces */
	private String textoBadWrongPinblock;
		//Card Internal Apps  
	/**texto a mostrar en la opcion de seleccion de app*/
	private String textoSelectAppCancelOption;
		//Fallback
	/**texto a mostrar cuando se produce un error de lectura de la tarjeta chip (fallback)*/
	private String textoFallback;
		//Datos del pinpad y su modelo
	/** string que contiene el nombre de la clase del pinpad a usar */
	private String modeloPinpad;
	/** string que contiene el nombre del pinpad a usar, inalambrico */
	private String pinpadNombre;
	/** string que contiene la direccion (MAC/ip) del pinpad a usar, inalambrico */
	private String pinpadDirecc;
	
	//Constructores
	/*Valores por default*/
	/**
	 * constructo con los valores por defecto de la transaccion
	 * 
	 * TODO - agregar un traductor a las distribuciones
	 */
	public BeanConfiguration() {
		
		/*Timeout's*/
		timeoutResponse = 60;
		timeoutHex = "0A";
		timeoutInt = 10;
		timeoutIntSelectApp = 15;
		timeoutIntCalibracion = 180;
		
		/*Intervalos*/
		intSegundo = 1000;
		
		/*Titulos*/
		tituloBadWrongpinblock = "PIN Incorrecto";
		tituloSelectApp = "Seleccione una opci�n";
		tituloFallback = "Error de Lectura";
		
		/*Textos*/
		textoAmbosLectores="Inserte o deslice Tarjeta...";
		textoLectorBanda="Deslice Tarjeta...";
		textoLectorChip="Inserte Tarjeta ...";
		textoEnterPinblock = "Por favor ingrese PIN...";
		textoReEnterPinblock = "Por favor re-ingrese PIN...";
		textoBadWrongPinblock = "Tarjeta Bloqueada, contacte a su banco";
		textoSelectAppCancelOption = "Cancelar Transacci�n";
		textoFallback = "Presione OK para continuar por banda";
		
		/*pinpad*/
		modeloPinpad = ModeloE105.class.getName();
		
	}
	
	/*Valores resibidos*/
	/**
	 * constructor con los valores recibidos por parte del usuario
	 * @param parametros (JSONObject) JSON con los parametros requeridos
	 * @throws JSONException - la excepcion pertinente a la extracion de los datos de los parametros
	 */
	public BeanConfiguration(JSONObject parametros) throws JSONException {
		/*Timeout's*/
		timeoutResponse = parametros.getInt(TIMEOUTRESPONSE);
		timeoutHex = parametros.getString(TIMEOUTHEX);
		timeoutInt = parametros.getInt(TIMEOUTINT);
		timeoutIntSelectApp = parametros.getInt(TIMEOUTINTSELECTAPP);
		timeoutIntCalibracion = parametros.getInt(TIMEOURINTCALIBRACION);
		
		/*Intervalos*/
		intSegundo = parametros.getInt(INTSEGUNDO);
		
		/*Titulos*/
		tituloBadWrongpinblock = parametros.getString(TITULOBADWRONGPINBLOCK);
		tituloSelectApp = parametros.getString(TITULOSELECTAPP);
		tituloFallback = parametros.getString(TITULOFALLBACK);
		
		/*Textos*/
		textoAmbosLectores=parametros.getString(TEXTOAMBOSLECTORES);
		textoLectorBanda=parametros.getString(TEXTOLECTORBANDA);
		textoLectorChip=parametros.getString(TEXTOLECTORCHIP);
		textoEnterPinblock = parametros.getString(TEXTOENTERPINBLOCK);
		textoReEnterPinblock = parametros.getString(TEXTOREENTERPINBLOCK);
		textoBadWrongPinblock = parametros.getString(TEXTOBADWRONGPINBLOCK);
		textoSelectAppCancelOption = parametros.getString(TEXTOSELECTAPPCANCELOPTION);
		textoFallback = parametros.getString(TEXTOFALLBACK);
		
		/*Pinpad*/
		modeloPinpad = parametros.getString(MODELOPINPAD);
		if(parametros.getString(PINPADNOMBRE)!=null && !parametros.getString(PINPADNOMBRE).equals("")){
			pinpadNombre = parametros.getString(PINPADNOMBRE);
		}
		if(parametros.getString(PINPADDIRECC)!=null && !parametros.getString(PINPADDIRECC).equals("")){
			pinpadDirecc = parametros.getString(PINPADDIRECC);
		}
		
	}

	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("/n [timeoutResponse] :"+timeoutResponse);
		sb.append("/n [timeoutHex] :"+timeoutHex);
		sb.append("/n [timeoutInt] :"+timeoutInt);
		sb.append("/n [timeoutIntSelectApp] :"+timeoutIntSelectApp);
		sb.append("/n [timeoutIntCalibracion] :"+timeoutIntCalibracion);
		
		sb.append("/n [intSegundo] :"+intSegundo);
		
		sb.append("/n [tituloBadWrongpinblock] :"+tituloBadWrongpinblock);
		sb.append("/n [tituloSelectApp] :"+tituloSelectApp);
		sb.append("/n [tituloFallback] :"+tituloFallback);
		
		sb.append("/n [textoAmbosLectores] :"+textoAmbosLectores);
		sb.append("/n [textoLectorBanda] :"+textoLectorBanda);
		sb.append("/n [textoLectorChip] :"+textoLectorChip);
		sb.append("/n [textoEnterPinblock] :"+textoEnterPinblock);
		sb.append("/n [textoReEnterPinblock] :"+textoReEnterPinblock);
		sb.append("/n [textoBadWrongPinblock] :"+textoBadWrongPinblock);
		sb.append("/n [textoSelectAppCancelOption] :"+textoSelectAppCancelOption);
		sb.append("/n [textoFallback] :"+textoFallback);
		
		sb.append("/n [modeloPinpad] :"+modeloPinpad);
		sb.append("/n [pinpadNombre] :"+pinpadNombre);
		sb.append("/n [pinpadDirecc] :"+pinpadDirecc);
		
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
		
		parametros.put(TIMEOUTRESPONSE, timeoutResponse);
		parametros.put(TIMEOUTHEX, timeoutHex);
		parametros.put(TIMEOUTINT, timeoutInt);
		parametros.put(TIMEOUTINTSELECTAPP, timeoutIntSelectApp);
		parametros.put(TIMEOURINTCALIBRACION, timeoutIntCalibracion);
		
		parametros.put(INTSEGUNDO, intSegundo);
		
		parametros.put(TITULOBADWRONGPINBLOCK, tituloBadWrongpinblock);
		parametros.put(TITULOSELECTAPP, tituloSelectApp);
		parametros.put(TITULOFALLBACK, tituloFallback);
		
		parametros.put(TEXTOAMBOSLECTORES, textoAmbosLectores);
		parametros.put(TEXTOLECTORBANDA, textoLectorBanda);
		parametros.put(TEXTOLECTORCHIP, textoLectorChip);
		parametros.put(TEXTOENTERPINBLOCK, textoEnterPinblock);
		parametros.put(TEXTOREENTERPINBLOCK, textoReEnterPinblock);
		parametros.put(TEXTOBADWRONGPINBLOCK, textoBadWrongPinblock);
		parametros.put(TEXTOSELECTAPPCANCELOPTION, textoSelectAppCancelOption);
		parametros.put(TEXTOFALLBACK, textoFallback);
		
		parametros.put(MODELOPINPAD, modeloPinpad);
		parametros.put(PINPADNOMBRE, pinpadNombre);
		parametros.put(PINPADDIRECC, pinpadDirecc);
		
		return parametros;
	}

	//Getter's && Setter's
	public int getTimeoutResponse() {
		return timeoutResponse;
	}

	public void setTimeoutResponse(int timeoutResponse) {
		this.timeoutResponse = timeoutResponse;
	}

	public String getTimeoutHex() {
		return timeoutHex;
	}

	public void setTimeoutHex(String timeoutHex) {
		this.timeoutHex = timeoutHex;
	}

	public int getTimeoutInt() {
		return timeoutInt;
	}

	public void setTimeoutInt(int timeoutInt) {
		this.timeoutInt = timeoutInt;
	}

	public int getTimeoutIntSelectApp() {
		return timeoutIntSelectApp;
	}

	public void setTimeoutIntSelectApp(int timeoutIntSelectApp) {
		this.timeoutIntSelectApp = timeoutIntSelectApp;
	}

	public int getIntSegundo() {
		return intSegundo;
	}

	public void setIntSegundo(int intSegundo) {
		this.intSegundo = intSegundo;
	}

	public String getTituloBadWrongpinblock() {
		return tituloBadWrongpinblock;
	}

	public void setTituloBadWrongpinblock(String tituloBadWrongpinblock) {
		this.tituloBadWrongpinblock = tituloBadWrongpinblock;
	}

	public String getTituloSelectApp() {
		return tituloSelectApp;
	}

	public void setTituloSelectApp(String tituloSelectApp) {
		this.tituloSelectApp = tituloSelectApp;
	}

	public String getTituloFallback() {
		return tituloFallback;
	}

	public void setTituloFallback(String tituloFallback) {
		this.tituloFallback = tituloFallback;
	}

	public String getTextoAmbosLectores() {
		return textoAmbosLectores;
	}

	public void setTextoAmbosLectores(String textoAmbosLectores) {
		this.textoAmbosLectores = textoAmbosLectores;
	}

	public String getTextoLectorBanda() {
		return textoLectorBanda;
	}

	public void setTextoLectorBanda(String textoLectorBanda) {
		this.textoLectorBanda = textoLectorBanda;
	}

	public String getTextoLectorChip() {
		return textoLectorChip;
	}

	public void setTextoLectorChip(String textoLectorChip) {
		this.textoLectorChip = textoLectorChip;
	}

	public String getTextoEnterPinblock() {
		return textoEnterPinblock;
	}

	public void setTextoEnterPinblock(String textoEnterPinblock) {
		this.textoEnterPinblock = textoEnterPinblock;
	}

	public String getTextoReEnterPinblock() {
		return textoReEnterPinblock;
	}

	public void setTextoReEnterPinblock(String textoReEnterPinblock) {
		this.textoReEnterPinblock = textoReEnterPinblock;
	}

	public String getTextoBadWrongPinblock() {
		return textoBadWrongPinblock;
	}

	public void setTextoBadWrongPinblock(String textoBadWrongPinblock) {
		this.textoBadWrongPinblock = textoBadWrongPinblock;
	}

	public String getTextoSelectAppCancelOption() {
		return textoSelectAppCancelOption;
	}

	public void setTextoSelectAppCancelOption(String textoSelectAppCancelOption) {
		this.textoSelectAppCancelOption = textoSelectAppCancelOption;
	}

	public String getTextoFallback() {
		return textoFallback;
	}

	public void setTextoFallback(String textoFallback) {
		this.textoFallback = textoFallback;
	}

	public String getModeloPinpad() {
		return modeloPinpad;
	}

	public void setModeloPinpad(String modeloPinpad) {
		this.modeloPinpad = modeloPinpad;
	}

	public int getTimeoutIntCalibracion() {
		return timeoutIntCalibracion;
	}

	public void setTimeoutIntCalibracion(int timeoutIntCalibracion) {
		this.timeoutIntCalibracion = timeoutIntCalibracion;
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
	
}
