package com.example.skynet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.example.fileexplorer.R;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {
	WifiManager wifiManager;
	TextView tvServerIP, tvSelfIP, tvSend, tvRefresh;
	EditText etIPAddress, etPath;
	public static final String TAG = "wifi";
	public static final int PORTNUMBER = 9999;
	
	ListView lvMyFolders,lvUrFolders;
	ArrayList<String> myFolderList, myFolderName, mySelectedFolders,urFolderList, urFolderName, urSelectedFolders;
	ArrayAdapter<String> myFolderArrayAdapter,urFolderArrayAdapter;
	File folder;
	File[] folders;
	TextView tvBack;
	String selectedFolderName = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread(new Server()).start();
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		final DhcpInfo dhcp = wifiManager.getDhcpInfo();
		tvServerIP = (TextView) findViewById(R.id.tvServerIP);
		tvSelfIP = (TextView) findViewById(R.id.tvSelfIP);
		tvSend = (TextView) findViewById(R.id.tvSend);
		tvSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String[] s = new String[2];
				s[0] = etIPAddress.getText().toString();
				s[1] = etPath.getText().toString();
				new Client().execute(s);
			}
		});
		tvRefresh = (TextView) findViewById(R.id.tvRefresh);
		tvRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final DhcpInfo dhcp = wifiManager.getDhcpInfo();
				tvServerIP.setText(convertIntIPtoStringIP(dhcp.serverAddress));
				tvSelfIP.setText(convertIntIPtoStringIP(dhcp.ipAddress));
				Toast.makeText(
						getApplicationContext(),
						convertIntIPtoStringIP(dhcp.dns1) + ":"
								+ convertIntIPtoStringIP(dhcp.gateway) + ":"
								+ convertIntIPtoStringIP(dhcp.serverAddress)
								+ ":" + convertIntIPtoStringIP(dhcp.ipAddress),
						Toast.LENGTH_SHORT).show();
			}
		});
		etIPAddress = (EditText) findViewById(R.id.etIPAddress);
		etPath = (EditText) findViewById(R.id.etPath);

		tvServerIP.setText(convertIntIPtoStringIP(dhcp.serverAddress));
		tvSelfIP.setText(convertIntIPtoStringIP(dhcp.ipAddress));

		Toast.makeText(
				getApplicationContext(),
				convertIntIPtoStringIP(dhcp.dns1) + ":"
						+ convertIntIPtoStringIP(dhcp.gateway) + ":"
						+ convertIntIPtoStringIP(dhcp.serverAddress) + ":"
						+ convertIntIPtoStringIP(dhcp.ipAddress),
				Toast.LENGTH_SHORT).show();
		
		

	}
	public void FolderUIInitialization()
	{
		tvBack = (TextView) findViewById(R.id.tvBack);
		lvFolders = (ListView) findViewById(R.id.lvMyFolders);

		folderList = new ArrayList<String>();
		selectedFolders = new ArrayList<String>();
		folderName = new ArrayList<String>();

		lvFolders.setOnItemClickListener(this);
		lvFolders.setOnItemLongClickListener(this);

		arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, folderName);
		lvFolders.setAdapter(arrayAdapter);

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
		arrayAdapter.notifyDataSetChanged();

		tvBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
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

				arrayAdapter.notifyDataSetChanged();
				lvFolders.setBackgroundColor(Color.TRANSPARENT);
			}
		});

	}
	@SuppressLint("DefaultLocale")
	public String convertIntIPtoStringIP(int ip) {
		return (String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff),
				(ip >> 16 & 0xff), (ip >> 24 & 0xff)));
	}
	public String getString(File[] list)
	{
		String itemString = null;
		for(File item:list)
		{
			itemString=itemString+item.getName()+":";
		}
		itemString=itemString.substring(0, itemString.length()-1);
		return itemString;
	}

	class Server implements Runnable {

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
					DataInputStream dis=new DataInputStream(clientSocket.getInputStream());
					String readData=dis.readUTF();
					if(readData.equals("GETLIST"));
					{
						DataOutputStream dos=new DataOutputStream(clientSocket.getOutputStream());
						dos.writeUTF(getString(Environment.getExternalStorageDirectory().listFiles()));
					}
					
					Log.i(TAG, "socket connection accepted");
					File f = new File(Environment.getExternalStorageDirectory()
							+ "/" + getPackageName()
							+ System.currentTimeMillis() + ".jpg");
					
					File dirs = new File(f.getParent());
					if (!dirs.exists())
						dirs.mkdirs();
					Log.i(TAG, "blank file created");
					BufferedInputStream bis = new BufferedInputStream(
							clientSocket.getInputStream());
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(f));
					Log.i(TAG, "receiving data");
					copyInputStreamToOutputStream(bis, bos);
					Log.i(TAG, "file ready");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							displayToast("file received");
						}
					});

					bis.close();
					bos.close();
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
			File f = new File(params[1].trim());
			if (f.exists()) {
				Log.i(TAG, "file exist");
			}
			try {
				Socket clientSocket = new Socket();
				InetAddress inetAddress = InetAddress.getByName(params[0]);
				Log.i(TAG, inetAddress.toString());
				Log.i(TAG, params[1]);
				clientSocket.connect(new InetSocketAddress(inetAddress,
						PORTNUMBER), 0);
				if (clientSocket.isConnected()) {
					Log.i(TAG, "socket connected");
				} else {
					Log.i(TAG, "socket not connected");
					return null;
				}

				BufferedInputStream bis = new BufferedInputStream(
						new FileInputStream(f));
				BufferedOutputStream bos = new BufferedOutputStream(
						clientSocket.getOutputStream());

				Log.i(TAG, "copying file");
				copyInputStreamToOutputStream(bis, bos);
				Log.i(TAG, "file transfer complete");
				bis.close();
				bos.close();
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

	public boolean copyInputStreamToOutputStream(InputStream inputStream,
			OutputStream outputStream) {
		try {

			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while ((len = inputStream.read(buffer)) != -1) {
				Log.i(TAG, "writing " + len + buffer.toString());
				outputStream.write(buffer, 0, len);
			}
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

	public void displayToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}
