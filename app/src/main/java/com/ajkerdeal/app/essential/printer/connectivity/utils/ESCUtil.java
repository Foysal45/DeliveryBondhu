package com.ajkerdeal.app.essential.printer.connectivity.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

//常用指令封装
public class ESCUtil {

	public static final byte ESC = 0x1B;// Escape
	public static final byte FS =  0x1C;// Text delimiter
	public static final byte GS =  0x1D;// Group separator
	public static final byte DLE = 0x10;// data link escape
	public static final byte EOT = 0x04;// End of transmission
	public static final byte ENQ = 0x05;// Enquiry character
	public static final byte SP =  0x20;// Spaces
	public static final byte HT =  0x09;// Horizontal list
	public static final byte LF =  0x0A;//Print and wrap (horizontal orientation)
	public static final byte CR =  0x0D;// Home key
	public static final byte FF =  0x0C;// Carriage control (print and return to the standard mode (in page mode))
	public static final byte CAN = 0x18;// Canceled (cancel print data in page mode)

	//初始化打印机
	public static byte[] init_printer() {
		byte[] result = new byte[2];
		result[0] = ESC;
		result[1] = 0x40;
		return result;
	}

	//打印浓度指令
	public static byte[] setPrinterDarkness(int value){
		byte[] result = new byte[9];
		result[0] = GS;
		result[1] = 40;
		result[2] = 69;
		result[3] = 4;
		result[4] = 0;
		result[5] = 5;
		result[6] = 5;
		result[7] = (byte) (value >> 8);
		result[8] = (byte) value;
		return result;
	}


