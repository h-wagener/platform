/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.mediator.test.xslt;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.mediator.test.ESBMediatorTest;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XsltTransformationWithPropertyTestCase extends ESBMediatorTest {
    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/xslt/xslt_transformation_with_property_synapse.xml");
    }


    @Test(groups = {"wso2.esb"},
          description = "Do XSLT transformation by Passing a value through the property element to the xslt")
    public void xsltTransformationWithProperty() throws AxisFault {
        OMElement response;

        response = axis2Client.sendCustomQuoteRequest(
                getMainSequenceURL(),
                null,
                "IBM");
        assertNotNull(response, "Response message null");
        assertTrue(response.toString().contains("Code"));
        assertTrue(response.toString().contains("WSO2"));
        assertTrue(response.toString().contains("Price"));
        assertTrue(response.toString().contains("37.50"));

    }

    @AfterClass
    private void destroy() {
        super.cleanup();
    }

}
