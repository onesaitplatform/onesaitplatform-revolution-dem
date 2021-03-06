/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.api.rule.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Priority;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.api.rule.DefaultRuleBase;
import com.minsait.onesait.platform.api.rule.RuleManager;
import com.minsait.onesait.platform.api.service.Constants;
import com.minsait.onesait.platform.api.service.api.ApiManagerService;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.model.ApiQueryParameter;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;

import lombok.Data;

@Component
@Rule
public class APIQueryOntologyRule extends DefaultRuleBase {

	@Autowired
	private ApiManagerService apiManagerService;

	private static final String SQL_LIKE_STR = "SQL";

	@Priority
	public int getPriority() {
		return 4;
	}

	@Condition
	public boolean existsRequest(Facts facts) {
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);
		return ((request != null) && canExecuteRule(facts));
	}

	@Action
	public void setFirstDerivedData(Facts facts) {
		String queryDb = "";
		String targetDb = "";
		final Map<String, Object> data = facts.get(RuleManager.FACTS);
		final HttpServletRequest request = facts.get(RuleManager.REQUEST);

		final Api api = (Api) data.get(Constants.API);
		final User user = (User) data.get(Constants.USER);
		final String pathInfo = (String) data.get(Constants.PATH_INFO);
		final String body = (String) data.get(Constants.BODY);
		String queryType = (String) data.get(Constants.QUERY_TYPE);

		final Ontology ontology = api.getOntology();
		if (ontology != null) {
			data.put(Constants.IS_EXTERNAL_API, false);

			final ApiOperation customSQL = apiManagerService.getCustomSQL(pathInfo, api);

			final String objectId = apiManagerService.getObjectidFromPathQuery(pathInfo);
			if (customSQL == null && !objectId.equals("") && (queryType.equals("") || queryType.equals("NONE"))) {

				queryDb = this.buildQueryByObjectId(ontology, objectId);

				data.put(Constants.QUERY_TYPE, SQL_LIKE_STR);
				queryType = SQL_LIKE_STR;
				data.put(Constants.QUERY, queryDb);
				data.put(Constants.QUERY_BY_ID, Boolean.TRUE);

			} else if (customSQL == null && objectId.equals("") && (queryType.equals("") || queryType.equals("NONE"))) {

				queryDb = "select c,_id from " + ontology.getIdentification() + " as c";

				data.put(Constants.QUERY_TYPE, SQL_LIKE_STR);
				queryType = SQL_LIKE_STR;
				data.put(Constants.QUERY, queryDb);
				data.put(Constants.QUERY_BY_ID, Boolean.TRUE);
			}

			if (customSQL != null) {
				CustomQueryData result = this.buildCustomQuery(customSQL, data, body, request, user);
				queryType = result.getQueryType();
				queryDb = result.getQueryDb();
				targetDb = result.getTargetDb();
			}

			data.put(Constants.QUERY_TYPE, queryType);
			data.put(Constants.QUERY, queryDb);
			data.put(Constants.TARGET_DB_PARAM, targetDb);

			data.put(Constants.OBJECT_ID, objectId);
			data.put(Constants.ONTOLOGY, ontology);

			// Guess type of operation!!!

		} else {
			data.put(Constants.IS_EXTERNAL_API, true);

		}
	}

	private static boolean matchParameter(String name, String match) {
		final String variable = match.replace("$", "");
		return (name.equalsIgnoreCase(match) || name.equalsIgnoreCase(variable));
	}

	private String buildQueryByObjectId(Ontology ontology, String objectId) {
		String queryDb = "";
		final RtdbDatasource dataSource = ontology.getRtdbDatasource();

		if (dataSource.equals(RtdbDatasource.MONGO))
			queryDb = "select *, _id from " + ontology.getIdentification() + " as c where  _id = OID(\"" + objectId
					+ "\")";
		else if (dataSource.equals(RtdbDatasource.ELASTIC_SEARCH))
			queryDb = "select * from " + ontology.getIdentification() + " where _id = IDS_QUERY("
					+ ontology.getIdentification() + "," + objectId + ")";

		return queryDb;
	}

	private CustomQueryData buildCustomQuery(ApiOperation customSQL, Map<String, Object> data, String body,
			HttpServletRequest request, User user) {
		String queryDb = "";
		String queryType = (String) data.get(Constants.QUERY_TYPE);
		String targetDb = "";

		final Set<ApiQueryParameter> queryParametersCustomQuery = new HashSet<>();

		data.put(Constants.API_OPERATION, customSQL);

		for (final ApiQueryParameter queryparameter : customSQL.getApiqueryparameters()) {
			final String name = queryparameter.getName();
			final String value = queryparameter.getValue();

			if (matchParameter(name, Constants.QUERY))
				queryDb = value;
			else if (matchParameter(name, Constants.QUERY_TYPE))
				queryType = value;
			else if (matchParameter(name, Constants.TARGET_DB_PARAM))
				targetDb = value;
			else
				queryParametersCustomQuery.add(queryparameter);

		}

		Map<String, String> queryParametersValues = apiManagerService.getCustomParametersValues(request, body, queryParametersCustomQuery, customSQL);

		if (body == null || body.equals("")) {
			queryDb = apiManagerService.buildQuery(queryDb, queryParametersValues, user);
		} else {
			queryDb = body;
		}

		CustomQueryData result = new CustomQueryData();
		result.setQueryDb(queryDb);
		result.setQueryType(queryType);
		result.setTargetDb(targetDb);

		return result;
	}

	@Data
	class CustomQueryData {
		private String queryDb;
		private String queryType;
		private String targetDb;
	}

}
