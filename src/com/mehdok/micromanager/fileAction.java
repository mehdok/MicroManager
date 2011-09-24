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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import android.os.Environment;
import android.webkit.MimeTypeMap;

public class fileAction
{
	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final int GB = MB * KB;
	private static final int BUFFERSIZE =64 * 1024;	
	
	
	public String getMimeType(String item)
	{
		String ext = item.substring(item.lastIndexOf(".") + 1, item.length());
		ext = ext.toLowerCase();
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		
		if (mimeType == null)
			return "*/";
		else
			return mimeType;
	}	
	
	public boolean checkFile(String currentPath)
	{
		File name = createFile(currentPath);
		return (name.isFile());
	}	
	
	public boolean checkDirectory(String currentPath)
	{
		File name = createFile(currentPath);
		return (name.isDirectory());
	}
	
	public String findParent(String currentPath)
	{
		File name = createFile(currentPath);
		String parentPath = name.getParent();
		if (!(parentPath.equals("/")))
			parentPath = parentPath + "/";
		return (parentPath);
	}	
	
	public String[] getItemList(String currentPath, boolean showHiddenFile)
	{
		if(showHiddenFile)
		{
			File name = createFile(currentPath);
			String itemList[] = name.list();
			return(itemList);
		}		
		else 
		{
			FilenameFilter filter = new FilenameFilter()
			{
				public boolean accept(File dir, String fileName)
				{
					return !fileName.startsWith(".");
				}
			};
			
			File name = createFile(currentPath);
			String itemList[] = name.list(filter);
			return itemList;			
		}		
	}	
	
	public String getSize(String currentPath)
	{
		File name = createFile(currentPath);
		if(name.isFile())
		{
			long size= name.length();
			if(size > GB)
			{
				size = size / GB;
				String s = new Long(size).toString();
				s = s + " GB";
				return (s);
			}			
			else if(size > MB)
			{
				size = size / MB;
				String s = new Long(size).toString();
				s = s + " MB";
				return (s);
			}			
			else if (size > KB)
			{
				size = size / KB;
				String s = new Long(size).toString();
				s = s + " KB";
				return (s);
			}			
			else
			{
				String s = new Long(size).toString();
				s = s + " B";
				return (s);
			}
		}
		else
			return "";
	}	
	
	public boolean createDir(String currentPath, String nm)
	{
		if(isWriteable(currentPath))
		{
			String tempPath = currentPath + java.io.File.separatorChar + nm;
			File name = createFile(tempPath);
			boolean result = name.mkdir();
			return (result);
		}
		else
			return (false);
	}	
	
	public boolean deleteFile(String currentPath)
	{
		if(checkFile(currentPath) && isReadable(currentPath) && isWriteable(currentPath))
		{
			File name = createFile(currentPath);
			return(name.delete());
		}
		else if(checkDirectory(currentPath) && isReadable(currentPath) && isWriteable(currentPath))
		{
			String[] items = getItemList(currentPath, true);
			if(items.length == 0)
			{
				File name = createFile(currentPath);
				return (name.delete());
			}
			else if(items.length > 0)
			{
				for(int i = 0 ; i < items.length ; i++)
				{
					String tempPath = currentPath + java.io.File.separatorChar + items[i];
					if(checkDirectory(tempPath))
						deleteFile(tempPath);
					else if(checkFile(tempPath))
					{
						File name = createFile(tempPath);
						name.delete();
					}
				}
			}
			File name = createFile(currentPath);
			return(name.delete());
		}
		return(false);		
	}	
	
	public boolean isWriteable(String currentPath)
	{
		File name = createFile(currentPath);
		boolean result = name.canWrite();
		return (result);
	}	
	
	public boolean isReadable(String currentPath)
	{
		File name = createFile(currentPath);
		boolean result = name.canRead();
		return (result);
	}	
	
