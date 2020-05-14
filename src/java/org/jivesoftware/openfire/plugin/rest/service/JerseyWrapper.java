package org.jivesoftware.openfire.plugin.rest.service;

//import com.sun.jersey.api.core.PackagesResourceConfig;
//import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.ifsoft.meet.MeetService;
import org.jivesoftware.openfire.plugin.rest.exceptions.RESTExceptionMapper;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.LoggerFactory;
import org.traderlynk.blast.MessageBlastService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class JerseyWrapper.
 */
public class JerseyWrapper extends ServletContainer {

    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(JerseyWrapper.class);

    /** The Constant CUSTOM_AUTH_PROPERTY_NAME */
    private static final String CUSTOM_AUTH_PROPERTY_NAME = "plugin.ofchat.customAuthFilter";

    /** The Constant REST_AUTH_TYPE */
    private static final String REST_AUTH_TYPE  = "plugin.ofchat.httpAuth";

    /** The Constant AUTHFILTER. */
    private static final String AUTHFILTER = "org.jivesoftware.openfire.plugin.rest.AuthFilter";

    /** The Constant CORSFILTER. */
    private static final String CORSFILTER = "org.jivesoftware.openfire.plugin.rest.CORSFilter";

    /** The Constant CONTAINER_REQUEST_FILTERS. */
//    private static final String CONTAINER_REQUEST_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private static final String CONTAINER_REQUEST_FILTERS = "javax.ws.rs.container.ContainerRequestFilter";

    /** The Constant CONTAINER_RESPONSE_FILTERS. */
//    private static final String CONTAINER_RESPONSE_FILTERS = "com.sun.jersey.spi.container.ContainerResponseFilters";
    private static final String CONTAINER_RESPONSE_FILTERS = "javax.ws.rs.container.ContainerResponseFilter";
    /** The Constant GZIP_FILTER. */
    private static final String GZIP_FILTER = "com.sun.jersey.api.container.filter.GZIPContentEncodingFilter";

    /** The Constant RESOURCE_CONFIG_CLASS_KEY. */
    private static final String RESOURCE_CONFIG_CLASS_KEY = "com.sun.jersey.config.property.resourceConfigClass";

    /** The Constant RESOURCE_CONFIG_CLASS. */
//    private static final String RESOURCE_CONFIG_CLASS = "com.sun.jersey.api.core.PackagesResourceConfig";
    private static final String RESOURCE_CONFIG_CLASS = "org.glassfish.jersey.server.ResourceConfig";

    /** The Constant SCAN_PACKAGE_DEFAULT. */
    private static final String SCAN_PACKAGE_DEFAULT = JerseyWrapper.class.getPackage().getName();

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant SERVLET_URL. */
    private static final String SERVLET_URL = "restapi/*";

    /** The config. */
    private static Map<String, Object> config;

    /** The prc. */
//    private static PackagesResourceConfig prc;
    private static ResourceConfig prc;

    /** The Constant JERSEY_LOGGER. */
    private final static Logger JERSEY_LOGGER = Logger.getLogger("org.glassfish.jersey");

    private static String loadingStatusMessage = null;

