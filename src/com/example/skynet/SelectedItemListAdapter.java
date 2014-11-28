package com.example.skynet;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class SelectedItemListAdapter extends ArrayAdapter<CustomListItem> {

	Server context;
	ArrayList<CustomListItem> displayList;

	public SelectedItemListAdapter(Server context, int resource,
			ArrayList<CustomListItem> displayList) {
		super(context, resource, displayList);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.displayList = displayList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.listitem_layout, parent, false);

		final CheckBox cb = (CheckBox) view.findViewById(R.id.cb);
		final ImageView iv = (ImageView) view.findViewById(R.id.iv);
		final TextView tv = (TextView) view.findViewById(R.id.tv);
		cb.setVisibility(View.INVISIBLE);

		CustomListItem obj = displayList.get(position);
		tv.setText(obj.getName());
		iv.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
				obj.getImage()));

		return view;

	}
}
