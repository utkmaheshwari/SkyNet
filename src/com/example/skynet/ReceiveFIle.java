package com.example.skynet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveFIle extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	WifiManager wifiManager;
	TextView tvServerIP, tvSelfIP;
	EditText etIP;
	Button btConnect, btDownload, btGet, btRefresh, btBack;
	public Socket clientSocket;

	public static final String TAG = "wifi";
	public static final int PORTNUMBER = 9999;
	public String response, request;
	public static volatile boolean waitingForrequest = true;
	public String currentFolderPath;

	ListView lvMyFolders;
	ArrayList<String> folderNameList, folderPathList, selectedFolderNameList,
			selectedFolderPathList;
	ArrayAdapter<String> arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receivefile_layout);
		UIInitialization();
		WifiPeriferalInitialization();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class GetFolderList extends AsyncTask<String, Integer, Boolean> {

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
				DataOutputStream dos = new DataOutputStream(
						clientSocket.getOutputStream());
				dos.writeUTF(Protocols.SSRC_GET_FOLDER_LIST);
				dos.close();

				DataInputStream dis = new DataInputStream(
						clientSocket.getInputStream());
				response = dis.readUTF();
				dis.close();

				if (response.equals(null))
					return false;
				else {
					String[] paths = response.split("$");
					folderPathList.clear();
					folderNameList.clear();
					for (String path : paths) {
						folderPathList.add(path);
						String[] pathPeices = path.split("/");
						folderNameList.add(pathPeices[pathPeices.length]);
					}
					arrayAdapter.notifyDataSetChanged();
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
				displayToast("folder list fetched");
			else
				displayToast("unable to fetch list");
		}
	}

	class GetSelectedFoldeList extends AsyncTask<String, Integer, Boolean> {

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
				DataOutputStream dos = new DataOutputStream(
						clientSocket.getOutputStream());
				request = Protocols.SSRC_GET_SELECTED_FOLDER_LIST + "#"
						+ params[0];
				dos.writeUTF(response);
				dos.close();

				DataInputStream dis = new DataInputStream(
						clientSocket.getInputStream());
				response = dis.readUTF();
				dis.close();
				if (response.equals(null))
					return false;
				else {
					String[] paths = response.split("$");
					folderPathList.clear();
					folderNameList.clear();
					for (String path : paths) {
						folderPathList.add(path);
						String[] pathPeices = path.split("/");
						folderNameList.add(pathPeices[pathPeices.length]);
					}
					arrayAdapter.notifyDataSetChanged();
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
				displayToast("folder list fetched");
			else
				displayToast("unable to fetch list");
		}

	}

	class DownloadFolders extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			displayToast("downloading files");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub

			try {
				DataOutputStream dos = new DataOutputStream(
						clientSocket.getOutputStream());
				response = Protocols.SSRC_DOWNLOAD_FOLDER + params[0];
				dos.writeUTF(response);
				dos.close();

				for (String path : selectedFolderPathList) {
					String fileName = path.substring(path.lastIndexOf("/") + 1);
					// DataInputStream dis = new DataInputStream(
					// clientSocket.getInputStream());
					// long fileSize = dis.readLong();
					long fileSize = Long.parseLong(path.split("/")[0]);
					// remember to attach filesize before the each
					// filepath......................
					// dis.close();
					File f = new File(Environment.getExternalStorageDirectory()
							+ "/" + getPackageName() + "/");
					if (!f.exists())
						f.mkdirs();
					File file = new File(f, fileName);
					BufferedInputStream bis = new BufferedInputStream(
							clientSocket.getInputStream());
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(file));
					Protocols.copyInputStreamToOutputStream(bis, bos, fileSize);
					bis.close();
					bos.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			displayToast("File : " + values[0] + " downloaded");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result)
				displayToast("downloading complete");
			else
				displayToast("downloading failed");
		}

	}

	class ConnectClientToServer extends AsyncTask<String, Integer, Boolean> {

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
				if (clientSocket.isConnected()) {
					clientSocket.close();
					return false;
				} else {
					clientSocket = new Socket();
					InetAddress inetAddress = InetAddress.getByName(params[0]);
					clientSocket.connect(new InetSocketAddress(inetAddress,
							PORTNUMBER), 0);
					if (clientSocket.isConnected())
						return true;
					else
						return false;
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
			if (result) {
				displayToast("socket connected");
				Log.i(TAG, "socket connected");
			} else {
				displayToast("socket disconnected");
				Log.i(TAG, "socket disconnected");
			}
		}
	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	public void WifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		clientSocket = new Socket();
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

		btRefresh = (Button) findViewById(R.id.tvRefresh);
		btRefresh.setOnClickListener(this);

		btDownload = (Button) findViewById(R.id.tvDownload);
		btDownload.setOnClickListener(this);

		btGet = (Button) findViewById(R.id.tvGet);
		btGet.setOnClickListener(this);

		btConnect = (Button) findViewById(R.id.btConnect);
		btConnect.setOnClickListener(this);

		btBack = (Button) findViewById(R.id.tvBack);
		btBack.setOnClickListener(this);

		etIP = (EditText) findViewById(R.id.etIP);

		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setOnItemClickListener(this);
		lvMyFolders.setOnItemLongClickListener(this);

		folderNameList = new ArrayList<String>();
		folderPathList = new ArrayList<String>();
		selectedFolderNameList = new ArrayList<String>();
		selectedFolderPathList = new ArrayList<String>();

		arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, folderNameList);
		lvMyFolders.setAdapter(arrayAdapter);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if (selectedFolderPathList.contains(folderPathList.get(arg2))) {
			arg1.setBackgroundColor(Color.TRANSPARENT);
			selectedFolderPathList.remove(folderPathList.get(arg2));

		} else {
			arg1.setBackgroundColor(Color.BLUE);
			selectedFolderPathList.add(folderPathList.get(arg2));
		}
		arrayAdapter.notifyDataSetChanged();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		currentFolderPath = folderPathList.get(position);
		new GetSelectedFoldeList().execute(currentFolderPath);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.tvBack) {
			// TODO Auto-generated method stub
			currentFolderPath = currentFolderPath.substring(0,
					currentFolderPath.lastIndexOf("/"));
			new GetSelectedFoldeList().execute(currentFolderPath);
		}

		else if (v.getId() == R.id.tvRefresh) {
			// TODO Auto-generated method stub
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress)
					+ ":" + Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
		}

		else if (v.getId() == R.id.tvDownload) {
			// TODO Auto-generated method stub
			String pathString = "";
			for (String path : selectedFolderPathList)
				pathString = pathString + path + "$";
			pathString = pathString.substring(0, pathString.length() - 1);
			new DownloadFolders().execute(pathString);

		}

		else if (v.getId() == R.id.tvGet) {
			// TODO Auto-generated method stub
			new GetFolderList().execute("");
		}

		else if (v.getId() == R.id.btConnect) {
			new ConnectClientToServer().execute(etIP.getText().toString()
					.trim());
		}
	}

}
