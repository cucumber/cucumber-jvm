package io.cucumber.core.plugin;


import java.io.OutputStream;

public class TestUTF8OutputStreamWriter {

    public static UTF8OutputStreamWriter create(OutputStream outputStream){
        return new UTF8OutputStreamWriter(outputStream);
    }

}