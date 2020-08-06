/**
 * Copyright Mega Soft Computación C.A. 
 */
package ve.com.megasoft.pinpad.bean;

/**
 * 
 * bean que contiene una serie de datos del telefono 
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class BeanTelephoneData extends BeanBase {

	//Atributos
	/**  Mobile Country Code (2 bytes) */
	private int mcc = -1;
	
	/** Mobile Network Code (1 byte)*/
	private int mnc = -1;
	
	/** Cell Id  (4 bytes)*/
	private int cid = -1;
	
	/** Location, network id[cdma] o local area code[gsm] (4 bytes) */
	private int loc = -1;
	
	/** International Mobile Station Equipment Identity  */
	private String imei;
	
	//Metodos Sobre Escritos
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		
		sb.append("\n mcc:["+mcc+"]");
		sb.append("\n mnc:["+mnc+"]");
		sb.append("\n cid:["+cid+"]");
		sb.append("\n loc:["+loc+"]");
		sb.append("\n imei size:["+imei.length()+"]");
		
		return sb.toString();
	}
	
	//Getter's && Setter's
	public int getMcc() {
		return mcc;
	}

	public void setMcc(int mcc) {
		this.mcc = mcc;
	}

	public int getMnc() {
		return mnc;
	}

	public void setMnc(int mnc) {
		this.mnc = mnc;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public int getLoc() {
		return loc;
	}

	public void setLoc(int loc) {
		this.loc = loc;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}
	
}
