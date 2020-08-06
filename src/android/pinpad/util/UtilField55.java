package ve.com.megasoft.pinpad.util;

import java.math.BigInteger;
import android.annotation.SuppressLint;
import emv.BerTlvChain;


public class UtilField55 {
	

	/** Indica que los Tag del campo55 estan todos en 4 digitos */
	public static final boolean initCerocampo55 = true;
	
	/**
	 * @param field55
	 *            : the field55 to set
	 */	public static BerTlvChain createField55(String campo55)
		throws Exception {
		
		BerTlvChain field55 = new BerTlvChain();
		try {
			field55.createChain(campo55);
		} catch (Exception e) {
			throw new Exception("BN");
		}
		return field55;
	}

	/**
	 * @param field55
	 *            : the field55 to set
	 */
	public static BerTlvChain createField55(String campo55, boolean parseStream)
		throws Exception {
		BerTlvChain field55 = new BerTlvChain();
		try {
			campo55 = field55.parserStream(campo55);
			field55.createChain(campo55);
		} catch (Exception e) {
			throw new Exception("BN");
		}
		return field55;
	}
	
	/**
	 * @param field55
	 *            : the field55 to set
	 */
	public static BerTlvChain createField55(BerTlvChain field55, String campo55)
		throws Exception {
		try {
			field55 = new BerTlvChain();
			field55.createChain(campo55);
		} catch (Exception e) {
			throw new Exception("BN");
		}
		return field55;
	}

	/**
	 * @param field55
	 *            : the field55 to set
	 */
	public static BerTlvChain createEmptyField55(BerTlvChain field55) throws Exception {
		try {
			field55 = new BerTlvChain();
			field55.emptyField55();
			return field55;
		} catch (Exception e) {
			throw new Exception("BN");
		}
	}


	/**
	 * Metodo encargado de extraer el valor de un tag especifico del Campo 55
	 *
	 * @param nameTag
	 *            : Tag a recuperar
	 * @return String con los datos del Tag requerido
	 */
	public static String getElementValue(BerTlvChain field55, String nameTag) {

		String tagValue = field55.getTlv(nameTag).getString();
		return tagValue;
	}



	/**
	 * Metodo encargado de asignar el valor de un tag especifico del Campo 55
	 *
	 * @param nameTag
	 *            : Tag a procesar
	 * @param value
	 *            : valor asignado al Tag
	 */
	public static void setElementValue(BerTlvChain field55, String nameTag, String value) {
		field55.setElementValue(nameTag, value);
	}
	
	public static String[] getFinishField55(BerTlvChain field55, boolean parseStream)  {

		String[] campos = null;
		
		try {
			if (field55 == null) {
				field55 = UtilField55.createEmptyField55(field55);
			}
		} catch (Exception e) {
			campos = null;
		}
		try {
			campos = new String[3];
			StringBuffer message = new StringBuffer();
			if (parseStream) {
				message.append("00");
			}
			message.append("91");
			String length = Integer.toHexString(field55.getTlv("91")
					.getString().length() / 2);
			if (length.length() == 1)
				length = "0" + length;
			message.append(length);
			message.append(field55.getTlv("91").getString());
			campos[0] = message.toString();

			message = new StringBuffer(); 
			if (parseStream) {
				message.append("00");
			}
			message.append("71");
			length = Integer.toHexString(field55.getTlv("71").getString()
					.length() / 2);
			if (length.length() == 1)
				length = "0" + length;
			message.append(length);
			message.append(field55.getTlv("71").getString());
			campos[1] = message.toString();

			message = new StringBuffer(); 
			if (parseStream) {
				message.append("00");
			}
			message.append("72");
			length = Integer.toHexString(field55.getTlv("72").getString()
					.length() / 2);
			if (length.length() == 1)
				length = "0" + length;
			message.append(length);
			message.append(field55.getTlv("72").getString());
			campos[2] = message.toString();
		} catch (Exception e) {
			campos = null;
		}

		return campos;
	}


	/**
	 * Metodo encargado de la contruccion de una cadena de bit's partiendo de un
	 * texto String cualquiera.
	 *
	 * @param text
	 * @return
	 */
	public static String toBitString(String text) {
		BigInteger bi = new BigInteger(text, 16);
		String result = bi.toString(2);

		while (result.length() < 8) {
			result = "0" + result;
		}

		return result;
	}


	/**
		Purpose: Convierte un String hexadecimal en un arreglo de bytes.
		@param String str es el String hexadecimal a convertir.
		@return un arreglo de bytes.
		@throws NumberFormatException en caso de que el String tenga algún caracter no hexadecimal.
	*/
	public static byte[] hexStr_bytes(String str) throws NumberFormatException {
		//Comprueba que la longitud del String sea par.
		if (str.length() % 2 != 0) {
			str = "0" + str;	//Lo convierte en par sin alterar el número.
		}
		int len = str.length() / 2 ;
		byte[] all_no = new byte[len] ;

		// Start a loop which reads two chars at a time in a string
		for ( int i = 0, j = 0; i < len ; i++,j+=2 ) {
			// read two chars a time in two_chars string variable
			all_no[i] = (byte)Integer.parseInt ( str.substring(j,j+2), 16 ) ;
		}
		return all_no;
	}

	/**
		Purpose: Convierte un entero en un arreglo de bytes.
		@param int entero a convertir.
		@return un arreglo de bytes.
	*/
	public static byte[] int_bytes(int entero){
		try {
			return hexStr_bytes(Integer.toHexString(entero));
		} catch (Exception e) {
			//No debería ocurrir una exception...
			return null;
		}
	}


	/**
		Purpose: Convierte un arreglo de bytes a un String Hexadecimal.
		@param byte[] array de bytes.
		@return un String hexadecimal.
	*/
	@SuppressLint("DefaultLocale")
	public static String  bytes_hexStr( byte[] array ){
		StringBuffer str = new StringBuffer( 512 );

		for (int i = 0; i < array.length; i++)
		{
			str.append( MyString.ajustaNum( Integer.toHexString( (int)array[i] ), 2 ) );
		}
		return str.toString().toUpperCase();
	}

	/**
	 * Purpose: Convierte cada caracter de s con una representación hex
	 * de dos caracteres en una cadena con su representación ascii equivalente.
	 * Por ejemplo, la cadena "314B" es convertida a "1K".
	 * La longitud de s debe ser par.
	 */
	public static String hexStr_ascii(String s) throws Exception{
		if (s==null)
			return null;

		int len = s.length()/2;
		byte[] array = new byte[len];
		for (int i = 0, j = 0; i < s.length(); i += 2, j++) {
			array[j] = Integer.valueOf(s.substring(i,i+2),16).byteValue();
		}
		return new String(array,"UTF-8");
	}

	/**
	 * Purpose: Convierte una cadena ascii a su representación hex.
	 */
	public static String ascii_hexStr(String s) throws Exception{
		if (s==null)
			return null;

		return bytes_hexStr(s.getBytes());
	}

	/**
		Purpose: Convierte un arreglo de bytes a un entero.
		@param byte[] array de bytes.
		@return un String hexadecimal.
	*/
	public static int bytes_int(byte[] array)throws NumberFormatException {
		String temp = bytes_hexStr(array);
		return Integer.parseInt(temp,16);
	}
}
