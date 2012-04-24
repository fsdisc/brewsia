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

package com.bewsia.schema;

import com.bewsia.script.safe.lucene.SEntity;

public class Spider extends SEntity {

	public static final String TITLE = "title";
	public static final String SCRIPT = "script";
	
    public Spider(Handler handler) {
    	super(handler);
    	setKind("Spider");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(TITLE, STRING);
        register(SCRIPT, STRING);
    }
    
    public String getTitle() {
    	return getString(TITLE);
    }
    
    public void setTitle(String src) {
    	setString(TITLE, src);
    }
    
    public String getScript() {
    	return getString(SCRIPT);
    }
    
    public void setScript(String src) {
    	setString(SCRIPT, src);
    }
    
}
