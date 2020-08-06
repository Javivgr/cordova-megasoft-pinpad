/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar los datos de los pinblock
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class BeanPinblock extends BeanBase {

	//CONSTANTES
//	private static final String LOG = BeanVerifonePinblock.class.getName();
	
	/*TAGS*/
	private static final String TAGENCPINBLOCK = "pinblock_data";
	private static final String TAGKSNPINBLOCK = "pinblock_ksn";
	
	//Atributos
	private String pinblockData;
	private String pinblockKsn;
	
	//Constructor
	public BeanPinblock(){}
	
	public BeanPinblock(@SuppressWarnings("rawtypes") ArrayList objectList, boolean emv) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		if(emv){
			pinblockData = (String) objectList.get(8);
			pinblockKsn = (String) objectList.get(9);
		}
		else{
			pinblockData = (String) objectList.get(2);
			pinblockKsn = (String) objectList.get(3);
		}
		
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		// TODO Auto-generated method stub
		JSONObject json = super.toJson();
		
		json.put(TAGENCPINBLOCK, pinblockData);
		json.put(TAGKSNPINBLOCK, pinblockKsn);
		
		return json;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n pinblockData size:["+pinblockData.length()+"]");
		sb.append("\n pinblockKsn size:["+pinblockKsn.length()+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public String getPinblockData() {
		return pinblockData;
	}

	public void setPinblockData(String pinblockData) {
		this.pinblockData = pinblockData;
	}

	public String getPinblockKsn() {
		return pinblockKsn;
	}

	public void setPinblockKsn(String pinblockKsn) {
		this.pinblockKsn = pinblockKsn;
	}
}
