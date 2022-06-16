package com.devit.mscore.indexing.elasticsearch;

import java.io.File;

import com.devit.mscore.Resource;
import com.devit.mscore.exception.ResourceException;

import org.json.JSONObject;

public class ElasticsearchMapping extends Resource {

    protected ElasticsearchMapping(File resourceFile) throws ResourceException {
        super(resourceFile);
        this.name = resourceFile.getName().split("\\.")[0];
    }

    @Override
    public String getContent() {
        return new JSONObject(this.content).toString();
    }
}
