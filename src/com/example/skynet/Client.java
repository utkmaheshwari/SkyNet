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

public class Client extends Activity implements OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	WifiManager wifiManager;
	TextView tvServerIP, tvSelfIP;
	EditText etIP;
	Button btConnect, btDownload, btGet, btRefresh, btBack;
	public Socket clientSocket = null;
	public InputStream is = null;
	public OutputStream os = null;
	public DataInputStream dis = null;
	public DataOutputStream dos = null;
	public BufferedInputStream bis = null;

	public static final String TAG = "wifi";
	public static final int PORTNUMBER = 9999;
	public String response, request;
	public static volatile boolean waitingForrequest = true;
	public String currentFolderPath = "";

	ListView lvMyFolders;
	ArrayList<String> folderNameList, encodedList, selectedEncodedList;
	ArrayAdapter<String> arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_layout);
		UIInitialization();
		WifiPeriferalInitialization();
	}

	public void UIInitialization() {
		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);

		btRefresh = (Button) findViewById(R.id.btRefresh);
		btRefresh.setOnClickListener(this);

		btDownload = (Button) findViewById(R.id.btDownload);
		btDownload.setOnClickListener(this);

		btGet = (Button) findViewById(R.id.btGet);
		btGet.setOnClickListener(this);

		btConnect = (Button) findViewById(R.id.btConnect);
		btConnect.setOnClickListener(this);

		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(this);

		etIP = (EditText) findViewById(R.id.etIP);

		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setOnItemClickListener(this);
		lvMyFolders.setOnItemLongClickListener(this);

		folderNameList = new ArrayList<String>();
		encodedList = new ArrayList<String>();
		selectedEncodedList = new ArrayList<String>();
		folderNameList.add("nothing in here....");
		arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, folderNameList);
		lvMyFolders.setAdapter(arrayAdapter);
	}

	public void WifiPeriferalInitialization() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		final DhcpInfo dhcp = wifiManager.getDhcpInfo();
		clientSocket = new Socket();
		tvServerIP
				.setText(Protocols.convertIntIPtoStringIP(dhcp.serverAddress));
		tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));

		displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress) + ":"
				+ Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		currentFolderPath = encodedList.get(position);
		new GetSelectedFolderList().execute(currentFolderPath);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.btRefresh) {
			// TODO Auto-generated method stub
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress)
					+ ":" + Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			folderNameList.clear();
			folderNameList.add("refreshed.....");
			encodedList.clear();
			selectedEncodedList.clear();
		}

		else if (v.getId() == R.id.btBack) {
			// TODO Auto-generated method stub
			new GetParentFolder().execute(currentFolderPath);
		}

		else if (v.getId() == R.id.btDownload) {
			// TODO Auto-generated method stub
			String pathString = Protocols
					.clubBySubSeperator(selectedEncodedList);
			new DownloadFolders().execute(pathString);
		}

		else if (v.getId() == R.id.btGet) {
			// TODO Auto-generated method stub
			new GetFolderList().execute("");
		}

		else if (v.getId() == R.id.btConnect) {
			new ConnectClientToServer().execute(etIP.getText().toString()
					.trim());
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
				// if (!clientSocket.isClosed() ||
				// !clientSocket.isInputShutdown()
				// || !clientSocket.isOutputShutdown()
				// || clientSocket.isConnected()) {
				// clientSocket = new Socket();
				// return false;
				// }
				clientSocket = new Socket();
				InetAddress inetAddress = InetAddress.getByName(params[0]);
				clientSocket.connect(new InetSocketAddress(inetAddress,
						PORTNUMBER));
				is = clientSocket.getInputStream();
				os = clientSocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);
				bis = new BufferedInputStream(is);
				if (clientSocket.isConnected()) {
					return true;
				} else
					return false;

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
				publishProgress("request sent : " + request);
				dos.writeUTF(request);
				dos.flush();
				response = dis.readUTF();
				publishProgress("response received : " + response);

				if (response.equals(null) || response.equals(""))
					return false;

				String[] paths = Protocols.splitBySubSeperator(response);
				encodedList.clear();
				folderNameList.clear();
				for (String path : paths) {
					encodedList.add(path);
					folderNameList.add(Protocols.getFileNameFromEncode(path));
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
			if (result) {
				displayToast("folder list fetched");
				arrayAdapter.notifyDataSetChanged();
			} else
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
				publishProgress("Request- " + request);
				dos.writeUTF(request);
				dos.flush();
				response = dis.readUTF();
				publishProgress("Response- " + response);
				if (response.equals(null) || response.equals(""))
					return false;
				else {
					String[] paths = response.split(Protocols.SUB_SEPERATOR);
					encodedList.clear();
					folderNameList.clear();
					for (String path : paths) {
						encodedList.add(path);
						folderNameList.add(Protocols
								.getFileNameFromEncode(path));
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
			if (result) {
				displayToast("folder list fetched");
				arrayAdapter.notifyDataSetChanged();
			} else
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
				publishProgress("request sent : " + request);
				dos.writeUTF(request);
				dos.flush();
				response = dis.readUTF();
				publishProgress("response received : " + response);
				if (response.equals(null) || response.equals(""))
					return false;
				else {
				//	Long parentFileSize=(long) 0;
					String[] paths = Protocols.splitBySubSeperator(response);
					encodedList.clear();
					folderNameList.clear();
					for (String path : paths) {
						encodedList.add(path);
						folderNameList.add(Protocols
								.getFileNameFromEncode(path));
				//		parentFileSize+=Protocols.getFileSizeFromEncode(path);
					}
				//	currentFolderPath=parentFileSize+Protocols.getParentFromEncode(paths[0]);
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
			if (result) {
				displayToast("folder list fetched");
				arrayAdapter.notifyDataSetChanged();
			} else
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
				request = Protocols.clubByMainSeperator(
						Protocols.DOWNLOAD_FOLDER, params[0]);
				dos.writeUTF(request);
				dos.flush();
				for (String path : selectedEncodedList) {
					String fileName = Protocols.getFileNameFromEncode(path);
					long fileSize = Protocols.getFileSizeFromEncode(path);
					File f = new File(Environment.getExternalStorageDirectory()
							+ "/" + getPackageName());
					if (!f.exists())
						f.mkdirs();
					File file = new File(f, fileName);
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(file));
					Protocols.copyInputStreamToOutputStream(bis, bos, fileSize);
					bos.flush();
					bos.close();
					publishProgress(fileName);
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
			if (result) {
				displayToast("downloading complete");
				arrayAdapter.notifyDataSetChanged();
			} else
				displayToast("downloading failed");
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
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