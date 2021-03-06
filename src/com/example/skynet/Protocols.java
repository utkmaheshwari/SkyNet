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
	public static final String PREPARE_FOR_DOWNLOAD = "13";
	public static final String START_DOWNLOAD = "14";
	public static final String MAIN_SEPERATOR = "<";
	public static final String SUB_SEPERATOR = ">";
	public static final String FILE = "file";
	public static final String FOLDER = "folder";
	public static final String TAG = "folderShare";
	public static final String IS_NULL = "";

	@SuppressLint("DefaultLocale")
	public static String convertIntIPtoStringIP(int ip) {
		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff)));
	}

	public static String getFileNameFromEncode(String encode) {
		if (encode.equals("") | encode.equals(null))
			return Protocols.IS_NULL;
		return encode.substring(encode.lastIndexOf("/") + 1);
	}

	public static String getFilePathFromEncode(String encode) {
		if (encode.equals("") | encode.equals(null))
			return Protocols.IS_NULL;
		return encode.substring(encode.indexOf("/"));
	}

	public static String getParentPathFromEncode(String encode) {
		if (encode.equals("") | encode.equals(null))
			return Protocols.IS_NULL;
		return encode.substring(encode.indexOf("/"), encode.lastIndexOf("/"));
	}

	public static String produceRelativePathEcodes(File f, String parentPath) {
		String filePath = f.getAbsolutePath();
		return f.length()
				+ filePath.replaceFirst(
						parentPath.substring(0, parentPath.lastIndexOf("/")),
						"");
	}

	public static String getParentNameFromEncode(String encode) {
		return getFileNameFromEncode(getParentPathFromEncode(encode));
	}

	public static Long getFileSizeFromEncode(String encode) {
		if (encode.equals("") | encode.equals(null))
			return (long) 0;
		return Long.parseLong(encode.substring(0, encode.indexOf("/")));
	}

	public static String[] splitByMainSeperator(String data) {
		if (data.equals("") | data.equals(null)) {
			String[] r = new String[0];
			return r;
		}
		return data.split(Protocols.MAIN_SEPERATOR);
	}

	public static String[] splitBySubSeperator(String data) {
		if (data.equals("") | data.equals(null)) {
			String[] r = { Protocols.IS_NULL, Protocols.IS_NULL };
			return r;
		}
		return data.split(Protocols.SUB_SEPERATOR);
	}

	public static String clubByMainSeperator(String code, String clubbedArray) {
		if (code.equals(null) | code.equals(""))
			return Protocols.IS_NULL;
		return code + Protocols.MAIN_SEPERATOR + clubbedArray;
	}

	public static String clubBySubSeperator(String[] dataArray) {
		if ((dataArray.length == 0) | (dataArray.equals(null)))
			return Protocols.IS_NULL;
		String clubbedString = "";
		for (String s : dataArray)
			clubbedString = clubbedString + s + Protocols.SUB_SEPERATOR;
		return clubbedString.substring(0, clubbedString.length() - 1);
	}

	public static String clubBySubSeperator(ArrayList<String> dataList) {
		if (dataList.isEmpty() | dataList.equals(null))
			return Protocols.IS_NULL;
		String clubbedString = "";
		for (String s : dataList)
			clubbedString = clubbedString + s + Protocols.SUB_SEPERATOR;
		return clubbedString.substring(0, clubbedString.length() - 1);
	}

	public static String createDataStringOfEntireFolder(File folder) {
		File[] files = folder.listFiles();
		if (files.length == 0)
			return Protocols.IS_NULL;
		String dataString = "";
		for (File f : files) {
			if (!((!f.isHidden()) && f.canRead() && f.exists()))
				continue;
			if (f.isDirectory())
				dataString = dataString + Protocols.FOLDER
						+ f.getAbsolutePath() + Protocols.SUB_SEPERATOR;
			else if (f.isFile())
				dataString = dataString + Protocols.FILE + f.getAbsolutePath()
						+ Protocols.SUB_SEPERATOR;
		}
		return dataString.equals("") ? "" : dataString.substring(0,
				dataString.length() - 1);
	}

	public static boolean checkFile(String encode) {
		if (encode.substring(0, encode.indexOf("/")).equals(Protocols.FILE))
			return true;
		else if (encode.substring(0, encode.indexOf("/")).equals(
				Protocols.FOLDER))
			return false;
		return true;
	}

	public static String createDownloadEncodeOfFile(File f) {
		if (!((!f.isHidden()) && f.canRead() && f.exists()))
			return Protocols.IS_NULL;
		return f.length() + f.getAbsolutePath();
	}

	public static String createListEncodeOfFile(File f) {
		if (!((!f.isHidden()) && f.canRead() && f.exists()))
			return Protocols.IS_NULL;
		if (f.isDirectory())
			return Protocols.FOLDER + f.getAbsolutePath();
		else if (f.isFile())
			return Protocols.FILE + f.getAbsolutePath();
		return Protocols.IS_NULL;
	}

	public static boolean copyInputStreamToOutputStream(
			InputStream inputStream, OutputStream outputStream, long fileSize) {
		if (inputStream.equals(null) | outputStream.equals(null)
				| fileSize == 0)
			return false;
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

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		return true;
	}
}