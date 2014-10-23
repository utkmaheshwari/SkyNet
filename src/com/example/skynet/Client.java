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
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Client extends Activity implements OnClickListener,
		OnItemClickListener{

	WifiManager wifiManager;
	TextView tvServerIP, tvSelfIP;
	EditText etIP;
	ImageButton ibConnect;
	private Socket clientSocket = null;
	private InputStream is = null;
	private OutputStream os = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private BufferedInputStream bis = null;

	private static final String TAG = "folderShare";
	private static final int PORTNUMBER = 9999;
	private String response, request;
	private static volatile boolean isConnected = false;
	private String currentFolderPath = "";

	ListView lvMyFolders;
	private ArrayList<CustomListItem> customList;
	ArrayList<String>  encodedList, selectedEncodedList,
			originalEncodeList;
	private ClientCustomListAdapter customAdapter;

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

		ibConnect = (ImageButton) findViewById(R.id.ibConnect);
		ibConnect.setOnClickListener(this);

		etIP = (EditText) findViewById(R.id.etIP);

		lvMyFolders = (ListView) findViewById(R.id.lvMyFolders);
		lvMyFolders.setOnItemClickListener(this);
		

		customList = new ArrayList<CustomListItem>();
		encodedList = new ArrayList<String>();
		selectedEncodedList = new ArrayList<String>();
		originalEncodeList = new ArrayList<String>();
		
		customAdapter = new ClientCustomListAdapter(this, R.layout.listitem_layout,
				customList);
		lvMyFolders.setAdapter(customAdapter);
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

		// //////////////////to prevent crash at on destroy....../////////
		try {
			is = clientSocket.getInputStream();
			os = clientSocket.getOutputStream();
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			bis = new BufferedInputStream(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// /////////...............................///////////////
	}

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if(Protocols.checkFile(encodedList.get(position)))
			return;
		currentFolderPath = encodedList.get(position);
		new GetSelectedFolderList().execute(currentFolderPath);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.ibConnect) {
			new ConnectClientToServer().execute(etIP.getText().toString()
					.trim());
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
				if (response.equals(null) | response.equals(""))
					return false;

				String[] paths = Protocols.splitBySubSeperator(response);
				encodedList.clear();
				customList.clear();
				originalEncodeList.clear();
				for (String path : paths) {
					encodedList.add(path);
					originalEncodeList.add(path);
					if(Protocols.checkFile(path))
						customList.add(new CustomListItem(R.drawable.ic_action_view_as_list, Protocols.getFileNameFromEncode(path), false));
					else
						customList.add(new CustomListItem(R.drawable.ic_action_collection, Protocols.getFileNameFromEncode(path), false));
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
				customAdapter.notifyDataSetChanged();
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
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				response = dis.readUTF();
				if (response.equals(null) | response.equals(""))
					return false;
				else {
					String[] paths = response.split(Protocols.SUB_SEPERATOR);
					encodedList.clear();
					customList.clear();
					for (String path : paths) {
						encodedList.add(path);
						if(Protocols.checkFile(path))
							customList.add(new CustomListItem(R.drawable.ic_action_view_as_list, Protocols.getFileNameFromEncode(path), false));
						else
							customList.add(new CustomListItem(R.drawable.ic_action_collection, Protocols.getFileNameFromEncode(path), false));
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
				customAdapter.notifyDataSetChanged();
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
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();

				response = dis.readUTF();
				if (response.equals(null) | response.equals(""))
					return false;
				else {
					String[] paths = Protocols.splitBySubSeperator(response);
					encodedList.clear();
					customList.clear();
					for (String path : paths) {
						encodedList.add(path);
						if(Protocols.checkFile(path))
							customList.add(new CustomListItem(R.drawable.ic_action_view_as_list, Protocols.getFileNameFromEncode(path), false));
						else
							customList.add(new CustomListItem(R.drawable.ic_action_collection, Protocols.getFileNameFromEncode(path), false));
					}
					currentFolderPath = (long)0
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
			if (result) {
				displayToast("folder list fetched");
				customAdapter.notifyDataSetChanged();
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
						Protocols.PREPARE_FOR_DOWNLOAD, params[0]);
				if (clientSocket.isClosed())
					return false;
				dos.writeUTF(request);
				dos.flush();
				publishProgress("PREPARING FOR DOWNLOAD");

				File mainFolder = new File(
						Environment.getExternalStorageDirectory() + "/"
								+ getPackageName());
				if (!mainFolder.exists())
					mainFolder.mkdirs();

				response = dis.readUTF();
				publishProgress(response);
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

				for (String encode : encodes) {
					File file = new File(mainFolder.getAbsoluteFile()
							+ Protocols.getParentPathFromEncode(encode),
							Protocols.getFileNameFromEncode(encode));
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(file));
					Protocols.copyInputStreamToOutputStream(bis, bos,
							Protocols.getFileSizeFromEncode(encode));
					bos.flush();
					bos.close();
					publishProgress(file.getName() + " downloaded");
				}
				selectedEncodedList.clear();
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
			displayToast(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result) {
				displayToast("downloading complete");
				customAdapter.notifyDataSetChanged();
			} else
				displayToast("downloading failed");
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
						if(Protocols.checkFile(path))
							customList.add(new CustomListItem(R.drawable.ic_action_view_as_list, Protocols.getFileNameFromEncode(path), false));
						else
							customList.add(new CustomListItem(R.drawable.ic_action_collection, Protocols.getFileNameFromEncode(path), false));
						encodedList.add(path);
					}
					currentFolderPath = Protocols.IS_NULL;
					customAdapter.notifyDataSetChanged();
					return true;
				}
			}
			// .....................................................................
			// .....................................................................
			// .....................................................................
			new GetParentFolder().execute(currentFolderPath);
			return true;
		}

		else if (item.getItemId() == R.id.action_download) {
			if (selectedEncodedList.size() == 0)
				return true;
			String pathString = Protocols
					.clubBySubSeperator(selectedEncodedList);

			new DownloadFolders().execute(pathString);
			return true;
		} else if (item.getItemId() == R.id.action_getlist) {
			if (isConnected)
				new GetFolderList().execute("");
		} else if (item.getItemId() == R.id.action_refresh) {
			final DhcpInfo dhcp = wifiManager.getDhcpInfo();
			tvServerIP.setText(Protocols
					.convertIntIPtoStringIP(dhcp.serverAddress));
			tvSelfIP.setText(Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			displayToast(Protocols.convertIntIPtoStringIP(dhcp.dns1) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.gateway) + ":"
					+ Protocols.convertIntIPtoStringIP(dhcp.serverAddress)
					+ ":" + Protocols.convertIntIPtoStringIP(dhcp.ipAddress));
			customList.clear();
			encodedList.clear();
			selectedEncodedList.clear();
			originalEncodeList.clear();
			customAdapter.notifyDataSetChanged();
			// /////////////////////////////////////////////
			try {
				if (clientSocket.isInputShutdown()
						| clientSocket.isOutputShutdown()
						| !clientSocket.isConnected() | clientSocket.isClosed())
					return true;
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

			return true;
		}
		return false;
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