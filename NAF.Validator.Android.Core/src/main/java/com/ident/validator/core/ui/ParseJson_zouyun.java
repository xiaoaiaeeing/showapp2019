package com.ident.validator.core.ui;

import com.google.gson.Gson;

/**
 * Created by pengllrn on 2019/1/7.
 */

public class ParseJson_zouyun {

    public TaggServer_aoao Json2TaggServer(String json) {
        Gson gson = new Gson();
        TaggServer_aoao taggServer = gson.fromJson(json,TaggServer_aoao.class);
        return taggServer;
    }
}
