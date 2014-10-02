package com.example.skynet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class SendFile extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	WifiManager wifiManager;
	Socket clientSocket=null;
	TextView tvServerIP, tvSelfIP, tvUpload, tvRefresh;

	public static final String TAG = "wifi";
	public static final int PORTNUMBER = 9999;
	public String response, request;
	public static volatile boolean waitingForrequest = true;

	ListView lvMyFolders;
	ArrayList<String> folderList, folderName, selectedFolders, parentPath;
	ArrayAdapter<String> myFolderArrayAdapter;
	File folder;
	File[] folders;
	TextView tvBack;
	String selectedFolderName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendfile_layout);

	}

	class ListenForClientConnection implements Runnable {
		@SuppressWarnings("resource")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				ServerSocket serverSocket = new ServerSocket(PORTNUMBER);
				Log.i(TAG, "server ready ");
				while (true)
					clientSocket = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void WifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		final DhcpInfo dhcp = wifiManager.getDhcpInfo();

		tvServerIP
				.setText(Protocols.convertIntIPtoStringIP(dhcp.serverAddress));
		tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));

		Toast.makeText(
				getApplicationContext(),
				Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
						+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
						+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress)
						+ ":"
						+ Protocols.convertIntIPtoStringIP(dhcp.ipAddress),
				Toast.LENGTH_SHORT).show();
	}

	public void UIInitialization() {
		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);

		tvRefresh = (TextView) findViewById(R.id.tvRefresh);
		tvRefresh.setOnClickListener(this);

		tvUpload = (TextView) findViewById(R.id.tvUpload);
		tvUpload.setOnClickListener(this);

		// ///////////////////////////////////////////////////////////////////////////

		tvBack = (TextView) findViewById(R.id.tvBack);
		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);

		folderList = new ArrayList<String>();
		selectedFolders = new ArrayList<String>();
		folderName = new ArrayList<String>();
		parentPath = new ArrayList<String>();

		lvMyFolders.setOnItemClickListener(this);
		lvMyFolders.setOnItemLongClickListener(this);

		myFolderArrayAdapter = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				folderName);
		lvMyFolders.setAdapter(myFolderArrayAdapter);

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			folder = Environment.getExternalStorageDirectory();
			folders = folder.listFiles();
			folderList.clear();
			folderName.clear();
			if (!folders.equals(null)) {
				for (File f : folders) {
					if (!f.equals(null)) {
						folderList.add(f.getAbsolutePath());
						folderName.add(f.getName());
					}
				}
			}
		} else {
			folderList.clear();
			folderName.clear();
			folderName.add("unmounted");
		}
		myFolderArrayAdapter.notifyDataSetChanged();
		tvBack.setOnClickListener(this);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if (selectedFolders.contains(folderList.get(arg2))) {
			arg1.setBackgroundColor(Color.TRANSPARENT);
			selectedFolders.remove(folderList.get(arg2));
		} else {
			arg1.setBackgroundColor(Color.BLUE);
			selectedFolders.add(folderList.get(arg2));
		}
		myFolderArrayAdapter.notifyDataSetChanged();
		selectedFolderName = "";
		for (String folder : selectedFolders) {
			selectedFolderName = selectedFolderName + folder + "$";
		}
		Toast.makeText(getApplicationContext(), selectedFolderName,
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		folder = new File(folderList.get(position));
		if (folder.isFile()) {
			Toast.makeText(getApplicationContext(), "is file",
					Toast.LENGTH_SHORT).show();
		} else {
			folders = folder.listFiles();
			folderList.clear();
			folderName.clear();
			if (!folders.equals(null)) {
				for (File f : folders) {
					if (!f.equals(null)) {
						folderList.add(f.getAbsolutePath());
						folderName.add(f.getName());
					}
				}
			}
			myFolderArrayAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.tvBack) {
			// TODO Auto-generated method stub
			if (folder.getAbsolutePath().equals("/storage")) {
				Toast.makeText(getApplicationContext(), ".......",
						Toast.LENGTH_SHORT).show();
				return;
			}

			String parent = folder.getParent().toString();
			folder = new File(parent);
			folders = folder.listFiles();
			folderList.clear();
			folderName.clear();
			if (!folders.equals(null)) {
				for (File f : folders) {
					if (!f.equals(null)) {
						folderList.add(f.getAbsolutePath());
						folderName.add(f.getName());
					}
				}
			}
			myFolderArrayAdapter.notifyDataSetChanged();
			lvMyFolders.setBackgroundColor(Color.TRANSPARENT);
		}

		else if (v.getId() == R.id.tvRefresh) {
			// TODO Auto-generated method stub
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			Toast.makeText(
					getApplicationContext(),
					Protocols.convertIntIPtoStringIP(dhcp.dns1)
							+ ":"
							+ Protocols.convertIntIPtoStringIP(dhcp.gateway)
							+ ":"
							+ Protocols
									.convertIntIPtoStringIP(dhcp.serverAddress)
							+ ":"
							+ Protocols.convertIntIPtoStringIP(dhcp.ipAddress),
					Toast.LENGTH_SHORT).show();
		}

		else if (v.getId() == R.id.tvUpload) {
			// TODO Auto-generated method stub
			request = selectedFolderName;
			waitingForrequest = false;
		}
	}

	public String getString(File[] list) {
		String itemString = null;
		for (File item : list) {
			itemString = itemString + item.getName() + ":";
		}
		itemString = itemString.substring(0, itemString.length() - 1);
		return itemString;
	}

	class ConnectToClient implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ServerSocket serverSocket = null;

			try {
				serverSocket = new ServerSocket(PORTNUMBER);
				Socket clientSocket = null;
				Log.i(TAG, "server ready ");

				while (true) {
					clientSocket = serverSocket.accept();
					DataInputStream dis = new DataInputStream(
							clientSocket.getInputStream());
					request = dis.readUTF();
					String[] requestArray = request.split("#");
					String code = requestArray[0];
					if (code.equals(Protocols.SSRC_GET_FOLDER_LIST)) {
						while (waitingForrequest)
							;

						DataOutputStream dos = new DataOutputStream(
								clientSocket.getOutputStream());
						response = "20#"
								+ selectedFolderName.substring(0,
										selectedFolderName.length() - 1);
						dos.writeUTF(request);
						dos.close();
					} else if (code
							.equals(Protocols.SSRC_GET_SELECTED_FOLDER_LIST)) {
						DataOutputStream dos = new DataOutputStream(
								clientSocket.getOutputStream());
						String path = requestArray[1];
						File folder = new File(path);
						File[] folders = folder.listFiles();
						response = "20#";
						for (File f : folders)
							response = response + f.getAbsolutePath() + "$";
						response = response.substring(0, response.length() - 1);
						dos.writeUTF(response);
						dos.close();
					} else if (code.equals(Protocols.SSRC_DOWNLOAD_FOLDER)) {
						String[] paths = requestArray[1].split("$");
						for (String path : paths) {
							File f = new File(path);
							BufferedOutputStream bos = new BufferedOutputStream(
									clientSocket.getOutputStream());
							BufferedInputStream bis = new BufferedInputStream(
									new FileInputStream(f));
							Protocols.copyInputStreamToOutputStream(bis, bos,
									f.length());
							bis.close();
							bos.close();
						}
					} else {
						DataOutputStream dos = new DataOutputStream(
								clientSocket.getOutputStream());
						dos.writeUTF("22#");
						dos.close();
					}
					dis.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	class Client extends AsyncTask<String, String, String> {

		@SuppressWarnings("resource")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {
				Socket clientSocket = new Socket();
				InetAddress inetAddress = null;
				clientSocket.connect(new InetSocketAddress(inetAddress,
						PORTNUMBER), 0);
				if (clientSocket.isConnected()) {
					Log.i(TAG, "socket connected");
				} else {
					Log.i(TAG, "socket not connected");
					return null;
				}
				DataOutputStream dos = new DataOutputStream(
						clientSocket.getOutputStream());
				dos.writeUTF(params[0]);
				dos.close();

				DataInputStream dis = new DataInputStream(
						clientSocket.getInputStream());
				request = dis.readUTF();
				String[] requestArray = request.split("#");
				String code = requestArray[0];
				if (code.equals(Protocols.CSRC_SET_FOLDER_LIST)) {
					String[] filepaths = requestArray[1].split("$");
					folderName.clear();
					for (String paths : filepaths) {
						folderName
								.add(paths.substring(paths.lastIndexOf("\\") + 1));
						parentPath.add(paths.substring(0,
								paths.lastIndexOf("\\")));
					}
				}

				clientSocket.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			displayToast("file trasfer complete");
		}

	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}