	public boolean rename(String currentPath, String newName)
	{		
		File oldFile = createFile(currentPath);
		String xxx = "";
		if(checkFile(currentPath))
			xxx = currentPath.substring(currentPath.lastIndexOf("."), currentPath.length());
		
		String pathTemp = findParent(currentPath) + java.io.File.separatorChar + newName + xxx;
		File newFile = createFile(pathTemp);
		boolean result = oldFile.renameTo(newFile);
		return (result);		
	}	
	
	public boolean copyFile(String src, String des, String oldDir)
	{
		String source = oldDir + java.io.File.separatorChar + src;
		String destination = des + java.io.File.separatorChar + src;		
		byte[] bufferedData =new byte[BUFFERSIZE];
		int readLength = 0;
		
		if(checkFile(source) && isReadable(source) && checkDirectory(des) && isWriteable(des))
		{
			try
			{				
				FileInputStream inStream = new FileInputStream(source);
				BufferedInputStream buffInStream = new BufferedInputStream(inStream);
				FileOutputStream outStream = new FileOutputStream(destination);
				BufferedOutputStream buffOutStream = new BufferedOutputStream(outStream);
				
				while((readLength = buffInStream.read(bufferedData, 0, BUFFERSIZE)) != -1)
					buffOutStream.write(bufferedData, 0, readLength);
				buffOutStream.flush();
				buffInStream.close();
				buffOutStream.close();
				inStream.close();
				outStream.close();
				return(true);																
			}
			catch(FileNotFoundException e)
			{
				return(false);
			}
			catch(IOException e)
			{
				return(false);
			}
		}
		
		else if(checkDirectory(source) && isReadable(source) && checkDirectory(des) && isWriteable(des))
		{
			String items[] = getItemList(source, true);
			String newDir = des + java.io.File.separatorChar + src;
			int Itemlength = items.length;
			boolean result = createDir(des, src);
			if(!result)
				return(false);
			for(int i = 0 ; i < Itemlength ; i++)
				copyFile(items[i], newDir, source);
		}
		return(true);
	}	
	
	private File createFile(String currentPath)
	{
		File name = new File(currentPath);
		return name;
	}
	
	public String getLastModified(String currentPath)
	{
		File name = createFile(currentPath);
		long lastModified = name.lastModified();
		String s = new Long(lastModified).toString();
		return (s);
	}	
	
	public boolean isHidden(String itemName)
	{
		if(itemName.charAt(0) == '.')
			return (true);
		else
			return (false);
	}	
	
	public boolean setHidden(String currentPath, String name)
	{		
		if(checkFile(currentPath + java.io.File.separatorChar + name))
		{
			String newName = "." + name.substring(0, name.lastIndexOf("."));
			boolean result = rename(currentPath + java.io.File.separatorChar + name, newName);
			return (result);
		}
		else if(checkDirectory(currentPath + java.io.File.separatorChar + name))
		{
			String newName = "." + name;
			boolean result = rename(currentPath + java.io.File.separatorChar + name, newName);
			return (result);
		}
		return (false);					
	}	
	
	public boolean setUnHidden(String currentPath, String name)
	{
		if(checkFile(currentPath + java.io.File.separatorChar + name))
		{
			String newName = name.substring(1, name.lastIndexOf("."));
			boolean result = rename(currentPath + java.io.File.separatorChar + name, newName);
			return (result);
		}
		else if(checkDirectory(currentPath + java.io.File.separatorChar + name))
		{
			String newName = name.substring(1);
			boolean result = rename(currentPath + java.io.File.separatorChar + name, newName);
			return (result);
		}
		return (false);		
	}	
	
	public String getInternalStorage()
	{
		File internal = Environment.getDataDirectory();
		String inAddress = internal.getPath();		
		return(inAddress);
	}	
	
	public String getExternalStorage()
	{
		File external = Environment.getExternalStorageDirectory();
		String exAddress = external.getPath() + "/";		
		return(exAddress);
	}		
}