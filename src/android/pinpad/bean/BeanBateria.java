/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar la bateria
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class BeanBateria extends BeanBase {

	//CONSTANTES
	//private static final String LOG = BeanVerifoneBateria.class.getName();
	
	/*TAGS*/
	private static final String TAGBATERIA = "bateria";
	
	//Atributos
	private String bateria;
	
	//Constructores
	public BeanBateria(){}
	
	public BeanBateria(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		bateria = (String) objectList.get(2);
		
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGBATERIA, bateria);
		
		return json;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n bateria:["+bateria+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public String getBateria() {
		return bateria;
	}

	public void setBateria(String bateria) {
		this.bateria = bateria;
	}
	
}
