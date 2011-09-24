/*
    Micro Manager, an open source file manager for the Android system
    Copyright (C) 2011  Mehdi Sohrabi <mehdok@gmail.com> <http://sourceforge.net/p/a-micromanager>

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

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.app.ProgressDialog;
import android.content.pm.*;

public class main extends ListActivity 
{	       
     
    private String currentSdState = android.os.Environment.getExternalStorageState();
    
    TextView selection;    	
    
    TextView copySelection;    	
    
    ImageButton backBtn;    	
    
    ImageButton homeBtn;    	
    
    ImageButton pasteBtn;    	
    
    String items[] = null;    	
    
    static fileAction fa = new fileAction();    		
    
    private static final String root = "/" ;    	
    
    public String currentPath = fa.getExternalStorage();    	
        	 
    public String oldName = null;    	
       	 
    public String action = "";    	
       	 
    public String copy_move_Path = "";    	
        	 
    public String copy_move_item = "";     	
        	 
    public String pastePath = "";   	
    
    public ProgressDialog pd;    	
    
    public boolean copyResult;    	
    
    Context lContext = null;
    	
    private SharedPreferences pref;
    	
    boolean showHiddenFile;    	
    	
    
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.listgui);
    	checkSD();    	
    	selection = (TextView)findViewById(R.id.selection);
    	copySelection = (TextView)findViewById(R.id.copy_move_label);
    	backBtn = (ImageButton)findViewById(R.id.back_button);
    	homeBtn = (ImageButton)findViewById(R.id.home_button);
    	pasteBtn = (ImageButton)findViewById(R.id.paste_button);
    	display();    	    		
    }
            	    	
    public void onResume()
    {
    	super.onResume();    		
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	showHiddenFile = pref.getBoolean("toggleHiddenFile", true);
    	display();
    }    	
    
    private void checkSD ()
    {
       	if(currentSdState.equals(android.os.Environment.MEDIA_MOUNTED))
        	currentPath = fa.getExternalStorage();
        else if(currentSdState.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY))
       	{
       		Toast.makeText(this, R.string.MEDIA_MOUNTED_READ_ONLY, Toast.LENGTH_SHORT).show();
       		currentPath = fa.getExternalStorage();
       	}
       	else if(currentSdState.equals(android.os.Environment.MEDIA_BAD_REMOVAL))
        {            	
            Toast.makeText(this, R.string.MEDIA_BAD_REMOVAL, Toast.LENGTH_LONG).show();
            currentPath = root;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_REMOVED))
        {            	
           	Toast.makeText(this, R.string.MEDIA_REMOVED, Toast.LENGTH_LONG).show();
           	currentPath = root;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_SHARED))
        {            	
            Toast.makeText(this, R.string.MEDIA_SHARED, Toast.LENGTH_LONG).show();
            currentPath = root;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_UNMOUNTABLE))
        {            	
           	Toast.makeText(this, R.string.MEDIA_UNMOUNTABLE, Toast.LENGTH_LONG).show();
           	currentPath = root;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_UNMOUNTED))
        {            	
           	Toast.makeText(this, R.string.MEDIA_UNMOUNTED, Toast.LENGTH_LONG).show();
           	currentPath = root;
        }
    }  
    
    public void onBackPressed()
    {
    	if (currentPath.equals(root))
    		super.onBackPressed();
    	else
    		backButtonMethod(backBtn);
    }        
    
    public void backButtonMethod(View theButton)
    {
    	currentPath = fa.findParent(currentPath);
    	display();
    }    	
    
    public void homeButtonMethod(View theButton)
    {
    	currentSdState = android.os.Environment.getExternalStorageState();
    	checkSD();
    	display();
    }    	
    
    public void newDirButtonMethod(View theButton)
    {
    	makeNewDirectory();			
    }    	
    
    public void pasteButtonMethod(View theButton)
    {
    	if(action.equals("COPY"))
    	{
    		lContext = this;
    		executeCopy();		   
    	}
    	else if(action.equals("MOVE"))
    	{
    		lContext = this;
    		executeMove();			
    	}			
    }    
    
    public void refreshButtonMethod(View theButton)
    {
    	display();
    }    
    
    public void infoButtonMethod(View theButton)
    {    	
    	AlertDialog.Builder b = new AlertDialog.Builder(this);    	
    	b.setIcon(R.drawable.micromanager);
    	b.setTitle(R.string.info_title);
    	b.setMessage(R.string.info_message);    	
    	AlertDialog ad = b.create();
    	ad.show();
    }    	
    						
    private void executeCopy()
    {
    	pd = ProgressDialog.show(this, "Copy Progress ...", "Coping  " + copy_move_item, true, false);
    	Thread copyThread = new Thread(null, doCopy, "backgroundCopy");
    	copyThread.start();
    }    	
    
    private Runnable doCopy = new Runnable()
    {
    	public void run()
    	{
    		copyResult = fa.copyFile(copy_move_item, currentPath, copy_move_Path);
    		copyHandler.sendEmptyMessage(0);
    	}
    };    	
    
    private Handler copyHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		pd.dismiss();    			
    		if(copyResult)
    		{
    			copy_move_item = "";
    			copy_move_Path = "";
    			action = "";
    			Toast.makeText(lContext, R.string.copy_succ, Toast.LENGTH_SHORT).show();
    			display();
    		}
    		else
    		{
    			copy_move_item = "";
    			copy_move_Path = "";
    			action = "";
    			Toast.makeText(lContext, R.string.copy_fail, Toast.LENGTH_SHORT).show();
    			display();
    		}
    	}
    };    	
    
    private void executeMove()
    {
    	pd = ProgressDialog.show(this, "Move Progress ...", "Moving  " + copy_move_item, true, false);
    	Thread moveThread = new Thread(null, doMove, "backgroundMove");
    	moveThread.start();
    }    	
    
    private Runnable doMove = new Runnable()
    {
    	public void run()
    	{
    		copyResult = fa.copyFile(copy_move_item, currentPath, copy_move_Path);
    		moveHandler.sendEmptyMessage(0);
    	}
    };    	
    
    private Handler moveHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		if(copyResult)
    		{
    			String deletePath = copy_move_Path + copy_move_item;
    			boolean deleteResult = fa.deleteFile(deletePath);
    			if(deleteResult)
    			{
    				pd.dismiss();
    				copy_move_Path = "";
    				copy_move_item = "";
    				action = "";
    				Toast.makeText(lContext, R.string.move_succ, Toast.LENGTH_SHORT).show();
    				display();
    			}
    			else
    			{
    				pd.dismiss();
    				copy_move_Path = "";
    				copy_move_item = "";
    				action = "";
    				Toast.makeText(lContext, R.string.copy_but_not_move, Toast.LENGTH_SHORT).show();
    				display();
    			}
    		}
    		else
    		{
    			pd.dismiss();
    			copy_move_Path = "";
    			copy_move_item = "";
    			action = "";
    			Toast.makeText(lContext, R.string.move_fail, Toast.LENGTH_SHORT).show();
    			display();
    		}
    	}
    };    	
    		
    public void showProperties(final int position)
    {
    	LayoutInflater li = getLayoutInflater();
    	final View v = li.inflate(R.layout.properties, null);
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle(R.string.properties_label);
    	b.setIcon(setThumb(currentPath + items[position]));
    	b.setView(v);    		
    	b.setNeutralButton(R.string.exit, new DialogInterface.OnClickListener() {    			
    		public void onClick(DialogInterface dialog, int which) {
    			// TODO Auto-generated method stub
    			display();
    		}
    	});    			
    	AlertDialog ad = b.create();
    	ad.show();
    	TextView name = (TextView)ad.findViewById(R.id.nameProperties);
    	name.setText("NAME : " + items[position]);
    	TextView path = (TextView)ad.findViewById(R.id.pathProperties);
    	path.setText("PATH : " + currentPath);
    	TextView size = (TextView)ad.findViewById(R.id.sizeProperties);
    	String pathTemp = currentPath + items[position];
    	String sSize = fa.getSize(pathTemp);
    	size.setText("SIZE : " + sSize);
    	CheckBox hiddenCheckBox = (CheckBox)ad.findViewById(R.id.hiddenCheckBox);
    	if(fa.isHidden(items[position]))		
    		hiddenCheckBox.setChecked(true);
    	hiddenCheckBoxListener hcbl = new hiddenCheckBoxListener(currentPath, items[position]);
    	hiddenCheckBox.setOnCheckedChangeListener(hcbl);    		
    }    	
    
    private class hiddenCheckBoxListener implements CompoundButton.OnCheckedChangeListener
    {
    	String currentPathTemp = null;
    	String nameTemp = null;
    	public hiddenCheckBoxListener(String currentPath, String name)
    	{
    		currentPathTemp = currentPath;
    		nameTemp = name;
    	}
    	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	{
    		if(isChecked)
    		{
    			boolean result = fa.setHidden(currentPathTemp, nameTemp);
    		}
    		else
    		{
    			boolean result = fa.setUnHidden(currentPathTemp, nameTemp);
    		}
    	}
    }    
    								
   	private void display()
    {    		
    	isBackDisabled(backBtn, currentPath);
    	isHomeDisabled(homeBtn, currentPath);
    	enablePaste();
    	if(fa.isReadable(currentPath))
    	{
    		selection.setText(currentPath);
    		if(action.equals("COPY"))
    			copySelection.setText("Copy Item : " + copy_move_item);
    		else if(action.equals("MOVE"))
    			copySelection.setText("Move Item : " + copy_move_item);
    		else
    			copySelection.setText("");
    		items = fa.getItemList(currentPath, showHiddenFile);
    		setListAdapter(new rowAdapter());    			
    		//Set Long Listener.
    		ListView lv = getListView();
    		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
    	   	{
    	    	public boolean onItemLongClick(AdapterView av , View v , int pos , long id)
    	    	{
    	    		onLongListItemClick(v,pos,id);
    	    		return (false);
    	    	}
    	    });
    	}
    	else
    		Toast.makeText(this, R.string.can_not_read_dir, Toast.LENGTH_SHORT).show();		
   	}    	
    
    private class rowAdapter extends ArrayAdapter
    {
    	rowAdapter()
    	{
    		super(main.this, R.layout.listgui, items);
    	}
    		
    	public View getView(int position, View convertView, ViewGroup parent)
    	{		
    		View rowView = convertView;
    		if(rowView == null)
    		{
    			LayoutInflater inflater = getLayoutInflater();
    			rowView = inflater.inflate(R.layout.row_format, parent, false);    				
    		}
    		TextView label = (TextView)rowView.findViewById(R.id.name);
    		label.setText(items[position]);    			
    		String currentPathTemp = currentPath + items[position];
    		TextView size = (TextView)rowView.findViewById(R.id.size);						
    		size.setText(fa.getSize(currentPathTemp));
    		ImageView icon = (ImageView)rowView.findViewById(R.id.row_image);    		
    		icon.setImageDrawable(setThumb(currentPathTemp));    			
    		return(rowView);			
    	}
    }    	
    
    public Drawable setThumb(String path)
    {
    	if(fa.checkDirectory(path))
    	{    		
    		Drawable ic = getResources().getDrawable(R.drawable.directory);
    		return ic;
    	}
    		
    	else if(fa.checkFile(path))
    	{
    		//web file
    		if(path.endsWith(".html") ||
    		  (path.endsWith(".htm")))
    		{
    			Drawable ic = getResources().getDrawable(R.drawable.web);
    			return ic;
    		}
    		//text file
    		else if(path.endsWith(".txt"))
    		{
    			Drawable ic = getResources().getDrawable(R.drawable.text);
    			return ic;
    		}
    		//apk file
    		else if(path.endsWith(".apk"))
    		{
    			try
    			{
    				PackageManager pk = getPackageManager();
        			PackageInfo apkInfo = pk.getPackageArchiveInfo(path, 0);
        			ApplicationInfo appInfo = apkInfo.applicationInfo;
        			Drawable ic = pk.getApplicationIcon(appInfo.packageName);        			
        			return ic;        			
    			}
    			catch(PackageManager.NameNotFoundException e)
    			{
    				Drawable ic = getResources().getDrawable(R.drawable.icon);
    				return ic;
    			}    				
    		}
    		//image file    			
    		else if(path.endsWith("jpg")   ||
    			   (path.endsWith(".jpeg"))||
    			   (path.endsWith(".png")) ||
    			   (path.endsWith(".bmp")))
    		{
    			Bitmap iconTemp;
    			iconTemp = createThumb(path, 32);
    			if(iconTemp != null)
    			{
    				Drawable ic = new BitmapDrawable(iconTemp);
    				return ic;
    			}    				
    			else
    			{
    				Drawable ic = getResources().getDrawable(R.drawable.timer);
    				return ic;
    			}    				
    		}    			
    		//other
    		else
    		{
    			String mimeType = fa.getMimeType(path);
        		Intent playFile = new Intent();
        		playFile.setAction(android.content.Intent.ACTION_VIEW);
        		Uri pathUri = Uri.parse("file://" + path);
        		playFile.setDataAndType(pathUri, mimeType);
        		PackageManager pm = getPackageManager();
        		try
        		{
            		Drawable ic = pm.getActivityIcon(playFile);
            		return ic;
        		}
        		catch (PackageManager.NameNotFoundException e)
        		{
        			Drawable ic = getResources().getDrawable(R.drawable.timer);
        			return ic;
        			//calling dynamic thubmnail creator
        		}
    		}    		
    	}
    	Drawable ic = getResources().getDrawable(R.drawable.timer);
    	return ic;    	
    }    	
    
    public Bitmap createThumb(String path, int width)
    {
    	try
    	{
    		BitmapFactory.Options opt = new BitmapFactory.Options();
    		int sam = 72;
    		opt.inSampleSize = sam;
    		BitmapFactory.decodeFile(path, opt);
    		int wid = (opt.outWidth) * sam;
    		int hight = (opt.outHeight) * sam;
    		int max = Math.max(wid, hight);
    		int outSize = max / sam ;    		
    		opt.inSampleSize = outSize;
    		return(android.graphics.BitmapFactory.decodeFile(path, opt));
    	}
    	catch(Exception e)
    	{
    		return null;
    	}    	
    }
    
    public void isBackDisabled(View theButton, String currentPath)
    {
    	if(currentPath.equals(root))    	
    		theButton.setEnabled(false);    	
    	else 
    		theButton.setEnabled(true);
    }    
    
    public void isHomeDisabled(View theButton, String currentPath)
    {
    	if(currentPath.equals(fa.getExternalStorage()))    	
    		theButton.setEnabled(false);    	
    	else
    		theButton.setEnabled(true);
    }
    
    public void enablePaste()
    {
    	if(copy_move_Path == "")
    		pasteBtn.setEnabled(false);
    	else
    		pasteBtn.setEnabled(true);
    }
    
   	public void onListItemClick(ListView parent,  View v, int position, long id)
   	{
    	String currentPathTemp = currentPath  + items[position] + java.io.File.separatorChar;
    	if(fa.checkDirectory(currentPathTemp))
    	{
    		if(fa.isReadable(currentPathTemp))
    		{
    			currentPath = currentPathTemp;
    			display();
    		}
    		else
    			Toast.makeText(this, R.string.can_not_read_dir, Toast.LENGTH_SHORT).show();
    	}    		
    	// The Path Is File
    	else 
    	{    		    		
    		String mimeType = fa.getMimeType(items[position]);
    		Intent playFile = new Intent();
    		playFile.setAction(android.content.Intent.ACTION_VIEW);
    		Uri pathUri = Uri.parse("file://" + currentPathTemp);
    		playFile.setDataAndType(pathUri, mimeType);
    		try
    		{
    			startActivity(playFile);
    		}
    		catch(ActivityNotFoundException e)
    		{
    			Toast.makeText(this, R.string.app_not_found, Toast.LENGTH_SHORT).show();
    		}    		
    	}    		
    }
    
    protected void onLongListItemClick(View v, int position, long id)
    {
    	final int pos = position;
        AlertDialog.Builder optionBuilder = new AlertDialog.Builder(this);
        optionBuilder.setTitle(items[position]);
       	optionBuilder.setItems(R.array.Options, new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialoginterface,int witchBtn) 
        		{
        			optionSelect(pos , witchBtn);
        		}
        	});
        AlertDialog optionDialog = optionBuilder.create();
       	optionDialog.show();
    }
    
   	protected void optionSelect(int position , int witchBtn) 
       {
    	// TODO Auto-generated method stub
        switch (witchBtn)
        {
        case 0:
        	//do copy
        	action = "COPY";
        	copy_move_Path = currentPath;
        	copy_move_item = items[position]; 
        	display();
        	break;
        case 1:
        	//do move
        	action = "MOVE";
        	copy_move_Path = currentPath;
        	copy_move_item = items[position];
        	display();
        	break;
        case 2:
        	//do rename
        	renameItem(position);
        	break;
        case 3:
        	//do delete
        	deleteObj(position, this);
        	break;
        case 4:
        	//Open Properties
        	showProperties(position);
        	break;
        }	
    }
    
    private void makeNewDirectory()
    {
    	LayoutInflater li = getLayoutInflater();
    	View v = li.inflate(R.layout.mkdir, null);
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle(R.string.mkdir);
    	b.setView(v);
    	mkdirDialogButtonListener mkdbl = new mkdirDialogButtonListener(v, this);
    	b.setPositiveButton(R.string.ok, mkdbl);
    	b.setNegativeButton(R.string.cancel, mkdbl);
    	AlertDialog ad = b.create();
    	ad.show();
    }
    
    private class mkdirDialogButtonListener implements android.content.DialogInterface.OnClickListener
    {
    	private Context localContext = null;
    	private String inputText = null;
    	private View inputDialogView = null;
    		
    	public mkdirDialogButtonListener(View v, Context lContext)
    	{
    		inputDialogView = v;
    		localContext = lContext;
    	}
    		
    	public void onClick(DialogInterface v, int buttonId)
    	{
    		if(buttonId == DialogInterface.BUTTON1)
    		{
    			inputText = getInputReply();
    			if(fa.isWriteable(currentPath))
    			{	
    				if(inputText.length() > 0)
    				{
    					boolean result = fa.createDir(currentPath, inputText);
    					if(result)
    					{
    						Toast.makeText(localContext, R.string.mkdir_succ, Toast.LENGTH_SHORT).show();
    					}
    					else
    						Toast.makeText(localContext, R.string.mkdir_fail, Toast.LENGTH_LONG).show();
    				}
    				else
    					Toast.makeText(localContext, R.string.mkdir_zero_input, Toast.LENGTH_SHORT).show();			
    			}
    			else
    				Toast.makeText(localContext, R.string.mkdir_not_writeable, Toast.LENGTH_LONG).show();
    			display();
    		}
    	}
    		
    	private String getInputReply()
    	{
    		EditText et = (EditText)inputDialogView.findViewById(R.id.mkdir_input);
    		return (et.getText().toString());
    	}
    }
    
    private void renameItem(int position)
    {
    	LayoutInflater li = getLayoutInflater();
    	View v = li.inflate(R.layout.rename, null);
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle(R.string.rename);
    	b.setIcon(setThumb(currentPath + items[position]));
    	b.setView(v);
    	renameDialogButtonListener rdbl = new renameDialogButtonListener(v, this, position);
    	b.setPositiveButton(R.string.ok, rdbl);
    	b.setNegativeButton(R.string.cancel, rdbl);
    	AlertDialog ad = b.create();
    	ad.show();
    }
    
    private class renameDialogButtonListener implements android.content.DialogInterface.OnClickListener
    {
    	private Context localContext = null;
    	private String inputText = null;
    	private View inputDialogView = null;
    	private int position;
    	private String oldPath= null;
    		
    	public renameDialogButtonListener(View v, Context lContext, int pos)
    	{
    		inputDialogView = v;
    		localContext = lContext;
    		position = pos;
    		oldPath = currentPath + items[position];
    	}
    		
    	public void onClick(DialogInterface v, int buttonId)
    	{
    		if(buttonId == DialogInterface.BUTTON1)
    		{
    			inputText = getInputReply();
    			if(fa.isWriteable(oldPath))
    			{	
    				if(inputText.length() > 0)
    				{
    					boolean result = fa.rename(oldPath, inputText);
    					if(result)
    					{
    						Toast.makeText(localContext, R.string.rename_succ, Toast.LENGTH_SHORT).show();
    					}
    					else
    						Toast.makeText(localContext, R.string.rename_fail, Toast.LENGTH_LONG).show();
    				}
    				else
    					Toast.makeText(localContext, R.string.rename_zero_input, Toast.LENGTH_SHORT).show();			
    			}
    			else
    				Toast.makeText(localContext, R.string.rename_not_writeable, Toast.LENGTH_LONG).show();
    			display();
    		}
    	}
    		
    	private String getInputReply()
    	{
    		EditText et = (EditText)inputDialogView.findViewById(R.id.rename_input);
    		return (et.getText().toString());
    	}
    }
    
    private void deleteObj(int position, Context localContext)
    {
        final Context lContext = localContext;
        final int pos = position;        	
        AlertDialog.Builder deletePromptBuilder = new AlertDialog.Builder(this);
        deletePromptBuilder.setTitle(items[position]);
        String pathTemp = currentPath + items[pos];
        deletePromptBuilder.setIcon(setThumb(pathTemp));
        deletePromptBuilder.setMessage("Are You Sure Want To Delete " + items[position]);
        deletePromptBuilder.setPositiveButton("OK",new DialogInterface.OnClickListener()
        		{
            		public void onClick(DialogInterface dialoginterface,int i) 
            		{
            			String pathTemp = currentPath + items[pos];
            			if(fa.deleteFile(pathTemp))
            			{
            				Toast.makeText(lContext, R.string.delete_succ, Toast.LENGTH_LONG).show();
                			display();
            			}
            			else
            				Toast.makeText(lContext, R.string.delete_fail, Toast.LENGTH_LONG).show();
               		}
        		});
        deletePromptBuilder.setNegativeButton("Cancel" , new DialogInterface.OnClickListener()
        		{
            		public void onClick(DialogInterface dialoginterface,int i) 
            		{            				
            		}
        		});
        AlertDialog deletePrompt = deletePromptBuilder.create();
        deletePrompt.show();		
    }
    
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	menu.add(Menu.NONE, 01, Menu.NONE, "Preferences")
    			.setIcon(R.drawable.preferences);
    			//.setAlphabeticShortcut('p');
    	return (super.onCreateOptionsMenu(menu));
    }
    	
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case 01:
    		startActivity(new Intent(this, preferences.class));
    		return (true);
    	}
    	return (super.onOptionsItemSelected(item));
    }    
}