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
package com.hangum.tadpole.sql.util;

import java.io.StringReader;
import java.sql.ResultSet;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * <pre>
 *  java.sql.ResultSet과 ResultSetMeta를 TableViewer로 바꾸기 위해 가공하는 Util
 *  
 *  resource데이터를 저장하기 위해 data를 배열화시킨다.
 * </pre>
 * 
 * @author hangum
 *
 */
public class SQLUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SQLUtil.class);
	
	/**
	 * pattern statement 
	 * 
	 * <PRE>
	 * 		CHECK는 MYSQL의 CHECK TABLE VIEW_TABLE_NAME; 명령으로 VIEW의 정보를 볼수 있습니다.
	 * 		PRAGMA는 sqlite의 시스템 쿼리 얻는 거.
	 * </PRE>
	 */
	private static final String PATTERN_STATEMENT = "^SELECT.*|^SHOW.*|^DESCRIBE.*|^DESC.*|^CHECK.*|^PRAGMA.*";
	private static final Pattern PATTERN_STATEMENT_QUERY = Pattern.compile(PATTERN_STATEMENT, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	
	private static final String PATTERN_EXECUTE = "^GRANT.*|^REVOKE.*|^ALTER.*|^DROP.*|^RENAME.*|^TRUNCATE.*|^COMMENT.*";
	private static final String PATTERN_EXECUTE_UPDATE = "^INSERT.*|^UPDATE.*|^DELETET.*|^MERGE.*|^COMMIT.*|^ROLLBACK.*|^SAVEPOINT.*";
	private static final String PATTERN_EXECUTE_CREATE = "^CREATE.*|^DECLARE.*";
	private static final Pattern PATTERN_EXECUTE_QUERY = Pattern.compile(PATTERN_EXECUTE + PATTERN_EXECUTE_UPDATE + PATTERN_EXECUTE_CREATE, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	

	private static final String PATTERN_COMMENT = "/\\*([^*]|[\r\n]|(\\*+([^*/]|[\r\n])))*\\*+/";
	private static final String PATTERN_COMMENT2 = "(--.*)|(//.*)";
	
	/** 허용되지 않는 sql 정의 */
	private static final String[] NOT_ALLOWED_SQL = {
		/* MSSQL- USE DATABASE명 */
		"USE"
		};
	
	/**
	 * remove comment
	 * 
	 * @param strSQL
	 * @return
	 */
	public static String removeComment(String strSQL) {
		if(null == strSQL) return "";
		
		String retStr = strSQL.replaceAll(PATTERN_COMMENT, "");
		retStr = retStr.replaceAll(PATTERN_COMMENT2, "");
		
		return retStr;
	}
	
	/**
	 * 쿼리중에 허용하지 않는 쿼리 목록.
	 * 쿼리문 위에 주석을 빼야... -- / ** * / / * * /
	 * 
	 * @param strSQL
	 * @return
	 */
	public static boolean isNotAllowed(String strSQL) {
		boolean isRet = false;
		strSQL = removeComment(strSQL);

		String cmpSql = StringUtils.trim(strSQL);
		
		for (String strNAllSQL : NOT_ALLOWED_SQL) {
			if(StringUtils.startsWith(cmpSql.toLowerCase(), strNAllSQL.toLowerCase())) {
				return true;
			}
		}
		
		return isRet;
	}
	
	
	/**
	 * 쿼리의 패턴이 <code>PATTERN_STATEMENT</code>인지?
	 * 
	 * @param strSQL
	 * @return
	 */
	public static boolean isStatement(String strSQL) {
		boolean isStatement = false;
		
		strSQL = removeComment(strSQL);
		if((PATTERN_STATEMENT_QUERY.matcher(strSQL)).matches()) {
			return true;
		} else {
			try {
				CCJSqlParserManager parserManager = new CCJSqlParserManager();
				Statement statement = parserManager.parse(new StringReader(strSQL));
				
				if(statement instanceof Select) return true;
			} catch(Exception e) {
				logger.error("SQL Parser Exception.\n sql is [" + strSQL + "]");
			}
		}
		
		return isStatement;
	}
	
	/**
	 * INSERT 문을 생성합니다.
	 * 
	 * @param tableName
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public static String makeInsertStatment(String tableName, ResultSet rs) throws Exception {
		StringBuffer result = new StringBuffer("INSERT INTO " + tableName + "(");
		
		Map<Integer, String> mapTable = ResultSetUtils.getColumnName(rs);
		for( int i=0 ;i<mapTable.size(); i++ ) {
			if( i != (mapTable.size()-1) ) result.append( mapTable.get(i) + ",");
			else result.append( mapTable.get(i));
		}
		
		result.append(") VALUES(");
		
		for( int i=0 ;i<mapTable.size(); i++ ) {
			if( i != (mapTable.size()-1) ) result.append("?,");
			else result.append('?');
		}
		
		result.append(')');			
		
		if(logger.isDebugEnabled()) logger.debug("[make insert statment is " + result.toString());
		
		return result.toString();
	}
	
	/**
	 * 쿼리 텍스트에 쿼리 이외의 특수 문자를 제거해 줍니다.
	 * 
	 * @param exeSQL
	 * @return
	 */
	public static String executeQuery(String exeSQL) {
		try {
//			
//			https://github.com/hangum/TadpoleForDBTools/issues/140 오류로 불럭지정하였습니다.
//			TO DO 특정 쿼리에서는 주석이 있으면 오류인데..DB에서 쿼리를 실행받는 다양한 조건을 고려할 필요가 있습니다. 
			
			// 문장 의 // 뒤에를 주석으로 인식 쿼리열에서 제외합니다.
			/*
			 *  mysql의 경우 주석문자 즉, -- 바로 다음 문자가 --와 붙어 있으면 주석으로 인식하지 않아 오류가 발생합니다. --comment 이면 주석으로 인식하지 않습니다.(다른 디비(mssql, oralce, pgsql)은 주석으로 인식합니다)
			 *  고칠가 고민하지만, 실제 쿼리에서도 동일하게 오류로 처리할 것이기에 주석을 지우지 않고 놔둡니다. - 2013.11.11- (hangum)
			 */
//			exeSQL = delComment(exeSQL, "--");
			
			// 마지막 쿼리를 재 사용하기위해
//			exeSQL = StringUtils.replace(exeSQL, "\r", " ");
//			exeSQL = StringUtils.replace(exeSQL, "\n", " ");
//			exeSQL = StringUtils.replace(exeSQL, Define.LINE_SEPARATOR, " ");
//			exeSQL = exeSQL.replaceAll("(\r\n|\n|\r)", " ");
			
			// 모든 쿼리에 공백 주석 제거
			exeSQL = StringUtils.trimToEmpty(exeSQL);
			
		} catch(Exception e) {
			logger.error("query execute", e);
		}
		
		return exeSQL;
	}
	
	/**
	 * db resource data를 저장할때 2000byte 단위로 저장하도록 합니다.
	 * 
	 * @param resource data
	 * @return
	 */
	public static String[] makeResourceDataArays(String resourceContent)  {
		int cutsize = 1998;
		String[] tmpRetArryStr = new String[2000];
		resourceContent = resourceContent == null ? "" : resourceContent;
		byte[] byteSqlText = resourceContent.getBytes();
		
		int isEndTextHangul = 0;
		int workCnt = 0;

		while (byteSqlText.length > cutsize) {
			isEndTextHangul = 0;
			for (int i=0; i<cutsize; i++) {
				if (byteSqlText[i] < 0) isEndTextHangul++;
			}

			if (isEndTextHangul%2 != 0) {
				tmpRetArryStr[workCnt] = new String(byteSqlText, 0, cutsize + 1);
				byteSqlText = new String(byteSqlText, cutsize + 1, byteSqlText.length - (cutsize + 1)).getBytes();
			} else {
				tmpRetArryStr[workCnt] = new String(byteSqlText, 0, cutsize);
				byteSqlText = new String(byteSqlText, cutsize, byteSqlText.length - cutsize).getBytes();
			}

			workCnt++;
		}
		tmpRetArryStr[workCnt] = new String(byteSqlText);
		
		// 결과가 있는 만큼 담기위해
		String[] returnDataArry = new String[workCnt+1];
		for (int i=0; i<=workCnt; i++) {
			returnDataArry[i] = tmpRetArryStr[i];
		}
		
		return returnDataArry;
	}
}
