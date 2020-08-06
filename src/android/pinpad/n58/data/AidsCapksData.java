/**
 * Copyright Mega Soft Computaci�n C.A.
 */
package ve.com.megasoft.pinpad.n58.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.newpos.mpos.tools.BaseUtils;

import android.annotation.SuppressLint;
import ve.com.megasoft.pinpad.bean.BeanAidsInfo;
import ve.com.megasoft.pinpad.bean.BeanEmvKeyInfo;
import ve.com.megasoft.pinpad.util.Utils;

/**
 * 
 * clase encargada de realizar la construccion de loas aids y de los capks 
 * segun el formato requerido por el n58
 * 
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class AidsCapksData {

	//CONSTANTES
//	private static final String TAG = AidsCapksData.class.getName();
	
		/*SPECIAL TAGS*/
	private static final String separator = "3D";
	private static final String moreThat127Char = "81";
		/*TAGS*/
		/*Common*/
	private static final String applicationIdentifier     			 = "9F06";
		/*AIDS*/
	private static final String supportPartialAidSelect   			 = "DF01";
	private static final String applicationVersionNumber  			 = "9F08";
	private static final String terminalAccionCodeDefault 			 = "DF11";
	private static final String terminalAccionCodeOnline  			 = "DF12";
	private static final String terminalAccionCodeDenial  			 = "DF13";
	private static final String terminalFloorLimit        			 = "9F1B";
	private static final String thresholdValue            			 = "DF15";
	private static final String maximumTargetPercentage   			 = "DF16";
	private static final String targetPercentage                     = "DF17";
	private static final String defaultTdol                          = "DF14";
	private static final String bOblinePin                           = "DF18";
		/*CAPKS*/
	private static final String certificationAuthorityPublicKeyIndex = "9F22";
	private static final String expirationDate                       = "DF05";
	private static final String hashAlgorithm                        = "DF06";
	private static final String electronicsCashMaxTxnAmount          = "DF07"; 
	private static final String modulus                              = "DF02";
	private static final String exponent                             = "DF04";
	private static final String hash                                 = "DF03"; 
	
		/*Valores equivalentes AIDS*/
	private static final String permiteSeleccionParcial = "00"; // TipoSeleccion = 2
	private static final String noPermiteSeleccionParcial = "01"; // TipoSeleccion = 1
	
	//Metodos Privados
	/**
	 * funcion que contruye el tag solicitado 
	 * @param tag (String) tag a construir
	 * @param value (String) valor correspondiente al tag
	 * @return (String) tag solicitado
	 */
	@SuppressLint("DefaultLocale")
	private static String buildTag (String tag, String value){
		
		StringBuffer sb = new StringBuffer();
		
		//Colocamos el tag a trabajar
		sb.append(tag);
		
		//calculamos el valor hex de la longitud de bytes a manipular y si es requerido el indicador de un tamano superior a 127
		int byteLength = value.length()/2;
		if(byteLength>127){sb.append(moreThat127Char);}
		String hex = Integer.toHexString(byteLength).toUpperCase();
		hex = hex.length()==1?"0"+hex:hex;
		sb.append(hex);
		
		//colocamos el valor del tag
		sb.append(value);
		
		//retornamos el tag construido
		return sb.toString();
		
	}
	
	//Metodos Publicos
	/**
	 * funcion que prepara la informacion de los AIDS de las marcas 
	 * para su carga en el N58
	 * @param aids (List[BeanAidsInfo]) lista de aids a cargar 
	 * @return (byte[]) todos los aids formateados segun el requerimiento del n58
	 */
	public static byte[] buildAidsInfo(List<BeanAidsInfo> aids){
		
		//inicializamos los elementos requeridos para la construccion de los AIDS
		StringBuffer sb = new StringBuffer();
		
		//procesamos los aids recibidos por el servicio
		for(BeanAidsInfo aid : aids){
			//verificamos si el string buffer fue inicializado
			if(sb.length()>0){sb.append(separator);}
			
			//construimos los tags con la informacion recibida del servicio
			sb.append(buildTag(applicationIdentifier, aid.getAid()));
			sb.append(buildTag(supportPartialAidSelect, (aid.getTipoSeleccion().equals("2"))?permiteSeleccionParcial:noPermiteSeleccionParcial));
			sb.append(buildTag(applicationVersionNumber, aid.getVersionApp()));
			sb.append(buildTag(terminalAccionCodeDefault, aid.getTACDefault()));
			sb.append(buildTag(terminalAccionCodeDenial, aid.getTACDenial()));
			sb.append(buildTag(terminalAccionCodeOnline, aid.getTACOnline()));
			sb.append(buildTag(terminalFloorLimit, aid.getFloorLimit()));
			sb.append(buildTag(thresholdValue, aid.getThresholdValue()));
			sb.append(buildTag(maximumTargetPercentage,aid.getMaximumTargetPercentage() ));
			sb.append(buildTag(targetPercentage, aid.getTargetPercentage()));
			sb.append(buildTag(defaultTdol, aid.getDDOL()));
			sb.append(buildTag(bOblinePin, "00"));
		}
		
		//retornamos el AIDS construido
		return BaseUtils.hexStringToByte(sb.toString());
	}
	
	/**
	 * funcion que prepara la informacion de las llaves EMV de las marcas 
	 * para su carga en el N58
	 * @param capks (List[BeanEmvKeyInfo]) lista de llaves EMV a cargar
	 * @return (byte[]) todas las llaves EMV formateadas segun el requerimiento del N58
	 * @throws ParseException - no logro parsear la fecha recibida de la llave
	 */
	@SuppressLint("DefaultLocale")
	public static ArrayList<byte[]> buildCapksInfo(List<BeanEmvKeyInfo> capks) throws ParseException{
		//Inicializamos la lista contenedora de CAPKS
		ArrayList<byte[]> llaves = new ArrayList<byte[]>();
		
		//procesamos los capks recibidos por el servicio
		StringBuffer sb = new StringBuffer();
		for(BeanEmvKeyInfo info : capks){
			//inicializamos los elementos requeridos para la construccion de los CAPKS
			sb = new StringBuffer();
			
			//Construimos los tags con la informacion de la llave emv
			sb.append(buildTag(applicationIdentifier, info.getRid()));
			sb.append(buildTag(certificationAuthorityPublicKeyIndex, info.getIndice()));
			sb.append(buildTag(expirationDate, Utils.stringToHex(Utils.dateToString(info.getFechaDeExpiracion(), Utils.DATE_YEAR_MONTH_DAY_WITHOUT_SEPARATOR))).toUpperCase());
			
			//colocamos el resto de los datos requeridos para la llave
			sb.append(buildTag(hashAlgorithm, "01"));
			sb.append(buildTag(electronicsCashMaxTxnAmount, "01"));
			sb.append(buildTag(modulus, info.getModulo()));
			sb.append(buildTag(exponent, (info.getExponente().length()==1)?"0"+info.getExponente():info.getExponente()));
			sb.append(buildTag(hash, info.getHash()));
			
			//agregar la llave a cargar a la lista
			llaves.add(BaseUtils.hexStringToByte(sb.toString()));
		}
		
		//retornamos el AIDS construido
		return llaves;		
	}

}
