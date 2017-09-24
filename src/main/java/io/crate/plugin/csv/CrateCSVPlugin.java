package io.crate.plugin.csv;

import io.crate.Plugin;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;

import java.util.Collection;
import java.util.Collections;

public class CrateCSVPlugin implements Plugin {


    public static final String PLUGIN_NAME = "crate-csv";
    public static final String PLUGIN_DESC = "Plugin to import csv files";


    public CrateCSVPlugin(Settings settings) {

    }

    @Override
    public String name() {
        return PLUGIN_NAME;
    }

    @Override
    public String description() {
        return PLUGIN_DESC;
    }

    @Override
    public Collection<Module> createGuiceModules() {
        return Collections.singletonList(new CSVModule());
    }
}
