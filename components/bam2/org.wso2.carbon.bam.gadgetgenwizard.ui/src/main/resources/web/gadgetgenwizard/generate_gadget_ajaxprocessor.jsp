<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GadgetGenAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.stub.types.WSMap" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    GadgetGenAdminClient gadgetGenAdminClient = new GadgetGenAdminClient(cookie, serverURL, configContext);

    List<String> attrKeys = new ArrayList<String>();

    String[] genericKeys = new String[] {"jdbcurl", "username", "password", "driver", "sql"};
    attrKeys.addAll(Arrays.asList(genericKeys));

    System.out.println("UI element session attribute : " + ((String[]) session.getAttribute("uielement"))[0]);
    System.out.println("Is equal to bar : " + ((String[]) session.getAttribute("uielement"))[0].equals("bar"));

    if ((session.getAttribute("uielement") != null) && (((String[]) session.getAttribute("uielement"))[0].equals("bar")))                     {
        String[] barChartKeys = new String[] {"bar-xlabel", "bar-xcolumn", "bar-ylabel", "bar-ycolumn", "bar-title"};
        attrKeys.addAll(Arrays.asList(barChartKeys));
    }
    WSMap wsMap = gadgetGenAdminClient.constructWSMap(session, attrKeys);


    gadgetGenAdminClient.generateGraph(wsMap);
%>