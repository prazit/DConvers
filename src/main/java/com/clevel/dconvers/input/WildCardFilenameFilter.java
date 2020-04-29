package com.clevel.dconvers.input;

import com.clevel.dconvers.DConvers;
import com.clevel.dconvers.ngin.AppBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

public class WildCardFilenameFilter extends AppBase implements FilenameFilter {
    private String regex;

    public WildCardFilenameFilter(DConvers dconvers, String name, String filter) {
        super(dconvers, name);
        this.regex = filter.replaceAll("[*]", "[A-Za-z_0-9\\-,.%()\\sก-ูเ-์]*");
        //log.debug("WildCardFilenameFilter(filter:{}) regex={}", filter, regex);
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.matches(regex);
        //log.debug("accept(name:{}) = {}", name, matches);
        //return matches;
    }

    @Override
    protected Logger loadLogger() {
        return LoggerFactory.getLogger(WildCardFilenameFilter.class);
    }
}
