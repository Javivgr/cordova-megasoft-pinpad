
package ve.com.megasoft.pinpad.n58.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

import com.newpos.app.AppContext;
import com.newpos.mpos.tools.BCDUtils;
import com.newpos.mpos.tools.BaseUtils;

/**
 * transacciones función @ resolver (información de transacciones para el formato TLV estándar EMV ) devueltos
 * @author panjp
 */
public class TransactionData {
	
	//CONSTANTES
	//logs
//	private static final String TAG = TransactionData.class.getName();
	
	private final static int applicationLabelTag                    = 0x50;
	private final static int track1Tag                              = 0x56; // track 1
    private final static int track2Tag                              = 0x57; // track2
    private final static int cardIdTag                              = 0x5A; // número de tarjeta
    private final static int appProfileTag                          = 0x82;
    private final static int dFNameTag                              = 0x84;
    private final static int authCodeTag                            = 0x89; // Código de autorización
    private final static int authorisationResponseCodeTag           = 0x8A;
    private final static int tvrTag                                 = 0x95; // tvr
    private final static int pinTag                                 = 0x99; //Pinblock capturado
    private final static int dateTag                                = 0x9A; // fecha
    private final static int tsiTag                                 = 0x9B;
    private final static int transactionTypeTag                     = 0x9C;
    private final static int cardHolderNameTag                      = 0x5F20;
    private final static int appExpirationDateTag                   = 0x5F24;
    private final static int issuerCountryCodeTag                   = 0x5F28;
    private final static int currencyTag                            = 0x5F2A;   // El código de moneda
    private final static int serviceCodeTag                         = 0x5F30;
    private final static int appPrimaryAccountNSequenceNoTag        = 0x5F34;
    private final static int amountTag                              = 0x9F02; // dinero - monto
    private final static int otherAmountTag                         = 0x9F03; // segundo monto
    private final static int appIdentifierTerminalTag               = 0x9F06;
    private final static int appUsageControlTag                     = 0x9F07;
    private final static int appVersionNumberTag                    = 0x9F09;
    private final static int issuerActionCodeDefaultTag             = 0x9F0D;
    private final static int issuerActionCodeDenialTag              = 0x9F0E;
    private final static int issuerActionCodeOnlineTag              = 0x9F0F;
    private final static int issuerAppDataTag                       = 0x9F10;
    private final static int appPreferredNameTag                    = 0x9F12;
    private final static int merchantTag                            = 0x9F16;// Número de negocios
    private final static int terminalCountryCodeTag                 = 0x9F1A;
    private final static int terIdTag                               = 0x9F1C;// Nº Terminal
    private final static int interfaceDeviceSNTag                   = 0x9F1E;
    private final static int timeTag                                = 0x9F21; // tiempo
    private final static int apCryptogramTag                        = 0x9F26;
    private final static int cryptogramInformationDataTag           = 0x9F27;
    private final static int terminalCapabilitiesTag                = 0x9F33;
    private final static int cardHolderVerificationMethodResultsTag = 0x9F34;
    private final static int terminalTypeTag                        = 0x9F35;
    private final static int appTransactionCounterTag               = 0x9F36;
    private final static int unpredictableNumberTag                 = 0x9F37;
    private final static int posEntryModeTag                        = 0x9F39;
    private final static int additionalTerminalCapabilitiTag        = 0x9F40;
    private final static int serialNoTag                            = 0x9F41; // número de serie
    private final static int locationTag                            = 0x9F4E;
    private final static int CTCILTag                               = 0x9F53;
    //#############
    private final static int batchIdTag                             = 0xFFF1; // Número de lote
    private final static int sysRefNoTag                            = 0xFFF2;  // Número de referencia del sistema
    private final static int oldSerialNoIdTag                       = 0xFFF3; // El número de serie de la transacción original

    //atributos
    private String applicationLabel;
    private String track1;
    private String track2;
    private String cardId;
    private String appProfile;
    private String dFName;
    private String authorisationResponseCode;
    private String tvr;
    private String pin;
    private String tsi;
    private String transactionType;
    private String cardHolderName;
    private String appExpirationDate;
    private String issuerCountryCode;
    private String currency;
    private String serviceCode;
    private String appPrimaryAccountNSequenceNo;
    private String amount;
    private String otherAmount;
    private String appIdentifierTerminal;
    private String appUsageControl;
    private String appVersionNumber;
    private String issuerActionCodeDefault;
    private String issuerActionCodeDenial;
    private String issuerActionCodeOnline;
    private String issuerAppData;
    private String appPreferredName;
    private String merchantId;
    private String terminalCountryCode;
    private String terminalId;
    private String interfaceDeviceSN;
    private String time;
    private String apCryptogram;
    private String cryptogramInformationData;
    private String terminalCapabilities;
    private String cardHolderVerificationMethodResults;
    private String terminalType;
    private String appTransactionCounter;
    private String unpredictableNumber;
    private String posEntryMode;
    private String additionalTerminalCapabiliti;
    private String location;
    private String CTCIL;
    //fin
    private String batchId;
    private String serialNo;
    private String date;
    private String authCode;
    private String sysRefNo;
    private String oldSerialNo;
    

