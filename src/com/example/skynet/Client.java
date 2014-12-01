package com.example.skynet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Client extends Activity implements OnClickListener,
		OnItemClickListener {

	
	TextView tvServerIP, tvSelfIP;
	EditText etIP;
	LinearLayout ll1;
	ImageButton ibConnect;
	ProgressDialog progressDialog;
	ListView lvMyFolders;
	
	private ArrayList<CustomListItem> customList;
	private ArrayList<String> encodedList, selectedEncodedList, originalEncodeList;
	private ClientCustomListAdapter customAdapter;
	
	WifiManager wifiManager;
	DhcpInfo dhcpInfo;

	private int choice2 = -1;
	private Socket clientSocket = null;
	private InputStream is = null;
	private OutputStream os = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private BufferedInputStream bis = null;
	
	private String response="", request="";
	private static volatile boolean isConnected = false;
	private String currentFolderPath = "";
	private String serverIP="";
	
	private static final String TAG = "folderShare";
	private static final int PORTNUMBER = 9999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_layout);
		wifiPeriferalInitialization();
		UIInitialization();
	}

	public void UIInitialization() {
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);
		serverIP = Protocols.convertIntIPtoStringIP(dhcpInfo.serverAddress);
		tvServerIP.setText(Protocols
				.convertIntIPtoStringIP(dhcpInfo.serverAddress));
		tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcpInfo.ipAddress));
		etIP = (EditText) findViewById(R.id.etIP);

		Intent intent = getIntent();
		choice2 = intent.getIntExtra("choice2", -1);
		if (choice2 == 1)
			ll1.setVisibility(View.GONE);
		else if (choice2 == 3)
			etIP.setHint("Dont enter IP if directly connected");

		ibConnect = (ImageButton) findViewById(R.id.ibConnect);
		ibConnect.setOnClickListener(this);
		
		customList = new ArrayList<CustomListItem>();
		encodedList = new ArrayList<String>();
		selectedEncodedList = new ArrayList<String>();
		originalEncodeList = new ArrayList<String>();

		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		customAdapter = new ClientCustomListAdapter(this,
				R.layout.listitem_layout, customList);
		lvMyFolders.setAdapter(customAdapter);
		lvMyFolders.setOnItemClickListener(this);	
	}

	public void wifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		dhcpInfo = wifiManager.getDhcpInfo();
