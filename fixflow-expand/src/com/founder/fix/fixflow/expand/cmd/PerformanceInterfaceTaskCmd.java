/**
 * Copyright 1996-2013 Founder International Co.,Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author kenshin
 */
package com.founder.fix.fixflow.expand.cmd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.founder.fix.fixflow.core.db.pagination.Pagination;
import com.founder.fix.fixflow.core.impl.Context;
import com.founder.fix.fixflow.core.impl.cmd.AbstractCommand;
import com.founder.fix.fixflow.core.impl.db.SqlCommand;
import com.founder.fix.fixflow.core.impl.interceptor.CommandContext;
import com.founder.fix.fixflow.core.objkey.TaskInstanceObjKey;

public class PerformanceInterfaceTaskCmd extends AbstractCommand<List<Map<String, Object>>> {
	private String[] org;
	private String startTime;
	private String endTime;

	public PerformanceInterfaceTaskCmd(Map<String, Object> parameterMap) {
		super(parameterMap);
		// TODO Auto-generated constructor stub
		this.org = (String[]) parameterMap.get("org");
		this.startTime = (String) parameterMap.get("starttime");
		this.endTime = (String) parameterMap.get("endtime");
	}

	public List<Map<String, Object>> execute(CommandContext commandContext) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");//小写的mm表示的是分钟  
		Date startdate = null;
		Date enddate = null;
		try {
			startdate=sdf.parse(startTime);
			enddate = sdf.parse(endTime);
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(enddate);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			enddate = calendar.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		List<Object> objectParamWhere = new ArrayList<Object>();
		StringBuffer stringBuffer = new StringBuffer();
		Pagination pagination = Context.getProcessEngineConfiguration().getDbConfig().getPagination();
		stringBuffer.append(
				"SELECT orgid as ORGID, ROUND("+pagination.getLocalismSql("datediffconvert", null)+",2) AS AVGTIME FROM " +
				" (select a.*,b.orgid from (SELECT * FROM "+TaskInstanceObjKey.TaskInstanceTableName()+" WHERE  assignee " +
				"IN (SELECT userid FROM au_orgmember WHERE orgid IN(");
		
			for(int j=0;j<org.length;j++) {
				if(j==0) {
					stringBuffer.append("?");
				}else {
					stringBuffer.append(",?");
				}
				objectParamWhere.add(org[j]);
			}
			
			stringBuffer.append(" ))) a,au_orgmember b ");
			stringBuffer.append(" where a.assignee=b.userid) x " +
					" where create_time>=? and create_time<=? " +
					" GROUP BY x.orgid ");
			
			objectParamWhere.add(startdate);
			objectParamWhere.add(enddate);
		
		SqlCommand sqlCommand=new SqlCommand(Context.getDbConnection());
		return sqlCommand.queryForList(stringBuffer.toString(),objectParamWhere);
	}

}
