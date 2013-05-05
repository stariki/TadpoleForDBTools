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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * DDL Source viewer dialog
 * 
 * @author hangum
 *
 */
public class DDLSourceViewerDialog extends Dialog {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DDLSourceViewerDialog.class);
	
	private DDLSourceComposite ddlSourceComposite; 
	
	private String tableName;
	private String ddlSource;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public DDLSourceViewerDialog(Shell parentShell, String tableName, String ddlSource) {
		super(parentShell);
		
		this.tableName = tableName;
		this.ddlSource = ddlSource;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(tableName);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		gridLayout.marginWidth = 2;
		
		ddlSourceComposite = new DDLSourceComposite(container, SWT.NONE, ddlSource);
		ddlSourceComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		ddlSourceComposite.setLayout(new GridLayout(1, false));

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}

	/**
	 * set ddl text
	 * 
	 * @param ddlText
	 */
	public void setDDLText(String ddlText) {
		ddlSourceComposite.setDdlSource(ddlText);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(681, 534);
	}

}