    //Metodos Privados
    private String fillWord(String src, String word) {
        int len = src.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i += 2) {
            sb.append(src.substring(i, i + 2));
            sb.append(word);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    //Metodos Sobre Escritos
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer();
        if (!"".equals(cardId)) {sb.append("Tarjeta: ["+cardId+"] \n");}
        if (!"".equals(amount)) { 
            BigDecimal bd1 = new BigDecimal(Double.toString(Double.parseDouble(amount)));
            BigDecimal bd2 = new BigDecimal(Double.toString(0.01));
            String amountStr = bd1.multiply(bd2).toString();
            if (amountStr.contains(".")) {
                // cifras decimales
                int decimalsNum = amountStr.substring(amountStr.indexOf(".") + 1,amountStr.length()).length();
                
                if (decimalsNum < 2) {amountStr += "0";}
                
                amountStr = amountStr.substring(0, amountStr.indexOf(".") + 3);
            }
            sb.append("Monto: ["+amountStr+"] \n");
        }
        if (!"".equals(currency)) {sb.append("Moneda: ["+currency+"] \n");}
        if (!"".equals(merchantId)) {sb.append("Merchant Id: ["+merchantId+"] \n");}
        if (!"".equals(terminalId)) {sb.append("Terminal Id: ["+terminalId+"] \n");}
        if (!"".equals(batchId)) {sb.append("Autorizacion : ["+batchId+"] \n");}
        if (!"".equals(serialNo)) {sb.append("Serial: ["+serialNo+"] \n");}
        if (!"".equals(date)) {sb.append("Fecha: ["+fillWord(date, "/")+"] \n"); }
        if (!"".equals(time)) {sb.append("Hora: ["+fillWord(time, ":")+"] \n");}
        if (!"".equals(authCode)) {sb.append("Codigo Autorizacion: ["+authCode+"] \n");}
        if (!"".equals(sysRefNo)) {sb.append("sysRefno: ["+sysRefNo+"] \n");}
        if (!"".equals(oldSerialNo)) {sb.append("old SerialNo: ["+oldSerialNo+"] \n");}

        return sb.toString();
    }
    