//		clientSocket = new Socket();
//
//		// displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
//		// + Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
//		// + Protocols.convertIntIPtoStringIP(dhcp.serverAddress) + ":"
//		// + Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
//
//		// //////////////////to prevent crash at on destroy....../////////
//		try {
//			is = clientSocket.getInputStream();
//			os = clientSocket.getOutputStream();
//			dis = new DataInputStream(is);
//			dos = new DataOutputStream(os);
//			bis = new BufferedInputStream(is);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if (Protocols.checkFile(encodedList.get(position)))
			return;
		currentFolderPath = encodedList.get(position);
		new GetSelectedFolderList().execute(currentFolderPath);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.ibConnect) {
			if (choice2 == 2) {
				new ConnectClientToServer().execute(etIP.getText().toString()
						.trim());
			} else if (choice2 == 3) {
				if (etIP.getText().toString() == "") {
					new ConnectClientToServer().execute(serverIP);
				} else {
					new ConnectClientToServer().execute(etIP.getText()
							.toString().trim());
				}
			} else {
				new ConnectClientToServer().execute(serverIP);
			}
		}
	}

	public void updateCheckboxes(int pos) {
		if (selectedEncodedList.contains(encodedList.get(pos))) {
			customList.get(pos).setCheckedState(true);
			customAdapter.notifyDataSetChanged();
		}
	}

	public void updateSelectedList(int pos, boolean add) {
		if (add) {
			if (selectedEncodedList.contains(encodedList.get(pos)))
				return;
			selectedEncodedList.add(encodedList.get(pos));
			customList.get(pos).setCheckedState(true);
		}

		else {
			if (!(selectedEncodedList.contains(encodedList.get(pos))))
				return;
			selectedEncodedList.remove(encodedList.get(pos));
			customList.get(pos).setCheckedState(false);
		}
	}

	class ConnectClientToServer extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("connecting socket");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				clientSocket = new Socket();
				InetAddress inetAddress = InetAddress.getByName(params[0]);
				clientSocket.connect(new InetSocketAddress(inetAddress,
						PORTNUMBER));
				isConnected = true;
				is = clientSocket.getInputStream();
				os = clientSocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);
				bis = new BufferedInputStream(is);
				if (clientSocket.isConnected()) {
					publishProgress("sockets connected");
					return true;
				} else
					publishProgress("sockets could not connect");
				return false;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				publishProgress("exception while connecting sockets");
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result)
				Log.i(TAG, "socket connected");
			else
				Log.i(TAG, "socket disconnected");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			displayToast(values[0]);
		}
	}

	class GetFolderList extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("fetching folder list");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {
				request = Protocols.clubByMainSeperator(
						Protocols.GET_FOLDER_LIST, params[0]);
				if (clientSocket.isClosed() | clientSocket.isInputShutdown()
						| clientSocket.isOutputShutdown()
						| !clientSocket.isConnected())
					return false;
				dos.writeUTF(request);
				dos.flush();

				response = dis.readUTF();
				if (response.equals(null)|response.equals(""))
					return false;

				String[] paths = Protocols.splitBySubSeperator(response);
				encodedList.clear();
				customList.clear();
				originalEncodeList.clear();
				for (String path : paths) {
					encodedList.add(path);
					originalEncodeList.add(path);
					if (Protocols.checkFile(path))
						customList.add(new CustomListItem(
								R.drawable.ic_action_view_as_list, Protocols
										.getFileNameFromEncode(path), false));
					else
						customList.add(new CustomListItem(
								R.drawable.ic_action_collection, Protocols
										.getFileNameFromEncode(path), false));
				}
				currentFolderPath = Protocols.IS_NULL;
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result)
				customAdapter.notifyDataSetChanged();
			else
				displayToast("unable to fetch list");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			displayToast(values[0]);
		}
	}

	class GetSelectedFolderList extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("fetching folder list");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				request = Protocols.clubByMainSeperator(
						Protocols.GET_SELECTED_FOLDER_LIST, params[0]);
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				response = dis.readUTF();
				if (response.equals(null)| response.equals(""))
					return false;
				else {
					String[] paths = response.split(Protocols.SUB_SEPERATOR);
					encodedList.clear();
					customList.clear();
					for (String path : paths) {
						encodedList.add(path);
						if (Protocols.checkFile(path))
							customList.add(new CustomListItem(
									R.drawable.ic_action_view_as_list,
									Protocols.getFileNameFromEncode(path),
									false));
						else
							customList
									.add(new CustomListItem(
											R.drawable.ic_action_collection,
											Protocols
													.getFileNameFromEncode(path),
											false));
					}
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result)
				customAdapter.notifyDataSetChanged();
			else
				displayToast("unable to fetch list");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			displayToast(values[0]);
		}
	}

	class GetParentFolder extends AsyncTask<String, String, Boolean> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("fetching folder list");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				request = Protocols.clubByMainSeperator(Protocols.GET_PARENT,
						params[0]);
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				response = dis.readUTF();
				if (response.equals(null)| response.equals(""))
					return false;
				else {
					String[] paths = Protocols.splitBySubSeperator(response);
					encodedList.clear();
					customList.clear();
					for (String path : paths) {
						encodedList.add(path);
						if (Protocols.checkFile(path))
							customList.add(new CustomListItem(
									R.drawable.ic_action_view_as_list,
									Protocols.getFileNameFromEncode(path),
									false));
						else
							customList
									.add(new CustomListItem(
											R.drawable.ic_action_collection,
											Protocols
													.getFileNameFromEncode(path),
											false));
					}
					currentFolderPath = (long) 0
							+ Protocols.getParentPathFromEncode(paths[0]);
					return true;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result)
				customAdapter.notifyDataSetChanged();
			else
				displayToast("unable to fetch list");
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			displayToast(values[0]);
		}
	}

	class DownloadFolders extends AsyncTask<String, String, Boolean> {
		NotificationCompat.Builder nb;
		NotificationManager nm;
		Intent i;
		PendingIntent pi;

		@SuppressLint("NewApi")
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(1);
			nb = new NotificationCompat.Builder(getApplicationContext());

			i = new Intent(getApplicationContext(), Client.class);
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

			nb.setContentIntent(pi)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.icon_skynet))
					.setContentTitle("Skynet")
					.setContentText("preparing files for download")
					.setTicker("preparing for file transfer")
					.setSmallIcon(R.drawable.notification_icon_animation)
					.setDefaults(Notification.DEFAULT_ALL)
					.setOnlyAlertOnce(true).setOngoing(true);

			progressDialog = new ProgressDialog(Client.this);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle("Skynet : Downloading Files....");
			progressDialog.setMessage("preparing files for download");
			progressDialog.setIcon(R.drawable.ic_action_download_dark);
			progressDialog.setMax(100);

			nm.notify(1, nb.build());
			progressDialog.show();
			nb.setContentTitle("Skynet : Downloading");

		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				request = Protocols.clubByMainSeperator(
						Protocols.PREPARE_FOR_DOWNLOAD, params[0]);
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				File mainFolder = new File(
						Environment.getExternalStorageDirectory() + "/"
								+ getPackageName());
				if (!mainFolder.exists())
					mainFolder.mkdirs();

				response = dis.readUTF();

				final String[] encodes = Protocols
						.splitBySubSeperator(response);
				for (String encode : encodes) {
					String parentPath = Protocols
							.getParentPathFromEncode(encode);
					File folder = new File(mainFolder.getAbsolutePath()
							+ parentPath);
					if (!folder.exists())
						folder.mkdirs();
				}
				request = Protocols.clubByMainSeperator(
						Protocols.START_DOWNLOAD, "");
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				progressDialog.setMax(encodes.length);

				int x = 1;
				for (String encode : encodes) {
					File file = new File(mainFolder.getAbsoluteFile()
							+ Protocols.getParentPathFromEncode(encode),
							Protocols.getFileNameFromEncode(encode));
					String[] progress = { file.getName(), "" + encodes.length,
							"" + x };
					publishProgress(progress);
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(file));
					Protocols.copyInputStreamToOutputStream(bis, bos,
							Protocols.getFileSizeFromEncode(encode));
					bos.flush();
					bos.close();
					++x;
				}
				selectedEncodedList.clear();
				progressDialog.cancel();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			String text = values[2] + "/" + values[1];
			nb.setContentText(text).setProgress(Integer.parseInt(values[1]),
					Integer.parseInt(values[2]), false);
			nm.notify(1, nb.build());
			progressDialog.setMessage(values[0]);
			progressDialog.incrementProgressBy(1);
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// progressDialog.cancel();
			nm.cancel(1);
			nb = new NotificationCompat.Builder(getApplicationContext());
			nb.setContentIntent(pi)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.icon_skynet))
					.setDefaults(Notification.DEFAULT_ALL)
					.setSmallIcon(R.drawable.ic_action_download)
					.setContentText(
							"Press notification to go back to application");
			if (result) {
				nb.setTicker("Download Successful...").setContentTitle(
						"Download Complete");

				displayToast("downloading complete");
				customAdapter.notifyDataSetChanged();
			} else {
				nb.setTicker("Download Unsuccessful...").setContentTitle(
						"Download Incomplete");
				displayToast("downloading failed");
			}
			nm.notify(2, nb.build());
