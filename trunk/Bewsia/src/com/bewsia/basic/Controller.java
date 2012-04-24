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

package com.bewsia.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.bewsia.schema.Spider;
import com.bewsia.script.LuceneHandler;
import com.bewsia.script.safe.lucene.SEntity;

public class Controller {

    private static Logger logger = Logger.getLogger(Controller.class);

    private Config config;
    private Config buffer;
    private String appDir;
    private String cfgDir;
    private String logDir;
    private String cfgFile;
    private String datDir;
    private String idxDir;
    private String bakDir;
    private String jsDir;
    private String engDir;

    public Controller() {
    	config = new Config();
    	buffer = new Config();
    	appDir = System.getProperty("user.dir");
    	cfgDir = new File(appDir, "cfg").getAbsolutePath();
    	logDir = new File(appDir, "log").getAbsolutePath();
    	datDir = new File(appDir, "dat").getAbsolutePath();
    	cfgFile = new File(cfgDir, "config.properties").getAbsolutePath();
    	engDir = new File(datDir, "engine").getAbsolutePath();
    	idxDir = new File(engDir, "index").getAbsolutePath();
    	bakDir = new File(engDir, "backup").getAbsolutePath();
    	jsDir = new File(datDir, "script").getAbsolutePath();
    	loadConfig();
    }

    public Config getConfig() {
    	return config;
    }
    
    public Config getBuffer() {
    	return buffer;
    }
    
    public String getAppDir() {
    	return appDir;
    }
    
    public String getCfgDir() {
    	return cfgDir;
    }
    
    public String getLogDir() {
    	return logDir;
    }
    
    public String getCfgFile() {
    	return cfgFile;
    }
    
    public String getDatDir() {
    	return datDir;
    }
    
    public String getIdxDir() {
    	return idxDir;
    }
    
    public String getBakDir() {
    	return bakDir;
    }

    public String getEngDir() {
    	return engDir;
    }
    
    public String getJsDir() {
    	return jsDir;
    }
    
    public void saveConfig() {
    	config.save(cfgFile);
    }
    
    public void loadConfig() {
    	config.load(cfgFile);
    }
    
    public SEntity newEntity() {
    	return new SEntity(new LuceneHandler(idxDir, bakDir, config.getDouble(Config.SYSTEM_QUOTA)));
    }

    public SEntity newSelectedEntity() {
    	String dirEng = getSelectedEnginePath();
    	double quota = getSelectedEngineQuota();
    	File file = new File(dirEng, "index");
    	file.mkdirs();
    	String dirIdx = file.getAbsolutePath();
    	file = new File(dirEng, "backup");
    	file.mkdirs();
    	String dirBak = file.getAbsolutePath();
    	return new SEntity(new LuceneHandler(dirIdx, dirBak, quota));
    }
    
    public Spider newSpider() {
    	String dirEng = getSelectedEnginePath();
    	double quota = getSelectedEngineQuota();
    	File file = new File(dirEng, "index");
    	file.mkdirs();
    	String dirIdx = file.getAbsolutePath();
    	file = new File(dirEng, "backup");
    	file.mkdirs();
    	String dirBak = file.getAbsolutePath();
    	return new Spider(new LuceneHandler(dirIdx, dirBak, quota));
    }
    
    public String loadJS(String filename) {
    	String tag = "";
    	try {
            String jsFile = new File(jsDir, filename).getAbsolutePath();
            InputStream is = new FileInputStream(jsFile);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            tag = new String(buffer, "UTF-8");
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return tag;
    }
    
    public String uniqid() {
    	return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public String suniqid() {
        Random random = new Random();
        return Long.toString(Math.abs(random.nextLong()), 36);
    }
    
    public void checkEngineList() {
    	loadConfig();
    	int size = config.getInt("engine.size");
    	if (size <= 0) {
    		config.setValue("engine.1.path", engDir);
    		config.setValue("engine.1.title", "Default engine");
    		config.setValue("engine.1.quota", config.getDouble(Config.SYSTEM_QUOTA));
    		config.setValue("engine.size", 1);
    		saveConfig();
    		size = config.getInt("engine.size");
    	}
    	String selPath = config.getString("engine.selected.path");
    	boolean found = false;
    	for (int i = 1; i <= size; i++) {
    		String tmp = config.getString("engine." + i + ".path");
    		if (tmp.equalsIgnoreCase(selPath)) {
    			found = true;
    			break;
    		}
    	}
    	if (!found) {
    		config.setValue("engine.selected.path", config.getString("engine.1.path"));
    		config.setValue("engine.selected.title", config.getString("engine.1.title"));
    		config.setValue("engine.selected.quota", config.getDouble("engine.1.quota"));
    		saveConfig();
    	}
    }
    
    public String getSelectedEnginePath() {
    	checkEngineList();
    	return config.getString("engine.selected.path");
    }
    
    public String getSelectedEngineTitle() {
    	checkEngineList();
    	return config.getString("engine.selected.title");
    }
    
    public double getSelectedEngineQuota() {
    	checkEngineList();
    	return config.getDouble("engine.selected.quota");
    }
    
}
