package com.example.skynet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class Protocols {

	public static final String SSRC_GET_FOLDER_LIST = "10";
	public static final String SSRC_GET_SELECTED_FOLDER_LIST = "11";
	public static final String SSRC_DOWNLOAD_FOLDER = "12";
	public static final String CSRC_SET_FOLDER_LIST = "20";
	public static final String CSRC_DOWNLOAD_FOLDER = "21";
	public static final String TAG = "folder_share";

	@SuppressLint("DefaultLocale")
	public static String convertIntIPtoStringIP(int ip) {
		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff)));
	}

	public static boolean copyInputStreamToOutputStream(
			InputStream inputStream, OutputStream outputStream, long fileSize) {
		try {

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;

			while ((fileSize > 0)
					&& (len = inputStream.read(buffer, 0,
							(int) Math.min(buffer.length, fileSize))) != -1) {
				outputStream.write(buffer, 0, len);
				outputStream.flush();
				fileSize -= len;
			}

			// while ((len = inputStream.read(buffer)) != -1) {
			// Log.i(TAG, "writing " + len + buffer.toString());
			// outputStream.write(buffer, 0, len);
			// }
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		return true;

	}

}
