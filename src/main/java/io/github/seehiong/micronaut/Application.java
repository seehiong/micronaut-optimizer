package io.github.seehiong.micronaut;

import com.google.ortools.Loader;

import io.micronaut.runtime.Micronaut;

public class Application {

    static {
        // Load the OR-Tools native library
        Loader.loadNativeLibraries();
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

}
