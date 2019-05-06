package org.wso2.extension.siddhi.io.feed.sink;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.feed.sink.exceptions.FeedErrorResponseException;
import org.wso2.extension.siddhi.io.feed.utils.BasicAuthProperties;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.extension.siddhi.io.feed.utils.EntryUtils;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.DynamicOptions;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */

@Extension(
        name = "feed",
        namespace = "sink",
        description = "The feed sink allows to publish atom feed entries to atom implemented http servers ",
        parameters = {
                @Parameter(name = Constants.URL,
                        description = "address of the feed end point",
                        type = DataType.STRING),
                @Parameter(name = Constants.ATOM_FUNC,
                        description = "atom function of the request. " +
                                "Acceptance parameters are 'create', 'delete', 'update'",
                        type = DataType.STRING),
                @Parameter(name = Constants.USERNAME,
                        description = "User name of the basic auth",
                        optional = true,
                        defaultValue = Constants.CREDENTIALS,
                        type = DataType.STRING),
                @Parameter(name = Constants.PASSWORD,
                        description = "Password of the basic auth",
                        optional = true,
                        defaultValue = Constants.CREDENTIALS,
                        type = DataType.INT),
                @Parameter(name = Constants.HTTP_RESPONSE_CODE,
                        description = "response code for http",
                        optional = true,
                        defaultValue = Constants.HTTP_CREATED,
                        type = DataType.INT)
        },
        examples = {
                @Example(
                        syntax = " ",
                        description = " support atom only"
                )
        }
)

public class FeedSink extends Sink {
    private static final Logger log = Logger.getLogger(FeedSink.class);
    private OptionHolder optionHolder;
    private URL url;
    private BasicAuthProperties authProperties;
    private Abdera abdera;
    private AbderaClient abderaClient;
    private int httpResponse;
    private String atomFunc;
    private StreamDefinition streamDefinition;

    @Override
    public Class[] getSupportedInputEventClasses() {
            return new Class[]{Map.class};
    }

    @Override
    public String[] getSupportedDynamicOptions() {
            return new String[0];
    }

    @Override
    protected void init(StreamDefinition streamDefinition, OptionHolder optionHolder, ConfigReader configReader,
            SiddhiAppContext siddhiAppContext) {
        this.optionHolder = optionHolder;
        try {
            url = new URL(this.optionHolder.validateAndGetStaticValue(Constants.URL));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException("url error");
        }
        authProperties = validateCredentials();
        this.streamDefinition = streamDefinition;
        abdera = new Abdera();
        abderaClient = new AbderaClient(abdera);
        httpResponse = Integer.parseInt(optionHolder.validateAndGetStaticValue(Constants.HTTP_RESPONSE_CODE,
                Constants.HTTP_CREATED));
        atomFunc = validateAtomFn(optionHolder.validateAndGetStaticValue(Constants.ATOM_FUNC, Constants.FEED_CREATE));
        if (authProperties.isEnable()) {
            abderaClient.registerTrustManager();
            try {
                abderaClient.addCredentials(String.valueOf(url),
                        "realm",
                        "basic",
                        new UsernamePasswordCredentials(authProperties.getUserName(), authProperties.getUserPass()));
            } catch (URISyntaxException e) {
                log.info(" " , e);
            }
        }
    }

    private String validateAtomFn(String atomFunc) {
        atomFunc = atomFunc.toLowerCase(Locale.ENGLISH);
        switch (atomFunc) {
            case Constants.FEED_CREATE : return atomFunc;
            case Constants.FEED_DELETE : return atomFunc;
            case Constants.FEED_UPDATE : return atomFunc;
            default : throw new SiddhiAppValidationException(" Atom finction validation error  in "
                    + streamDefinition.getId() + ". Acceptance parameters are 'create', 'delete', 'update'");
        }
    }

    private BasicAuthProperties validateCredentials() {
        BasicAuthProperties properties = new BasicAuthProperties();
        if (!optionHolder.validateAndGetStaticValue(
                Constants.USERNAME, Constants.CREDENTIALS).equals(Constants.CREDENTIALS) ||
                !optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.CREDENTIALS)
                        .equals(Constants.CREDENTIALS)) {
            properties.setEnable(true);
            properties.setUserName(optionHolder.validateAndGetStaticValue(Constants.USERNAME, Constants.CREDENTIALS));
            properties.setUserPass(optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.CREDENTIALS));
        }
        return properties;
    }


    @Override
    public void publish(Object payload, DynamicOptions dynamicOptions) throws ConnectionUnavailableException {
        HashMap map = (HashMap) payload;
        ClientResponse resp = null;
        if (atomFunc.equals(Constants.FEED_CREATE)) {
            Entry entry = abdera.newEntry();
            entry = EntryUtils.createEntry(map, entry);
            entry.setPublished(new Date());
            resp = abderaClient.post(url.toString() , entry);
        } else if (atomFunc.equals(Constants.FEED_DELETE)) {
            resp = abderaClient.delete((String) map.get("id"));
        } else if (atomFunc.equals(Constants.FEED_UPDATE)) {
            resp = abderaClient.get(url.toString());
            Document<Entry> doc = resp.getDocument();
            Entry entry = doc.getRoot();
            entry =  EntryUtils.createEntry(map, entry);
            resp = abderaClient.put(url.toString(), entry);
        }

        if (resp != null) {
            if (resp.getStatus() != httpResponse) {
                throw new FeedErrorResponseException("Response status conflicts response status code is :" +
                        resp.getStatus() + "-" + resp.getStatusText());
            }
            resp.release();
        }
    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {
        abderaClient.clearCredentials();
    }

    @Override
    public Map<String, Object> currentState() {
            return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}

