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

package com.bewsia.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;
import com.bewsia.schema.Spider;

public class AddSpiderDialog extends Dialog {

    private Logger logger = Logger.getLogger(AddSpiderDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textTitle;
    private Text textScript;
    private Combo comboBuiltin;
    
    private List<String> scripts;
    private boolean canceled = true;
	
    public AddSpiderDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("AddSpiderDialog.WindowTitle"));
    }
	
    public boolean getCanceled() {
    	return canceled;
    }
    
    public void open() {
        shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContents(shell);
        shell.pack();
        shell.open();
        UITool.placeCentered(shell);
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
    private void createContents(final Shell shell) {
    	FormData fd;
    	Label label;
    	Button button;
    	Text text;
    	Combo combo;
    	
        shell.setImage(Images.get("Icon.Bewsia"));
        shell.setLayout(new FormLayout());
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddSpiderDialog.LabelTitle"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textTitle = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddSpiderDialog.LabelScript"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);

        text = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textScript = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 385;
        fd.height = 300;
        text.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddSpiderDialog.LabelBuiltinScript"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 410);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        combo = new Combo(shell, SWT.READ_ONLY);
        comboBuiltin = combo;
        fd = new FormData();
        fd.top = new FormAttachment(0, 430);
        fd.left = new FormAttachment(0, 10);
        fd.width = 390;
        combo.setLayoutData(fd);
        
        int size = controller.getConfig().getInt("script.size");
        scripts = new ArrayList<String>();
        combo.add("      ");
        for (int i = 1; i <= size; i++) {
        	scripts.add(controller.getConfig().getString("script." + i + ".filename"));
        	combo.add(controller.getConfig().getString("script." + i + ".title"));
        }
        combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int selIdx = comboBuiltin.getSelectionIndex();
				if (selIdx < 1) return;
				String title = comboBuiltin.getItem(selIdx);
				String filename = scripts.get(selIdx - 1);
				String content = controller.loadJS(filename);
				textTitle.setText(title);
				textScript.setText(content);
			}
        });
        combo.select(0);
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("AddSpiderDialog.ButtonClose"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("AddSpiderDialog.ButtonAdd"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -95);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Spider sp = controller.newSpider();
				sp.setId(controller.uniqid());
				sp.setTitle(textTitle.getText());
				sp.setScript(textScript.getText());
				sp.save();
				canceled = false;
				shell.close();
			}
        });
        
        label = new Label(shell, SWT.LEFT);
        fd = new FormData();
        fd.top = new FormAttachment(0, 500);
        fd.left = new FormAttachment(0, 435);
        label.setLayoutData(fd);
        
    }
    
}
