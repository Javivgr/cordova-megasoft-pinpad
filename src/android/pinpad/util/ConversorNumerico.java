package ve.com.megasoft.pinpad.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


public class ConversorNumerico {
	
	private static DecimalFormat dfNumerico = new DecimalFormat("###########0.00#");
	private static DecimalFormat dfAlfanumerico = new DecimalFormat("###,###,###,##0.00#");
	
	public ConversorNumerico (String separadorDecimales) {
		
		String miles = ".";
		if (separadorDecimales.charAt(0) == '.') {
			miles = ",";
		}
		Locale loc = (",".equals(miles)
				? new Locale("en", "US")
				: new Locale("de","DE"));
		
		String mascara = ("".equals(miles) ? "########0,00" : "########0.00");
		NumberFormat formato = NumberFormat.getNumberInstance(loc);
		dfNumerico = (DecimalFormat) formato;
		dfNumerico.applyPattern(mascara);
		
		mascara = ("".equals(miles) ? "###.###.##0,00" : "###,###,##0.00");
		formato = NumberFormat.getNumberInstance(loc);
		dfAlfanumerico = (DecimalFormat) formato;
		dfAlfanumerico.applyPattern(mascara);
		
	}
		
	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static double formatMontoDoubleP(String monto) {
		String montoTxn = monto;
		montoTxn = replace(montoTxn, ".", "");
		montoTxn = replace(montoTxn, ",", "");
		double dbAux = 0;
		try {
			dbAux = Double.parseDouble(montoTxn);
		} catch (Exception e) {
			return Double.valueOf(0);
		}
		if (dbAux == 0 || dbAux == -1) {
			return Double.valueOf(dbAux);
		}
		dbAux = dbAux / 100;
		Double dbMonto = Double.valueOf(dbAux);
		return dbMonto.doubleValue();
	}

	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static Double formatMontoDouble(String monto) {
		String montoTxn = monto;
		montoTxn = replace(montoTxn, ".", "");
		montoTxn = replace(montoTxn, ",", "");
		double dbAux = 0;
		try {
			dbAux = Double.parseDouble(montoTxn);
		} catch (Exception e) {
			return Double.valueOf(0);
		}
		if (dbAux == 0 || dbAux == -1) {
			return Double.valueOf(dbAux);
		}
		dbAux = dbAux / 100;
		Double dbMonto = Double.valueOf(dbAux);
		return dbMonto;
	}
	
	/**
	 * Formateo de monto.
	 *
	 * @param montoTxn
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static String formatMontoString(String montoTxn, boolean separadorMiles) {
		
		double dbAux = -1;
		if (montoTxn == null) {
			return montoTxn;
		}
		String montoAux = montoTxn;
		montoAux = replace(montoAux, ".", "");
		montoAux = replace(montoAux, ",", "");
		
		try {
			dbAux = Double.parseDouble(montoAux);
		} catch (Exception e) {
		}
		if (dbAux == -1) {
			return montoTxn;
		}
		dbAux = dbAux / 100;

		String monto = "";
		if (separadorMiles) {
			monto = dfAlfanumerico.format(dbAux);
		} else {
			monto = dfNumerico.format(dbAux);
		}
		return monto;
	}

	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static String formatMontoString(double dbMonto) {

		String montoTxn = dfNumerico.format(dbMonto);
		return montoTxn;
	}


	public static String formatMontoEntero(double dbMonto) {

		String montoAux = dfNumerico.format(dbMonto);
		montoAux = replace(montoAux, ".", "");
		montoAux = replace(montoAux, ",", "");
		return montoAux;
	}

	public static String formatMontoEntero(String monto) {

		monto = replace(monto, ".", "");
		monto = replace(monto, ",", "");
		return monto;
	}

	/**
	 * Modifica el monto para usar los dos últimos dígitos como decimales, si el
	 * monto viene como un entero. Cuando viene un monto como si fuera un
	 * entero, desde el Wrapper .net o desde un sistConfiguracion externo, se deben usar
	 * los dos últimos dígitos del número entero como si fueran en realidad dos
	 * decimales. Se le agrega un separador decimal.
	 *
	 * Si es un solo dígito se toma como centésima o céntimos, si son dos
	 * dígitos se toma como décima y céntimo.
	 *
	 * Si el monto viene con el separador decimal configurado en
	 * miscelaneos->configuración no se realiza la modificación.
	 *
	 * @param monto
	 *            Monto a modificar, si fuera necesario.
	 * @param vpos
	 *            Componente vpos.
	 * @return Monto modificado, si era entero; el mismo monto de entrada si
	 *         tenía el separador decimal configurado en
	 *         miscelaneos->configuración
	 */
	public static String colocarDosDecimalesSiElMontoEsEntero(String monto,
			String separadorDecimal ) {
		String montoRetorno = monto;
		if (monto != null && monto.length() > 0
				&& monto.indexOf(separadorDecimal) < 0) {
			switch (monto.length()) {
			case 1:
				montoRetorno = "0" + separadorDecimal + "0" + monto;
				break;
			case 2:
				montoRetorno = "0" + separadorDecimal + monto;
				break;
			default:
				int l = montoRetorno.length() - 2;
				montoRetorno = montoRetorno.substring(0, l) + separadorDecimal
						+ montoRetorno.substring(l);
			}
		}
		return montoRetorno;
	}
	
