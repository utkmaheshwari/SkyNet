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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements OnClickListener {
	LinearLayout ll1, ll2;
	RadioButton rb1, rb2, rb3, rb4, rb5;
	Button bt2, bt3;
	int choice1 = 0, choice2 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		rb1 = (RadioButton) findViewById(R.id.rb1);
		rb2 = (RadioButton) findViewById(R.id.rb2);
		rb3 = (RadioButton) findViewById(R.id.rb3);
		rb4 = (RadioButton) findViewById(R.id.rb4);
		rb5 = (RadioButton) findViewById(R.id.rb5);
		bt2 = (Button) findViewById(R.id.bt2);
		bt3 = (Button) findViewById(R.id.bt3);

		bt2.setOnClickListener(this);
		bt3.setOnClickListener(this);
		rb1.setOnClickListener(this);
		rb2.setOnClickListener(this);
		rb3.setOnClickListener(this);
		rb4.setOnClickListener(this);
		rb5.setOnClickListener(this);
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
						
					}
				}).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt2: {
			if (choice1 == 1) {
				if (choice2 == 1)
					startActivity(new Intent(
							"android.settings.WIRELESS_SETTINGS"));
				else if (choice2 == 2)
					startActivity(new Intent("android.settings.WIFI_SETTINGS"));
				else if (choice2 == 3) {
				}
			} else if (choice1 == 2) {
				if (choice2 == 1)
					startActivity(new Intent("android.settings.WIFI_SETTINGS"));
				else if (choice2 == 2)
					startActivity(new Intent("android.settings.WIFI_SETTINGS"));
				else if (choice2 == 3) {
				}
			}
			break;
		}

		case R.id.bt3: {
			if (choice1 == 1) {
				Intent i = new Intent(getApplicationContext(), Server.class);
				i.putExtra("choice2", choice2);
				startActivity(i);
				finish();
			}

			else if (choice1 == 2) {
				Intent i = new Intent(getApplicationContext(), Client.class);
				i.putExtra("choice2", choice2);
				startActivity(i);
				finish();
			}
			break;
		}

		case R.id.rb1: {
			choice1 = 1;
			ll2.setVisibility(View.INVISIBLE);
			rb3.setText("Create ur own hotspot");
			ll2.setVisibility(View.VISIBLE);
			break;
		}

		case R.id.rb2: {
			choice1 = 2;
			ll2.setVisibility(View.INVISIBLE);
			rb3.setText("Directly connect to the persons hotspot");
			ll2.setVisibility(View.VISIBLE);
			break;
		}

		case R.id.rb3: {
			choice2 = 1;
			break;
		}

		case R.id.rb4: {
			choice2 = 2;
			break;
		}

		case R.id.rb5: {
			choice2 = 3;
			break;
		}
		}
	}

}
