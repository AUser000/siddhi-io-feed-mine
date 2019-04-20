package org.wso2.extension.siddhi.io.feed.source;

import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.feed.utils.BasicAuthProperties;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */

/**
 * Annotation of Siddhi Extension.
 * <pre><code>
 * eg:-
 * {@literal @}Extension(
 * name = "The name of the extension",
 * namespace = "The namespace of the extension",
 * description = "The description of the extension (optional).",
 * //Source configurations
 * parameters = {
 * {@literal @}Parameter(name = "The name of the first parameter",
 *                               description= "The description of the first parameter",
 *                               type =  "Supported parameter types.
 *                                        eg:{DataType.STRING, DataType.INT, DataType.LONG etc}",
 *                               dynamic= "false
 *                                         (if parameter doesn't depend on each event then dynamic parameter is false.
 *                                         In Source, only use static parameter)",
 *                               optional= "true/false, defaultValue= if it is optional then assign a default value
 *                                          according to the type."),
 * {@literal @}Parameter(name = "The name of the second parameter",
 *                               description= "The description of the second parameter",
 *                               type =   "Supported parameter types.
 *                                         eg:{DataType.STRING, DataType.INT, DataType.LONG etc}",
 *                               dynamic= "false
 *                                         (if parameter doesn't depend on each event then dynamic parameter is false.
 *                                         In Source, only use static parameter)",
 *                               optional= "true/false, defaultValue= if it is optional then assign a default value
 *                                         according to the type."),
 * },
 * //If Source system configurations will need then
 * systemParameters = {
 * {@literal @}SystemParameter(name = "The name of the first  system parameter",
 *                                      description="The description of the first system parameter." ,
 *                                      defaultValue = "the default value of the system parameter.",
 *                                      possibleParameter="the possible value of the system parameter.",
 *                               ),
 * },
 * examples = {
 * {@literal @}Example(syntax = "sample query with Source annotation that explain how extension use in Siddhi."
 *                              description =" The description of the given example's query."
 *                      ),
 * }
 * )
 * </code></pre>
 */

@Extension(
        name = "feed",
        namespace = "source",
        description = " ",
        parameters = {
                @Parameter(name = Constants.URL,
                        description = "address of the feed end point",
                        type = DataType.STRING),
                @Parameter(name = Constants.FEED_TYPE,
                        description = " Rss or Atom",
                        type = DataType.STRING),
                @Parameter(name = Constants.REQUEST_INTERVAL,
                        description = "request interval in minutes",
                        type = DataType.INT),
                @Parameter(name = Constants.USERNAME,
                        description = "User name of the basic auth",
                        optional = true,
                        defaultValue = Constants.CREDENTIALS,
                        type = DataType.INT),
                @Parameter(name = Constants.PASSWORD,
                        description = "Password of the basic auth",
                        optional = true,
                        defaultValue = Constants.CREDENTIALS,
                        type = DataType.INT),
        },
        examples = {
                @Example(
                        syntax = " ",
                        description = " "
                )
        }
)

// for more information refer https://wso2.github.io/siddhi/documentation/siddhi-4.0/#sources
public class FeedSource extends Source {
    Logger logger = Logger.getLogger(FeedSource.class);
    OptionHolder optionHolder;
    private URL url;
    private String type;
    private int requestInterval;
    private FeedListener listener;
    private SiddhiAppContext siddhiAppContext;
    private ScheduledFuture future;
    private SourceEventListener sourceEventListener;
    private ScheduledExecutorService scheduledExecutorService;
    private BasicAuthProperties basicAuthProperties;
    /**
     * The initialization method for {@link Source}, will be called before other methods. It used to validate
     * all configurations and to get initial values.
     * @param sourceEventListener After receiving events, the source should trigger onEvent() of this listener.
     *                            Listener will then pass on the events to the appropriate mappers for processing .
     * @param optionHolder        Option holder containing static configuration related to the {@link Source}
     * @param configReader        ConfigReader is used to read the {@link Source} related system configuration.
     * @param siddhiAppContext    the context of the {@link org.wso2.siddhi.query.api.SiddhiApp} used to get Siddhi
     *                            related utility functions.
     */
    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {

        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        try {
            url = new URL(this.optionHolder.validateAndGetStaticValue(Constants.URL));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException("url error");
        }
        basicAuthProperties = validateCredentials();
        type = validateType();
        requestInterval = validateRequestInterval();
        scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
        logger.info("init triggerd"); //ToDo
    }

    private String validateType() {
        String type = optionHolder.validateAndGetStaticValue(Constants.FEED_TYPE);
        type = type.toLowerCase(Locale.ENGLISH);
        if(type.equals(Constants.RSS)) {
            return type;
        } else if (type.equals(Constants.ATOM)) {
            return type;
        } else {
            throw new SiddhiAppValidationException("type error");
        }
    }

    private int validateRequestInterval() {
        int requestInterval = Integer.parseInt(optionHolder.validateAndGetStaticValue(Constants.REQUEST_INTERVAL));
        return requestInterval;
    }

    private BasicAuthProperties validateCredentials() {
        BasicAuthProperties properties = new BasicAuthProperties();
        if(!optionHolder.validateAndGetStaticValue(Constants.USERNAME, Constants.CREDENTIALS).equals(Constants.CREDENTIALS) ||
                !optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.CREDENTIALS).equals(Constants.CREDENTIALS)) {
            properties.setEnable(true);
            properties.setUserName(optionHolder.validateAndGetStaticValue(Constants.USERNAME, Constants.CREDENTIALS));
            properties.setUserPass(optionHolder.validateAndGetStaticValue(Constants.PASSWORD, Constants.CREDENTIALS));
        }
        return properties;
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[] {Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        logger.info(" connect method trig."); // TODO
        try {
            listener = new FeedListener(sourceEventListener, url, type, basicAuthProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        future = scheduledExecutorService.scheduleAtFixedRate(listener, 0, requestInterval, TimeUnit.MINUTES);
    }

    /**
     * This method can be called when it is needed to disconnect from the end point.
     */
    @Override
    public void disconnect() {

    }

    /**
     * Called at the end to clean all the resources consumed by the {@link Source}
     */
    @Override
    public void destroy() {
        future.cancel(true);
        scheduledExecutorService.shutdown();
    }

    /**
     * Called to pause event consumption
     */
    @Override
    public void pause() {
        listener.pause();
    }

    /**
     * Called to resume event consumption
     */
    @Override
    public void resume() {
        listener.resume();
    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as a map
     */
    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param map the stateful objects of the processing element as a map.
     * This map will have the  same keys that is created upon calling currentState() method.
     */
     @Override
     public void restoreState(Map<String, Object> map) {

     }
}

