/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manupular la fecha
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class BeanFecha extends BeanBase{

	//CONSTANTES
//	private static final String LOG = BeanVerifoneFecha.class.getName();

	/*TAGS*/
	private static final String TAGFECHA = "fecha";
	
	//Atributos
	private String fecha;
	
	//Constructor
	public BeanFecha(){};
	
	public BeanFecha(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		fecha = (String)objectList.get(2);
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		
		JSONObject json = super.toJson();
		
		json.put(TAGFECHA, fecha);
		
		return json;
		
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n fecha:["+fecha+"]");
		
		return sb.toString();
	}
		
	//gette's && setter's
	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	
}
