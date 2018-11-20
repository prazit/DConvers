package com.clevel.dconvers.conf;

import com.clevel.dconvers.Application;
import com.clevel.dconvers.ngin.transform.TransformTypes;
import javafx.util.Pair;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TransformConfig extends Config {

    private String transform;

    // ArgumentValue = String
    // ArgumentName = String
    // ArgumentList = Map<ArgumentName,ArgumentValue>>
    // TransformList = List<Pair<TransformTypes,ArgumentList>>
    private List<Pair<TransformTypes, Map<String, String>>> transformList;

    public TransformConfig(Application application, String baseProperty, Configuration baseProperties) {
        super(application, baseProperty);
        this.properties = baseProperties;

        valid = loadProperties();
        if (valid) valid = validate();

        log.trace("TransformConfig({}) is created", name);
    }

    @Override
    protected boolean loadProperties() {

        Map<String, String> argumentList;
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

        transformList = new ArrayList<>();
        transform = "";

        String transformString;
        for (Object transformObject : transformArray) {

            transformString = transformObject.toString();
            if (transformString.length() == 0) {
                continue;
            }
            transform += transformString + ",";

            // example: transformString=TRANSOP1(arguments),TRANSOP2(arguments),TRANSOP3(arguments)
            // transformValues=[TRANSOP1, arguments, TRANSOP2, arguments, TRANSOP3, arguments]
            String[] transformValues = transformString.split("\\),|[()]");
            log.debug("transformValues = {}", Arrays.asList(transformValues));
            for (int index = 0; index < transformValues.length; index += 2) {
                transformTypeName = transformValues[index];
                transformType = TransformTypes.valueOf(transformTypeName.toUpperCase());

                argumentList = new HashMap<>();
                argumentName = "arguments";
                argumentValue = transformValues[index + 1];
                argumentList.put(argumentName, argumentValue);

                transProperties = properties.subset(transformProperty + "." + transformTypeName);
                transKeyList = transProperties.getKeys();
                for (Iterator<String> it = transKeyList; it.hasNext(); ) {
                    argumentName = it.next();
                    argumentValue = transProperties.getString(argumentName);
                    argumentList.put(argumentName, argumentValue);
                }

                this.transformList.add(new Pair<>(transformType, argumentList));
            }

        }

        transform = transform.substring(0, transform.length() - 2);
        log.debug("transformList = {}", this.transformList);
        return true;
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

    public List<Pair<TransformTypes, Map<String, String>>> getTransformList() {
        return transformList;
    }
}