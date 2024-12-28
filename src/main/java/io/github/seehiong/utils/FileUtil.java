package io.github.seehiong.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import io.micronaut.http.multipart.CompletedFileUpload;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {

    public List<String> readFile(CompletedFileUpload file) throws IOException {
        // Read the contents of the file into a List of Strings
        List<String> lines = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("UTF-8")))) {
            String line;
            while ((line = input.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

}
