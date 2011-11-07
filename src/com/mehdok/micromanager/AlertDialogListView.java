/*
    Micro Manager, an open source file manager for the Android system
    Copyright (C) 2011  Mehdi Sohrabi <a.micromanager@gmail.com> <http://mehdok.blogspot.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mehdok.micromanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AlertDialogListView implements android.content.DialogInterface.OnShowListener
{
	public void onShow(DialogInterface dialog) 
	{
		ListView listView = ((AlertDialog)dialog).getListView();
		final ListAdapter originalAdapter = listView.getAdapter();
		listView.setAdapter(new ListAdapter()
        {
			public int getCount() 
			{
                return originalAdapter.getCount();
            }
			
			public Object getItem(int id) 
			{
                return originalAdapter.getItem(id);
            }
			
			public long getItemId(int id) 
			{
                return originalAdapter.getItemId(id);
            }
			
			public int getItemViewType(int id) 
			{
                return originalAdapter.getItemViewType(id);
            }
			
			public View getView(int position, View convertView, ViewGroup parent) 
			{
                View view = originalAdapter.getView(position, convertView, parent);
                TextView textView = (TextView)view;
        		textView.setTypeface(Main.fontBold);
                textView.setGravity(Main.direction | Main.CENTER);
                textView.setTextSize(Main.textSize);  
                return view;
            }
			
			public int getViewTypeCount() {
                return originalAdapter.getViewTypeCount();
            }
            
            public boolean hasStableIds() 
            {
                return originalAdapter.hasStableIds();
            }
            
            public boolean isEmpty() 
            {
                return originalAdapter.isEmpty();
            }
            
            public void registerDataSetObserver(DataSetObserver observer) 
            {
                originalAdapter.registerDataSetObserver(observer);
            }
            
            public void unregisterDataSetObserver(DataSetObserver observer) 
            {
                originalAdapter.unregisterDataSetObserver(observer);
            }
            
            public boolean areAllItemsEnabled() 
            {
                return originalAdapter.areAllItemsEnabled();
            }
            
            public boolean isEnabled(int position) 
            {
                return originalAdapter.isEnabled(position);
            }
        });
	}
}