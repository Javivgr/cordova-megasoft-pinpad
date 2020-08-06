/**
 * 
 */
package ve.com.megasoft.pinpad.util;

import java.util.Arrays;

import android.util.Log;
import emv.BerTlvChain;
import ve.com.megasoft.pinpad.interfaz.CODIGOS_CONDICION;

/**
 * 
 * clase para el procesamiento de informacion con respecto 
 * a la tarjeta 
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class Tarjeta {

	//CONSTANTES
	private static final String TAG = Tarjeta.class.getName();
	
	private static final int MAXIMO_LEN = 10;
	private static final char TRACK1_DELIMITADOR_NOMBRE = '^';
	private static final char TRACK2_SEPARADOR_PINPAD = 'D';
	private static final char TRACK2_SEPARADOR = '=';
	private static final int TRACK2_POS_SERVICE_CODE = 5;

	//Metodos Privados

	/**
	 * determina la longitud de caracteres a ser enmascarados
	 * @param numeroTarjeta (String) numero de tarjeta
	 * @return (int) longitud 
	 */
	private static int determinarLenPAN(String numeroTarjeta) {
		int len;		
		if (numeroTarjeta.length() > MAXIMO_LEN) {
			len = numeroTarjeta.length() - MAXIMO_LEN;
		} 
		else {
			len = 0;
		}
		return len;
	}
	
	/**
	 * 
	 * @param numeroTarjeta
	 * @return
	 */
	private static int determinarLenMaximo(String numeroTarjeta) {		
		if (numeroTarjeta.length() > MAXIMO_LEN) {return MAXIMO_LEN;} 
		else {return numeroTarjeta.length();}
	}
	
	//Metodos Publicos
	/**
	 * funcion que extrae los datos de la informacion contenida en el track2 
	 * de la tarjeta 
	 * @param track2 (String) datos de la tarjeta 
	 * @return (String[]) datos extraidos.
	 */
	public static String[] extraerDatosTrack2(String track2){
		Log.i(TAG, "Extrayendo datos del track 2");
		String[] campos = {"",""};
		
		track2 = track2.replace(TRACK2_SEPARADOR_PINPAD, TRACK2_SEPARADOR);
		int index = track2.indexOf(TRACK2_SEPARADOR);
		if(index == -1){return campos;}
		
		String numeroTarjeta = track2.substring(0, index);
		String fechaVecmto = track2.substring(index + 1, index + 5);

		campos[0] = numeroTarjeta;
		campos[1] = fechaVecmto; 
		return campos;
	}
	
	/**
	 * funcion que enmascar el pan de la tarjeta
	 * @param numeroTarjeta (String) numero de tarjeta a enmascarar 
	 * @return (String) pan enmascarado
	 */
	public static String obtenerPanEnmascarado(String numeroTarjeta) {
		Log.i(TAG, "Enmascarando Numero de tarjeta");
		
		//determinamos la cantidad de caracteres a enmascarar
		String panEnmascarado = numeroTarjeta;
		int len = determinarLenPAN(numeroTarjeta);

		//enmascaramos los caracteres indicados
		char[] complemento = new char[len];
		Arrays.fill(complemento, '*');

		//Armamos la respuesta enmascarada y la entregamos
		if (panEnmascarado.length() > 5) {
			panEnmascarado = panEnmascarado.substring(0, 6)
					+ new String(complemento)
					+ panEnmascarado.substring(len + 6, len + determinarLenMaximo(numeroTarjeta));
		}
		return panEnmascarado;
	}

	/**
	 * funcion que extrae el service code de la tarjeta
	 * @param track2 (String) track 2 de la tarejta
	 * @return (String) service code recuperado
	 */
	public static String extraerServiceCode(String track2) {
		Log.i(TAG, "Obteniendo Service Code de la Tarjeta");
		
		String tipoTipoTarjeta = "";
		
		//ubicamos el separador
		int index = track2.indexOf(TRACK2_SEPARADOR);
		
		//calculo de inicio y fin de service code
		int posSC = index + TRACK2_POS_SERVICE_CODE;
		int posSCF = index + TRACK2_POS_SERVICE_CODE + 3;
		
		//si la pos final es mayor se entrega blanco
		if (posSCF > track2.length()) { return tipoTipoTarjeta;}
		
		//extraemos y entregamos el service code
		String serviceCode = track2.substring(posSC, posSCF);
		return serviceCode;
	}
	
	/**
	 * permite recuperar el campo 55 de una cadena de caracteres 
	 * @param respuesta (String) cadena de caracteres que contiene el campo 55
	 * @return (BerTlvChain) campo 55 (tlv) de la peticion de primer certificado
	 * @throws Exception - excepciones procesando dicho tlv 
	 */
	public static BerTlvChain obtenerTlv(String respuesta, boolean initCeroTag) throws Exception{
		//Verificamos si la respuesta esta correcta para su procesamiento
		int index = Integer.valueOf(respuesta.substring(5,8)).intValue();
		if((8+index) > respuesta.length()){throw new Exception(CODIGOS_CONDICION.TRX_REQUEST_INCORRECTO+" campos del request incorrecto");}
		
		//recuperamos el tlv obtenido de la tarjeta
		String campo55 = "";
		BerTlvChain tlv = null;
		try{
			campo55 = respuesta.substring(8, (8+index));
			tlv = UtilField55.createField55(campo55, initCeroTag);
		}
		catch(Exception e){
			Log.e(TAG, "No se proceso la respuesta del comando E01, error: ",e);
			throw new Exception(CODIGOS_CONDICION.TRX_REQUEST_INCORRECTO+" campos del request incorrecto");
		}
		
		return tlv;
	}
	
	/**
	 * funcion que retorna el campo 55 recuperado de la respuesta
	 * @param respuesta (String) respuesta obtenida del pinpad 
	 * @return (String) campo55 del comando
	 * @throws Exception
	 */
	public static String obtenerCampo55(String respuesta) throws Exception{
		//Verificamos si la respuesta esta correcta para su procesamiento
		int index = Integer.valueOf(respuesta.substring(5,8)).intValue();
		if((8+index) > respuesta.length()){throw new Exception(CODIGOS_CONDICION.TRX_REQUEST_INCORRECTO+" campos del request incorrecto");}
		
		//recuperamos el tlv obtenido de la tarjeta
		return respuesta.substring(8, (8+index));
	}
	
	/**
	 * funcion que recupera el track2 del tlv de una tarjeta chip
	 * @param tlv (BerTlvChain) tlv que contiene el campo 55
	 * @return (String) track2 recuperado
	 * @throws Exception - cuando se logra recuperar el mismo
	 */
	public static String obtenerTrack2(BerTlvChain tlv) throws Exception{
		try{
			if(tlv.get("57") == null){throw new Exception("Problemas con el tag 57 (track2), tlv: "+tlv.getString(true));}
			String track2 = tlv.getTlv("57").getString();
			return track2.replace('D', '=');
		}
		catch(Exception e){
			Log.e(TAG, "No se pudo obtener el track2, error: ",e);
			throw new Exception(CODIGOS_CONDICION.TRX_REQUEST_INCORRECTO+" campos del request incorrecto");
		}
	}

	/**
	 * funcion que extrae el track 1 de la respuesta del comando
	 * @param respuesta (String) respuesta del comando 
	 * @return (String) track 1 encontrado
	 */
	public static String obtenerTrack1(String respuesta){
		String track1 = "";
		if(respuesta.indexOf("%")>0 && respuesta.indexOf("?")>0){
			int indexEnd =  respuesta.indexOf("?");
			track1 = respuesta.substring(respuesta.indexOf("%")+1,indexEnd);
		}
		
		return track1;
	}

	/**
	 * funcion que extrae el nombre del  tarjetahabiente 
	 * del track 1 de la tarjeta
	 * @param track1 (String) track 1 de la tarjeta
	 * @return (String) nombre del tarjetahabiente
	 */
	public static String extrarDatosTrack1(String track1) {

		String nombreTarjetahabiente = "";
		int index = track1.indexOf(TRACK1_DELIMITADOR_NOMBRE);
		if (index > -1 && index + 2 <= track1.length()) {
			nombreTarjetahabiente = track1.substring(index + 1);
			if(nombreTarjetahabiente.indexOf(TRACK1_DELIMITADOR_NOMBRE)>-1){
				nombreTarjetahabiente = nombreTarjetahabiente.substring(0, nombreTarjetahabiente.indexOf(TRACK1_DELIMITADOR_NOMBRE));
			}
			nombreTarjetahabiente = nombreTarjetahabiente.replace('_', ' ');
		}
		return nombreTarjetahabiente;
	}
}
