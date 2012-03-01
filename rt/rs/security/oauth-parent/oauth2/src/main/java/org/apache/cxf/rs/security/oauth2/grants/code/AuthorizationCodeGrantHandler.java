/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.cxf.rs.security.oauth2.grants.code;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.rs.security.oauth2.common.Client;
import org.apache.cxf.rs.security.oauth2.common.ServerAccessToken;
import org.apache.cxf.rs.security.oauth2.grants.AbstractGrantHandler;
import org.apache.cxf.rs.security.oauth2.provider.OAuthServiceException;
import org.apache.cxf.rs.security.oauth2.tokens.bearer.BearerAccessToken;
import org.apache.cxf.rs.security.oauth2.utils.OAuthConstants;
import org.apache.cxf.rs.security.oauth2.utils.OAuthUtils;



public class AuthorizationCodeGrantHandler extends AbstractGrantHandler {
    
    public AuthorizationCodeGrantHandler() {
        super(OAuthConstants.AUTHORIZATION_CODE_GRANT);
    }
    
    public ServerAccessToken createAccessToken(Client client, MultivaluedMap<String, String> params) 
        throws OAuthServiceException {
        String codeValue = params.getFirst(OAuthConstants.AUTHORIZATION_CODE_VALUE);
        ServerAuthorizationCodeGrant grant = 
            ((AuthorizationCodeDataProvider)getDataProvider()).removeCodeGrant(codeValue);
        if (grant == null) {
            return null;
        }
        if (OAuthUtils.isExpired(grant.getIssuedAt(), grant.getLifetime())) {
            throw new OAuthServiceException(OAuthConstants.INVALID_GRANT);
        }
        
        String expectedRedirectUri = grant.getRedirectUri();
        if (expectedRedirectUri != null) {
            String providedRedirectUri = params.getFirst(OAuthConstants.REDIRECT_URI);
            if (providedRedirectUri == null || !providedRedirectUri.equals(expectedRedirectUri)) {
                throw new OAuthServiceException(OAuthConstants.INVALID_REQUEST);
            }
        }
        BearerAccessToken token = new BearerAccessToken(client, 
                                                        generateRandomTokenKey(),
                                                        getTokenLifetime(), 
                                                        System.currentTimeMillis() / 1000);
        token.setScopes(grant.getApprovedScopes());
        token.setSubject(grant.getSubject());
        token.setGrantType(OAuthConstants.AUTHORIZATION_CODE_GRANT);
        return token;
    }
    
}
