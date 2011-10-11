/*
    Micro Manager, an open source file manager for the Android systems
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

public class FileAction
{	
	private static final int BUFFER_SIZE = 64 * 1024;
	public static final Long DIRECTORY_SIZE = new Long(-1);
	
	
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
		File file = createFile(currentPath);
		return (file.isFile());
	}	
	
	public boolean checkDirectory(String currentPath)
	{
		File file = createFile(currentPath);
		return (file.isDirectory());
	}
	
	public String findParent(String currentPath)
	{
		File file = createFile(currentPath);
		String parentPath = file.getParent();
		if (!(parentPath.equals("/")))
			parentPath = parentPath + "/";
		return (parentPath);
	}	
	
	public String[] getItemList(String currentPath, boolean showHiddenFile)
	{		
		if(showHiddenFile)
		{
			File file = createFile(currentPath);
			String itemList[] = file.list();
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
			
			File file = createFile(currentPath);
			String itemList[] = file.list(filter);
			return itemList;			
		}			
	}
	
	public Long getSize(String currentPath)
	{
		File file = createFile(currentPath);
		if(file.isFile())
		{
			long size= file.length();
			return (size);			
		}
		else
			//TO DO reverse call
			return (DIRECTORY_SIZE);
	}
	
	public boolean createDir(String currentPath, String item)
	{
		if(isWriteable(currentPath))
		{
			String tempPath = currentPath + item;
			File file = createFile(tempPath);
			boolean result = file.mkdir();
			return (result);
		}
		else
			return (false);
	}	
	
	public boolean deleteFile(String currentPath)
	{
		if(checkFile(currentPath) && isReadable(currentPath) && isWriteable(currentPath))
		{
			File file = createFile(currentPath);
			return(file.delete());
		}
		else if(checkDirectory(currentPath) && isReadable(currentPath) && isWriteable(currentPath))
		{
			String[] items = getItemList(currentPath, true);
			if(items.length == 0)
			{
				File file = createFile(currentPath);
				return (file.delete());
			}
			else if(items.length > 0)
			{
				for(int i = 0 ; i < items.length ; i++)
				{
					String tempPath = currentPath + java.io.File.separatorChar + items[i] ;
					if(checkDirectory(tempPath))
						deleteFile(tempPath);
					else if(checkFile(tempPath))
					{
						File file = createFile(tempPath);
						file.delete();
					}
				}
			}
			File file = createFile(currentPath);
			return(file.delete());
		}
		return(false);		
	}	
	
	public boolean isWriteable(String currentPath)
	{
		File file = createFile(currentPath);
		boolean result = file.canWrite();
		return (result);
	}	
	
	public boolean isReadable(String currentPath)
	{
		File file = createFile(currentPath);
		boolean result = file.canRead();
		return (result);
	}	
	
	public boolean rename(String oldName, String newName)
	{		
		File oldFile = createFile(oldName);
		File newFile = createFile(newName);
		boolean result = oldFile.renameTo(newFile);
		return (result);
	}	
	
	public boolean copyFile(String srcItem, String desDir, String srcDir)
	{
		String srcPath = srcDir + srcItem;
		String desPath = desDir + srcItem;		
		byte[] bufferedData =new byte[BUFFER_SIZE];
		int readLength = 0;
		
		if(checkFile(srcPath) && isReadable(srcPath) && checkDirectory(desDir) && isWriteable(desDir))
		{
			try
			{				
				FileInputStream inStream = new FileInputStream(srcPath);
				BufferedInputStream buffInStream = new BufferedInputStream(inStream);
				FileOutputStream outStream = new FileOutputStream(desPath);
				BufferedOutputStream buffOutStream = new BufferedOutputStream(outStream);
				
				while((readLength = buffInStream.read(bufferedData, 0, BUFFER_SIZE)) != -1)
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
		
		else if(checkDirectory(srcPath) && isReadable(srcPath) && checkDirectory(desDir) && isWriteable(desDir))
		{
			String items[] = getItemList(srcPath, true);
			String newDir = desDir + srcItem + java.io.File.separatorChar ;
			int Itemlength = items.length;
			boolean result = createDir(desDir, srcItem);
			if(!result)
				return(false);
			for(int i = 0 ; i < Itemlength ; i++)
				copyFile(items[i], newDir, srcPath+ java.io.File.separatorChar);
		}
		return(true);		
	}	
	
	private File createFile(String currentPath)
	{
		File file = new File(currentPath);
		return file;
	}	
	
	public boolean isHidden(String itemName)
	{
		if(itemName.charAt(0) == '.')
			return (true);
		else
			return (false);
	}
	
	public String getExternalStorageAddress()
	{
		File external = Environment.getExternalStorageDirectory();
		String exAddress = external.getPath() + java.io.File.separatorChar;		
		return(exAddress);
	}		
}