	/**
	 * Print a single QR code sunmi custom instructions
	 * @param code:			QR code data
	 * @param modulesize:	Size of QR code block (unit: point, value 1 to 16)
	 * @param errorlevel:	2D code error correction level (0 to 3)
	 *                0 -- Error Correction Level L ( 7%)
	 *                1 -- Error Correction Level M (15%)
	 *                2 -- Error Correction Level Q (25%)
	 *                3 -- Error Correction Level H (30%)
	 */
	public static byte[] getPrintQRCode(String code, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code));
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}

	/**
	 * 横向两个二维码 sunmi自定义指令
	 * @param code1:			二维码数据
	 * @param code2:			二维码数据
	 * @param modulesize:	二维码块大小(单位:点, 取值 1 至 16 )
	 * @param errorlevel:	二维码纠错等级(0 至 3)
	 *                0 -- 纠错级别L ( 7%)
	 *                1 -- 纠错级别M (15%)
	 *                2 -- 纠错级别Q (25%)
	 *                3 -- 纠错级别H (30%)
	 */
	public static byte[] getPrintDoubleQRCode(String code1, String code2, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code1));
			buffer.write(getBytesForPrintQRCode(false));
			buffer.write(getQCodeBytes(code2));

			//加入横向间隔
			buffer.write(new byte[]{0x1B, 0x5C, 0x18, 0x00});

			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}

	/**
	 * 光栅打印二维码
	 */
	/*public static byte[] getPrintQRCode2(String data, int size){
		byte[] bytes1  = new byte[4];
		bytes1[0] = GS;
		bytes1[1] = 0x76;
		bytes1[2] = 0x30;
		bytes1[3] = 0x00;

		byte[] bytes2 = BytesUtil.getZXingQRCode(data, size);
		return BytesUtil.byteMerger(bytes1, bytes2);
	}*/

	/**
	 * 打印一维条形码
	 */
	public static byte[] getPrintBarCode(String data, int symbology, int height, int width, int textposition){
		if(symbology < 0 || symbology > 10){
			return new byte[]{LF};
		}

		if(width < 2 || width > 6){
			width = 2;
		}

		if(textposition <0 || textposition > 3){
			textposition = 0;
		}

		if(height < 1 || height>255){
			height = 162;
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(new byte[]{0x1D,0x66,0x01,0x1D,0x48,(byte)textposition,
					0x1D,0x77,(byte)width,0x1D,0x68,(byte)height,0x0A});

			byte[] barcode;
			if(symbology == 10){
				barcode = BytesUtil.getBytesFromDecString(data);
			}else{
				barcode = data.getBytes("GB18030");
			}


			if(symbology > 7){
				buffer.write(new byte[]{0x1D,0x6B,0x49,(byte)(barcode.length+2),0x7B, (byte) (0x41+symbology-8)});
			}else{
				buffer.write(new byte[]{0x1D,0x6B,(byte)(symbology + 0x41),(byte)barcode.length});
			}
			buffer.write(barcode);
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}

	//光栅位图打印
	public static byte[] printBitmap(Bitmap bitmap){
		byte[] bytes1  = new byte[4];
		bytes1[0] = GS;
		bytes1[1] = 0x76;
		bytes1[2] = 0x30;
		bytes1[3] = 0x00;

		byte[] bytes2 = BytesUtil.getBytesFromBitMap(bitmap);
		return BytesUtil.byteMerger(bytes1, bytes2);
	}

	//

	/**
	 * 光栅位图打印 设置mode
	 * Raster bitmap printing set mode
	 *
	 * @param bitmap b
	 * @param mode raster bitmap 			=> 3 width hight, 1 width, 2 height, 0 normal
	 *             8 point single density 	=> 0
	 *             8 point double density 	=> 1
	 *             24 point single density 	=> 32
	 *             24 point double density 	=> 33
	 *
	 * @return byte[]
	 */
	public static byte[] printBitmap(Bitmap bitmap, int mode){
		byte[] bytes1  = new byte[4];
		bytes1[0] = GS;
		bytes1[1] = 0x76;
		bytes1[2] = 0x30;
		bytes1[3] = (byte) mode;

		byte[] bytes2 = BytesUtil.getBytesFromBitMap(bitmap);
		return BytesUtil.byteMerger(bytes1, bytes2);
	}

	//光栅位图打印
	public static byte[] printBitmap(byte[] bytes){
		byte[] bytes1  = new byte[4];
		bytes1[0] = GS;
		bytes1[1] = 0x76;
		bytes1[2] = 0x30;
		bytes1[3] = 0x00;

		return BytesUtil.byteMerger(bytes1, bytes);
	}

	/*
	*	选择位图指令 设置mode
	*	需要设置1B 33 00将行间距设为0
	 */
	public static byte[] selectBitmap(Bitmap bitmap, int mode){
		return BytesUtil.byteMerger(BytesUtil.byteMerger(new byte[]{0x1B, 0x33, 0x00}, BytesUtil.getBytesFromBitMap(bitmap, mode)), new byte[]{0x0A, 0x1B, 0x32});
	}

	/**
	 * 跳指定行数
	 * Jump specified number of rows
	 */
    public static byte[] nextLine(int lineNum) {
        byte[] result = new byte[lineNum];
        for (int i = 0; i < lineNum; i++) {
            result[i] = LF;
        }

        return result;
    }

	// ------------------------underline-----------------------------
	//设置下划线1点
	public static byte[] underlineWithOneDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 1;
		return result;
	}

	//设置下划线2点
	public static byte[] underlineWithTwoDotWidthOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 2;
		return result;
	}

	//取消下划线
	public static byte[] underlineOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 45;
		result[2] = 0;
		return result;
	}

	// ------------------------bold-----------------------------
	/**
	 * 字体加粗
	 */
	public static byte[] boldOn() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0xF;
		return result;
	}

	/**
	 * 取消字体加粗
	 */
	public static byte[] boldOff() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 69;
		result[2] = 0;
		return result;
	}


	// char repeater
	public static byte[] createByteArray(byte character, int times ){
		byte[] result = new byte[times];
		for (int i=0; i<times; i++){
			result[i] = character;
		}
		return result;
	}


	// ------------------------Font Size-----------------------------
	public static byte[] setFontEnlarge(byte FontEnlarge) {
		byte[] size = new byte[]{GS, 33, FontEnlarge};
		return size;
	}
	public static byte[] setFontSize0() {
		byte[] size = new byte[]{GS, 33, 0x00};
		return size;
	}
	public static byte[] setFontSize1() {
		byte[] size = new byte[]{GS, 33, 0x11};
		return size;
	}
	public static byte[] setFontSize2() {
		byte[] size = new byte[]{GS, 33, 0x22};
		return size;
	}
	public static byte[] setFontSize3() {
		byte[] size = new byte[]{GS, 33, 0x33};
		return size;
	}
	public static byte[] setFontSize4() {
		byte[] size = new byte[]{GS, 33, 0x44};
		return size;
	}
	public static byte[] setFontSize5() {
		byte[] size = new byte[]{GS, 33, 0x55};
		return size;
	}
	public static byte[] setFontSize6() {
		byte[] size = new byte[]{GS, 33, 0x66};
		return size;
	}
	public static byte[] setFontSize7() {
		byte[] size = new byte[]{GS, 33, 0x77};
		return size;
	}


	// ------------------------character-----------------------------

	/**
	 * Single-byte mode is turned on
	 *
	 * DOC: Cancel Chinese character mode (ESC-POS Command Set Doc)
	 * [Format]
	 * ASCII	FS	.
	 * Hex 		1C	2E
	 * Decimal	28	46
	 * [Note]
	 * While all the Chinese character has been canceled ,all those character will be regard as ASCII,
	 * the system will handle only one character each time.
	 * Power on and automatic selecting Chinese character mode.
	 *
	 * @return byte[] command
	 */
	public static byte[] singleByte(){
		byte[] result = new byte[2];
		result[0] = FS;
		result[1] = 0x2E;
		return result;
	}

	/*
	* Single-byte mode is off
	* Doc: Select Chinese character mode (ESC-POS Command Set Doc)
	* [Format]
	* ASCII 	FS 	&
	* Hex 		1C	26
	* Decimal	28	38
	* [Note]
	* While choosing the chinese character mode ,the printer will judge if the Chinese character is
	* internal code. Handling the first character then judge the if the next character s internal code.
	* The printer will automatic select the Chinese mode while the printer is power on.
	*
	* @return byte[] command
 	*/
	public static byte[] singleByteOff(){
		byte[] result = new byte[2];
		result[0] = FS;
		result[1] = 0x26;
		return result;
	}

	/**
	 * Set single-byte character set
	 * DOC: Print and feed paper (ESC-POS Command Set Doc)
	 * [Format]
	 * ASCII	ESC	J	n
	 * Hex 		1B	4A	n
	 * Decimal	27	74	n
	 * [Range] 0 n 255
	 * [Description]
	 * Print the data in buffer area and feed paper for n dots line.
	 * [Note]
	 * When printing finished, puts the current print position at beginning of line.
	 * ESC 2 ESC 3 Feeding of paper wont be affected by ESC 2 or ESC 3 order set.
	 * The max paper feed is 1016mm(40). If distance exceeds it, the max value is taken.
	 *
	 * @return byte[] command
	 */
	public static byte[] setCodeSystemSingle(byte charset){
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 0x74;
		result[2] = charset;
		return result;
	}

	/**
	 *
	 * Set multibyte character set
	 * Doc: Select Kanji character code system (ESC-POS Command Set Doc)
	 * [Format] FS 43 n
	 * [Range] 0 n 2 | 48 n 50 Default: n = 0
	 * [Description]
	 * n	 Encoding system Range:
	 * 0,48	 GBK simple Chinese
	 * 1,49	 BIG5 traditional Chinese
	 * 2,50	 KSC5601 korean
	 *
	 * @param charset byte n
	 * @return byte[] command
	 */
	public static byte[] setCodeSystem(byte charset){
		byte[] result = new byte[3];
		result[0] = FS;
		result[1] = 0x43;
		result[2] = charset;
		return result;
	}

	// ------------------------Align-----------------------------

	/**
	 * 居左
	 */
	public static byte[] alignLeft() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 0;
		return result;
	}


	/**
	 *
	 * @return byte command
	 */
	public static byte[] alignCenter() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 1;
		return result;
	}

	/**
	 * 居右
	 */
	public static byte[] alignRight() {
		byte[] result = new byte[3];
		result[0] = ESC;
		result[1] = 97;
		result[2] = 2;
		return result;
	}


	////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////          private                /////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	private static byte[] setQRCodeSize(int modulesize){
		//二维码块大小设置指令
		byte[] dtmp = new byte[8];
		dtmp[0] = GS;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x43;
		dtmp[7] = (byte)modulesize;
		return dtmp;
	}

	private static byte[] setQRCodeErrorLevel(int errorlevel){
		//二维码纠错等级设置指令
		byte[] dtmp = new byte[8];
		dtmp[0] = GS;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x45;
		dtmp[7] = (byte)(48+errorlevel);
		return dtmp;
	}


	private static byte[] getBytesForPrintQRCode(boolean single){
		//打印已存入数据的二维码
		byte[] dtmp;
		if(single){		//同一行只打印一个QRCode， 后面加换行
			dtmp = new byte[9];
			dtmp[8] = 0x0A;
		}else{
			dtmp = new byte[8];
		}
		dtmp[0] = 0x1D;
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x51;
		dtmp[7] = 0x30;
		return dtmp;
	}

	private static byte[] getQCodeBytes(String code) {
		//二维码存入指令
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			byte[] d = code.getBytes("GB18030");
			int len = d.length + 3;
			if (len > 7092) len = 7092;
			buffer.write((byte) 0x1D);
			buffer.write((byte) 0x28);
			buffer.write((byte) 0x6B);
			buffer.write((byte) len);
			buffer.write((byte) (len >> 8));
			buffer.write((byte) 0x31);
			buffer.write((byte) 0x50);
			buffer.write((byte) 0x30);
			for (int i = 0; i < d.length && i < len; i++) {
				buffer.write(d[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
}