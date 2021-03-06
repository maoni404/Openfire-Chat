package org.jivesoftware.openfire.plugin.rest.service;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.broadbear.link.preview.SourceContent;
import org.broadbear.link.preview.TextCrawler;
import org.dom4j.Element;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.plugin.rest.RESTServicePlugin;
import org.jivesoftware.openfire.plugin.rest.entity.AskQueue;
import org.jivesoftware.openfire.plugin.rest.entity.AssistEntity;
import org.jivesoftware.openfire.plugin.rest.entity.WorkgroupEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.smack.OpenfireConnection;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.StringUtils;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@Path("restapi/v1/ask")
public class AskService {

    private static final Logger Log = LoggerFactory.getLogger(AskService.class);
    private static final String TEXT_PLAIN = "text/plain";
    private static final String APPLICATION_JSON = "application/json";
    private static final Header HEADER_ACCEPT_APPLICATION_JSON = new Header("Accept", APPLICATION_JSON);
    private static final Header HEADER_CONTENT_TYPE_APPLICATION_JSON = new Header("Content-Type", APPLICATION_JSON);
    private static final Header HEADER_ACCEPT_CHARSET_UTF8 = new Header("Accept-Charset", "UTF-8");
    private static final MessageRouter MESSAGE_ROUTER = XMPPServer.getInstance().getMessageRouter();
    private static final String DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
    private static final String SERVER = XMPPServer.getInstance().getServerInfo().getHostname();
    private static final Cache<String, String> cachedIrmaRequests;

    static {
       cachedIrmaRequests = CacheFactory.createLocalCache("IRMA Verifications");
    }
    private HttpClient client;

    @PostConstruct
    public void init()
    {
        MultiThreadedHttpConnectionManager threadedConnectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(threadedConnectionManager);
        //HttpClientParams hcParams = client.getParams();
        //hcParams.setAuthenticationPreemptive(true);
    }

    /*
        {
          "emailAddress": "patient@hospital.com",
          "userID": "patient",
          "question": "What is the meaning of life",
          "workgroup": "diabetes"
        }
    */

