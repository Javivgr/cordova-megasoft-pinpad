/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar los datos de las tarjetas
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class BeanTarjeta extends BeanBase {

	//CONSTANTES
//	private static final String LOG = BeanVerifoneTarjeta.class.getName();
	
	/*TAGS*/
	private static final String TAGOBFPAN="obfuscated_pan";
	private static final String TAGCRDHLDNM="cardholder_name";
	private static final String TAGENCTRCK = "track_2_data";
	private static final String TAGKSNTRCK = "track_2_ksn";
	private static final String TAGSERVICECODE = "service_code";
	private static final String TAGEXTRATIONMODE = "extration_mode";
	private static final String TAGTLV="tlv";
	
	//Atributos
	private boolean flag = true;
	private String tlv;
	private String obfuscatedPan;
	private String cardholderName;
	private String track2Data;
	private String track2Ksn;
	private String serviceCode;
	private String extrationMode;
	
	//Constructor
	public BeanTarjeta(boolean flag){this.flag = flag;}
	
	public BeanTarjeta(@SuppressWarnings("rawtypes") ArrayList objectList, boolean emv) {
		
		super((String)objectList.get(0), (String)objectList.get(1));
		
		if(emv){
			tlv = (String) objectList.get(2);
			obfuscatedPan = (String) objectList.get(3);
			cardholderName = (String) objectList.get(4);
			track2Data = (String) objectList.get(5);
			track2Ksn = (String) objectList.get(6);
			serviceCode = (String) objectList.get(7);
			extrationMode = (String) objectList.get(11);
		}
		else{
			obfuscatedPan = (String) objectList.get(2);
			cardholderName = (String) objectList.get(3);
			track2Data = (String) objectList.get(4);
			track2Ksn = (String) objectList.get(5);
			serviceCode = (String) objectList.get(6);
			tlv = null;
			extrationMode = "B";
		}
		
		flag = true;
	}
	
	public BeanTarjeta(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		
		tlv = (String) objectList.get(2);
		
		flag = false;
	}

	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		if(flag){
			json.put(TAGOBFPAN, obfuscatedPan);
			json.put(TAGCRDHLDNM, cardholderName);
			json.put(TAGENCTRCK, track2Data);
			json.put(TAGKSNTRCK, track2Ksn);
			json.put(TAGSERVICECODE, serviceCode);
			json.put(TAGEXTRATIONMODE, extrationMode);
		}
		if(tlv!=null){json.put(TAGTLV, tlv);}
		
		return json;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		if(obfuscatedPan!=null){sb.append("\n obfuscatedPan:["+obfuscatedPan+"]");}
		if(cardholderName!=null){sb.append("\n cardholderName:["+cardholderName+"]");}
		if(track2Data!=null){sb.append("\n track2Data size:["+track2Data.length()+"]");}
		if(track2Ksn!=null){sb.append("\n track2Ksn size:["+track2Ksn.length()+"]");}
		if(serviceCode!=null){sb.append("\n serviceCode:["+serviceCode+"]");}
		if(extrationMode!=null){sb.append("\n extrationMode:["+extrationMode+"]");}
		if(tlv!=null){sb.append("\n tlv:["+tlv+"]");}
		
		return sb.toString();
	}

	//Getter's && Setter's
	public String getObfuscatedPan() {
		return obfuscatedPan;
	}

	public void setObfuscatedPan(String obfuscatedPan) {
		this.obfuscatedPan = obfuscatedPan;
	}

	public String getCardholderName() {
		return cardholderName;
	}

	public void setCardholderName(String cardholderName) {
		this.cardholderName = cardholderName;
	}

	public String getTrack2Data() {
		return track2Data;
	}

	public void setTrack2Data(String track2Data) {
		this.track2Data = track2Data;
	}

	public String getTrack2Ksn() {
		return track2Ksn;
	}

	public void setTrack2Ksn(String track2Ksn) {
		this.track2Ksn = track2Ksn;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	
	public String getTlv() {
		return tlv;
	}

	public void setTlv(String tlv) {
		this.tlv = tlv;
	}

	public String getExtrationMode() {
		return extrationMode;
	}

	public void setExtrationMode(String extrationMode) {
		this.extrationMode = extrationMode;
	}
	
	
}
