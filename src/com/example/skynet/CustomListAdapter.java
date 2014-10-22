package com.example.skynet;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter<CustomListItem> {

	Server context;
	ArrayList<CustomListItem> customList;

	public CustomListAdapter(Server context, int resource,
			ArrayList<CustomListItem> customList) {
		super(context, resource, customList);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.customList = customList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final int pos = position;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.listitem_layout, parent, false);
		ImageView iv = (ImageView) view.findViewById(R.id.iv);
		TextView tv = (TextView) view.findViewById(R.id.tv);
		CheckBox cb = (CheckBox) view.findViewById(R.id.cb);

		CustomListItem obj = customList.get(position);
		tv.setText(obj.getName());
		iv.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
				obj.getImage()));
		cb.setChecked(customList.get(pos).getCheckedState());
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				context.updateSelectedList(pos, arg1);
			}
		});
		return view;
	}
}