    //-------------------------------------------------------
    //
    //  ask
    //
    //-------------------------------------------------------

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AssistEntity requestAssistance(AssistEntity assistance) throws ServiceException
    {
        try {
            String response = OpenfireConnection.requestAssistance(assistance);

            if (response != null)
            {
                throw new ServiceException(response, response, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        return assistance;
    }

    @POST
    @Path("/admin")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createWorgroup(WorkgroupEntity workgroup) throws ServiceException
    {
        try {
            String response = RESTServicePlugin.getInstance().createWorkgroup(workgroup.getName(), workgroup.getDescription(), workgroup.getMembers().split(","));

            if (response != null)
            {
                throw new ServiceException(response, response, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        return Response.status(Response.Status.OK).build();
    }
    @POST
    @Path("/admin/{workgroup}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateWorgroup(@PathParam("workgroup") String workgroup, String members) throws ServiceException
    {
        try {
            String[] memberList = new String[]{};
            if (members.contains(",")) memberList = members.split(",");
            if (members.contains(" ")) memberList = members.split(" ");

            String response = RESTServicePlugin.getInstance().updateWorkgroup(workgroup, memberList);

            if (response != null)
            {
                throw new ServiceException(response, response, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/admin/{workgroup}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteWorgroup(@PathParam("workgroup") String workgroup) throws ServiceException
    {
        try {
            String response = RESTServicePlugin.getInstance().deleteWorkgroup(workgroup);

            if (response != null)
            {
                throw new ServiceException(response, response, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/{userId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AskQueue queryAssistance(@PathParam("userId") String userId) throws ServiceException
    {
        try {
            AskQueue queue = OpenfireConnection.queryAssistance(userId);

            if (queue == null)
            {
                throw new ServiceException("Error", "Ask workgroup is inaccesible", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }
            return queue;

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("/{userId}")
    public Response joinAssistChat(@PathParam("userId") String userId) throws ServiceException
    {
        try {
            boolean resp = OpenfireConnection.joinAssistChat(userId);

            if (resp == false)
            {
                throw new ServiceException("Error", "Ask workgroup is inaccesible", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{userId}")
    public Response sendAssistMessage(@PathParam("userId") String userId, String body) throws ServiceException
    {
        try {
            boolean resp = OpenfireConnection.sendAssistMessage(userId, body);

            if (resp == false)
            {
                throw new ServiceException("Error", "Ask workgroup is inaccesible", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{userId}")
    public Response revokeAssistance(@PathParam("userId") String userId) throws ServiceException
    {
        try {
            String response = OpenfireConnection.revokeAssistance(userId);

            if (response != null)
            {
                throw new ServiceException(response, response, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }

        return Response.status(Response.Status.OK).build();
    }

    //-------------------------------------------------------
    //
    //  upload
    //
    //-------------------------------------------------------

    @GET
    @Path("/upload/{userId}/{fileName}/{fileSize}")
    public String uploadRequest(@PathParam("userId") String userId, @PathParam("fileName") String fileName, @PathParam("fileSize") String fileSize) throws ServiceException
    {
        try {
            JSONObject response = OpenfireConnection.getUploadRequest(userId, fileName, Long.parseLong(fileSize));

            if (response.has("error"))
            {
                throw new ServiceException("Exception", response.getString("error"), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

            return response.toString();

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    //-------------------------------------------------------
    //
    //  preview link (url)
    //
    //-------------------------------------------------------

    @GET
    @Path("/previewlink/{quality}/{url}")
    public String previewLink(@PathParam("quality") String quality, @PathParam("url") String url) throws ServiceException
    {
        Log.debug("previewLink " + url + " " + quality);

        try {
            JSONObject jsonObject = new JSONObject();
            SourceContent sourceContent = TextCrawler.scrape(new String(DatatypeConverter.parseBase64Binary(url)), Integer.parseInt(quality));

            if (sourceContent == null)
            {
                throw new ServiceException("Exception", "bad url", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

            if (sourceContent.getImages() != null)      jsonObject.put("image", sourceContent.getImages().get(0));
            if (sourceContent.getDescription() != null) jsonObject.put("descriptionShort", sourceContent.getDescription());
            if (sourceContent.getTitle()!=null)         jsonObject.put("title", sourceContent.getTitle());

            return jsonObject.toString();

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    //-------------------------------------------------------
    //
    //  uport
    //
    //-------------------------------------------------------

    @GET
    @Path("/uport/{appId}/{clientId}")
    public String getSigner(@PathParam("appId") String appId, @PathParam("clientId") String clientId) throws ServiceException
    {
        Log.debug("getSigner uport.clientid." + appId + "." + clientId);

        try {
            String signer = JiveGlobals.getProperty("uport.clientid." + appId + "." + clientId);

            Log.debug("gotSigner " + signer);

            if (signer == null || signer.equals(""))
            {
                throw new ServiceException("Exception", "client Id not found", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

            return signer;

        } catch (Exception e) {
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @Path("/uport/register")
    public String uportRegister(String json) throws ServiceException
    {
        Log.debug("uportRegister\n" + json);

        JSONObject response = new JSONObject();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserManager userManager = UserManager.getInstance();

        try {
            JSONObject uportCreds = new JSONObject(json);

            if (uportCreds.has("address") && uportCreds.has("name") && uportCreds.has("publicKey"))
            {
                String email = uportCreds.has("email") ? uportCreds.getString("email") : null;
                String password = StringUtils.randomString(32);
                String finalUsername = null;
                User user = null;

                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement("SELECT username FROM ofUserProp WHERE propValue=?");
                pstmt.setString(1, uportCreds.getString("address"));

                rs = pstmt.executeQuery();

                if (!rs.next())
                {
                    String[] parts = uportCreds.getString("name").split(" ");
                    String username = parts[0].toLowerCase().trim();

                    if (parts.length > 1)
                    {
                        username = username + "." + parts[1].toLowerCase().trim();
                    }
                    boolean ok = false;
                    finalUsername = username;
                    int count = 0;

                    while (!ok)
                    {
                        try {
                            userManager.getUser(finalUsername);
                            finalUsername = username + "-" + String.valueOf(++count);

                        } catch (UserNotFoundException e) {
                            ok = true;
                        }
                    }

                    user = userManager.createUser(finalUsername, password, uportCreds.getString("name"),  email);

                    user.getProperties().put("etherlynk.address", uportCreds.getString("address"));
                    user.getProperties().put("etherlynk.account", uportCreds.getString("account"));
                    user.getProperties().put("etherlynk.public.key", uportCreds.getString("publicKey"));

                } else {
                    finalUsername = rs.getString(1);
                    user = userManager.getUser(finalUsername);

                    if (uportCreds.has("password"))
                    {
                        AuthFactory.authenticate(finalUsername, uportCreds.getString("password"));

                        user.setPassword(password);
                        user.setName(uportCreds.getString("name"));
                        if (email != null) user.setEmail(email);
                    }
                }

                if (uportCreds.has("avatar")) user.getProperties().put("etherlynk.avatar", uportCreds.getJSONObject("avatar").getString("uri"));
                if (uportCreds.has("country")) user.getProperties().put("etherlynk.country", uportCreds.getString("country"));
                if (uportCreds.has("phone")) user.getProperties().put("etherlynk.phone", uportCreds.getString("phone"));

                response.put("username", finalUsername);
                response.put("password", password);

            } else {
                Log.error("bad request\n" + uportCreds);
                throw new ServiceException("Exception", "bad request", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            }

        } catch (Exception e) {
            Log.error("uportRegister execption", e);
            throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

        return response.toString();
    }

    //-------------------------------------------------------
    //
    //  IRMA
    //
    //-------------------------------------------------------

    @POST
    @Path("/irma/reveal/{jid}")
    public String irmaReveal(@PathParam("jid") String jid, String irmaRequest) throws ServiceException
    {
        Log.debug("irmaReveal " + jid + " " + irmaRequest + "\n" + cachedIrmaRequests.values() + "\n" + cachedIrmaRequests.keySet());

        String response = "\"ERROR\"";

        if (cachedIrmaRequests.containsKey(jid))
        {
           Log.debug("irmaReveal cached " + jid);
           response = cachedIrmaRequests.get(jid);
           int tries = 0;

           while (response.contains("PENDING") && tries < 20)
           {
                Log.debug("irmaReveal pending " + tries);
                try {
                    Thread.sleep(3000);

                } catch (Exception e) {
                    Log.error("irmaReveal thread sleep execption", e);
                }

                response = cachedIrmaRequests.get(jid);
                tries++;
           }

           if (tries >= 10 || response.contains("ERROR")) cachedIrmaRequests.remove(jid);
        }
        else {
            cachedIrmaRequests.put(jid, "\"PENDING\"");

            String revealUrl = JiveGlobals.getProperty("irma.external.url", "http://" + SERVER + ":" + JiveGlobals.getProperty("httpbind.port.plain", "7070")) +  "/session";
            PostMethod post = null;
            GetMethod get = null;

            Log.debug("irmaReveal get session " + revealUrl);

            try {
                post = new PostMethod(revealUrl);
                post.addRequestHeader(HEADER_ACCEPT_APPLICATION_JSON);
                post.addRequestHeader(HEADER_CONTENT_TYPE_APPLICATION_JSON);
                post.addRequestHeader(HEADER_ACCEPT_CHARSET_UTF8);

                if (irmaRequest != null)
                {
                    post.setRequestEntity(new StringRequestEntity(irmaRequest, TEXT_PLAIN, "UTF-8"));
                    int httpCode = client.executeMethod(post);

                    if (httpCode < 300)
                    {
                        String payload = getStringFromResponse(post);
                        JSONObject json = new JSONObject(payload);

                        if (json.has("sessionPtr") && json.has("token"))
                        {
                            routeMessage(jid, payload, "reveal");

                            String sessionId = json.getString("token");
                            String checkUrl = revealUrl + "/" + sessionId + "/status";

                            Log.debug("irmaReveal session id " + sessionId);

                            while (httpCode < 300 && !response.contains("DONE") && !response.contains("TIMEOUT") && !response.contains("CANCELLED"))
                            {
                                Thread.sleep(3000);

                                get = new GetMethod(checkUrl + "?" + System.currentTimeMillis());
                                get.addRequestHeader(HEADER_ACCEPT_APPLICATION_JSON);
                                get.addRequestHeader(HEADER_CONTENT_TYPE_APPLICATION_JSON);
                                get.addRequestHeader(HEADER_ACCEPT_CHARSET_UTF8);

                                httpCode = client.executeMethod(get);

                                response = "\"ERROR\"";
                                if (httpCode < 300) response = getStringFromResponse(get);

                                Log.debug("irmaReveal status " + response + " " + httpCode);
                                routeMessage(jid, response, "status");
                            }

                            if (response.contains("DONE"))
                            {
                                Log.debug("irmaReveal done " + sessionId);

                                String resultUrl = revealUrl + "/" + sessionId + "/result";
                                get = new GetMethod(resultUrl);
                                httpCode = client.executeMethod(get);

                                if (httpCode < 300)
                                {
                                    response = getStringFromResponse(get);
                                    cachedIrmaRequests.put(jid, response);

                                    Log.debug("irmaReveal payload \n" + response);
                                }
                                routeMessage(jid, response, "done");
                            }
                            else {
                                Log.debug("irmaReveal error " + response + " " + httpCode);
                                routeMessage(jid, response, "error");
                            }
                        }
                        else {
                            Log.debug("irmaReveal error " + response + " " + httpCode);
                            routeMessage(jid, response, "error");
                        }
                    }
                    else {
                        Log.error("irmaReveal http error response " + httpCode);
                        throw new ServiceException("Exception", "http error " + httpCode, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                    }
                } else {
                    Log.error("irmaReveal missing payload");
                    throw new ServiceException("Exception", "missing payload", ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
                }

            } catch (Exception e) {
                Log.error("irmaReveal execption", e);
                throw new ServiceException("Exception", e.getMessage(), ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, Response.Status.BAD_REQUEST);
            } finally {
                if (post != null) post.releaseConnection();
                if (get != null) get.releaseConnection();
            }
        }
        return response;
    }

    private String getStringFromResponse(HttpMethod method) throws IOException {
        StringBuilder responseContent;
        BufferedReader reader;
        responseContent = new StringBuilder();
        reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String line;

            while ((line = reader.readLine()) != null)
                responseContent.append(line).append(System.getProperty("line.separator"));
        } catch (Exception e) {
            Log.warn("Error while extracting response body from response");

            if (reader != null)
                try {
                    reader.close();
                } catch (IOException _ex) {
                }
            return null;
        }

        if (reader != null)
            try {
                reader.close();
            } catch (IOException _ex) {
            }

        return responseContent.toString();
    }

    private JSONObject sanitizeJson(String response) throws JSONException {
        return new JSONObject(response.replaceAll("^[^{]", ""));
    }

    private void routeMessage(String jid, String payload, String action)
    {
        Log.debug("routeMessage " + jid + " " + action + "\n" + payload);

        Message message = new Message();
        message.setFrom(DOMAIN);
        message.setTo(jid);

        Element irma = message.addChildElement("irma", "http://igniterealtime.org/xmlns/xmpp/irma");
        irma.addAttribute("action", action);
        irma.setText(payload);

        MESSAGE_ROUTER.route(message);
    }
}
