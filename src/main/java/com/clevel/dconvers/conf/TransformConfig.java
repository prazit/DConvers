package com.clevel.dconvers.conf;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.LibraryMode;
import com.clevel.dconvers.transform.TransformTypes;
import com.clevel.dconvers.ngin.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TransformConfig extends Config {

    private String transform;

    // ArgumentValue = String
    // ArgumentName = String
    // ArgumentList = HashMap<ArgumentName,ArgumentValue>>
    // TransformList = List<Pair<TransformTypes,ArgumentList>>
    private List<Pair<TransformTypes, HashMap<String, String>>> transformList;

    public TransformConfig(DConvers dconvers, String baseProperty, Configuration baseProperties) {
        super(dconvers, baseProperty);
        this.properties = baseProperties;

        loadDefaults();
        if (LibraryMode.MANUAL != dconvers.switches.getLibraryMode()) {
            valid = loadProperties();
            if (valid) valid = validate();
        }

        log.trace("TransformConfig({}) is created", name);
    }

    @Override
    public void loadDefaults() {
        transformList = new ArrayList<>();
        transform = "";
    }

    @Override
    protected boolean loadProperties() {

        Configuration transProperties;
        Iterator<String> transKeyList;
        TransformTypes transformType;
        String transformTypeName;
        String argumentValue;
        String argumentName;

        String baseProperty = name;
        String transformProperty = baseProperty + "." + Property.TRANSFORM.key();

        List<Object> transformArray;
        try {
            transformArray = properties.getList(transformProperty);
        } catch (ConversionException ex) {
            transformArray = new ArrayList<>();
        }

        if (transformArray.size() == 0) {
            return true;
        }

        StringBuilder transformStringBuilder = new StringBuilder();
        HashMap<String, String> argumentList;
        String transformString;
        for (Object transformObject : transformArray) {
            transformString = transformObject.toString();
            if (transformString.length() == 0) continue;
            transformStringBuilder.append(transformString).append(",");

            addTransforms(transformString);

            for (Pair<TransformTypes, HashMap<String, String>> pair : transformList) {
                transProperties = properties.subset(transformProperty + "." + pair.getKey().name().toLowerCase());
                transKeyList = transProperties.getKeys();
                argumentList = pair.getValue();
                for (Iterator<String> it = transKeyList; it.hasNext(); ) {
                    argumentName = it.next();
                    argumentValue = getPropertyString(transProperties, argumentName);
                    argumentList.put(argumentName, argumentValue);
                }
            }
        }
        transform = transformStringBuilder.substring(0, transform.length() - 2);
        log.debug("transformList = {}", transformList);

        return true;
    }

    /**
     * @param transformString <b>example</b>:<ul>
     *                        <li>transformString=TRANSOP1(arguments),TRANSOP2(arguments),TRANSOP3(arguments)</li>
     *                        </ul>
     */
    public void addTransforms(String transformString) {
        HashMap<String, String> argumentList;
        TransformTypes transformType;
        String transformTypeName;
        String argumentValue;
        String argumentName;

        // transformValues=[TRANSOP1, arguments, TRANSOP2, arguments, TRANSOP3, arguments]
        String[] transformValues = transform.split("\\),|[()]");
        log.debug("transformValues = {}", Arrays.asList(transformValues));
        for (int index = 0; index < transformValues.length; index += 2) {
            transformTypeName = transformValues[index];
            transformType = TransformTypes.parse(transformTypeName);

            argumentList = new HashMap<>();
            argumentName = "arguments";
            argumentValue = transformValues[index + 1];
            argumentList.put(argumentName, argumentValue);

            transformList.add(new Pair<>(transformType, argumentList));
        }
    }

    @Override
    public boolean validate() {
        // TODO: Might be need to validate transform configuration
        return true;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(TransformConfig.class);
    }

    public String getTransform() {
        return transform;
    }

    public List<Pair<TransformTypes, HashMap<String, String>>> getTransformList() {
        return transformList;
    }

    public boolean needTransform() {
        return transformList.size() > 0;
    }

    public void setTransformList(List<Pair<TransformTypes, HashMap<String, String>>> transformList) {
        this.transformList = transformList;
    }

    @Override
    public void saveProperties() throws ConfigurationException {
        String transformProperty = name + "." + Property.TRANSFORM.key();
        String arguments, transform, value;
        for (Pair<TransformTypes, HashMap<String, String>> transformation : transformList) {
            TransformTypes transformTypes = transformation.getKey();
            HashMap<String, String> argumentMap = transformation.getValue();
            for (String argumentName : argumentMap.keySet()) {
                if (argumentName.compareTo("arguments") == 0) {
                    arguments = argumentMap.get(argumentName);
                    transform = transformTypes.name() + "(" + arguments + ")";
                    addPropertyString(properties, transformProperty, "", transform);
                } else {
                    value = argumentMap.get(argumentName);
                    addPropertyString(properties, Property.connectKeyString(transformProperty, transformTypes.name(), argumentName), "", value);
                }
            }
        }// end for(Pair)
    }
}