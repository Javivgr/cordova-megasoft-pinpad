package ve.com.megasoft.pinpad.util;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * Purpose: Ofrece un grupo de funciones útilies para el manejo de cadenas.
 */
public class MyString {
	
	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con ceros
	 * a la izquierda.
	 */
	public static String ajustaNum(String s, int largo) {
		String ceros = StringFilled("0", largo);
		return (Right(ceros + s, largo));
	}

	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con ceros
	 * a la izquierda.
	 */
	public static String ajustaNum(long s, int largo) {
		String ceros = StringFilled("0", largo);
		return (Right(ceros + s, largo));
	}

	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con ceros
	 * a la izquierda.
	 */
	public static String ajustaNum(int s, int largo) {
		String ceros = StringFilled("0", largo);
		return (Right(ceros + s, largo));
	}

	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con
	 * espacios a la derecha.
	 */
	public static String ajustaString(String s, int largo) {
		String espacios = StringFilled(" ", largo);
		return (Left(s + espacios, largo));
	}

	/**
	 * Purpose: Devuelve un substring de la cadena s con los n caracteres que
	 * están más a la derecha.
	 */
	public static String Right(String s, int n) {
		int len = s.length();
		return s.substring(len - n);

	}

	/**
	 * Purpose: Devuelve un substring de la cadena s con los n caracteres que
	 * están más a la izquierda.
	 */
	public static String Left(String s, int n) {
		return s.substring(0, n);

	}

	/**
	 * Purpose: Devuelve un substring de la cadena s comenzando en posicion y
	 * conteniendo un número de caracteres igual a cantidad.
	 */
	public static String VBMid(String s, int posicion, int cantidad) {
		if (posicion > 0) {
			posicion = posicion - 1;
			return (s.substring(posicion).substring(0, cantidad));
		} else
			return null;

	}

	/**
	 * Purpose: Crea un string de tamaño igual a largo compuesto por
	 * repeticiones de la cadena s.
	 */
	public static String StringFilled(String s, int largo) {
		String str = "";
		for (int i = 0; i < largo; i++)
			str += s;
		return str;
	}

	/**
	 * Purspose: Devuelve la extensión del nombre de un archivo sin el punto.
	 */
	public static String getExtension(String NameFile) {
		int pos = NameFile.lastIndexOf(".") + 1; // Posición donde comienza la
													// extensión.
		if (pos == -1)
			return ""; // No tiene extensión.
		else
			return NameFile.substring(pos, NameFile.length());
	}

	/**
	 * Purpose: Devuelve el nombre del archivo sin la extensión.
	 */
	public static String delExtension(String NameFile) {
		int pos = NameFile.lastIndexOf("."); // Posición donde comienza la
												// extensión.
		if (pos == -1)
			return NameFile; // No tiene extensión.
		else
			return NameFile.substring(0, pos);
	}

	/**
	 * Replaces all occurences of one string with another string.
	 * 
	 * @param str
	 *            Source string with which to read
	 * @param from
	 *            String to search for
	 * @param to
	 *            String to replace with
	 * @return The string after the replacement
	 */
	public static String stringReplace(String str, String from, String to) {

		int pos = str.indexOf(from);
		if (pos == -1)
			return str;
		int lastPos = 0;
		StringBuffer buff = new StringBuffer();
		while (pos >= 0) {
			buff.append(str.substring(lastPos, pos));
			buff.append(to);
			lastPos = pos + from.length();
			pos = str.indexOf(from, lastPos);
		}
		buff.append(str.substring(lastPos));
		return new String(buff);
	}

	/**
	 * Split a strings delimited by token in several strings
	 * 
	 * @param String
	 *            source
	 * @param char token
	 * @return String[] array of strings
	 */
	public static String[] split(String source, char token) {
		String[] ndest;
		String[] dest = new String[10];
		int i1 = 0, i2 = 0, i = 0;
		while ((i2 = source.indexOf(token, i1)) != -1) {
			dest[i] = source.substring(i1, i2);
			i++;
			i1 = ++i2;
			if (i == dest.length) {
				ndest = new String[i + 10];
				System.arraycopy(dest, 0, ndest, 0, i);
				dest = ndest;
			}
		}
		dest[i++] = source.substring(i1);

		ndest = new String[i];
		System.arraycopy(dest, 0, ndest, 0, i);
		return ndest;
	}

	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con ceros
	 * a la derecha.
	 */
	public static String ajustaNumRight(String s, int largo) {
		String ceros = StringFilled("0", largo);
		return (Left(s + ceros, largo));
	}

	/**
	 * Purpose: Genera un string a partir de s de tamaño igual a largo con
	 * asteriscos a la derecha.
	 */
	public static String ajustaAstRight(String s, int largo) {
		String asterisco = StringFilled("*", largo);
		return (Left(s + asterisco, largo));
	}

	/**
	 * return number formatted as a string with mask applied
	 * 
	 * @param value
	 *            : value to be formated
	 * @param mask
	 *            : valid decimal number mask like #,##0.00
	 */
	public static String formatNumber(String value, String mask) {

		DecimalFormat decfmt = new DecimalFormat();
		decfmt.applyPattern(mask);
		return decfmt.format(Double.parseDouble(value)).toString();

	}
	
	
	/**
	 * Formateo de String.
	 *
	 * @param origen
	 *            Valor inicial.
	 * @param len
	 *            Longitud máxima del valor de retorno.
	 * @return String: Contiene el valor origen alineado a la izquierda con
	 *         espacios en blanco.
	 */
	public static String stringFormat(String origen, int len) {
		String destino = "";
		int fill = len - origen.length();

		destino = repeat(' ', fill) + origen;
		return destino;
	}
	
	/**
	 * Formateo de String.
	 *
	 * @param origen
	 *            Valor inicial.
	 * @param len
	 *            Longitud máxima del valor de retorno.
	 * @return String: Contiene el valor origen alineado a la izquierda con
	 *         espacios en blanco.
	 */
	public static String stringFormatRight(String origen, int len) {
		String destino = "";
		int fill = len - origen.length();

		destino = origen + repeat(' ', fill);
		return destino;
	}
	
	/**
	 * Formateo de mensaje.
	 *
	 * @param origen
	 *            Valor inicial.
	 * @param separador
	 *            Separador de palabras.
	 * @param maximo
	 *            Longitud máxima de línea.
	 * @param align
	 *            Tipo de alineación.
	 * @return String: Mensaje formateado.
	 */
	public static String formatMsg(String origen, String separador, int maximo,
			char align) {
		String texto = "";
		String palabra = "";
		String tmp = "";

		StringTokenizer itemsMsg = new StringTokenizer(origen, separador);
		while (itemsMsg.hasMoreElements()) {
			palabra = itemsMsg.nextToken() + " ";

			if (tmp.length() + palabra.length() > maximo) {
				texto += (align == 'R' ? stringFormat(tmp,
						((maximo - tmp.length()) / 2) + tmp.length()) : tmp);
				tmp = "";
			}
			tmp += palabra;
		}
		texto += (align == 'R' ? stringFormat(tmp,
				((maximo - tmp.length()) / 2) + tmp.length()) : tmp);

		return texto;
	}

	
	/**
	 * Método repeat.
	 *
	 * @param c
	 *            Caracter de relleno.
	 * @param n
	 *            Número de caracteres "c" a incluir en el string de retorno.
	 * @return String: Cadena que contiene el caracter c n veces.
	 */
	private static String repeat(char c, int n) {
		if (n <= 0) {
			return ("");
		}

		StringBuffer s = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			s.append(c);
		}
		return s.toString();
	}

}
