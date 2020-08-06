package ve.com.megasoft.pinpad.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contiene una llave pública EMV para ser cargada en el PINPad.
 * 
 * Este bean se crea con los datos de llaves públicas EMV que envía el Merchant
 * Server.
 * 
 * Una lista de estas llaves será cargada en el PINPad cuando corresponda.
 * 
 * Las llaves públicas generadas con RSA constan de un exponente y un módulo.
 * 
 * @author Camilo Torres
 * 
 */
public class BeanEmvKeyInfo {

	//CONSTANTES
//	private static final String TAG = BeanEmvKeyInfo.class.getName();
		/*Aids-Schema*/
	public static Map<String, String> aidsSchema;
		/*TAGS*/
	private static final String TAGMODULO = "modulo";
	private static final String TAGRID = "rid";
	private static final String TAGINDICE = "indice";
	private static final String TAGFECHADEEXPIRACION = "fechaDeExpiracion";
	private static final String TAGEXPONENTE = "exponente";
	private static final String TAGHASH = "hash";
//	private static final String TAGLONGITUD = "longitud"; //calculada
//	private static final String TAGLLAVECOMPUESTA = "llaveCompuesta";//ignorar
	private static final String TAGTACDEFAULT = "tacDefault";
	private static final String TAGTACDENIAL = "tacDenial";
	private static final String TAGONLINE = "tacOnline";
	
	//Atributos
	/**
	 * Módulo de la llave.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	private String modulo;
	/**
	 * Registered Application Provider Identifier. Es el identificador de la
	 * franquicia.
	 * 
	 * A000000004 es Master Card, A000000025 es Amex, etc.
	 */
	private String rid;
	/**
	 * Índice de la llave.
	 */
	private String indice;
	/**
	 * Fecha de expiración de la llave.
	 */
	private Date fechaDeExpiracion;
	/**
	 * Exponente de la llave. Normalmente viene el valor 03, pero puede venir
	 * otro valor.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	private String exponente;
	/**
	 * Hash SHA-1 sobre la llave (Es opcional). Este valor puede no venir en el
	 * mensaje del Merchant Server, por lo tanto puede estar en null.
	 */
	private String hash;
	/**
	 * Tamaño del Modulo o Valor de la Llave.
	 */
	private String longitud;
	/**
	 * Indicador de llaves compuestas para llaves que tienen más de 256 bytes.
	 * Si su valor es 0, la llave es una nueva llave que no es compuesta.
	 * Si su valor es 1, la llave es una nueva llave que si es compuesta.
	 * Si el valor es mayor que 1, lo que vino es parte de la llave anterior.
	 */
//	private int llaveCompuesta;
	/** Terminal Action Code Default. */
	private String TACDefault;
	/** Terminal Action Code Denial. */
	private String TACDenial;
	/** Terminal Action Code Online. */
	private String TACOnline;
	
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
	 * constructor de la clase, instancia vacia
	 */
	public BeanEmvKeyInfo(){initAidsSchema();}
	
	/**
	 * constructor qeu recibe el json del servicio.
	 * @param json (JSONObject) json con la data del objeto
	 * @throws JSONException
	 */
	public BeanEmvKeyInfo(JSONObject json) throws JSONException{
		initAidsSchema();
		
		//recuperamos los datos del JSOn
		modulo = json.getString(TAGMODULO);
		longitud = Integer.toString(modulo.length()/2);
		rid = json.getString(TAGRID);
		indice = json.getString(TAGINDICE);
		fechaDeExpiracion = new Date(json.getLong(TAGFECHADEEXPIRACION));
		exponente = json.getString(TAGEXPONENTE);
		hash = json.getString(TAGHASH);
		TACDefault = json.getString(TAGTACDEFAULT);
		TACDenial = json.getString(TAGTACDENIAL);
		TACOnline = json.getString(TAGONLINE);
		
	}

	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BeanEmvKeyInfo [modulo=").append(modulo)
			   .append(", rid=").append(rid)
			   .append(", indice=").append(indice)
			   .append(", fechaDeExpiracion=").append(fechaDeExpiracion)
			   .append(", exponente=").append(exponente)
			   .append(", hash=").append(hash)
			   .append(", longitud=").append(longitud)
//			   .append(", llaveCompuesta=").append(llaveCompuesta)
			   .append("]");
		return builder.toString();
	}
	
	/**
	 * funciomn que genera la representacion JSON del objeto
	 * @return (JSONObject) representacion JSOn del objeto
	 * @throws JSONException - 
	 */
	public JSONObject toJson() throws JSONException{
		
		JSONObject json = new JSONObject();
		
		json.put(TAGMODULO, modulo);
		json.put(TAGRID, rid);
		json.put(TAGINDICE, indice);
		json.put(TAGFECHADEEXPIRACION, fechaDeExpiracion);
		json.put(TAGEXPONENTE, exponente);
		json.put(TAGHASH, hash);
//		json.put(TAGLONGITUD, longitud);
//		json.put(TAGLLAVECOMPUESTA, llaveCompuesta);
		json.put(TAGTACDEFAULT, TACDefault);
		json.put(TAGTACDENIAL, TACDenial);
		json.put(TAGONLINE, TACOnline);
		
		return json;
		
	}
	
	//Getter's && Setter's
	/**
	 * Módulo de la llave.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	public String getModulo() {
		return modulo;
	}

	/**
	 * Módulo de la llave.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	/**
	 * Registered Application Provider Identifier. Es el identificador de la
	 * franquicia.
	 * 
	 * A000000004 es Master Card, A000000025 es Amex, etc.
	 */
	public String getRid() {
		return rid;
	}

	/**
	 * Registered Application Provider Identifier. Es el identificador de la
	 * franquicia.
	 * 
	 * A000000004 es Master Card, A000000025 es Amex, etc.
	 */
	public void setRid(String rid) {
		this.rid = rid;
	}

	/**
	 * Índice de la llave.
	 */
	public String getIndice() {
		return indice;
	}

	/**
	 * Índice de la llave.
	 */
	public void setIndice(String indice) {
		this.indice = indice;
	}

	public Date getFechaDeExpiracion() {
		return fechaDeExpiracion;
	}

	public void setFechaDeExpiracion(Date fechaDeExpiracion) {
		this.fechaDeExpiracion = fechaDeExpiracion;
	}

	/**
	 * Exponente de la llave. Normalmente viene el valor 03, pero puede venir
	 * otro valor.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	public String getExponente() {
		return exponente;
	}

	/**
	 * Exponente de la llave. Normalmente viene el valor 03, pero puede venir
	 * otro valor.
	 * 
	 * Las llaves públicas generadas con RSA constan de un exponente y un
	 * módulo.
	 */
	public void setExponente(String exponente) {
		this.exponente = exponente;
	}

	/**
	 * Hash SHA-1 sobre la llave (Es opcional). Este valor puede no venir en el
	 * mensaje del Merchant Server, por lo tanto puede estar en null.
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Hash SHA-1 sobre la llave (Es opcional). Este valor puede no venir en el
	 * mensaje del Merchant Server, por lo tanto puede estar en null.
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * @return the length
	 */
	public String getLongitud() {
		return longitud;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLongitud(String longitud) {
		this.longitud = longitud;
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
	
	
}
