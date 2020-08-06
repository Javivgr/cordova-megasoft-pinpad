/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar el estado tamper del dispositivo
 * 
 * (TAMPER: si el dispositivo fue abierto o manipulado de alguna forma)
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanTamper extends BeanBase {

	//CONSTANTES
//	private static final String LOG = BeanVerifoneTamper.class.getName();
	
	/*TAGS*/
	private static final String TAGTAMPER = "tamper";
	
	//Atributos
	private String tamper;
	
	//Constructor
	public BeanTamper(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		tamper = (String) objectList.get(2);
		
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGTAMPER, tamper);
		
		return json;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n tamper:["+tamper+"]");
		
		return sb.toString();
	}

	
	//Getter's && Setter's
	public String getTamper() {
		return tamper;
	}

	public void setTamper(String tamper) {
		this.tamper = tamper;
	}
	
}
