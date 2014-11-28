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
import java.util.Collection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

	TextView tvServerIP, tvSelfIP, tvServerIpAddress, tvSelfIpAddress;
	private WifiManager wifiManager;
	private DhcpInfo dhcpInfo;
	private static final String TAG = "folderShare";
	private static final int PORTNUMBER = 9999;
	private String response = "", request = "";
	private String pathString = "";
	private SharedPreferences selectedFoldersPrefernces;
	private SharedPreferences.Editor preferenceEditor;
	private ListView lvMyFolders, lvSelectedFolders;
	private ArrayList<CustomListItem> displayList;
	private ArrayList<String> currentDirectoryEncodeList,
			selectedFoldersEncodedList, sharedFoldersActualEncodedList,
			sharedFoldersRelativeEncodeList;
	private SelectedItemListAdapter selectedAdapter;
	private ArrayList<CustomListItem> selectedDisplayList;

	private ServerCustomListAdapter customAdapter;
	private File currentFolder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_layout);
		// startService(new Intent(Server.this,
		// ListenForClientConnection.class));
		new Thread(new ListenForClientConnection()).start();
		wifiPeriferalInitialization();
		UIInitialization();
	}

	public void wifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		dhcpInfo = wifiManager.getDhcpInfo();
	}

	@SuppressWarnings("unchecked")
	public void UIInitialization() {

		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);
		tvServerIpAddress = (TextView) findViewById(R.id.tvServerIpAddress);
		tvSelfIpAddress = (TextView) findViewById(R.id.tvSelfIPAddress);

		Intent intent = getIntent();
		int choice2 = intent.getIntExtra("choice2", -1);
		if (choice2 == 1) {
			tvServerIP.setVisibility(View.GONE);
			tvSelfIP.setVisibility(View.GONE);
			tvServerIpAddress.setVisibility(View.GONE);
			tvSelfIpAddress.setVisibility(View.GONE);
		} else if (choice2 == 3) {
			if (!wifiManager.isWifiEnabled()) {
				tvServerIP.setVisibility(View.GONE);
				tvSelfIP.setVisibility(View.GONE);
				tvServerIpAddress.setVisibility(View.GONE);
				tvSelfIpAddress.setVisibility(View.GONE);
			}
		}
		tvServerIP.setText(Protocols
				.convertIntIPtoStringIP(dhcpInfo.serverAddress));
		tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcpInfo.ipAddress));
		//***************************************************************************************************
		currentDirectoryEncodeList = new ArrayList<String>();
		selectedFoldersEncodedList = new ArrayList<String>();
		sharedFoldersActualEncodedList = new ArrayList<String>();
		sharedFoldersRelativeEncodeList = new ArrayList<String>();
		selectedFoldersPrefernces = getSharedPreferences("skynet", MODE_PRIVATE);
		preferenceEditor = selectedFoldersPrefernces.edit();
		selectedFoldersEncodedList = new ArrayList<String>(
				(Collection<? extends String>) selectedFoldersPrefernces
						.getAll().values());
		//***************************************************************************************************
		displayList = new ArrayList<CustomListItem>();
		customAdapter = new ServerCustomListAdapter(this,
				R.layout.listitem_layout, displayList);
		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setAdapter(customAdapter);
		lvMyFolders.setOnItemClickListener(this);
		
		selectedDisplayList = new ArrayList<CustomListItem>();
		for (String s : selectedFoldersEncodedList)
			selectedDisplayList
					.add(new CustomListItem(
							(s.substring(0, s.indexOf("/")).equals("folder")) ? R.drawable.ic_action_collection
									: R.drawable.ic_action_view_as_list,
							Protocols.getFilePathFromEncode(s), true));
		selectedAdapter = new SelectedItemListAdapter(Server.this,
				R.layout.listitem_layout, selectedDisplayList);
		lvSelectedFolders = (ListView) findViewById(R.id.lvSelectedItems);
		lvSelectedFolders.setAdapter(selectedAdapter);
		selectedAdapter.notifyDataSetChanged();
		//**************************************************************************************************
		pathString = Protocols.clubBySubSeperator(selectedFoldersEncodedList);
		//**************************************************************************************************
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			File externalStorage = Environment.getExternalStorageDirectory();
			File[] subFiles = externalStorage.listFiles();
			displayList.clear();
			currentDirectoryEncodeList.clear();

			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()
						&& f.length() != 0) {
					if (f.isFile())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					currentDirectoryEncodeList.add(Protocols
							.createListEncodeOfFile(f));
				}
			}
			currentFolder = externalStorage;
		} else {
			currentDirectoryEncodeList.clear();
			displayList.clear();
		}
		customAdapter.notifyDataSetChanged();
		//**************************************************************************************************
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub

		File file = new File(
				Protocols.getFilePathFromEncode(currentDirectoryEncodeList
						.get(position)));
		if (!((!file.isHidden()) && file.exists() && file.canRead()))
			return;
		if (file.isFile())
			return;

		File[] subFiles = file.listFiles();
		currentDirectoryEncodeList.clear();
		displayList.clear();
		if (subFiles.length != 0) {
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					if (f.isFile())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					currentDirectoryEncodeList.add(Protocols
							.createListEncodeOfFile(f));
				}
			}
		}
		currentFolder = file;
		customAdapter.notifyDataSetChanged();
	}

	public void updateCheckboxes(int pos) {
		if (selectedFoldersEncodedList.contains(currentDirectoryEncodeList
				.get(pos))) {
			displayList.get(pos).setCheckedState(true);
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

	// class ListenForClientConnection extends IntentService {
	// public ListenForClientConnection(String name) {
	// super("skynet");
	// // TODO Auto-generated constructor stub
	// }
	//
	//
	// @Override
	// protected void onHandleIntent(Intent intent) {
	// // TODO Auto-generated method stub
	// try {
	// @SuppressWarnings("resource")
	// ServerSocket serverSocket = new ServerSocket(PORTNUMBER);
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// displayToast("server ready");
	// }
	// });
	// Log.i(TAG, "server ready ");
	// while (true) {
	// Socket clientSocket = serverSocket.accept();
	// new Thread(new ListenForInput(clientSocket)).start();
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// displayToast("connected to a new client");
	// }
	// });
	// Log.i(TAG, "socket connectetd");
	// }
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }
	// }

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
						if (!((!folder.isHidden()) && folder.exists()
								&& folder.canRead() && (folder.length() != 0)))
							continue;
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
								.clubBySubSeperator(sharedFoldersRelativeEncodeList);
						dos.writeUTF(response);
						dos.flush();
					} else if (code.equals(Protocols.START_DOWNLOAD)) {
						for (String encode : sharedFoldersActualEncodedList) {
							File f = new File(
									Protocols.getFilePathFromEncode(encode));
							if (!((!f.isHidden()) && f.exists() && f.canRead() && (f
									.length() != 0)))
								continue;
							BufferedInputStream bis = new BufferedInputStream(
									new FileInputStream(f));
							Protocols.copyInputStreamToOutputStream(bis, bos,
									Protocols.getFileSizeFromEncode(encode));
							bos.flush();
							bis.close();
						}
						sharedFoldersActualEncodedList.clear();
						sharedFoldersRelativeEncodeList.clear();

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

			if (!((!f.isHidden()) && f.canRead() && f.exists() && (f.length() != 0)))
				return;
			if (f.isFile()) {
				sharedFoldersActualEncodedList.add(Protocols
						.createDownloadEncodeOfFile(f));
				sharedFoldersRelativeEncodeList.add(Protocols
						.produceRelativePathEcodes(f, filePath));
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
			if (currentFolder.getAbsolutePath().equals("/storage"))
				return true;
			String parent = currentFolder.getParent();
			currentFolder = new File(parent);
			File[] subFiles = currentFolder.listFiles();
			displayList.clear();
			currentDirectoryEncodeList.clear();
			if (subFiles.length == 0)
				return true;
			for (File f : subFiles) {
				if ((!f.isHidden()) && f.exists() && f.canRead()) {
					if (f.isFile())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, f.getName(),
								false));
					else if (f.isDirectory())
						displayList.add(new CustomListItem(
								R.drawable.ic_action_collection, f.getName(),
								false));
					currentDirectoryEncodeList.add(Protocols
							.createListEncodeOfFile(f));
				}
			}
			customAdapter.notifyDataSetChanged();
			return true;

		} else if (item.getItemId() == R.id.acttion_upload) {
			pathString = Protocols
					.clubBySubSeperator(selectedFoldersEncodedList);
			displayToast("Folders uploaded");
			return true;
		} else if (item.getItemId() == R.id.action_getlist) {
			if (lvMyFolders.getVisibility() == View.GONE) {
				lvSelectedFolders.setVisibility(View.GONE);
				lvMyFolders.setVisibility(View.VISIBLE);
			} else {
				selectedDisplayList.clear();
				for (String s : selectedFoldersEncodedList)
					selectedDisplayList
							.add(new CustomListItem(
									(s.substring(0, s.indexOf("/"))
											.equals("folder")) ? R.drawable.ic_action_collection
											: R.drawable.ic_action_view_as_list,
									Protocols.getFilePathFromEncode(s), true));
				lvMyFolders.setVisibility(View.GONE);
				lvSelectedFolders.setVisibility(View.VISIBLE);
				selectedAdapter.notifyDataSetChanged();
			}
		} else if (item.getItemId() == R.id.action_refresh) {
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			selectedFoldersEncodedList.clear();
			preferenceEditor.clear();
			preferenceEditor.commit();
			sharedFoldersActualEncodedList.clear();
			sharedFoldersRelativeEncodeList.clear();
			displayToast("refresh complete");
			customAdapter.notifyDataSetChanged();
			startActivity(new Intent(Server.this, Server.class));
			finish();
			return true;
		}
		return false;
	}

	public void updateSelectedList(int pos, boolean add) {
		if (add) {
			if (selectedFoldersEncodedList.contains(currentDirectoryEncodeList
					.get(pos)))
				return;
			selectedFoldersEncodedList.add(currentDirectoryEncodeList.get(pos));
			preferenceEditor.putString(currentDirectoryEncodeList.get(pos),
					currentDirectoryEncodeList.get(pos));
			displayList.get(pos).setCheckedState(true);
		}

		else {
			if (!(selectedFoldersEncodedList
					.contains(currentDirectoryEncodeList.get(pos))))
				return;
			selectedFoldersEncodedList.remove(currentDirectoryEncodeList
					.get(pos));
			preferenceEditor.remove(currentDirectoryEncodeList.get(pos));
			displayList.get(pos).setCheckedState(false);
		}
		if (preferenceEditor.commit())
			Toast.makeText(getApplicationContext(),
					selectedFoldersPrefernces.toString(), Toast.LENGTH_LONG)
					.show();
		else
			Toast.makeText(getApplicationContext(), "unable to commit",
					Toast.LENGTH_LONG).show();

	}

	public void onBackPressed() {
		startActivity(new Intent(getApplicationContext(), MainActivity.class));
		finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}