    //Metodos Publicos
    /**
     * Información de la transacción analítica ( TLV reglas de codificación ) devuelto
     * 
     * @param cxt
     * @param tlvData datos TVL
     * @return
     */
    @SuppressLint("UseSparseArrays")
	public void parseTLVData(byte[] tlvData) {
        int tlvInfosize = tlvData.length;
        int index = 0;
        int tag = 0;
        int len = 0;
        byte[] value = null;

        Map<Integer, byte[]> tagValueMap = new HashMap<Integer, byte[]>();

        while (index < tlvInfosize) {
            // Tag 1 ~ 2 bytes
            tag = (tlvData[index] & 0xFF) & 0x1F;
            if (tag == 0x1F) {
                tag = (tlvData[index] & 0xFF);
                index++;
                tag = (tag << 8) | (tlvData[index] & 0xFF);
            } 
            else {tag = tlvData[index] & 0xFF;}

            index++;

            /*
             * Dominio de codificación de longitud , un máximo de cuatro bytes , si el primer byte de la b8 de bits más alta es 0 , el valor b7 ~ b1 es la longitud del campo valor . Si b8 es 1 ,
             * Valor b7 ~ b1 indica que el niño Aquí hay unos pocos bytes . El siguiente valor del sub - byte es la longitud del campo de valor .
             */
            int tmpLen = tlvData[index] & 0xFF;
            // El bit más significativo del primer byte
            int lenBit8 = (tmpLen & 0x80) >>> 7;
            // La primera b7 byte ~ b1
            int lenNum = tmpLen & 0x7F;

            if (lenBit8 == 0) {// lenNum es la longitud del campo valor
                len = lenNum;
            } 
            else if (lenBit8 == 1) {// Si b8 es 1 , indica lenNum Aquí hay varias sub - byte
                // El siguiente valor del sub - byte es la longitud del campo de valor
                if (lenNum == 1) {
                    index++;
                    len = tlvData[index] & 0xFF;
                } 
                else if (lenNum == 2) {
                    index++;
                    int len1 = tlvData[index] & 0xFF;
                    index++;
                    len = (len1 << 8) | (tlvData[index] & 0xFF);
                } 
                else if (lenNum == 3) {
                    index++;
                    int len1 = tlvData[index] & 0xFF;
                    index++;
                    int len2 = tlvData[index] & 0xFF;
                    index++;
                    len = (len1 << 16) | (len2 << 8) | (tlvData[index] & 0xFF);
                }
            }

            index++;

            // Value
            if (len > 0 && (index + len) <= tlvInfosize) {
                value = new byte[len];
                System.arraycopy(tlvData, index, value, 0, len);
                index += len;
                tagValueMap.put(tag, value);
            }
        }
        
        cardId = tagValueMap.get(cardIdTag) != null ? BaseUtils.byteArr2HexStr(tagValueMap.get(cardIdTag)).replace("F", "").replace("A", "*") : "";
        amount = tagValueMap.get(amountTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(amountTag)): "";
        otherAmount = tagValueMap.get(otherAmountTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(otherAmountTag)): "";
        AppContext.getAppContext().setAmount(tagValueMap.get(amountTag));
        
        //recuperamos el track2 y track 1
        track1  =  tagValueMap.get(track1Tag) != null ? BaseUtils.byteArr2HexStr(tagValueMap.get(track1Tag)) : "";
        track2 = tagValueMap.get(track2Tag) != null ? BaseUtils.byteArr2HexStr(tagValueMap.get(track2Tag)).replace("D", "=") : "";        

        currency = tagValueMap.get(currencyTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(currencyTag)) : "";
        merchantId = tagValueMap.get(merchantTag) != null ? new String(tagValueMap.get(merchantTag)): "";
        terminalId = tagValueMap.get(terIdTag) != null ? new String(tagValueMap.get(terIdTag)) : "";
        batchId = tagValueMap.get(batchIdTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(batchIdTag)) : "";
        serialNo = tagValueMap.get(serialNoTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(serialNoTag)) : "";
        AppContext.getAppContext().setSerialNo(tagValueMap.get(serialNoTag));
        
        date = tagValueMap.get(dateTag) != null ? BaseUtils.byteArr2HexStr(tagValueMap.get(dateTag)) : "";
        time = tagValueMap.get(timeTag) != null ? BaseUtils.byteArr2HexStr(tagValueMap.get(timeTag)) : "";
        authCode = tagValueMap.get(authCodeTag) != null ? new String(tagValueMap.get(authCodeTag)): "";
        sysRefNo = tagValueMap.get(sysRefNoTag) != null ? new String(tagValueMap.get(sysRefNoTag)): "";
        oldSerialNo = tagValueMap.get(oldSerialNoIdTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(oldSerialNoIdTag)) : "";
        
        applicationLabel = tagValueMap.get(applicationLabelTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(applicationLabelTag)) : "";
        appProfile = tagValueMap.get(appProfileTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(appProfileTag)) : "";
        dFName = tagValueMap.get(dFNameTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(dFNameTag)) : "";
        authorisationResponseCode = tagValueMap.get(authorisationResponseCodeTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(authorisationResponseCodeTag)) : "";
        
        tvr = tagValueMap.get(tvrTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(tvrTag)) : "";
        pin = tagValueMap.get(pinTag) != null ?  BaseUtils.byteArr2HexStr(tagValueMap.get(pinTag)) : "";
        tsi = tagValueMap.get(tsiTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(tsiTag)) : "";
        transactionType = tagValueMap.get(transactionTypeTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(transactionTypeTag)) : "";
        cardHolderName = tagValueMap.get(cardHolderNameTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(cardHolderNameTag)) : "";
        
        appExpirationDate = tagValueMap.get(appExpirationDateTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(appExpirationDateTag)) : "";
        issuerCountryCode = tagValueMap.get(issuerCountryCodeTag) != null ?  BCDUtils.bcd2Str(tagValueMap.get(issuerCountryCodeTag)) : "";
        serviceCode = tagValueMap.get(serviceCodeTag) != null ? new String(tagValueMap.get(serviceCodeTag)): "";
        appPrimaryAccountNSequenceNo = tagValueMap.get(appPrimaryAccountNSequenceNoTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appPrimaryAccountNSequenceNoTag)) : "";

