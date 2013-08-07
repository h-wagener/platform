/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.repository.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.cache.AppVersionCache;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.utilities.project.ProjectUtils;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.Date;

/**
 * This is an admin service for repository related operations.
 * Note:This service depends on external information(RXT) for getting repository type of
 * application
 */
public class RepositoryManagementService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(RepositoryManagementService.class);
    private RepositoryManager repositoryManager;

    public RepositoryManagementService() {
        this.repositoryManager = new RepositoryManager();
    }

    /**
     * Create a repository for an application with type{svn,git}
     *
     * @param applicationKey
     * @param type
     * @return
     * @throws RepositoryMgtException
     */
    /*public String createRepository(String applicationKey, String type)
            throws RepositoryMgtException {
        long s = new Date().getTime();
        String ss = repositoryManager.createRepository(applicationKey, type);
        log.info("Repo Time : " + ((new Date().getTime()) - s));
        return ss;
    }*/

    /**
     * @param applicationKey
     * @param type
     * @param username
     * @throws RepositoryMgtException
     */
    public void provisionUser(String applicationKey, String type, String username)
            throws RepositoryMgtException {
        repositoryManager.provisionUser(applicationKey, type, username);
    }

    /**
     * Gives the repository url of application
     *
     * @param applicationKey
     * @return
     * @throws RepositoryMgtException
     */
    public String getURL(String applicationKey) throws RepositoryMgtException {
        return repositoryManager.getAppRepositoryURL(applicationKey,
                                                     getRepositoryType(applicationKey));
    }

    /**
     * Gives the URL of repository of a version of application
     *
     * @param applicationKey
     * @param version
     * @return
     * @throws RepositoryMgtException
     */
    public String getURLForAppVersion(String applicationKey, String version)
            throws RepositoryMgtException {
        return repositoryManager.getURLForAppversion(applicationKey, version,
                                                     getRepositoryType(applicationKey));
    }

    /**
     * Branches the repository of an application
     *
     * @param appId
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @throws RepositoryMgtException
     */
    public void branch(String appId, String currentVersion, String targetVersion,
                       String currentRevision) throws RepositoryMgtException {
        AppVersionCache cache = AppVersionCache.getAppVersionCache();
        cache.clearCacheForAppId(appId);
        repositoryManager.branch(appId, getRepositoryType(appId), currentVersion, targetVersion,
                                 currentRevision);

    }

    /**
     * Tags the repository of an application
     *
     * @param appId
     * @param currentVersion
     * @param targetVersion
     * @param currentRevision
     * @throws AppFactoryException
     * @throws RepositoryMgtException
     */
    public void tag(String appId, String currentVersion, String targetVersion,
                    String currentRevision) throws AppFactoryException, RepositoryMgtException {
        repositoryManager.tag(appId, getRepositoryType(appId), currentVersion, targetVersion,
                              currentRevision);
    }

    private String getRepositoryType(String applicationId) throws RepositoryMgtException {
        String type;
        try {
            type = ProjectUtils.getRepositoryType(applicationId);
        } catch (AppFactoryException e) {
            String msg = "Error while getting repository type of application " + applicationId;
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
        return type;
    }
   
}
