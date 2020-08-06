/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cordova Java Part Plugin Project
 * 
 * "Cordova-Megasoft-Pinpad"
 * 
 * bena base para todos los beans del paquete
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanBase {
	
	//CONSTANTES
//	private static final String LOG = BeanVerifoneBase.class.getName();
	
	/*TAGS*/
	private static final String TAGESTATUS = "estatus";
	private static final String TAGMENSAJE = "mensaje";
	
	//Atributos
	protected String estatus;
	protected String mensaje;
	
	//Constructores
	public BeanBase() {
		estatus = null;
		mensaje = null;
	}
	
	public BeanBase(String estatus, String mensaje){
		this.estatus = estatus;
		this.mensaje = mensaje;
	}
	
	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("\n estatus:["+estatus+"]");
		sb.append("\n mensaje:["+mensaje+"]");
	
		return sb.toString();
	}
	
	//Metodo public
	/**
	 * funcion que crea un json para ser entregado por el plugin
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJson() throws JSONException{
		JSONObject json = new JSONObject();
		
		json.put(TAGESTATUS, estatus);
		json.put(TAGMENSAJE, mensaje);
		
		return json;
	}
	
	//Getter's && Setter's
	public String getEstatus() {
		return estatus;
	}
	
	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}
	
	public String getMensaje() {
		return mensaje;
	}
	
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}