        appIdentifierTerminal = tagValueMap.get(appIdentifierTerminalTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appIdentifierTerminalTag)) : "";
        appUsageControl = tagValueMap.get(appUsageControlTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appUsageControlTag)) : "";
        appVersionNumber = tagValueMap.get(appVersionNumberTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appVersionNumberTag)) : "";
        issuerActionCodeDefault  = tagValueMap.get(issuerActionCodeDefaultTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(issuerActionCodeDefaultTag)) : "";

        issuerActionCodeDenial = tagValueMap.get(issuerActionCodeDenialTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(issuerActionCodeDenialTag)) : "";
        issuerActionCodeOnline = tagValueMap.get(issuerActionCodeOnlineTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(issuerActionCodeOnlineTag)) : "";
        issuerAppData =  tagValueMap.get(issuerAppDataTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(issuerAppDataTag)) : "";
        appPreferredName = tagValueMap.get(appPreferredNameTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appPreferredNameTag)) : "";

        terminalCountryCode = tagValueMap.get(terminalCountryCodeTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(terminalCountryCodeTag)) : "";
        interfaceDeviceSN = tagValueMap.get(interfaceDeviceSNTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(interfaceDeviceSNTag)) : "";
        apCryptogram = tagValueMap.get(apCryptogramTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(apCryptogramTag)) : "";
        cryptogramInformationData = tagValueMap.get(cryptogramInformationDataTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(cryptogramInformationDataTag)) : "";

        terminalCapabilities = tagValueMap.get(terminalCapabilitiesTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(terminalCapabilitiesTag)) : "";
        cardHolderVerificationMethodResults = tagValueMap.get(cardHolderVerificationMethodResultsTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(cardHolderVerificationMethodResultsTag)) : "";
        terminalType = tagValueMap.get(terminalTypeTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(terminalTypeTag)) : "";
        appTransactionCounter  = tagValueMap.get(appTransactionCounterTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(appTransactionCounterTag)) : "";

        unpredictableNumber = tagValueMap.get(unpredictableNumberTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(unpredictableNumberTag)) : "";
        posEntryMode = tagValueMap.get(posEntryModeTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(posEntryModeTag)) : "";
        additionalTerminalCapabiliti = tagValueMap.get(additionalTerminalCapabilitiTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(additionalTerminalCapabilitiTag)) : "";
        location = tagValueMap.get(locationTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(locationTag)) : "";
        CTCIL    =  tagValueMap.get(CTCILTag) != null ? BCDUtils.bcd2Str(tagValueMap.get(CTCILTag)) : "";
        
    }
    
    /**
     * 
     * @param a
     * @param string
     * @param t
     * @return
     */
    public String Stringinsert(String a,String string,int t){
    	return a.substring(0,t)+string+a.substring(t+1,2);	
    }

    //Getter's && Setter's
    public String getApplicationLabel() {
		return applicationLabel;
	}

	public void setApplicationLabel(String applicationLabel) {
		this.applicationLabel = applicationLabel;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getAppProfile() {
		return appProfile;
	}

	public void setAppProfile(String appProfile) {
		this.appProfile = appProfile;
	}

	public String getdFName() {
		return dFName;
	}

	public void setdFName(String dFName) {
		this.dFName = dFName;
	}

	public String getAuthorisationResponseCode() {
		return authorisationResponseCode;
	}

	public void setAuthorisationResponseCode(String authorisationResponseCode) {
		this.authorisationResponseCode = authorisationResponseCode;
	}

	public String getTvr() {
		return tvr;
	}

	public void setTvr(String tvr) {
		this.tvr = tvr;
	}

	public String getTsi() {
		return tsi;
	}

	public void setTsi(String tsi) {
		this.tsi = tsi;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getCardHolderName() {
		return cardHolderName;
	}

	public void setCardHolderName(String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}

	public String getAppExpirationDate() {
		return appExpirationDate;
	}

	public void setAppExpirationDate(String appExpirationDate) {
		this.appExpirationDate = appExpirationDate;
	}

	public String getIssuerCountryCode() {
		return issuerCountryCode;
	}

	public void setIssuerCountryCode(String issuerCountryCode) {
		this.issuerCountryCode = issuerCountryCode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getAppPrimaryAccountNSequenceNo() {
		return appPrimaryAccountNSequenceNo;
	}

	public void setAppPrimaryAccountNSequenceNo(String appPrimaryAccountNSequenceNo) {
		this.appPrimaryAccountNSequenceNo = appPrimaryAccountNSequenceNo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAppIdentifierTerminal() {
		return appIdentifierTerminal;
	}

	public void setAppIdentifierTerminal(String appIdentifierTerminal) {
		this.appIdentifierTerminal = appIdentifierTerminal;
	}

	public String getAppUsageControl() {
		return appUsageControl;
	}

	public void setAppUsageControl(String appUsageControl) {
		this.appUsageControl = appUsageControl;
	}

	public String getAppVersionNumber() {
		return appVersionNumber;
	}

	public void setAppVersionNumber(String appVersionNumber) {
		this.appVersionNumber = appVersionNumber;
	}

	public String getIssuerActionCodeDefault() {
		return issuerActionCodeDefault;
	}

	public void setIssuerActionCodeDefault(String issuerActionCodeDefault) {
		this.issuerActionCodeDefault = issuerActionCodeDefault;
	}

	public String getIssuerActionCodeDenial() {
		return issuerActionCodeDenial;
	}

	public void setIssuerActionCodeDenial(String issuerActionCodeDenial) {
		this.issuerActionCodeDenial = issuerActionCodeDenial;
	}

	public String getIssuerActionCodeOnline() {
		return issuerActionCodeOnline;
	}

	public void setIssuerActionCodeOnline(String issuerActionCodeOnline) {
		this.issuerActionCodeOnline = issuerActionCodeOnline;
	}

	public String getIssuerAppData() {
		return issuerAppData;
	}

	public void setIssuerAppData(String issuerAppData) {
		this.issuerAppData = issuerAppData;
	}

	public String getAppPreferredName() {
		return appPreferredName;
	}

	public void setAppPreferredName(String appPreferredName) {
		this.appPreferredName = appPreferredName;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getTerminalCountryCode() {
		return terminalCountryCode;
	}

	public void setTerminalCountryCode(String terminalCountryCode) {
		this.terminalCountryCode = terminalCountryCode;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getInterfaceDeviceSN() {
		return interfaceDeviceSN;
	}

	public void setInterfaceDeviceSN(String interfaceDeviceSN) {
		this.interfaceDeviceSN = interfaceDeviceSN;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getApCryptogram() {
		return apCryptogram;
	}

	public void setApCryptogram(String apCryptogram) {
		this.apCryptogram = apCryptogram;
	}

	public String getCryptogramInformationData() {
		return cryptogramInformationData;
	}

	public void setCryptogramInformationData(String cryptogramInformationData) {
		this.cryptogramInformationData = cryptogramInformationData;
	}

	public String getTerminalCapabilities() {
		return terminalCapabilities;
	}

	public void setTerminalCapabilities(String terminalCapabilities) {
		this.terminalCapabilities = terminalCapabilities;
	}

	public String getCardHolderVerificationMethodResults() {
		return cardHolderVerificationMethodResults;
	}

	public void setCardHolderVerificationMethodResults(String cardHolderVerificationMethodResults) {
		this.cardHolderVerificationMethodResults = cardHolderVerificationMethodResults;
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public String getAppTransactionCounter() {
		return appTransactionCounter;
	}

	public void setAppTransactionCounter(String appTransactionCounter) {
		this.appTransactionCounter = appTransactionCounter;
	}

	public String getUnpredictableNumber() {
		return unpredictableNumber;
	}

	public void setUnpredictableNumber(String unpredictableNumber) {
		this.unpredictableNumber = unpredictableNumber;
	}

	public String getPosEntryMode() {
		return posEntryMode;
	}

	public void setPosEntryMode(String posEntryMode) {
		this.posEntryMode = posEntryMode;
	}

	public String getAdditionalTerminalCapabiliti() {
		return additionalTerminalCapabiliti;
	}

	public void setAdditionalTerminalCapabiliti(String additionalTerminalCapabiliti) {
		this.additionalTerminalCapabiliti = additionalTerminalCapabiliti;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getSysRefNo() {
		return sysRefNo;
	}

	public void setSysRefNo(String sysRefNo) {
		this.sysRefNo = sysRefNo;
	}

	public String getOldSerialNo() {
		return oldSerialNo;
	}

	public void setOldSerialNo(String oldSerialNo) {
		this.oldSerialNo = oldSerialNo;
	}

	public String getTrack2() {
		return track2;
	}

	public void setTrack2(String track2) {
		this.track2 = track2;
	}

	public String getTrack1() {
		return track1;
	}

	public void setTrack1(String track1) {
		this.track1 = track1;
	}

	public String getCTCIL() {
		return CTCIL;
	}

	public void setCTCIL(String cTCIL) {
		CTCIL = cTCIL;
	}

	
	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(String otherAmount) {
		this.otherAmount = otherAmount;
	}
	
}