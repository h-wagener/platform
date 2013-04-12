/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.endpoint.user;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.ui.OAuthClientException;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2ServiceClient;
import org.wso2.carbon.identity.oauth.ui.internal.OAuthUIServiceComponentHolder;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.user.api.Claim;

/**
 * Class which will invoke the OAuth2ServiceClient.
 */
public class OAuth2UserClient {

	private static Log log = LogFactory.getLog(OAuth2UserClient.class);

	private String backendServerURL;
	private ConfigurationContext configContext;

	public OAuth2UserClient() {
		OAuthUIServiceComponentHolder serviceComponentHolder =
		                                                       OAuthUIServiceComponentHolder.getInstance();
		backendServerURL =
		                   CarbonUIUtil.getServerURL(serviceComponentHolder.getServerConfigurationService());
		configContext =
		                serviceComponentHolder.getConfigurationContextService()
		                                      .getServerConfigContext();
	}

	/**
	 * 
	 * @param accessToken
	 * @return
	 * @throws OAuthClientException
	 */
	public Claim[] getUserClaims(String accessToken) throws OAuthClientException {
		try {
			OAuth2ServiceClient oauth2ServiceClient =
			                                          new OAuth2ServiceClient(backendServerURL,
			                                                                  configContext);
			return oauth2ServiceClient.getUserClaims(accessToken);
		} catch (Exception e) {
			String errorMsg = "Error when getting user claims.";
			throw new OAuthClientException(errorMsg, e); 
		}
	}
}
