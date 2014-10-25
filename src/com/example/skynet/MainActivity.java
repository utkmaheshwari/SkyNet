package com.example.skynet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btServer = (Button) findViewById(R.id.btServer);
		btServer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(), Server.class));
				finish();
			}
		});

		Button btClient = (Button) findViewById(R.id.btClient);
		btClient.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(), Client.class));
				finish();
			}
		});
	}
	
	public void onBackPressed() {
		new AlertDialog.Builder(MainActivity.this)
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

}
