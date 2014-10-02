package com.example.skynet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class Protocols {

	public static final String GET_FOLDER_LIST = "10";
	public static final String GET_SELECTED_FOLDER_LIST = "11";
	public static final String GET_PARENT = "12";
	public static final String DOWNLOAD_FOLDER = "13";
	public static final String MAIN_SEPERATOR = "#";
	public static final String SUB_SEPERATOR = "$";
	public static final String TAG = "folderShare";

	@SuppressLint("DefaultLocale")
	public static String convertIntIPtoStringIP(int ip) {
		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff)));
	}

	public static String getFileNameFromEncode(String encode) {
		return encode.substring(encode.lastIndexOf("/") + 1);
	}

	public static String getFilePathFromEncode(String encode) {
		return encode.substring(encode.lastIndexOf("/"));
	}

	public static Long getFileSizeFromEncode(String encode) {
		return Long.parseLong(encode.substring(0, encode.indexOf("/")));
	}

	public static String[] splitByMainSeperator(String data) {
		return data.split(Protocols.MAIN_SEPERATOR);
	}

	public static String[] splitBySubSeperator(String data) {
		return data.split(Protocols.SUB_SEPERATOR);
	}

	public static String clubBySubSeperator(String[] dataArray) {
		String clubbedString = "";
		for (String s : dataArray)
			clubbedString = clubbedString + s + Protocols.SUB_SEPERATOR;
		return clubbedString.substring(0, clubbedString.length() - 1);
	}

	public static String clubBySubSeperator(ArrayList<String> dataList) {
		String clubbedString = "";
		for (String s : dataList)
			clubbedString = clubbedString + s + Protocols.SUB_SEPERATOR;
		return clubbedString.substring(0, clubbedString.length() - 1);
	}

	public static String createEncodeFromFile(File f) {
		String encode = "";
		encode = encode + f.length() + f.getAbsolutePath()
				+ Protocols.SUB_SEPERATOR;
		return encode.substring(0, encode.length() - 1);
	}

	public static String clubByMainSeperator(String code, String clubbedArray) {
		return code + Protocols.MAIN_SEPERATOR + clubbedArray;
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
