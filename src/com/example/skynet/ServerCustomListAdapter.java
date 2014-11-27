package com.example.skynet;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ServerCustomListAdapter extends ArrayAdapter<CustomListItem> {
	/*
	 * static class ViewHolder { TextView tv; ImageView iv; CheckBox cb; }
	 */
	Server context;
	ArrayList<CustomListItem> displayList;

	public ServerCustomListAdapter(Server context, int resource,
			ArrayList<CustomListItem> displayList) {
		super(context, resource, displayList);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.displayList = displayList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		/*
		 * final int pos = position; ViewHolder viewHolder; if (convertView ==
		 * null) { LayoutInflater inflater = (LayoutInflater) context
		 * .getSystemService(Context.LAYOUT_INFLATER_SERVICE); convertView =
		 * inflater.inflate(R.layout.listitem_layout, parent, false);
		 * 
		 * viewHolder = new ViewHolder(); viewHolder.cb = (CheckBox)
		 * convertView.findViewById(R.id.cb); viewHolder.iv = (ImageView)
		 * convertView.findViewById(R.id.iv); viewHolder.tv = (TextView)
		 * convertView.findViewById(R.id.tv);
		 * 
		 * convertView.setTag(viewHolder); } else { viewHolder = (ViewHolder)
		 * convertView.getTag(); }
		 * 
		 * context.updateCheckboxes(pos);
		 * 
		 * CustomListItem obj = displayList.get(position);
		 * viewHolder.tv.setText(obj.getName());
		 * viewHolder.iv.setImageBitmap(BitmapFactory.decodeResource(
		 * context.getResources(), obj.getImage()));
		 * viewHolder.cb.setChecked(obj.getCheckedState()); final boolean
		 * isChecked = viewHolder.cb.isChecked();
		 * 
		 * viewHolder.cb.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { // TODO Auto-generated
		 * method stub context.updateSelectedList(pos, isChecked); } }); return
		 * convertView; }
		 */

		final int pos = position;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listitem_layout, parent,
					false);
		}
		final CheckBox cb = (CheckBox) convertView.findViewById(R.id.cb);
		final ImageView iv = (ImageView) convertView.findViewById(R.id.iv);
		final TextView tv = (TextView) convertView.findViewById(R.id.tv);
		context.updateCheckboxes(pos);
		
		CustomListItem obj = displayList.get(pos);
		tv.setText(obj.getName());
		iv.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
				obj.getImage()));
		cb.setChecked(obj.getCheckedState());
		
		cb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) { // TODO Auto-generated method stub
				context.updateSelectedList(pos, cb.isChecked());
			}
		});
		return convertView;
	}
}