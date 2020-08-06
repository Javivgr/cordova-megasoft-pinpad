/**
 * Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar la impresora
 * 
 * @author Javier González
 *
 */
public class BeanImpresora extends BeanBase {

	

	
	/*TAGS*/
	private static final String TAGIMPRESORA = "impresora";
	
	//Atributos
	private String impresora;
	
	//Constructores
	public BeanImpresora(){}
	
	public BeanImpresora(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		impresora = (String) objectList.get(2);
		
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGIMPRESORA, impresora);
		
		return json;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n impresora:["+impresora+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public String getImpresora() {
		return impresora;
	}

	public void setImpresora(String impresora) {
		this.impresora = impresora;
	}
	
}
