/**
 * Copyright Mega Soft Computacion C.A.
 */
package ve.com.megasoft.pinpad.bean;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * bean para manejar los datos del dispositivo
 * 
 * @author Adrian Jesus Silva Simoes
 *
 */
public class BeanSerial extends BeanBase {

	//CONSTANES
//	private static final String LOG = BeanVerifoneSerial.class.getName();
	
	/*TAGS*/
	private static final String TAGSERIAL = "serial";
	private static final String TAGOS = "os";
	private static final String TAGAPP = "app";
	private static final String TAGKERNEL = "kernel";
	private static final String TAGMARCA = "marca";
	private static final String TAGMODELO = "modelo";
	private static final String TAGISPRINTER = "impresora";
	private static final String TAGISICCARD = "iccard";
	private static final String TAGISMAGCARD = "magcard";
	
	//Atributos
	private String serial;
	private String os;
	private String app;
	private String kernel;
	private String marca;
	private String modelo;
	private String isPrinter;
	private String isICCard;
	private String isMagCard;
	
	//Constructor
	public BeanSerial(){};
	
	public BeanSerial(@SuppressWarnings("rawtypes") ArrayList objectList) {
		super((String)objectList.get(0), (String)objectList.get(1));
		serial = (String) objectList.get(2);
		os = (String) objectList.get(3);
		app = (String) objectList.get(4);
		kernel = (String) objectList.get(5);
		marca = (String) objectList.get(6);
		modelo = (String) objectList.get(7);
		isPrinter = (String) objectList.get(8);
		isICCard = 	(String) objectList.get(9);
		isMagCard =	(String) objectList.get(10);
	}
	
	//Metodos Sobre Escritos
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = super.toJson();
		
		json.put(TAGSERIAL, serial);
		json.put(TAGOS, os);
		json.put(TAGAPP, app);
		json.put(TAGKERNEL, kernel);
		json.put(TAGMARCA, marca);
		json.put(TAGMODELO, modelo);
		json.put(TAGISPRINTER, isPrinter);
		json.put(TAGISICCARD, isICCard);
		json.put(TAGISMAGCARD, isMagCard);
		
		return json;
	}
	
	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(super.toString());
		sb.append("\n serial:["+serial+"]");
		sb.append("\n os:["+os+"]");
		sb.append("\n app:["+app+"]");
		sb.append("\n kernel:["+kernel+"]");
		sb.append("\n marca:["+marca+"]");
		sb.append("\n modelo:["+modelo+"]");
		sb.append("\n isPrinter:["+isPrinter+"]");
		sb.append("\n isICCard:["+isICCard+"]");
		sb.append("\n isMagCard:["+isMagCard+"]");
		
		return sb.toString();
	}
	
	//Getter's && setter's
	public String getOs() {
		return os;
	}
	
	public void setOs(String os) {
		this.os = os;
	}
	
	public String getApp() {
		return app;
	}
	
	public void setApp(String app) {
		this.app = app;
	}
	
	public String getSerial() {
		return serial;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public String getKernel() {
		return kernel;
	}
	
	public void setKernel(String kernel) {
		this.kernel = kernel;
	}
	
	public String getMarca() {
		return marca;
	}
	
	public void setMarca(String marca) {
		this.marca = marca;
	}
	
	public String getModelo() {
		return modelo;
	}
	
	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getIsPrinter() {
		return isPrinter;
	}
	
	public void setIsPrinter(String isPrinter) {
		this.isPrinter = isPrinter;
	}

	public String getIsCCard() {
		return isICCard;
	}
	
	public void setIsICCard(String isICCard) {
		this.isICCard = isICCard;
	}

	public String getIsMagCard() {
		return isMagCard;
	}
	
	public void setIsMagCard(String isMagCard) {
		this.isMagCard = isMagCard;
	}
}
