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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.preference.PreferenceManager;
import android.app.ProgressDialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Main extends ListActivity 
{		
	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final int GB = MB * KB;	
	public static final int NO_REQUEST = 0;
	public static final int MESSAGE_ICON_CHANGED = 1;
	public static final int MESSAGE_ICON_LOAD_END = 2;
	public static final int MESSAGE_OUT_OF_BOUNDS = 3;
	public static final int COPY_REQUEST = 4;
	public static final int MOVE_REQUEST = 5;
	public static final int PREFERENCES_MENU = 6;
	public static final int SORT_BY_NAME_ASCE = 7;
	public static final int SORT_BY_NAME_DESC = 8;
	public static final int SORT_BY_SIZE_ASCE = 9;
	public static final int SORT_BY_SIZE_DESC = 10;
	public static final int LTR = android.view.Gravity.LEFT;
	public static final int RTL = android.view.Gravity.RIGHT;
	public static final int CENTER = android.view.Gravity.CENTER;	
	public static final String ROOT = "/" ;
	public static final String ENGLISH = "en";
	public static final String PERSIAN = "fa";
	public static final String DEFAULT_TEXT_SIZE = "14";
	public static final String DEFAULT_THUMBNAIL_SIZE = "64";
	
	
	public static FileAction fileAction = new FileAction();
	public static Context lContext ;
	public static int thumbnailSize;
	public static String language = ENGLISH;
	public static int direction = LTR;
	public static int textColor;
	public static int backColor1;
	public static int backColor2;
	public static int textSize = Integer.parseInt(DEFAULT_TEXT_SIZE);//14;
	public static Typeface fontRegular;
	public static Typeface fontBold;
	
    public String currentSdState = android.os.Environment.getExternalStorageState();
    public String currentPath = fileAction.getExternalStorageAddress();
    public String items[] = null;    
    public String oldName = null; 
    public String copy_move_Path = "";
    public String copy_move_item = ""; 
    public String pastePath = "";
    public TextView selection;    
    public TextView notification;    
    public ImageButton backBtn;    
    public ImageButton homeBtn;    
    public ImageButton pasteBtn;    
    public int pasteRequest;
    public int sortType = SORT_BY_NAME_ASCE;
    public boolean showHiddenFile;
    public boolean copyResult;
    public boolean cancelThumbnailLoading = false;
    
    public ProgressDialog copyProgress; 	
    public SharedPreferences sharedPreferences;
	public ListView listView;	
	public ArrayList<RowHolder> rowHolder = new ArrayList<RowHolder>();	
	public RowAdapter rowAdapter;	
	
    
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);    	
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	setContentView(R.layout.listgui);    	
    	checkSD();    	
    	selection = (TextView)findViewById(R.id.selection);
    	notification = (TextView)findViewById(R.id.copy_move_label);
    	backBtn = (ImageButton)findViewById(R.id.back_button);
    	homeBtn = (ImageButton)findViewById(R.id.home_button);
    	pasteBtn = (ImageButton)findViewById(R.id.paste_button);
    	lContext = this;//getApplicationContext();
    	textColor = getResources().getColor(R.color.text_color);
    	backColor1 = getResources().getColor(R.color.background_color_1);
    	backColor2 = getResources().getColor(R.color.background_color_2);
    	fontRegular = Typeface.createFromAsset(getAssets(), "fonts/FreeFarsi.ttf");  
    	fontBold = Typeface.createFromAsset(getAssets(), "fonts/FreeFarsi-Bold.ttf");
    	setLanguage();
    	displayContent();
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    public void onResume()
    {
    	super.onResume();    		
    	sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	showHiddenFile = sharedPreferences.getBoolean("toggleHiddenFile", true);    	
    	String sThumbnailSize = sharedPreferences.getString("thumbnailSize", DEFAULT_THUMBNAIL_SIZE);    	
    	thumbnailSize = Integer.parseInt(sThumbnailSize);
    	String sTextSize = sharedPreferences.getString("textSize", DEFAULT_TEXT_SIZE); 
    	textSize = Integer.parseInt(sTextSize);
    	textColor = sharedPreferences.getInt("textColor", textColor);
    	backColor1 = sharedPreferences.getInt("backColor1", backColor1);
    	backColor2 = sharedPreferences.getInt("backColor2", backColor2);
    	language = sharedPreferences.getString("language", ENGLISH);
    	
    	if(language.equals(PERSIAN))
    		direction = RTL;
    	else
    		direction = LTR;
    	
    	setLanguage();
    	displayContent();
    }    
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     * onSavedInstanceState() save some variable value, iit need specially when
     * rotation occur.
     */
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	super.onSaveInstanceState(savedInstanceState);
    	savedInstanceState.putInt("pref_pastRequest", this.pasteRequest);
    	savedInstanceState.putInt("pref_sortType", this.sortType);
    	savedInstanceState.putInt("pref_direction", direction);
    	savedInstanceState.putInt("pref_textSize", textSize);
    	savedInstanceState.putInt("pref_textColor", textColor);
    	savedInstanceState.putInt("pref_backColor1", backColor1);
    	savedInstanceState.putInt("pref_backColor2", backColor2);
    	savedInstanceState.putString("pref_currentPath", this.currentPath);    	
    	savedInstanceState.putString("pref_copyMovePath", this.copy_move_Path);
    	savedInstanceState.putString("pref_copyMoveItem", this.copy_move_item);
    	savedInstanceState.putString("pref_language", language);    
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.ListActivity#onRestoreInstanceState(android.os.Bundle)
     * onRestoreInstanceState() fill some variable with saved value in
     * onSavedInstanceState()
     */
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	this.pasteRequest = savedInstanceState.getInt("pref_pastRequest");
    	this.sortType = savedInstanceState.getInt("pref_sortType");
    	direction = savedInstanceState.getInt("pref_direction");
    	textSize = savedInstanceState.getInt("pref_textSize");
    	textColor = savedInstanceState.getInt("pref_textColor");
    	backColor1 = savedInstanceState.getInt("pref_backColor1");
    	backColor2 = savedInstanceState.getInt("pref_backColor2");
    	this.currentPath = savedInstanceState.getString("pref_currentPath");    	
    	this.copy_move_Path = savedInstanceState.getString("pref_copyMovePath");
    	this.copy_move_item = savedInstanceState.getString("pref_copyMoveItem");
    	language = savedInstanceState.getString("pref_language");
    }    
    
    public void checkSD ()
    {
       	if(currentSdState.equals(android.os.Environment.MEDIA_MOUNTED))
        	currentPath = fileAction.getExternalStorageAddress();
        else if(currentSdState.equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY))
       	{        	
        	showMessage(R.string.MEDIA_MOUNTED_READ_ONLY);
       		currentPath = fileAction.getExternalStorageAddress();
       	}
       	else if(currentSdState.equals(android.os.Environment.MEDIA_BAD_REMOVAL))
        {            	
       		showMessage(R.string.MEDIA_BAD_REMOVAL);
            currentPath = ROOT;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_REMOVED))
        {            	
        	showMessage(R.string.MEDIA_REMOVED);
           	currentPath = ROOT;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_SHARED))
        {            	
        	showMessage(R.string.MEDIA_SHARED);
            currentPath = ROOT;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_UNMOUNTABLE))
        {            	
        	showMessage(R.string.MEDIA_UNMOUNTABLE);
           	currentPath = ROOT;
        }            
        else if(currentSdState.equals(android.os.Environment.MEDIA_UNMOUNTED))
        {            	
        	showMessage(R.string.MEDIA_UNMOUNTED);        			
           	currentPath = ROOT;
        }
    }  
    
    public void onBackPressed()
    {
    	cancelThumbnailLoading = true;
    	if (currentPath.equals(ROOT))
    		super.onBackPressed();
    	else
    		backButtonMethod(backBtn);
    }        
    
    public void backButtonMethod(View theButton)
    {
    	cancelThumbnailLoading = true;
    	currentPath = fileAction.findParent(currentPath);
    	displayContent();
    }    	
    
    public void homeButtonMethod(View theButton)
    {
    	cancelThumbnailLoading = true;
    	currentSdState = android.os.Environment.getExternalStorageState();
    	checkSD();
    	displayContent();
    }    	
    
    public void newDirButtonMethod(View theButton)
    {
    	makeNewDirectory();			
    }    	
    
    public void pasteButtonMethod(View theButton)
    {
    	switch(pasteRequest)
    	{
    	case COPY_REQUEST:
    		executeCopy();
    		break;
    	case MOVE_REQUEST:
    		executeMove();
    		break;
    	}    				
    }  
    
    public void sortButtonMethod(View theButton)
    {    	
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(lContext);
    	dialogBuilder.setTitle(R.string.sort);
    	dialogBuilder.setIcon(R.drawable.sort);
    	dialogBuilder.setItems(R.array.sort_by, new DialogInterface.OnClickListener()
    		{			
			public void onClick(DialogInterface dialogInterface, int witchBtn) 
			{				
				switch(witchBtn)
				{
				case 0:
					AlertDialog.Builder dialogBuilder1 = new AlertDialog.Builder(lContext);
			    	dialogBuilder1.setTitle(R.string.sort_by_name);
			    	dialogBuilder1.setIcon(R.drawable.sort);
			    	dialogBuilder1.setItems(R.array.asce_desc, new DialogInterface.OnClickListener() 
			    		{						
							public void onClick(DialogInterface dialogInterface, int witchBtn) 
							{
								switch(witchBtn)
								{
								case 0:
									sortType = SORT_BY_NAME_ASCE;
									sortItem();
									rowAdapter.notifyDataSetChanged();
									break;
								case 1:
									sortType = SORT_BY_NAME_DESC;
									sortItem();
									rowAdapter.notifyDataSetChanged();
									break;
								}							
							}
			    		});
			    	AlertDialog sortByNameDialog = dialogBuilder1.create();
			    	sortByNameDialog.setOnShowListener(new AlertDialogListView());
			    	sortByNameDialog.show();
			    	break;
				case 1:
					AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(lContext);
			    	dialogBuilder2.setTitle(R.string.sort_by_size);
			    	dialogBuilder2.setIcon(R.drawable.sort);
			    	dialogBuilder2.setItems(R.array.asce_desc, new DialogInterface.OnClickListener() 
			    		{						
							public void onClick(DialogInterface dialogInterface, int witchBtn)
							{
								switch(witchBtn)
								{
								case 0:
									sortType = SORT_BY_SIZE_ASCE;
									sortItem();
									rowAdapter.notifyDataSetChanged();
									break;
								case 1:
									sortType = SORT_BY_SIZE_DESC;
									sortItem();
									rowAdapter.notifyDataSetChanged();
									break;
								}
							}
			    		});
			    	AlertDialog sortBySizeDialog = dialogBuilder2.create();
			    	sortBySizeDialog.setOnShowListener(new AlertDialogListView());
			    	sortBySizeDialog.show();
			    	break;
				}								
			}			
		});
		
    	AlertDialog sortDialog = dialogBuilder.create();
    	sortDialog.setOnShowListener(new AlertDialogListView());
    	sortDialog.show();
    }
    
    public void refreshButtonMethod(View theButton)
    {
    	displayContent();    	
    }    
    
    public void infoButtonMethod(View theButton)
    {    	
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);    	
    	dialogBuilder.setIcon(R.drawable.micromanager);
    	dialogBuilder.setTitle(R.string.info_title);
    	TextView message = new TextView(lContext);
    	message.setTypeface(fontRegular);
    	message.setTextColor(textColor);
    	message.setTextSize(textSize);
    	message.setGravity(direction);
    	message.setText(R.string.info_message);
    	dialogBuilder.setView(message);    	
    	AlertDialog infoDialog = dialogBuilder.create();
    	infoDialog.show();
    }    	
    						
    private void executeCopy()
    {
    	copyProgress = ProgressDialog.show(this, "Copy Progress ...", "Coping  " + copy_move_item, true, false);
    	Thread copyThread = new Thread(null, doCopy, "backgroundCopy");
    	copyThread.start();
    }    	
    
    private Runnable doCopy = new Runnable()
    {
    	public void run()
    	{
    		copyResult = fileAction.copyFile(copy_move_item, currentPath, copy_move_Path);
    		copyHandler.sendEmptyMessage(0);
    	}
    };    	
    
    private Handler copyHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		copyProgress.dismiss();    			
    		if(copyResult)
    		{
    			copy_move_item = "";
    			copy_move_Path = "";
    			pasteRequest = NO_REQUEST;
    			showMessage(R.string.copy_succ);
    			displayContent();
    		}
    		else
    		{
    			copy_move_item = "";
    			copy_move_Path = "";
    			pasteRequest = NO_REQUEST;
    			showMessage(R.string.copy_fail);
    			displayContent();
    		}
    	}
    };    	
    
    private void executeMove()
    {
    	String srcItem = copy_move_Path + copy_move_item;
    	String desItem = currentPath + copy_move_item;
    	boolean result = fileAction.rename(srcItem, desItem);
    	if(result)
    	{
    		copy_move_Path = "";
    		copy_move_item = "";
    		pasteRequest = NO_REQUEST;
    		showMessage(R.string.move_succ);
    		displayContent();
    	}
    	else
    	{
    		copy_move_Path = "";
    		copy_move_item = "";
    		pasteRequest = NO_REQUEST;
    		showMessage(R.string.move_fail);
    		displayContent();
    	}    	
    } 
    		
    public void showProperties(int pos)
    {
    	final int position = pos;
    	LayoutInflater lInflater = getLayoutInflater();
    	final View view = lInflater.inflate(R.layout.properties, null);
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    	dialogBuilder.setTitle(R.string.properties_label);
    	dialogBuilder.setIcon(rowHolder.get(position).getIcon());
    	dialogBuilder.setView(view);    		
    	dialogBuilder.setNeutralButton(R.string.exit, new DialogInterface.OnClickListener() {    			
    		public void onClick(DialogInterface dialog, int which) 
    		{    			
    		}
    	});    			
    	AlertDialog propertiesDialog = dialogBuilder.create();
    	propertiesDialog.show();
    	TextView name = (TextView)propertiesDialog.findViewById(R.id.nameProperties);
    	name.setTypeface(fontRegular);
    	name.setTextColor(textColor);
    	name.setTextSize(textSize);
    	String nameStr = getResources().getString(R.string.prop_name);
    	name.setText(nameStr + rowHolder.get(position).getLabel());
    	TextView path = (TextView)propertiesDialog.findViewById(R.id.pathProperties);
    	path.setTypeface(fontRegular);
    	path.setTextColor(textColor);
    	path.setTextSize(textSize);
    	String pathStr = getResources().getString(R.string.prop_path);
    	path.setText(pathStr + currentPath);
    	TextView size = (TextView)propertiesDialog.findViewById(R.id.sizeProperties);
    	size.setTypeface(fontRegular);
    	size.setTextColor(textColor);
    	size.setTextSize(textSize);
    	String sSize = getSizeString(rowHolder.get(position).getSize());
    	String sizeStr = getResources().getString(R.string.prop_size);
    	size.setText(sizeStr + sSize);
    	CheckBox hiddenCheckBox = (CheckBox)propertiesDialog.findViewById(R.id.hiddenCheckBox);
    	if(fileAction.isHidden(rowHolder.get(position).getLabel()))		
    		hiddenCheckBox.setChecked(true);
    	HiddenCheckBoxListener hcbl = new HiddenCheckBoxListener(currentPath, rowHolder.get(position).getLabel(), position);
    	hiddenCheckBox.setOnCheckedChangeListener(hcbl);    		
    }    	
    
    private class HiddenCheckBoxListener implements CompoundButton.OnCheckedChangeListener
    {
    	String currentPathTemp = null;
    	String nameTemp = null;
    	int position;
    	public HiddenCheckBoxListener(String currentPath, String oldName, int pos)
    	{
    		currentPathTemp = currentPath;
    		nameTemp = oldName;
    		position = pos;
    	}
    	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	{
    		if(isChecked)
    		{
    			String newName = "." + nameTemp;
    			boolean result = fileAction.rename(currentPathTemp + nameTemp, currentPathTemp + newName);    			
    			if(!result)
    			{
    				showMessage(R.string.hidden_fail);
    			}
    			else
    			{
    				rowHolder.get(position).setLabel(newName);
    				rowAdapter.notifyDataSetChanged();
    			}
    		}
    		else
    		{
    			String newName = nameTemp.substring(1);
    			boolean result = fileAction.rename(currentPathTemp + nameTemp, currentPathTemp + newName);    			
    			if(!result)
    			{
    				showMessage(R.string.unhidden_fail);
    			}
    			else
    			{
    				rowHolder.get(position).setLabel(newName);
    				rowAdapter.notifyDataSetChanged();
    			}
    		}
    	}
    }    
    								
   	private void displayContent()
    {   
   		cancelThumbnailLoading = false;   		
    	isBackDisabled(backBtn, currentPath);
    	isHomeDisabled(homeBtn, currentPath);
    	isPasteEnabled();
    	LinearLayout listGui = (LinearLayout)findViewById(R.id.list_gui);
    	listGui.setBackgroundColor(backColor2);    	
    	if(fileAction.isReadable(currentPath))
    	{
    		selection.setTypeface(fontRegular);
    		selection.setTextSize(textSize);
    		selection.setTextColor(textColor);
    		selection.setText(currentPath);
    		switch(pasteRequest)
    		{
    		case COPY_REQUEST:
    			notification.setText("Copy Item : " + copy_move_item);
    			break;
    		case MOVE_REQUEST:
    			notification.setText("Move Item : " + copy_move_item);
    			break;
    		default:
    			notification.setText("");
    			break;
    		}      		
    		items = fileAction.getItemList(currentPath, showHiddenFile);    		
    		int itemsCount = items.length;
    		Drawable sIcon = getResources().getDrawable(R.drawable.timer);
    		if(rowHolder.size() != 0)
    			rowHolder.clear();
    		rowHolder.ensureCapacity(itemsCount);
    		for(int i = 0 ; i < itemsCount ; i++)
    		{
    			rowHolder.add(new RowHolder(sIcon, items[i], fileAction.getSize(currentPath + items[i])));    			
    		}
    		sortItem();
    		rowAdapter = new RowAdapter();
    		setListAdapter(rowAdapter);
    		thumbnailLoader();    		
    		listView = getListView();    		
    		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
    	   	{
    	    	public boolean onItemLongClick(AdapterView av , View v , int pos , long id)
    	    	{
    	    		onLongListItemClick(v,pos,id);
    	    		return (false);
    	    	}
    	    });
    	}
    	else
    	{
    		showMessage(R.string.can_not_read_dir);
    	}
   	}    	
    
    public class RowAdapter extends ArrayAdapter
    {    	
    	RowAdapter()
    	{
    		super(Main.this, R.layout.listgui, items);
    	}
    		
    	public View getView(int position, View convertView, ViewGroup parent)
    	{		
    		View rowView = convertView;
    		LayoutInflater lInflater = getLayoutInflater();
    		rowView = lInflater.inflate(R.layout.row_format, parent, false);
    		if((position % 2) == 0)
    			rowView.setBackgroundColor(backColor1);
    		else
    			rowView.setBackgroundColor(backColor2);
    		
    		TextView label = (TextView)rowView.findViewById(R.id.name); 
    		label.setTypeface(fontBold);
    		label.setTextSize(textSize);
    		label.setTextColor(textColor);
    		label.setText(rowHolder.get(position).getLabel());
    		TextView size = (TextView)rowView.findViewById(R.id.size); 
    		size.setTypeface(fontRegular);
    		size.setTextSize(textSize);
    		size.setTextColor(textColor);
    		size.setText(getSizeString(rowHolder.get(position).getSize()));
    		ImageView icon = (ImageView)rowView.findViewById(R.id.row_image);
    		icon.setImageDrawable(rowHolder.get(position).getIcon());    		    			
    		return(rowView);			
    	}    	
    }  
    
    public Drawable createThumb(String path)
    {
    	if(fileAction.checkDirectory(path))
    	{    		
    		Drawable icon = getResources().getDrawable(R.drawable.directory);
    		return icon;
    	}    		
    	else if(fileAction.checkFile(path))
    	{
    		//web file
    		if(path.endsWith(".html") ||
    		  (path.endsWith(".htm")))
    		{
    			Drawable icon = getResources().getDrawable(R.drawable.web);
    			return icon;
    		}
    		//text file
    		else if(path.endsWith(".txt"))
    		{
    			Drawable icon = getResources().getDrawable(R.drawable.text);
    			return icon;
    		}
    		//apk file
    		else if(path.endsWith(".apk"))
    		{
    			try
    			{
    				PackageManager pkManager = getPackageManager();
        			PackageInfo apkInfo = pkManager.getPackageArchiveInfo(path, 0);
        			ApplicationInfo appInfo = apkInfo.applicationInfo;
        			Drawable icon = pkManager.getApplicationIcon(appInfo.packageName);        			
        			return icon;        			
    			}
    			catch(PackageManager.NameNotFoundException e)
    			{
    				Drawable icon = getResources().getDrawable(R.drawable.icon);
    				return icon;
    			}    				
    		}
    		//image file    			
    		else if(path.endsWith("jpg")   ||
    			   (path.endsWith(".jpeg"))||
    			   (path.endsWith(".png")) ||
    			   (path.endsWith(".gif")) ||
    			   (path.endsWith(".bmp")))
    		{
    			Bitmap iconTemp = createPictureThumb(path);
    			if(iconTemp != null)
    			{
    				Drawable icon = new BitmapDrawable(iconTemp);
    				return icon;
    			}    				
    			else
    			{
    				Drawable icon = getResources().getDrawable(R.drawable.photo);
    				return icon;
    			}     			  				
    		}    			
    		//other
    		else
    		{
    			String mimeType = fileAction.getMimeType(path);
        		Intent playFile = new Intent();
        		playFile.setAction(android.content.Intent.ACTION_VIEW);
        		Uri pathUri = Uri.parse("file://" + path);
        		playFile.setDataAndType(pathUri, mimeType);
        		PackageManager pkManager = getPackageManager();
        		try
        		{
            		Drawable icon = pkManager.getActivityIcon(playFile);
            		return icon;
        		}
        		catch (PackageManager.NameNotFoundException e)
        		{
        			//TODO
        			//change icon with ?
        			Drawable icon = getResources().getDrawable(R.drawable.timer);
        			return icon;        			
        		}
    		}    		
    	}
    	//TODO
    	//change icon with ?
    	Drawable icon = getResources().getDrawable(R.drawable.timer);
    	return icon;    	
    }    	
    
    public Bitmap createPictureThumb(String path)
    {
    	try
    	{    		
    		BitmapFactory.Options opt = new BitmapFactory.Options();
    		opt.inJustDecodeBounds = true;
    		opt.outHeight = 0;
    		opt.outWidth = 0;
    		opt.inSampleSize = 1;
    		//does this code wasting time ?
    		BitmapFactory.decodeFile(path, opt);
    		int width = (opt.outWidth);
    		int hight = (opt.outHeight);
    		int min = Math.min(width, hight);
    		int outSize = min / thumbnailSize ;    		
    		opt.inSampleSize = outSize;
    		opt.inJustDecodeBounds = false;
    		return(android.graphics.BitmapFactory.decodeFile(path, opt));    		
    	}
    	catch(Exception e)
    	{
    		return null;
    	}    	
    }
    
    public void isBackDisabled(View theButton, String currentPath)
    {
    	if(currentPath.equals(ROOT))    	
    		theButton.setEnabled(false);    	
    	else 
    		theButton.setEnabled(true);
    }    
    
    public void isHomeDisabled(View theButton, String currentPath)
    {
    	if(currentPath.equals(fileAction.getExternalStorageAddress()))    	
    		theButton.setEnabled(false);    	
    	else
    		theButton.setEnabled(true);
    }
    
    public void isPasteEnabled()
    {
    	switch(pasteRequest)
    	{
    	case NO_REQUEST:
    		pasteBtn.setEnabled(false);
    		break;
    	default:
    		pasteBtn.setEnabled(true);
    		break;
    	}    	
    }
    
   	public void onListItemClick(ListView parent,  View v, int position, long id)
   	{
   		cancelThumbnailLoading = true ;   		
    	String currentPathTemp = currentPath  + rowHolder.get(position).getLabel();
    	
    	if(fileAction.checkDirectory(currentPathTemp))
    	{
    		if(fileAction.isReadable(currentPathTemp))
    		{
    			currentPath = currentPathTemp + java.io.File.separatorChar;
    			displayContent();
    		}
    		else
    		{
    			showMessage(R.string.can_not_read_dir);
    		}
    	}    		
    	// The Path Is File
    	else 
    	{    		    		
    		String mimeType = fileAction.getMimeType(rowHolder.get(position).getLabel());
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
    			//TODO
    			//create a dialog with all app listed    			
    			showMessage(R.string.app_not_found);    			
    		}    		
    	}    	  	  		
    }
    
    protected void onLongListItemClick(final View v, int pos, long id)
    {
    	final int position = pos;
        AlertDialog.Builder optionBuilder = new AlertDialog.Builder(this);
        optionBuilder.setTitle(rowHolder.get(position).getLabel());
        optionBuilder.setIcon(rowHolder.get(position).getIcon());
        optionBuilder.setItems(R.array.Options, new DialogInterface.OnClickListener() 
        	{
        		public void onClick(DialogInterface dialoginterface,int witchBtn) 
        		{
        			optionSelect(v, position , witchBtn);
        		}
        	});
        AlertDialog optionDialog = optionBuilder.create();        
        optionDialog.setOnShowListener(new AlertDialogListView());        
       	optionDialog.show();       	
    }
    
   	protected void optionSelect(View v, int position , int witchBtn) 
       {
        switch (witchBtn)
        {
        case 0:
        	//do copy
        	pasteRequest = COPY_REQUEST;
        	copy_move_Path = currentPath;
        	copy_move_item = rowHolder.get(position).getLabel();         	
			notification.setText("Copy Item : " + copy_move_item);
        	break;
        case 1:
        	//do move
        	pasteRequest = MOVE_REQUEST;
        	copy_move_Path = currentPath;
        	copy_move_item = rowHolder.get(position).getLabel();        	
			notification.setText("Move Item : " + copy_move_item);
        	break;
        case 2:
        	//do rename
        	renameItem(v, position);
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
    	LayoutInflater lInflater = getLayoutInflater();
    	View view = lInflater.inflate(R.layout.mkdir, null);
    	TextView message = (TextView)view.findViewById(R.id.mkdir_input_message);
    	message.setTypeface(fontRegular);
    	message.setTextColor(textColor);
    	message.setTextSize(textSize);
    	message.setGravity(direction);
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    	dialogBuilder.setTitle(R.string.mkdir);
    	dialogBuilder.setView(view);
    	MakedirDialogButtonListener mkdbl = new MakedirDialogButtonListener(view, this);
    	dialogBuilder.setPositiveButton(R.string.ok, mkdbl);
    	dialogBuilder.setNegativeButton(R.string.cancel, mkdbl);
    	AlertDialog newDirDialog = dialogBuilder.create();
    	newDirDialog.show();
    }
    
    private class MakedirDialogButtonListener implements android.content.DialogInterface.OnClickListener
    {
    	private String inputText = null;
    	private View inputDialogView = null;
    		
    	public MakedirDialogButtonListener(View v, Context lContext)
    	{
    		inputDialogView = v;
    	}
    		
    	public void onClick(DialogInterface v, int buttonId)
    	{
    		if(buttonId == DialogInterface.BUTTON1)
    		{
    			inputText = getInputReply();
    			if(fileAction.isWriteable(currentPath))
    			{	
    				if(inputText.length() > 0)
    				{
    					boolean result = fileAction.createDir(currentPath, inputText);
    					if(result)
    					{
    						showMessage(R.string.mkdir_succ);
    					}
    					else
    					{
    						showMessage(R.string.mkdir_fail);
    					}
    				}
    				else
    				{
    					showMessage(R.string.mkdir_zero_input);
    				}
    			}
    			else
    			{
    				showMessage(R.string.mkdir_not_writeable);
    			}
    			displayContent();
    		}
    	}
    		
    	private String getInputReply()
    	{
    		EditText eText = (EditText)inputDialogView.findViewById(R.id.mkdir_input);
    		return (eText.getText().toString());
    	}
    }
    
    private void renameItem(View rowView, int position)
    {
    	LayoutInflater lInflater = getLayoutInflater();
    	View view = lInflater.inflate(R.layout.rename, null);
    	TextView message = (TextView)view.findViewById(R.id.rename_input_message);
    	message.setTypeface(fontRegular);
    	message.setTextColor(textColor);
    	message.setTextSize(textSize);
    	message.setGravity(direction);
    	EditText eText = (EditText)view.findViewById(R.id.rename_input);
    	eText.setText(rowHolder.get(position).getLabel());
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    	dialogBuilder.setTitle(R.string.rename_label);
    	dialogBuilder.setIcon(rowHolder.get(position).getIcon());
    	dialogBuilder.setView(view);
    	RenameDialogButtonListener rdbl = new RenameDialogButtonListener(view, this, position);
    	dialogBuilder.setPositiveButton(R.string.ok, rdbl);
    	dialogBuilder.setNegativeButton(R.string.cancel, rdbl);
    	AlertDialog renameDialog = dialogBuilder.create();
    	renameDialog.show();
    }    
    
    private class RenameDialogButtonListener implements android.content.DialogInterface.OnClickListener
    {
    	private String inputText = null;
    	private View inputDialogView = null;
    	private int position;
    	private String oldPath= null;
    		
    	public RenameDialogButtonListener(View v, Context lContext, int pos)
    	{
    		inputDialogView = v;
    		position = pos;
    		oldPath = currentPath + rowHolder.get(position).getLabel();
    	}
    		
    	public void onClick(DialogInterface v, int buttonId)
    	{
    		if(buttonId == DialogInterface.BUTTON1)
    		{
    			inputText = getInputReply();
    			if(fileAction.isWriteable(oldPath))
    			{	
    				if(inputText.length() > 0)
    				{
    					String newPath = currentPath + inputText;
    					boolean result = fileAction.rename(oldPath, newPath);
    					if(result)    						
    					{
    						rowHolder.get(position).setLabel(inputText);
    						showMessage(R.string.rename_succ);
    					}
    					else
    					{
    						showMessage(R.string.rename_fail);
    					}
    				}
    				else
    				{
    					showMessage(R.string.rename_zero_input);
    				}
    			}
    			else
    			{
    				showMessage(R.string.rename_not_writeable);
    			}
    			rowAdapter.notifyDataSetChanged();
    		}
    	}
    		
    	private String getInputReply()
    	{
    		EditText eText = (EditText)inputDialogView.findViewById(R.id.rename_input);
    		return (eText.getText().toString());
    	}
    }
    
    private void deleteObj(int pos, Context localContext)
    {
        final Context lContext = localContext;
        final int position = pos;        	
        AlertDialog.Builder deletePromptBuilder = new AlertDialog.Builder(this);
        deletePromptBuilder.setTitle(R.string.delete_label);        
        deletePromptBuilder.setIcon(rowHolder.get(position).getIcon());
        String warn = getString(R.string.delete_warn);
        TextView message = new TextView(lContext);
        message.setTypeface(fontRegular);
        message.setTextColor(textColor);
        message.setTextSize(textSize);
        message.setGravity(direction);
        message.setText(warn + "\n" + rowHolder.get(position).getLabel());
        deletePromptBuilder.setView(message);
        deletePromptBuilder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener()
        		{
            		public void onClick(DialogInterface dialoginterface,int i) 
            		{
            			String pathTemp = currentPath + rowHolder.get(position).getLabel();
            			if(fileAction.deleteFile(pathTemp))
            			{
            				showMessage(R.string.delete_succ);
            				displayContent();
            			}
            			else
            			{
            				showMessage(R.string.delete_fail);
            			}
               		}
        		});
        deletePromptBuilder.setNegativeButton(R.string.cancel , new DialogInterface.OnClickListener()
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
    	menu.add(Menu.NONE, PREFERENCES_MENU, Menu.NONE, R.string.preferences)
    			.setIcon(R.drawable.preferences);
    			//.setAlphabeticShortcut('p');
    	return (super.onCreateOptionsMenu(menu));
    }
    	
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case PREFERENCES_MENU:
    		startActivity(new Intent(this, Preferences.class));
    		return (true);
    	}
    	return (super.onOptionsItemSelected(item));
    }    
    
    public void thumbnailLoader()
    {
    	setProgressBarIndeterminateVisibility(true);
    	Thread iconThread = new Thread(null, loadThumb, "backgroundThumb");
    	iconThread.start();
    }
    
    private Runnable loadThumb = new Runnable()
    {    	
    	public void run()
    	{
    		try
    		{
    			for(int i = 0 ; i < items.length ; i++)
        		{
        			if(cancelThumbnailLoading)
        				return;
        			String tempPath = currentPath + rowHolder.get(i).getLabel();
        			Drawable icon = createThumb(tempPath);        			
        			rowHolder.get(i).setIcon(icon);
        			Message msg = thumbnailLoaderHandler.obtainMessage(MESSAGE_ICON_CHANGED);
        			thumbnailLoaderHandler.sendMessage(msg);        			
        		}
        		Message msg = thumbnailLoaderHandler.obtainMessage(MESSAGE_ICON_LOAD_END);
        		thumbnailLoaderHandler.sendMessage(msg);        		
    		}
    		catch(IndexOutOfBoundsException e)
    		{
    			Message msg = thumbnailLoaderHandler.obtainMessage(MESSAGE_OUT_OF_BOUNDS);
    			thumbnailLoaderHandler.sendMessage(msg);
    		}    		
    	}
    };    
    
    private Handler thumbnailLoaderHandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		switch(msg.what)
    		{
    		case MESSAGE_ICON_CHANGED:
    			rowAdapter.notifyDataSetChanged();
    			break;
    		case MESSAGE_ICON_LOAD_END:
    			setProgressBarIndeterminateVisibility(false);
    			break;
    		case MESSAGE_OUT_OF_BOUNDS:
    			displayContent();
    			break;
    		}    			
    	}
    }; 
    
    public String getSizeString(Long size)
    {
    	if(size.equals(FileAction.DIRECTORY_SIZE))
    	{
    		return("");
    	}
    	else if(size > GB)
		{
			size = size / GB;
			String sSize = new Long(size).toString();
			sSize = sSize + " GB";
			return (sSize);
		}			
		else if(size > MB)
		{
			size = size / MB;
			String sSize = new Long(size).toString();
			sSize = sSize + " MB";
			return (sSize);
		}			
		else if (size > KB)
		{
			size = size / KB;
			String sSize = new Long(size).toString();
			sSize = sSize + " KB";
			return (sSize);
		}			
		else
		{
			String sSize = new Long(size).toString();
			sSize = sSize + " B";
			return (sSize);
		}
    }
    
    public void sortItem()
    {
    	switch(sortType)
    	{
    	case SORT_BY_NAME_ASCE:
    		Collections.sort(rowHolder);
    		break;
    	case SORT_BY_NAME_DESC:
    		Collections.sort(rowHolder);
    		Collections.reverse(rowHolder);
    		break;
    	case SORT_BY_SIZE_ASCE:
    		Collections.sort(rowHolder, RowHolder.sortBySize);
    		break;
    	case SORT_BY_SIZE_DESC:
    		Collections.sort(rowHolder, RowHolder.sortBySize);
    		Collections.reverse(rowHolder);
    		break;
    	}    	
    }
    
    public void setLanguage()
    {
    	
    	Resources res = lContext.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(language);
		res.updateConfiguration(conf, dm);		
    }
    
    public void showMessage(int text)
    {
    	LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.toast, null);
		TextView message = (TextView)view.findViewById(R.id.toast_message);
		message.setTypeface(fontRegular);
		message.setTextColor(textColor);
		message.setTextSize(textSize);
		message.setGravity(direction);
		message.setText(text);
		Toast toast = new Toast(lContext);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(view);
		toast.show();
    }
}