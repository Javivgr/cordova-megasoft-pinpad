/**
 * Mega Soft Computación C.A.
 */
package ve.com.megasoft.pinpad.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * clase de utilitarios, comportamientos comunes para todas las clases
 * 
 * @author Adrian Jesus Silva Simoes 
 *
 */
public class Utils {

	//CONSTANTES
//	private static final String TAG = Utils.class.getName();
	
	/*Formateo de fecha */
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_FORMAT_WITHOUT_SEPARATOR = "yyyyMMddHHmmss";
	public static final String DATE_DAY_MONTH_2_DIGITS_YEAR_WITHOUT_SEPARATOR = "ddMMyy";
	public static final String DATE_YEAR_MONTH_DAY_WITHOUT_SEPARATOR = "yyyyMMdd";
	
	//Metodos Publicos
	/*formatemo de fechas*/
	/**
	 * funcion que entrega una fecha formateada 
	 * @param fecha (Date) fecha a formatear
	 * @param format (String) formato a usar
	 * @return (String) fecha formateada
	 */
	public static String dateToString(Date fecha, String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.getDefault());
		return sdf.format(fecha);
	}
	
	/**
	 * funcion que recupera una fecha de una cadena de caracteres
	 * @param date (String) fecha a recuperar
	 * @param format (String) formato a usar
	 * @return (Date) fecha recuperada
	 * @throws ParseException - por no poder parcear
	 */
	public static Date stringToDate(String date, String format) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		return sdf.parse(date);
	}
	
	/*conversores*/
	/**
	 * conversor de hexadecimal a bytes 
	 * @param hex (String) representacion hexadecimal
	 * @return (byte[]) arreglo de bytes recuperados
	 */
	public static byte[] hexToBytes(String hex){
		int len = hex.length();
		byte[] data = new byte[len/2];
		for(int i = 0; i<len; i+=2){
			data[i/2] = (byte)((Character.digit(hex.charAt(i), 16)<<4)+Character.digit(hex.charAt(i+1),16));
		}
		return data;
	}
	
	/**
	 * conversor de bytes a su representacion Hexadecimal
	 * @param bytes (byte[]) arreglo de bytes a parcear
	 * @return (String) representacion Hex.
	 */
	public static String bytesToHex(byte[] bytes){
		final char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChar = new char[bytes.length * 2];
		for(int i=0; i<bytes.length; i++){
			int v = bytes[i] & 0xFF;
			hexChar[i*2] = hexArray[v >>> 4];
			hexChar[i*2+1] = hexArray[v & 0x0F];
		}
		return new String(hexChar);
	}
	
	/**
	 * Conversor de caracterea a hexadecimal
	 * @param msg (String) cadena a convertir a hex
	 * @return (String) hexadecimal de la cadena 
	 */
	public static String stringToHex(String msg){
		return bytesToHex(msg.getBytes());
	}
	
	/**
	 * Recupera el mensaje del hexadecimal
	 * @param hex (String) hexadecimal con el mensaje a recuperar
	 * @return (String) mensaje recuperado
	 */
	public static String hexToString(String hex){
		return new String(hexToBytes(hex));
	}
	
	/* almacenamiento compartido */
	/**
 	 * funcion que almacena informacion en disco
 	 * @param key (String) llave de identificacion de valor 
 	 * @param data (String) valor a guardar
 	 */
 	public static void saveTmpData(String key, String data, SharedPreferences sp){
 		Editor editor = sp.edit();
 		editor.putString(key, data);
 		editor.commit();
 	}
 	
 	/**
 	 * funcion que recupera informacion del disco
 	 * @param key (String) llave de identificacion de valor 
 	 * @return (String) valor recuperado o null
 	 */
 	public static String getTmpData(String key, SharedPreferences sp){
 		return sp.getString(key, null);
 	}
 	
 	/**
 	 * procedimiento que limpia toda la data almacenada en disco
 	 */
 	public static void clearTmpData(SharedPreferences sp){
 		Editor e = sp.edit();
 		e.clear();
 		e.commit();
 	}
 	
 	/**
 	 * procedimiento que permite remover un dato almacenado en disco
 	 * @param key (String) llave de identificacion de valor 
 	 */
 	public static void removeTmpData(String key, SharedPreferences sp){
 		Editor e = sp.edit();
 		e.remove(key);
 		e.commit();
 	}
	
}
