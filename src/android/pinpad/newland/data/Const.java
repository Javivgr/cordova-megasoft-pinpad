package ve.com.megasoft.pinpad.newland.data;

/**
 * Created by YJF on 2015/8/11 0011.
 */
public class Const {
	/**
	 * Password entered is complete
	 */
	public static final int PIN_FINISH = 1;
	public static final String CHECK_MODULE = "checkedModule";

	public static class ScanType {
		/**
		 * Front scan
		 */
		public static final int FRONT = 1;
		/**
		 * Back scan
		 */
		public static final int BACK = 0;
	}

	public static class CheckedModuleName {
		public static final int TERNAL = 0;
		public static final int CARDREADER = 1;
		public static final int EMV = 2;

		public static final int ICCARD = 3;
		public static final int LIGHT = 4;
		public static final int CONSUME = 5;

		public static final int PIN = 6;
		public static final int PRINTER = 7;

		public static final int RFCARD = 8;
		public static final int SECURITY = 9;
		public static final int SCANNER = 10;
		public static final int SWIP = 11;
		public static final int STORAGE = 12;
		public static final int USBSERIAL = 13;
		public static final int SM = 14;
		
		public static final int MESSAGECASE = 15;
		public static final int EXTERNALPIN=16;
		public static final int EXTERNALSCAN=17;
		public static final int EXTERNAL_GUESTDISPLAY=18;
	}

	/**
	 * Main key index
	 * <p>
	 * 
	 * If the indexes are the same, it means the same main key index is used各索引若相同则表示使用同一组主密钥索引
	 * 
	 * 
	 */
	public static class MKIndexConst {

		/**
		 * Main key index
		 */
		public static final int DEFAULT_MK_INDEX = 1;
		public static final int ZERO_MK_INDEX = 0;

	}
	
	/**
	 * 
	 * @author LinDan
	 * dukpt index
	 */
	public static class DUKPTIndexConst{
		
		public static final int DEFAULT_DUKPT_INDEX=1;
	}
	/**
	 * Working key type:
	 * 
	 */
	public static class PinWKIndexConst {
		/**
		 * Default that the working key index encrypted by PIN
		 */
		public static final int DEFAULT_PIN_WK_INDEX = 2;
//		public static final int EXTERNAL_PIN_WK_INDEX = 0;
	}

	/**
	 * Working key type:
	 * 
	 */
	public static class MacWKIndexConst {
		/**
		 * Default that the working key index encrypted by MAC
		 */
		public static final int DEFAULT_MAC_WK_INDEX = 3;
//		public static final int EXTERNAL_MAC_WK_INDEX = 1;
	}

	/**
     *
     */
	public static class DataEncryptWKIndexConst {
		/**
		 * Default that the working key index encrypted by track
		 */
		public static final int DEFAULT_TRACK_WK_INDEX = 4;
//		public static final int EXTERNAL_TRACK_WK_INDEX = 0;
//
		public static final int DEFAULT_MUTUALAUTH_WK_INDEX = 5;

	}

	/**
	 * The message tag of operation tips
	 * 
	 */
	public static class MessageTag {
		/**
		 * Normal messages<tt>tag</tt>
		 */
		public static final int NORMAL = 0;
		/**
		 * Error messages<tt>tag</tt>
		 */
		public static final int ERROR = 1;
		/**
		 * Hint message<tt>tag</tt>
		 */
		public static final int TIP = 2;
		/**
		 * Data<tt>tag</tt>
		 */
		public static final int DATA = 3;
		/**
		 * Warn<tt>tag</tt>
		 */
		public static final int WARN = 4;
	}

	/**
	 * Device params stored in the relevant format
	 * 
	 * 
	 */
	public static class DeviceParamsPattern {

		/**
		 * Default that stored coding set
		 * <p>
		 */
		public static final String DEFAULT_STORENCODING = "utf-8";

		/**
		 * Date format
		 * <p>
		 */
		public static final String DEFAULT_DATEPATTERN = "yyyyMMddHHmmss";
	}

	/**
	 * Device params<tt>tag</tt>
	 * 
	 * 
	 */
	public static class DeviceParamsTag {

		/**
		 * Merchant No. stored<tt>tag</tt>
		 */
		public static final int MRCH_NO = 0xFF9F11;

		/**
		 * Terminal No. stored<tt>tag</tt>
		 */
		public static final int TRMNL_NO = 0xFF9F12;
		/**
		 * Working key stored<tt>tag</tt>
		 */
		public static final int WK_UPDATEDATE = 0xFF9F13;
		/**
		 * POS  identification stored 标示存放<tt>tag</tt>
		 */
		public static final int DEVICE_TYPE = 0xFF9F14;
		/**
		 * Merchant name stored<tt>tag</tt>
		 */
		public static final int MRCH_NAME = 0xFF9F15;

	}

	public static class CardType {
		/**
		 * Magnetic stripe card
		 */
		public static final int COMMON = 0;
		/**
		 * IC card
		 */
		public static final int ICCARD = 1;
	}

	public static class DialogView {
		/**
		 * Dialog of Mac calculate
		 */
		public static final int MAC_CACL_DIALOG = 0;
		/**
		 * Dialog of RFCard key
		 */
		public static final int NC_CARD_KEY_DIALOG = 1;
		/**
		 * Dialog of IC Card
		 */
		public static final int IC_CARD_ICCardSlot_DIALOG = 2;
		/**
		 * Dialog of choosing scan
		 */
		public static final int SCAN_SELECT_DIALOG=3;

		/**
		 * IC Card opened
		 */
		public static final int IC_CARD_OPRN_CARDTYPE_DIALOG = 4;


		/**
		 * 外接键盘设备类型选择
		 */
		public static final int EXTERNALPIN_TYPE_DIALOG = 5;
		/**
		 * Dialog of CPU Card COMM
		 */
		public static final int IC_CARD_CPU_COMM = 6;
	}
	public static class ScanResult{
		public static final int SCAN_FINISH = 0;
		public static final int SCAN_RESPONSE = 1;
		public static final int SCAN_ERROR = 2;
		public static final int SCAN_TIMEOUT = 3;
		public static final int SCAN_CANCEL = 4;


	}
}
