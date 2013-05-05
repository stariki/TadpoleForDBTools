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
package com.hangum.tadpole.rdb.core.actions.object.rdb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;

import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.commons.sql.TadpoleSQLManager;
import com.hangum.tadpole.dao.mysql.TableColumnDAO;
import com.hangum.tadpole.dao.mysql.TableDAO;
import com.hangum.tadpole.define.DB_Define;
import com.hangum.tadpole.define.DB_Define.DB_ACTION;
import com.hangum.tadpole.exception.dialog.ExceptionDetailsErrorDialog;
import com.hangum.tadpole.rdb.core.Activator;
import com.hangum.tadpole.rdb.core.Messages;
import com.hangum.tadpole.rdb.core.util.FindEditorAndWriteQueryUtil;
import com.ibatis.sqlmap.client.SqlMapClient;

public class GenerateSQLUpdateAction extends GenerateSQLSelectAction {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(GenerateSQLUpdateAction.class);
	public final static String ID = "com.hangum.db.browser.rap.core.actions.object.GenerateSQLUpdateAction"; //$NON-NLS-1$

	public GenerateSQLUpdateAction(IWorkbenchWindow window, DB_ACTION actionType, String title) {
		super(window, actionType, title);
	}
	
	@Override
	public void run() {
		StringBuffer sbSQL = new StringBuffer();
		try {
			TableDAO tableDAO = (TableDAO)sel.getFirstElement();
			
			Map<String, String> parameter = new HashMap<String, String>();
			parameter.put("db", userDB.getDb());
			parameter.put("table", tableDAO.getName());
			
			SqlMapClient sqlClient = TadpoleSQLManager.getInstance(userDB);
			List<TableColumnDAO> showTableColumns = sqlClient.queryForList("tableColumnList", parameter); //$NON-NLS-1$
			
			sbSQL.append(" UPDATE " + tableDAO.getName() + PublicTadpoleDefine.LINE_SEPARATOR + " SET "); //$NON-NLS-1$ //$NON-NLS-2$
			for (int i=0; i<showTableColumns.size(); i++) {
				TableColumnDAO dao = showTableColumns.get(i);
				sbSQL.append(dao.getField());
				
				// 마지막 컬럼에는 ,를 않넣어주어야하니까 
				if(i < (showTableColumns.size()-1)) sbSQL.append("= ?,  ");  //$NON-NLS-1$
				else sbSQL.append("=? "); //$NON-NLS-1$
			}

			sbSQL.append(PublicTadpoleDefine.LINE_SEPARATOR + " WHERE " + PublicTadpoleDefine.LINE_SEPARATOR); //$NON-NLS-1$
			int cnt = 0;
			for (int i=0; i<showTableColumns.size(); i++) {
				TableColumnDAO dao = showTableColumns.get(i);
				if(DB_Define.isKEY(dao.getKey())) {
					if(cnt == 0) sbSQL.append("\t" + dao.getField() + " = ? " + PublicTadpoleDefine.LINE_SEPARATOR); //$NON-NLS-1$ //$NON-NLS-2$
					else sbSQL.append("\tAND " + dao.getField() + " = ? "); //$NON-NLS-1$ //$NON-NLS-2$
					cnt++;
				}				
			}
			sbSQL.append(" ; "); //$NON-NLS-1$
			
			//
//			QueryEditorAction qea = new QueryEditorAction();
			FindEditorAndWriteQueryUtil.run(userDB, sbSQL.toString());
		} catch(Exception e) {
			logger.error("Generate SQL Statement Error", e); //$NON-NLS-1$
			
			Status errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); //$NON-NLS-1$
			ExceptionDetailsErrorDialog.openError(null, "Error", Messages.GenerateSQLUpdateAction_13, errStatus); //$NON-NLS-1$
		}
	}

}
