package software.chronicle;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;


import java.io.BufferedWriter;
import java.io.FileWriter;

public class ReadRawData {

    public static void main(String[] args) throws IOException {

        ReadRawData readRawData = new ReadRawData();

        String s = readRawData.readFileToString("/Users/robaustin/git-projects/gpt3/src/main/resources/raw-data.txt");
        readRawData.split(s);
    }

    public String readFileToString(String filePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        return new String(encoded);
    }


    void split(String inputString) {
        ArrayList<Integer> items = new ArrayList<>();

// Split the string by newline characters
        String[] lines = inputString.split("\\r?\\n");

// Find the index of the line that starts with a number
        int index = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.length() == 0)
                continue;
            if (Character.isDigit(line.charAt(0))) {
                index = i;


                if (index == lines.length)
                    return;
// Check if the line before the number line contains the word "Problem"
                String line1 = lines[index + 1];
                if (line1.equals("Problem")) {
                    // Concatenate the lines before the number line to get the title
                    items.add(index);

                }
            }
        }

        int count = 0;
        StringBuilder out = new StringBuilder();

        for (int i = 1; i < items.size(); i++) {
            out.setLength(0);
            System.out.println("\n------------------------------------------------\n");

            for (int j = items.get(i - 1); j < items.get(i); j++) {
                out.append(lines[j]);
                out.append("\n");
            }

            count++;
            tofile(out.toString(), "/Users/robaustin/git-projects/gpt3/src/main/resources/extracted/" + count);
        }


    }


    public static void tofile(String myString, String filePath) {
        // Specify the path and filename of the file you want to write to

        try {
            // Create a new BufferedWriter object to write to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            // Write the string to the file
            writer.write(myString);

            // Close the writer
            writer.close();

            System.out.println("Successfully wrote string to file.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing the string to file: " + e.getMessage());
        }
    }

}
