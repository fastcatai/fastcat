package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class M3UParser {

    public static HashMap<String, List<String>> readFile(File timestampFile) throws WrongFileFormatException {
        HashMap<String, List<String>> timestamps = new HashMap<>();
        String timeInterval = null;
        try (BufferedReader br = new BufferedReader(new FileReader(timestampFile))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] lineData = line.trim().split(":");
                if (lineData[0].equals("#EXTVLCOPT")) {
                    String[] time = lineData[1].trim().split("=");
                    if (time[0].equals("start-time") && timeInterval == null) {
                        timeInterval = time[1].trim() + "-";
                    } else if (time[0].equals("stop-time") && timeInterval != null) {
                        timeInterval += time[1].trim();
                    } else throw new WrongFileFormatException("The data ordering of the file is wrong");
                } else if (lineData.length == 1 && timeInterval != null && timeInterval.split("-").length == 2) {
                    String filename = lineData[0];
                    List<String> times;
                    if (timestamps.containsKey(filename))
                        times = timestamps.get(filename);
                    else times = new ArrayList<>();
                    times.add(timeInterval);
                    timestamps.put(lineData[0].trim(), times);
                    timeInterval = null;
                } else throw new WrongFileFormatException("The data ordering of the file is wrong");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (timeInterval == null)
            return sortLists(timestamps);
        else throw new WrongFileFormatException("The data ordering of the file is wrong");
    }

    private static HashMap<String, List<String>> sortLists(HashMap<String, List<String>> timestamps) {
        for (String key : timestamps.keySet()) {
            List<String> values = timestamps.get(key);
            values.sort(Comparator.comparingInt(s -> Integer.parseInt(s.split("-")[0])));
            timestamps.put(key, values);
        }
        return timestamps;
    }
}
