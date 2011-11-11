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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnPreferenceClickListener, ColorPickerDialog.OnColorChangedListener
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		addPreferencesFromResource(R.xml.preferences);
		Preference textColorPreferences = (Preference)this.findPreference("textColor");
		textColorPreferences.setOnPreferenceClickListener(this);
		Preference backColor_1_preferences = (Preference)this.findPreference("backColor1");
		backColor_1_preferences.setOnPreferenceClickListener(this);
		Preference backColor_2_preferences = (Preference)this.findPreference("backColor2");
		backColor_2_preferences.setOnPreferenceClickListener(this);
		Preference resetSetting_preferences = (Preference)this.findPreference("resetSetting");
		resetSetting_preferences.setOnPreferenceClickListener(this);		
	}
	
	 public boolean onPreferenceClick(Preference pref)	 
	 {	
		 String key = pref.getKey();
		 if(key.equals("textColor"))
		 {
			 int color;
			 color = Main.textColor;
			 new ColorPickerDialog(this, this, key, color, color).show();	 
		     return true;
		 }
		 else if(key.equals("backColor1"))
		 {
			 int color;
			 color = Main.backColor1;
			 new ColorPickerDialog(this, this, key, color, color).show();	 
		     return true;
		 }
		 else if(key.equals("backColor2"))
		 {
			 int color;
			 color = Main.backColor2;
			 new ColorPickerDialog(this, this, key, color, color).show();	 
		     return true;
		 }
		 else
		 {
			 this.findPreference("toggleHiddenFile").getEditor().putBoolean("toggleHiddenFile", true).commit();
			 this.findPreference("thumbnailSize").getEditor().putString("thumbnailSize", Main.DEFAULT_THUMBNAIL_SIZE).commit();
			 this.findPreference("textSize").getEditor().putString("textSize", Main.DEFAULT_TEXT_SIZE).commit();
			 this.findPreference("language").getEditor().putString("language", "en").commit();
			 int textColor = Main.lContext.getResources().getColor(R.color.text_color);
			 this.findPreference("textColor").getEditor().putInt("textColor", textColor).commit();
			 int backColor1 = Main.lContext.getResources().getColor(R.color.background_color_1);
			 this.findPreference("backColor1").getEditor().putInt("backColor1", backColor1).commit();
			 int backColor2 = Main.lContext.getResources().getColor(R.color.background_color_2);
			 this.findPreference("backColor2").getEditor().putInt("backColor2", backColor2).commit();
			 
			 return true;
		 }	     	 
	 }
	 
	 public void colorChanged(String key, int color)
	 {
		 this.findPreference(key).getEditor().putInt(key, color).commit();
	 }	 
}