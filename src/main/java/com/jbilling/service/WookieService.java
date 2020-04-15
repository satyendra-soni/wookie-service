package com.jbilling.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

@Service
public class WookieService {
    private static final String DATA_BOOST_1 = "Data Boost 1";
    private static final String DATA_BOOST_2 = "Data Boost 2";
    private static final String DATA_BOOST_3 = "Data Boost 3";
    private static final STGroup stGroup = new STGroupFile("sms.stg");
    private static final Logger wookieLogger = LoggerFactory.getLogger("wookieLogger");


    public String usagesPoolTemplate(JsonNode jsonNode) {
        final ST emailTemplate = stGroup.getInstanceOf("usagesPool");
        setTemplateParameters(jsonNode, emailTemplate);
        return emailTemplate.render();
    }

    public String optusMurTemplate(JsonNode jsonNode) {
        final ST emailTemplate = stGroup.getInstanceOf("usagesPool");
        String currentUsagePoolName = jsonNode.get("currentUsagePoolName").asText();
        int percentageConsumption = jsonNode.get("percentageConsumption").asInt();
        Integer currentDataBoostValue = null;
        if (!currentUsagePoolName.equals("null")) {
            for (String s : currentUsagePoolName.split("Boost-")) {
                if (NumberUtils.isDigits(s)) {
                    currentDataBoostValue = Integer.parseInt(s);
                }
            }
            if (percentageConsumption == 100 && currentDataBoostValue != null && currentDataBoostValue == 3) {
                final ST optusMur3Template = stGroup.getInstanceOf("optusMur3");
                setTemplateParameters(jsonNode, optusMur3Template);
                optusMur3Template.add("currentDataBoostValue", currentDataBoostValue);
                return optusMur3Template.render();
            }
        }

        setTemplateParameters(jsonNode, emailTemplate);
        if (percentageConsumption >= 100) {
            String dataBoost = jsonNode.get("dataBoost").asText();
            if (!dataBoost.equals("null")) {
                int dataBoostNumber = 0;
                for (String s : dataBoost.split(" ")) {
                    if (NumberUtils.isDigits(s)) {
                        dataBoostNumber = Integer.parseInt(s);
                    }
                }
                switch (dataBoost) {
                    case DATA_BOOST_1:
                    case DATA_BOOST_2:
                    case DATA_BOOST_3:
                        emailTemplate.add("nextDataBoostValue", DataBoostCardinal.values()[dataBoostNumber - 1]);
                        break;
                    default:
                        wookieLogger.error("No Email Template found for {}", dataBoost);
                        return "No Email Template found for " + dataBoost;
                }
            }
        }
        return emailTemplate.render();
    }

    public String creditPoolTemplate(JsonNode jsonNode) {
        ST emailTemplate;
        if (jsonNode.get("percentageConsumption").asInt() == 100) {
            emailTemplate = stGroup.getInstanceOf("creditPool100");
        } else if (jsonNode.get("percentageConsumption").asInt() > 100) {
            emailTemplate = stGroup.getInstanceOf("creditPool101");
        } else {
            emailTemplate = stGroup.getInstanceOf("creditPool");
            setTemplateParameters(jsonNode, emailTemplate);
        }
        return emailTemplate.render();
    }

    private void setTemplateParameters(JsonNode jsonNode, ST emailTemplate) {
        jsonNode.fieldNames().forEachRemaining(fieldName ->
                emailTemplate.getAttributes().forEach((attr, value) -> {
                    if (fieldName.equalsIgnoreCase(attr)) {
                        emailTemplate.add(attr, jsonNode.get(fieldName).asText());
                    }
                }));
    }
}
