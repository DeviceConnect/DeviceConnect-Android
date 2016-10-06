package com.barchart.udt.lib;

import com.barchart.udt.ResourceUDT;

public class AndroidLoaderUDT implements LibraryLoader {

    public static void load() {

        ResourceUDT.setLibraryLoaderClassName(AndroidLoaderUDT.class.getName());
        try {
            AndroidLoaderUDT udt = new AndroidLoaderUDT();
            udt.load("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(final String location) throws Exception {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("barchart-udt-core-android");
    }
}