    static {
        JERSEY_LOGGER.setLevel(Level.SEVERE);
        config = new HashMap<String, Object>();
        config.put(RESOURCE_CONFIG_CLASS_KEY, RESOURCE_CONFIG_CLASS);
        //prc = new PackagesResourceConfig(SCAN_PACKAGE_DEFAULT);
        prc = new ResourceConfig();
        LOG.error("weizisheng ResourceConfig init...");
        if (prc == null) {
            LOG.error("weizisheng ResourceConfig init error！");
        }
        prc.packages(SCAN_PACKAGE_DEFAULT);
        prc.setProperties(config);
        //prc.setPropertiesAndFeatures(config);
        prc.property(CONTAINER_RESPONSE_FILTERS, CORSFILTER);
        //prc.getProperties().put(CONTAINER_RESPONSE_FILTERS, CORSFILTER);
        //prc.getProperties().put(CONTAINER_RESPONSE_FILTERS, GZIP_FILTER);
        loadAuthenticationFilter();

        prc.register(RestAPIService.class);

        prc.register(MUCRoomService.class);
        prc.register(MUCRoomOwnersService.class);
        prc.register(MUCRoomAdminsService.class);
        prc.register(MUCRoomMembersService.class);
        prc.register(MUCRoomOutcastsService.class);

        prc.register(UserServiceLegacy.class);
        prc.register(UserService.class);
        prc.register(UserRosterService.class);
        prc.register(UserGroupService.class);
        prc.register(UserLockoutService.class);

        prc.register(GroupService.class);
        prc.register(SessionService.class);
        prc.register(MsgArchiveService.class);
        prc.register(StatisticsService.class);
        prc.register(MessageService.class);
        prc.register(SipService.class);
        prc.register(BookmarkService.class);
        prc.register(ChatService.class);
        prc.register(MeetService.class);
        prc.register(AskService.class);
        prc.register(MessageBlastService.class);

        prc.register(RESTExceptionMapper.class);

        LOG.error("weizisheng register class successfully");
//        prc.getClasses().add(RestAPIService.class);

//        prc.getClasses().add(MUCRoomService.class);
//        prc.getClasses().add(MUCRoomOwnersService.class);
//        prc.getClasses().add(MUCRoomAdminsService.class);
//        prc.getClasses().add(MUCRoomMembersService.class);
//        prc.getClasses().add(MUCRoomOutcastsService.class);
//
//        prc.getClasses().add(UserServiceLegacy.class);
//        prc.getClasses().add(UserService.class);
//        prc.getClasses().add(UserRosterService.class);
//        prc.getClasses().add(UserGroupService.class);
//        prc.getClasses().add(UserLockoutService.class);
//
//        prc.getClasses().add(GroupService.class);
//        prc.getClasses().add(SessionService.class);
//        prc.getClasses().add(MsgArchiveService.class);
//        prc.getClasses().add(StatisticsService.class);
//        prc.getClasses().add(MessageService.class);
//        prc.getClasses().add(SipService.class);
//        prc.getClasses().add(BookmarkService.class);
//        prc.getClasses().add(ChatService.class);
//        prc.getClasses().add(MeetService.class);
//        prc.getClasses().add(AskService.class);
//        prc.getClasses().add(MessageBlastService.class);
//
//        prc.getClasses().add(RESTExceptionMapper.class);
    }

    public static String tryLoadingAuthenticationFilter(String customAuthFilterClassName) {

        try {
            if(customAuthFilterClassName != null) {
                Class.forName(customAuthFilterClassName, false, JerseyWrapper.class.getClassLoader());
                loadingStatusMessage = null;
            }
        } catch (ClassNotFoundException e) {
            loadingStatusMessage = "No custom auth filter found for restAPI plugin with name " + customAuthFilterClassName;
        }

        if(customAuthFilterClassName == null || customAuthFilterClassName.isEmpty())
            loadingStatusMessage = "Classname field can't be empty!";
        return loadingStatusMessage;
    }

    public static String loadAuthenticationFilter() {

        // Check if custom AuthFilter is available
        String customAuthFilterClassName = JiveGlobals.getProperty(CUSTOM_AUTH_PROPERTY_NAME);
        String restAuthType = JiveGlobals.getProperty(REST_AUTH_TYPE);
        String pickedAuthFilter = AUTHFILTER;

        try {
            if(customAuthFilterClassName != null && "custom".equals(restAuthType)) {
                Class.forName(customAuthFilterClassName, false, JerseyWrapper.class.getClassLoader());
                pickedAuthFilter = customAuthFilterClassName;
                loadingStatusMessage = null;
            }
        } catch (ClassNotFoundException e) {
            loadingStatusMessage = "No custom auth filter found for restAPI plugin! " + customAuthFilterClassName + " " + restAuthType;
        }

        //prc.getProperties().put(CONTAINER_REQUEST_FILTERS, GZIP_FILTER);
        prc.property(CONTAINER_REQUEST_FILTERS, pickedAuthFilter);
//        prc.getProperties().put(CONTAINER_REQUEST_FILTERS, pickedAuthFilter);
        return loadingStatusMessage;
    }

    /**
     * Instantiates a new jersey wrapper.
     */
    public JerseyWrapper() {
        super(prc);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        loadAuthenticationFilter();
        super.init(servletConfig);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.jersey.spi.container.servlet.ServletContainer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
    }

    /*
     * Returns the loading status message.
     *
     * @return the loading status message.
     */
    public static String getLoadingStatusMessage() {
        return loadingStatusMessage;
    }

}
