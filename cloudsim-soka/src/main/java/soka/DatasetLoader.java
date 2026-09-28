package soka;

import java.io.*;
import java.util.*;

public class DatasetLoader {
    public static List<Long> load(String resourceName) throws IOException {
        List<Long> mi = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                DatasetLoader.class.getClassLoader().getResourceAsStream(resourceName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) mi.add(Long.parseLong(line));
            }
        }
        return mi;
    }
}