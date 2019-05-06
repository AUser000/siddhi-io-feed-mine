package org.wso2.extension.siddhi.io.feed.source;

import org.apache.log4j.Logger;
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

@Extension(
        name = "feed",
        namespace = "source",
        description = " can consume atom and rss feed entries ",
        parameters = {
                @Parameter(name = Constants.URL,
                        description = "address of the feed end point",
                        type = DataType.STRING),
                @Parameter(name = Constants.FEED_TYPE,
                        description = " Rss or Atom",
                        type = DataType.STRING),
                @Parameter(name = Constants.REQUEST_INTERVAL,
                        description = "request interval in minutes",
                        type = DataType.INT)
        },
        examples = {
                @Example(
                        syntax = " ",
                        description = "  "
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

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {

        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        try {
            url = new URL(this.optionHolder.validateAndGetStaticValue(Constants.URL));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(" Url Error in siddhi stream " +
                    siddhiAppContext.getSiddhiAppString());
        }
        this.type = validateType();
        this.requestInterval = validateRequestInterval();
        this.scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
        this.siddhiAppContext = siddhiAppContext;
    }

    private String validateType() {
        String type = optionHolder.validateAndGetStaticValue(Constants.FEED_TYPE);
        type = type.toLowerCase(Locale.ENGLISH);
        if (type.equals(Constants.RSS)) {
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


    @Override
    public Class[] getOutputEventClasses() {
        return new Class[] {Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        try {
            listener = new FeedListener(sourceEventListener, url, type, siddhiAppContext);
        } catch (IOException e) {
                logger.info(e);
        }
        future = scheduledExecutorService.scheduleAtFixedRate(listener, 0, requestInterval, TimeUnit.MINUTES);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {
        future.cancel(true);
        scheduledExecutorService.shutdown();
    }

    @Override
    public void pause() {
        listener.pause();
    }

    @Override
    public void resume() {
        listener.resume();
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}

