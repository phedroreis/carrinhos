import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Gpt {

    public static void main(String[] args) throws IOException {

        File dirCurrent = new File(".");

        String[] filenames = dirCurrent.list(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File f, String s) {
                        return s.toLowerCase().matches(".*\\.html?");
                    }
                }
        );

        for (String filename : filenames) {

            Path path = Path.of(filename);

            String content = Files.readString(path);

            Scanner scanner = new Scanner(content);

            StringBuilder originalOrder = new StringBuilder(1024);
            List<String> orderedByLength = new ArrayList<>();

            int countLines = 1;

            while (scanner.hasNext()) {

                String line = scanner.nextLine();

                originalOrder.append(
                        String.format(
                                "%06d] %,d\n",
                                countLines,
                                line.length()
                        )
                );

                orderedByLength.add(
                        String.format(
                                "%06d] %d\n",
                                countLines,
                                line.length()
                        )
                );

                countLines++;
            }

            try (PrintWriter pwOriginal = new PrintWriter(filename + "_originalOrder.txt");
                 PrintWriter pwOrderedByLength = new PrintWriter(filename + "_orderedByLength.txt")) {

                pwOriginal.print(originalOrder.toString());

                Collections.sort(orderedByLength, (s1, s2) -> {
                    int len1 = Integer.parseInt(s1.split("]")[1].trim());
                    int len2 = Integer.parseInt(s2.split("]")[1].trim());
                    return Integer.compare(len1, len2);
                });

                for (String line : orderedByLength) {
                    pwOrderedByLength.print(line);
                }

            }
        }
    }//main
}
