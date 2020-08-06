package ve.com.megasoft.pinpad.newland.data;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

//import com.newland.mesdk.demo.origin.R;

/**
 * Random password Keyboard 
 * @author Administrator
 *
 */
public class N900PinKeyBoard extends View {
	private float width, height;
	private Paint paint;
	private int[] nums;
	private int contentsize;
	private Path path;
	// For function key randomization
	private byte[] rf = new byte[] { 0x1B, 0x0A, 0x0D };
	private DisplayMetrics dm;
	// digital background color, the dividing line, the font color of the function key, the number, the cancel background, the backspace background, and the determination background
	private int[] colors = new int[]{0xfff5f5f9,0xffe1e1e1,0xffffffff,0xff000000,0xfff24c4d,0xfff3e250,0xff70d145};

	public N900PinKeyBoard(Context context) {
		super(context);
		getScreenResolution(context);
		init();
	}

	public N900PinKeyBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		getScreenResolution(context);
		init();
	}
	
	private void getScreenResolution(Context context){
		WindowManager wm =(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		Log.i("N900PinKeyBoard----1", "height="+dm.heightPixels+";width"+dm.widthPixels);
	}

	private void init() {
		path = new Path();
		paint = new Paint();
		paint.setAntiAlias(true);
		nums = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawColor(colors[0]);

		
		if (rf[0] == 0x1B)
			// red
			paint.setColor(colors[4]);
		else if (rf[0] == 0x0A)
			// yellow
			paint.setColor(colors[5]);
		else if (rf[0] == 0x0D)
			// green
			paint.setColor(colors[6]);
		canvas.drawRect(0, height / 4 * 3 + 1, width / 4 - 1, height, paint);

		
		if (rf[1] == 0x1B)
			// red
			paint.setColor(colors[4]);
		else if (rf[1] == 0x0A)
			// yellow
			paint.setColor(colors[5]);
		else if (rf[1] == 0x0D)
			// green
			paint.setColor(colors[6]);
		canvas.drawRect(width / 4 * 3 + 1, 0, width, height / 2, paint);

		
		if (rf[2] == 0x1B)
			// red
			paint.setColor(colors[4]);
		else if (rf[2] == 0x0A)
			// yellow
			paint.setColor(colors[5]);
		else if (rf[2] == 0x0D)
			// green
			paint.setColor(colors[6]);
		canvas.drawRect(width / 4 * 3 + 1, height / 2, width, height, paint);

		paint.setColor(colors[1]);
		paint.setStrokeWidth(1f);
		canvas.drawLine(0, height / 4, width / 4 * 3, height / 4, paint);
		canvas.drawLine(0, height / 2, width / 4 * 3, height / 2, paint);
		canvas.drawLine(0, height / 4 * 3, width / 4 * 3, height / 4 * 3, paint);
		canvas.drawLine(width / 4, 0, width / 4, height, paint);
		canvas.drawLine(width / 2, 0, width / 2, height / 4 * 3, paint);
		canvas.drawLine(width / 4 * 3, 0, width / 4 * 3, height, paint);

		paint.setColor(colors[2]);
		paint.setTextSize(contentsize);
		for (int i = 0; i < rf.length; i++) {
			float cx = 0, cy = 0;
			if (i == 0) {
				cx = width / 8;
				cy = height / 8 * 7;
			} else if (i == 1) {
				cx = width / 8 * 7;
				cy = height / 4;
			} else if (2 == i) {
				cx = width / 8 * 7;
				cy = height / 4 * 3;
			}
			if (rf[i] == 0x1B) {
				//drawStringCenter(canvas, cx, cy, getResources().getString(R.string.keyboard_cancel));
				drawStringCenter(canvas, cx, cy, "Cancelar");
			} else if (rf[i] == 0x0A) {
				drawdelete(canvas, cx, cy, height / 12);
			} else if (rf[i] == 0x0D) {
				//drawStringCenter(canvas, cx, cy, getResources().getString(R.string.keyboard_confirm));
				drawStringCenter(canvas, cx, cy, "Confirmar");
			}
		}
		// drawStringCenter(canvas, width / 8, height / 8 * 7, "取 消");
		// drawStringCenter(canvas, width / 8 * 7, height / 4 * 3, "确 认");
		// drawdelete(canvas, width / 8 * 7, height / 4, height / 12);

		paint.setColor(colors[3]);
		paint.setTextSize(contentsize + 10);
		drawStringCenter(canvas, width / 8, height / 8, nums[0] + "");
		drawStringCenter(canvas, width / 8 * 3, height / 8, nums[1] + "");
		drawStringCenter(canvas, width / 8 * 5, height / 8, nums[2] + "");

		drawStringCenter(canvas, width / 8, height / 8 * 3, nums[3] + "");
		drawStringCenter(canvas, width / 8 * 3, height / 8 * 3, nums[4] + "");
		drawStringCenter(canvas, width / 8 * 5, height / 8 * 3, nums[5] + "");

		drawStringCenter(canvas, width / 8, height / 8 * 5, nums[6] + "");
		drawStringCenter(canvas, width / 8 * 3, height / 8 * 5, nums[7] + "");
		drawStringCenter(canvas, width / 8 * 5, height / 8 * 5, nums[8] + "");
		drawStringCenter(canvas, width / 2, height / 8 * 7, nums[9] + "");
	}

	private void drawStringCenter(Canvas canvas, float centerpointX,
			float centerpointY, String s) {
		paint.setStyle(Style.FILL);
		paint.setStrokeWidth(4);
		FontMetrics fmtemp = paint.getFontMetrics();
		int ctwidth = (int) paint.measureText(s);
		int ctheight = (int) Math.ceil(fmtemp.descent - fmtemp.ascent);
		int ctdescent = (int) fmtemp.descent;
		canvas.drawText(s, centerpointX - ctwidth / 2, centerpointY - ctdescent
				+ ctheight / 2, paint);
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = width / 5 * 4;
		for (int size = 15; size < 100; size++) {
			paint.setTextSize(size);
			FontMetrics fm = paint.getFontMetrics();
			float cs = fm.descent - fm.ascent;
			//float tempw = paint.measureText(getResources().getString(R.string.keyboard_confirm));
			float tempw = paint.measureText("Confirm");
			if (cs > height / 8 || tempw > width / 8) {
				contentsize = size;
				break; 
			}
		}
		this.setMeasuredDimension((int)width,(int)height);
	}
	
	public void loadRandomKeyboardfinished(byte[] sr) {
		byte[] numsbyte = new byte[] { sr[0], sr[1], sr[2], sr[4], sr[5],
				sr[6], sr[8], sr[9], sr[10], sr[12] };
		int[] nums = new int[numsbyte.length];
		for (int i = 0; i < numsbyte.length; i++) {
			nums[i] = numsbyte[i] - 48;
		}
		rf[0] = sr[3]; //随机功能键盘
		rf[1] = sr[7];
		rf[2] = sr[14];
		setRandomNumber(nums);
	}
	// Set the corresponding key position after random
	public void setRandomNumber(int[] nums) {
		this.nums = nums;
		invalidate();
	}
	private int[] coordinateInt;

	// Gets the random keyboard key coordinates
	public byte[] getCoordinate() {
		int[] l = new int[2];
		getLocationOnScreen(l);
		int x0 = l[0], x1 = (int) (l[0] + width / 4), x2 = (int) (l[0] + width / 2), x3 = (int) (l[0] + width / 4 * 3), x4 = (int) (l[0] + width);
		int y0 = l[1], y1 = (int) (l[1] + height / 4), y2 = (int) (l[1] + height / 2), y3 = (int) (l[1] + height / 4 * 3), y4 = (int) (l[1] + height);
		if (l[1] != 0)
			coordinateInt = new int[] { x0, y0, x1, y1, x1, y0, x2, y1, x2,y0, x3, y1, 
				x0, y3, x1, y4,//Cancel
				x0, y1, x1, y2, x1, y1, x2, y2, x2,y1, x3, y2,
				x3, y0, x4, y2, //Delete
				x0, y2, x1, y3, x1, y2, x2, y3, x2,y2, x3, y3, 
				x0, y0, x0, y0, // null
				x1, y3, x3, y4, // 0
				x4, y4, x4, y4, // null
				x3, y2, x4, y4 }; //confirm
		// Initial set of coordinates
		byte[] initCoordinate = new byte[coordinateInt.length * 2];
		for (int i = 0, j = 0; i < coordinateInt.length; i++, j++) {
			initCoordinate[j] = (byte) ((coordinateInt[i] >> 8) & 0xff);
			j++;
			initCoordinate[j] = (byte) (coordinateInt[i] & 0xff);
		}
		return initCoordinate;
	}
	private void drawdelete(Canvas canvas, float centerX, float centerY,
			float sizeheight) {
		float left = centerX - sizeheight;
		float top = centerY - sizeheight / 2;
		path.reset();
		path.moveTo(left, top + sizeheight / 2);
		path.lineTo(left + sizeheight / 2, top);
		path.lineTo(left + 2 * sizeheight, top);
		path.lineTo(left + 2 * sizeheight, top + sizeheight);
		path.lineTo(left + sizeheight / 2, top + sizeheight);
		path.close();

		int gap = 8;
		path.moveTo(left + 2 * sizeheight / 8 * 3 + 5, top + gap);
		path.lineTo(left + 2 * sizeheight / 8 * 7 - 5, top + sizeheight - gap);
		path.moveTo(left + 2 * sizeheight / 8 * 7 - 5, top + gap);
		path.lineTo(left + 2 * sizeheight / 8 * 3 + 5, top + sizeheight - gap);
		
		paint.setStrokeWidth(4);
//		paint.setColor(keyboardcolors[2]);
		paint.setStyle(Style.STROKE);
		canvas.drawPath(path, paint);
	}
	public byte[] getPinKeySeq(int KEYTAG) {
		if (KEYTAG ==PinKeySeq.NORMAL) {// not random
			return  new byte[] { 0x31, 0x32, 0x33, 0x1B, 0x34, 0x35, 0x36,
					0x0A, 0x37, 0x38, 0x39, 0x2E, 0x30, 0x1C, 0x0D };
		} else if (KEYTAG == PinKeySeq.RANDOM_NUM) {//Numbers are random,but function keys are not random.
			return null;
		} else {//Number key and function key are random.
			return new byte[] { 0x7E, 0x7E, 0x7E, 0x7F, 0x7E, 0x7E, 0x7E, 0x7F,
					0x7E, 0x7E, 0x7E, 0x2E, 0x7E, 0x1C, 0x7F };
		}
	}
	public int[] getColors() {
		return colors;
	}

	public void setColors(int[] colors) {
		this.colors = colors;
	}
	
	public static class PinKeySeq {
		/**
		 * Number key and function key are not random.
		 */
		public static final int NORMAL = 0;
		/**
		 * Numbers are random,but function keys are not random.
		 */
		public static final int RANDOM_NUM = 1;
		/**
		 * Number key and function key are random.
		 */
		public static final int RANDOM_ALL = 2;
		
	}
}
