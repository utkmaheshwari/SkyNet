package com.example.skynet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class Server extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	TextView tvServerIP, tvSelfIP;
	Button btUpload, btRefresh, btBack;

	private WifiManager wifiManager;
	private static final String TAG = "wifi";
	private static final int PORTNUMBER = 9999;
	private String response="", request="";
	private String pathString = "";

	private ListView lvMyFolders;
	private ArrayList<String> folderNameList, encodedList, selectedEncodedList,
			actualEncodedList, relativeEncodeList;
	private ArrayAdapter<String> arrayAdapter;
	private File currentFolder=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_layout);
		new Thread(new ListenForClientConnection()).start();
		UIInitialization();
		wifiPeriferalInitialization();
	}

	public void wifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		final DhcpInfo dhcp = wifiManager.getDhcpInfo();

		tvServerIP
				.setText(Protocols.convertIntIPtoStringIP(dhcp.serverAddress));
		tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));

		displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
	}

	public void UIInitialization() {
		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);

		btRefresh = (Button) findViewById(R.id.btRefresh);
		btRefresh.setOnClickListener(this);

		btUpload = (Button) findViewById(R.id.btUpload);
		btUpload.setOnClickListener(this);

		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);

		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setOnItemClickListener(this);
		lvMyFolders.setOnItemLongClickListener(this);

		folderNameList = new ArrayList<String>();
		encodedList = new ArrayList<String>();
		selectedEncodedList = new ArrayList<String>();
		actualEncodedList = new ArrayList<String>();
		relativeEncodeList = new ArrayList<String>();

		arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, folderNameList);
		lvMyFolders.setAdapter(arrayAdapter);

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File externalStorage = Environment.getExternalStorageDirectory();
			File[] subFiles = externalStorage.listFiles();
			folderNameList.clear();
			encodedList.clear();

			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					encodedList.add(Protocols.createEncodeOfFile(f));
					folderNameList.add(f.getName());
				}
			}
			currentFolder = externalStorage;
		} else {
			encodedList.clear();
			folderNameList.clear();
		}
		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if (selectedEncodedList.contains(encodedList.get(arg2))) {
			arg1.setBackgroundColor(Color.TRANSPARENT);
			selectedEncodedList.remove(encodedList.get(arg2));
		} else {
			arg1.setBackgroundColor(Color.BLUE);
			selectedEncodedList.add(encodedList.get(arg2));
		}
		arrayAdapter.notifyDataSetChanged();
		Toast.makeText(getApplicationContext(), selectedEncodedList.toString(),
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		File file = new File(Protocols.getFilePathFromEncode(encodedList
				.get(position)));
		if (!((!file.isHidden()) && file.exists() && file.canRead()))
			return;
		if (file.isFile()) {
			displayToast("is file");
			return;
		}
		File[] subFiles = file.listFiles();
		encodedList.clear();
		folderNameList.clear();
		if (subFiles.length != 0) {
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					encodedList.add(Protocols.createEncodeOfFile(f));
					folderNameList.add(f.getName());
				}
			}
		}
		currentFolder = file;
		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btBack) {
			// TODO Auto-generated method stub
			if (currentFolder.getAbsolutePath().equals("/storage")) {
				Toast.makeText(getApplicationContext(), ".......",
						Toast.LENGTH_SHORT).show();
				return;
			}
			String parent = currentFolder.getParent();
			currentFolder = new File(parent);
			File[] subFiles = currentFolder.listFiles();
			folderNameList.clear();
			encodedList.clear();
			if (subFiles.length == 0)
				return;
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					encodedList.add(Protocols.createEncodeOfFile(f));
					folderNameList.add(f.getName());
				}
			}
			arrayAdapter.notifyDataSetChanged();
			lvMyFolders.setBackgroundColor(Color.TRANSPARENT);
		}

		else if (v.getId() == R.id.btRefresh) {
			// TODO Auto-generated method stub
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress)
					+ ":" + Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			selectedEncodedList.clear();
			actualEncodedList.clear();
			relativeEncodeList.clear();
			displayToast("refresh complete");
			arrayAdapter.notifyDataSetChanged();
		}

		else if (v.getId() == R.id.btUpload) {
			// TODO Auto-generated method stub
			pathString = Protocols.clubBySubSeperator(selectedEncodedList);
			displayToast("selected folders uploaded");
			displayToast(pathString);
		}
	}

	class ListenForClientConnection implements Runnable {
		@SuppressWarnings("resource")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				ServerSocket serverSocket = new ServerSocket(PORTNUMBER);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						displayToast("server ready");
					}
				});
				Log.i(TAG, "server ready ");
				while (true) {
					Socket clientSocket = serverSocket.accept();
					new Thread(new ListenForInput(clientSocket)).start();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							displayToast("connected to a new client");
						}
					});
					Log.i(TAG, "socket connected");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class ListenForInput implements Runnable {

		Socket clientSocket = null;

		public ListenForInput(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(TAG, "listening for input");
			InputStream is = null;
			OutputStream os = null;
			DataInputStream dis = null;
			DataOutputStream dos = null;
			BufferedOutputStream bos = null;
			try {
				is = clientSocket.getInputStream();
				os = clientSocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);
				bos = new BufferedOutputStream(os);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while (true) {
				try {
					if (clientSocket.isOutputShutdown()
							|| clientSocket.isInputShutdown())
						break;
					request = dis.readUTF();
					Log.i(TAG, request);
					if (request.equals(null) || request.equals(""))
						continue;
					String code = Protocols.splitByMainSeperator(request)[0];
					Log.i(TAG, code);
					if (code.equals(Protocols.GET_FOLDER_LIST)) {
						Log.i(TAG, pathString);
						dos.writeUTF(pathString);
						dos.flush();
					} else if (code.equals(Protocols.GET_SELECTED_FOLDER_LIST)) {
						String selectedPath = Protocols
								.splitByMainSeperator(request)[1];
						Log.i(TAG, "selectedPath= " + selectedPath);
						String actualPath = Protocols
								.getFilePathFromEncode(selectedPath);
						Log.i(TAG, "actualPath= " + actualPath);
						File folder = new File(actualPath);
						response = Protocols
								.createDataStringOfEntireFolder(folder);
						Log.i(TAG, response);
						dos.writeUTF(response);
						dos.flush();
					} else if (code.equals(Protocols.GET_PARENT)) {
						String encode = Protocols.splitByMainSeperator(request)[1];
						String actualCurrentPath = Protocols
								.getFilePathFromEncode(encode);
						if (actualCurrentPath.equals("/storage"))
							continue;
						File currentFolder = new File(actualCurrentPath);
						File parentFolder = new File(currentFolder.getParent());
						response = Protocols
								.createDataStringOfEntireFolder(parentFolder);
						Log.i(TAG, response);
						dos.writeUTF(response);
						dos.flush();
					} else if (code.equals(Protocols.PREPARE_FOR_DOWNLOAD)) {
						String[] selectedFolderPaths = Protocols
								.splitBySubSeperator(Protocols
										.splitByMainSeperator(request)[1]);
						if (selectedFolderPaths.length == 0)
							continue;

						for (String path : selectedFolderPaths) {
							File f = new File(
									Protocols.getFilePathFromEncode(path));
							getFilesInFolders(f,
									Protocols.getFilePathFromEncode(path));
						}
						response = Protocols
								.clubBySubSeperator(relativeEncodeList);
						dos.writeUTF(response);
						dos.flush();
					} else if (code.equals(Protocols.START_DOWNLOAD)) {
						for (String encode : actualEncodedList) {
							File f = new File(
									Protocols.getFilePathFromEncode(encode));
							if (!((!f.isHidden()) && f.exists() && f.canRead()))
								continue;
							BufferedInputStream bis = new BufferedInputStream(
									new FileInputStream(f));
							Protocols.copyInputStreamToOutputStream(bis, bos,
									Protocols.getFileSizeFromEncode(encode));
							bos.flush();
							bis.close();
						}
						actualEncodedList.clear();
						relativeEncodeList.clear();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				bos.flush();
				dos.flush();
				os.close();
				is.close();
				bos.close();
				dos.close();
				dis.close();
				clientSocket.shutdownOutput();
				clientSocket.shutdownInput();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void getFilesInFolders(File f, String filePath) {

			if (!((!f.isHidden()) && f.canRead() && f.exists()))
				return;
			if (f.isFile()) {
				actualEncodedList.add(Protocols.createEncodeOfFile(f));
				relativeEncodeList.add(Protocols.produceRelativePathEcodes(f,
						filePath));
			} else if (f.isDirectory()) {
				File[] fileList = f.listFiles();
				if (fileList.length == 0)
					return;
				for (File file : fileList)
					getFilesInFolders(file, filePath);
			}
		}
	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}