//			startActivity(new Intent(Client.this, Client.class));
//			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.client_actionbar_items, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.action_back) {
			if (originalEncodeList.equals(encodedList))
				return true;

			String s = Protocols.getParentPathFromEncode(encodedList.get(0));
			for (String originalEncodePath : originalEncodeList) {
				if (s.equals(Protocols
						.getFilePathFromEncode(originalEncodePath))) {
					customList.clear();
					encodedList.clear();
					for (String path : originalEncodeList) {
						if (Protocols.checkFile(path))
							customList.add(new CustomListItem(
									R.drawable.ic_action_view_as_list,
									Protocols.getFileNameFromEncode(path),
									false));
						else
							customList
									.add(new CustomListItem(
											R.drawable.ic_action_collection,
											Protocols
													.getFileNameFromEncode(path),
											false));
						encodedList.add(path);
					}
					currentFolderPath = Protocols.IS_NULL;
					customAdapter.notifyDataSetChanged();
					return true;
				}
			}
			new GetParentFolder().execute(currentFolderPath);
			return true;
		}

		else if (item.getItemId() == R.id.action_download) {
			if (selectedEncodedList.size() == 0)
				return true;
			uncheckAllChildrenCascade(lvMyFolders);
			String pathString = Protocols
					.clubBySubSeperator(selectedEncodedList);
			new DownloadFolders().execute(pathString);
			return true;
		} else if (item.getItemId() == R.id.action_getlist) {
			if (isConnected)
				new GetFolderList().execute("");
		} else if (item.getItemId() == R.id.action_refresh) {
			selectedEncodedList.clear();
			startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("choice2", choice2));
			finish();
			return true;
		}
		return false;
	}

	private void uncheckAllChildrenCascade(ViewGroup vg) {
		for (int i = 0; i < vg.getChildCount(); i++) {
			View v = vg.getChildAt(i);
			if (v instanceof CheckBox) {
				((CheckBox) v).setChecked(false);
			} else if (v instanceof ViewGroup) {
				uncheckAllChildrenCascade((ViewGroup) v);
			}
		}
	}


	public void onBackPressed() {
		finish();
		startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("choice2", choice2));
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			if (!isConnected)
				return;
			dos.flush();
			os.close();
			is.close();
			dos.close();
			dis.close();
			bis.close();
			clientSocket.shutdownOutput();
			clientSocket.shutdownInput();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}