	/**
	 * Reemplaza una cadena de caracteres por otra sobre una cadena de
	 * caracteres.
	 *
	 * @param origen
	 *            Cadena original.
	 * @param patron
	 *            Patrón a reemplazar.
	 * @param reemplazo
	 *            Patrón de reemplazo.
	 * @return String: Cadena con valor reemplazado.
	 */
	public static String replace(String origen, String patron, String reemplazo) {

		if (origen != null) {
			final int len = patron.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int inicio = 0;

			while ((found = origen.indexOf(patron, inicio)) != -1) {
				sb.append(origen.substring(inicio, found));
				sb.append(reemplazo);
				inicio = found + len;
			}
			sb.append(origen.substring(inicio));
			return sb.toString();

		} else {
			return "";
		}
	}
	
	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static Integer validarInteger(String monto, Integer valorDefecto) {
		String montoTxn = monto;
		montoTxn = replace(montoTxn, ".", "");
		montoTxn = replace(montoTxn, ",", "");
		int intAux = 0;
		try {
			intAux = Integer.parseInt(montoTxn);
		} catch (Exception e) {
			return valorDefecto;
		}
		if (intAux == 0 || intAux == -1) {
			return Integer.valueOf(intAux);
		}
		return intAux;
	}
	
	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static Long validarLong(String monto, Long valorDefecto) {
		String montoTxn = monto;
		montoTxn = replace(montoTxn, ".", "");
		montoTxn = replace(montoTxn, ",", "");
		long lgAux = 0;
		try {
			lgAux = Long.parseLong(montoTxn);
		} catch (Exception e) {
			return valorDefecto;
		}
		if (lgAux == 0 || lgAux == -1) {
			return Long.valueOf(lgAux);
		}
		return lgAux;
	}

	/**
	 * Formateo de monto.
	 *
	 * @param monto
	 *            Monto a formatear.
	 * @return Double: Monto formateado.
	 */
	public static Double validarDouble(String monto, Double valorDefecto) {
		String montoTxn = monto;
		montoTxn = replace(montoTxn, ".", "");
		montoTxn = replace(montoTxn, ",", "");
		double dbAux = 0;
		try {
			dbAux = Double.parseDouble(montoTxn);
		} catch (Exception e) {
			return valorDefecto;
		}
		if (dbAux == 0 || dbAux == -1) {
			return Double.valueOf(dbAux);
		}
		dbAux = dbAux / 100;
		Double dbMonto = Double.valueOf(dbAux);
		return dbMonto;
	}
}
