/*
 *  Bewsia - Micro Search Engine for Desktop
 * 
 *  Copyright (c) 2011 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.bewsia.script.safe.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.bewsia.basic.Controller;

public class SDisk {

	private Logger logger = Logger.getLogger(SDisk.class);
	
	private Controller controller;
	private String root;
	
	public SDisk() {
		controller = new Controller();
    	String dirEng = controller.getSelectedEnginePath();
    	File file = new File(dirEng, "disk");
    	file.mkdirs();
		root = file.getAbsolutePath();
	}
	
	public boolean exists(String path) {
		String filename = parse(path);
		return new File(filename).exists();
	}
	
	public byte[] read(String path) {
		byte[] tag = new byte[0];
		try {
			String filename = parse(path);
    		InputStream is = new FileInputStream(filename);
    		tag = new byte[is.available()];
    		is.read(tag);
    		is.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	public void mkdirs(String path) {
		new File(parse(path)).mkdirs();
	}
	
	public void write(String path, byte[] data) {
		try {
			String filename = parse(path);
			if (root.equals(filename)) return;
			File file = new File(filename);
			if (file.getName().lastIndexOf(".") < 0) return;
			file.getParentFile().mkdirs();
			OutputStream os = new FileOutputStream(filename);
			os.write(data);
			os.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public List<FileEntry> list(String path) {
		List<FileEntry> tag = new ArrayList<FileEntry>();
		try {
			File[] children = new File(parse(path)).listFiles(); 
			for (int i = 0; i < children.length; i++) {
				File child = children[i];
				FileEntry fe = new FileEntry();
				fe.name = child.getName();
				fe.size = child.length();
				fe.directory = child.isDirectory();
				tag.add(fe);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	private String parse(String path) {
		String tag = path;
		tag = tag.replaceAll("\\.\\.", "");
		if (tag.startsWith("/")) tag = tag.substring(1);
		if (File.pathSeparator.equals("\\")) {
			tag = tag.replaceAll("/", File.pathSeparator);
		}
		if (tag.length() == 0) {
			return root;
		} else {
			return new File(root, tag).getAbsolutePath();
		}
	}
	
	public static class FileEntry {
		public String name = "";
		public long size = 0;
		public boolean directory = false;
	}
	
}
