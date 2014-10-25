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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class Server extends Activity implements OnItemClickListener {

	TextView tvServerIP, tvSelfIP;
	private WifiManager wifiManager;
	private static final String TAG = "folderShare";
	private static final int PORTNUMBER = 9999;
	private String response = "", request = "";
	private String pathString = "";

	private ListView lvMyFolders;
	private ArrayList<CustomListItem> customList;
	private ArrayList<String> encodedList, selectedEncodedList,
			actualEncodedList, relativeEncodeList;
	private ServerCustomListAdapter customAdapter;
	private File currentFolder = null;

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

		encodedList = new ArrayList<String>();
		selectedEncodedList = new ArrayList<String>();
		actualEncodedList = new ArrayList<String>();
		relativeEncodeList = new ArrayList<String>();
		
		customList = new ArrayList<CustomListItem>();
		customAdapter = new ServerCustomListAdapter(this,
				R.layout.listitem_layout, customList);
		
		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setAdapter(customAdapter);
		lvMyFolders.setOnItemClickListener(this);

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File externalStorage = Environment.getExternalStorageDirectory();
			File[] subFiles = externalStorage.listFiles();
			customList.clear();
			encodedList.clear();

			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					if (f.isFile())
						customList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						customList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					encodedList.add(Protocols.createListEncodeOfFile(f));
				}
			}
			currentFolder = externalStorage;
		} else {
			encodedList.clear();
			customList.clear();
		}
		customAdapter.notifyDataSetChanged();
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
//			displayToast("is file");
			return;
		}
		File[] subFiles = file.listFiles();
		encodedList.clear();
		customList.clear();
		if (subFiles.length != 0) {
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					if (f.isFile())
						customList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						customList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					encodedList.add(Protocols.createListEncodeOfFile(f));
				}
			}
		}
		currentFolder = file;
		customAdapter.notifyDataSetChanged();
	}

	public void updateCheckboxes(int pos) {
		if (selectedEncodedList.contains(encodedList.get(pos))) {
			customList.get(pos).setCheckedState(true);
			customAdapter.notifyDataSetChanged();
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
							if (!((!f.isHidden()) && f.exists() && f.canRead()&&(f.length()!=0)))
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
				actualEncodedList.add(Protocols.createDownloadEncodeOfFile(f));
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.server_actionbar_items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.action_back) {
			if (currentFolder.getAbsolutePath().equals("/storage")) {
				return true;
			}
			String parent = currentFolder.getParent();
			currentFolder = new File(parent);
			File[] subFiles = currentFolder.listFiles();
			customList.clear();
			encodedList.clear();
			if (subFiles.length == 0)
				return true;
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					if (f.isFile())
						customList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						customList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					encodedList.add(Protocols.createListEncodeOfFile(f));
				}
			}
			customAdapter.notifyDataSetChanged();
			return true;

		} else if (item.getItemId() == R.id.acttion_upload) {
			pathString = Protocols.clubBySubSeperator(selectedEncodedList);
			displayToast("selected folders uploaded");
//			displayToast(pathString);
			return true;

		} else if (item.getItemId() == R.id.action_refresh) {
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
			customAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}

	public void updateSelectedList(int pos, boolean add) {
		if (add) {
			if (selectedEncodedList.contains(encodedList.get(pos)))
				return;
			selectedEncodedList.add(encodedList.get(pos));
			customList.get(pos).setCheckedState(true);
			Toast.makeText(getApplicationContext(), pos + " added",
					Toast.LENGTH_SHORT).show();
		}

		else {
			if (!(selectedEncodedList.contains(encodedList.get(pos))))
				return;
			selectedEncodedList.remove(encodedList.get(pos));
			customList.get(pos).setCheckedState(false);
			Toast.makeText(getApplicationContext(), pos + " removed",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onBackPressed() {
		new AlertDialog.Builder(Server.this)
				.setCancelable(false)
				.setIcon(R.drawable.icon_skynet)
				.setTitle("Logout")
				.setMessage("Would you like to exit ?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {								
								finish(); // Call finish here.
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// user doesn't want to logout
						Toast.makeText(getApplicationContext(), "welcome back",
								Toast.LENGTH_LONG).show();
					}
				}).show();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}