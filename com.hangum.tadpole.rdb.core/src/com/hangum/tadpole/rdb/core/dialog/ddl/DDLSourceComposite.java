/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.rdb.core.dialog.ddl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.hangum.tadpole.editor.core.widgets.editor.TadpoleOrionHubEditor;

/**
 * Table summary composite
 * 
 * @author hangum
 *
 */
public class DDLSourceComposite extends Composite {
	
	private TadpoleOrionHubEditor ddlSource;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public DDLSourceComposite(Composite parent, int style, String initDDL) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite compositeBasic = new Composite(this, SWT.NONE);
		compositeBasic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		compositeBasic.setLayout(new GridLayout(1, false));
		
		ddlSource = new TadpoleOrionHubEditor(compositeBasic, SWT.BORDER, initDDL);
		ddlSource.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
	}
	
	/**
	 * set ddl
	 * 
	 * @param ddlSource
	 * @deprecated
	 */
	public void setDdlSource(String strDdl) {
		ddlSource.setText(strDdl);
	}
	
	/**
	 * get ddl
	 * 
	 * @return
	 */
	public String getDdlSource() {
		return ddlSource.getText();
	}

	@Override
	protected void checkSubclass() {
	}

}
