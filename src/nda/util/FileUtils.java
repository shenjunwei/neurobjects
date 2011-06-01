package nda.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * 
 * @author Giuliano Vilela
 */
public final class FileUtils {
    public static String parseFileName(String filepath) {
        int start = filepath.lastIndexOf('.');
        int end = filepath.lastIndexOf(File.separatorChar);

        if (start < 0) start = filepath.length();

        String newName = filepath.substring(end+1, start);
        return newName;
    }


    public static int[] readIntArray(String filepath)
    throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(filepath));
        ArrayList<Integer> l = new ArrayList<Integer>();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) continue;
            l.add(Integer.parseInt(line));
        }

        int[] res = new int[l.size()];
        for (int i = 0; i < l.size(); ++i)
            res[i] = l.get(i);

        return res;
    }


    public static double[] readDoubles(String filepath)
    throws FileNotFoundException, IOException {
        BufferedReader in = new BufferedReader(new FileReader(filepath));
        ArrayList<Double> l = new ArrayList<Double>();
        String line;

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) continue;
            l.add(Double.parseDouble(line));
        }

        double[] res = new double[l.size()];
        for (int i = 0; i < l.size(); ++i)
            res[i] = l.get(i);

        return res;
    }


    public static void saveIntArray(String filepath, int[] array)
    throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filepath));

        for (int x : array)
            out.write(Integer.toString(x) + '\n');

        out.close();
    }
}
