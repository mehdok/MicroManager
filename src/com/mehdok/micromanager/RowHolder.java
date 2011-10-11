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

import java.util.Comparator;

import android.graphics.drawable.Drawable;

public class RowHolder implements Comparable <RowHolder>
{
	private Drawable icon ;
	private String label;
	private Long size;
	
	public RowHolder(Drawable drawable, String lString, Long lSize)
	{		
		icon = 	drawable;				
		label = lString;
		size = lSize;
	}
	
	public void setIcon(Drawable drawable)
	{
		icon = drawable;
	}
	
	public Drawable getIcon()
	{
		return (icon);
	}
	
	public void setLabel(String string)
	{
		label = string;
	}
	
	public String getLabel()
	{
		return (label);
	}
	
	public void setSize(Long longSize)
	{
		size = longSize;		
	}
	
	public Long getSize()
	{
		return (size);
	}
	
	//sort by name
	public int compareTo(RowHolder other)
	{
		if(this.size.equals(FileAction.DIRECTORY_SIZE))
		{
			if(other.getSize().equals(FileAction.DIRECTORY_SIZE))
			{
				return (this.label.compareToIgnoreCase(other.getLabel()));
			}
			else
			{
				return (-1);
			}
		}
		else if(other.getSize().equals(FileAction.DIRECTORY_SIZE))
		{
			return (1);
		}
		else
		{
			return (this.label.compareToIgnoreCase(other.getLabel()));
		}
		/*
		return ((this.label.compareToIgnoreCase(other.getLabel())) &
				(this.size.compareTo(other.getSize())));
		*/
	}
	
	public static Comparator <RowHolder> sortBySize = new Comparator <RowHolder>()
	{
		public int compare(RowHolder first, RowHolder second)
		{
			return (first.size.compareTo(second.size));
		}
	};
}