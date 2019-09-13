package no.capraconsulting.mixpanel;

import com.mixpanel.mixpanelapi.ClientDelivery;
import com.mixpanel.mixpanelapi.MessageBuilder;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import no.capraconsulting.chat.StudentInfo;
import no.capraconsulting.config.PropertiesHelper;
import no.capraconsulting.enums.MixpanelEvent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class MixpanelService {
    private static Logger LOG = LoggerFactory.getLogger(MixpanelService.class);
    private final ClientDelivery delivery = new ClientDelivery();
    private final MixpanelAPI mixpanel = new MixpanelAPI();

    private MessageBuilder messageBuilder;

    public MixpanelService() {
        String mixpanelToken = PropertiesHelper.getStringProperty(PropertiesHelper.getProperties(), PropertiesHelper.MIXPANEL_PROJECT_TOKEN, null);
        messageBuilder = new MessageBuilder(mixpanelToken);
    }

    public void trackEvent(MixpanelEvent eventType, JSONObject data) {
        if (eventType.getPropsFromJSONObject == null) {
            throw new IllegalArgumentException("Missing getPropsFromJSONObject from eventType " + eventType.name());
        }

        sendEvent(UUID.randomUUID().toString(), eventType.getPropsFromJSONObject.apply(data), eventType);
    }

    public void trackEventWithStudentInformation(MixpanelEvent eventType, StudentInfo studentInfo) {
        trackEventWithDuration(eventType, studentInfo, System.currentTimeMillis() - studentInfo.getTimePlacedInQueue());
    }

    public void trackEventWithDuration(MixpanelEvent eventType, StudentInfo studentInfo, long duration) {
        if (eventType.getPropsFromStudentInfo == null) {
            throw new IllegalArgumentException("Missing getPropsFromStudentInfo from eventType " + eventType.name());
        }

        sendEvent(studentInfo.getUniqueID(), eventType.getPropsFromStudentInfo.apply(studentInfo, duration), eventType);
    }

    private void sendEvent(String uniqueId, JSONObject props, MixpanelEvent eventType) {
        JSONObject event = messageBuilder.event(uniqueId, eventType.toString(), props);

        delivery.addMessage(event);

        try {
            mixpanel.deliver(delivery);
            LOG.info("Event was sent to Mixpanel's servers");
        } catch (IOException e) {
            LOG.error("Unable to send event to Mixpanel's servers");
            LOG.error(e.getMessage());
        }
    }
}
