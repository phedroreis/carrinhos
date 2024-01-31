import java.io.IOException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.Charset;
import toolbox.clipboard.TransferArea;
import toolbox.regex.Regex;

public class FixYTClipboard {
    
    private static final String HTML_EDITOR = "code";
    
    private static final String HEADER = 
        "<!DOCTYPE html><html lang=\"pt-br\"><head><meta charset=\"UTF-8\"></head><body>\n<div style=\"\">\n";
    
    private static final Map<String, String> FIRST_MAP = new TreeMap<>(new Comp());
    
    private static final Map<String, String> FINAL_MAP = new TreeMap<>();
    
    private static final String A_REGEX = "<a[\\S\\s]+?(href=\"[\\S\\s]+?\")[\\S\\s]+?>([\\S\\s]+?)</a>";
    
    public static void main(String[] args) {
  
        
        String htmlFlavor = null; String stringFlavor = null;
        
        try {
            
            htmlFlavor = TransferArea.getHtml();
            stringFlavor = TransferArea.getPlainText();
        } 
        catch(UnsupportedFlavorException | IOException e) {
            
            System.err.println("Conte\u00fado inesperado no clipboard! Abortando...");
            System.exit(0);
        }
    
        String originalStringFlavor = stringFlavor;  
        
        Regex regex = new Regex(A_REGEX); regex.setTarget(htmlFlavor);
        
        int count = 0;
          
        while (regex.find() != null)  {
            
            String href = regex.group(1);
            String link = regex.group(2);
            String code = "*$#@§°" + count++ + "°§@#*";
            FIRST_MAP.put(link, code);
            FINAL_MAP.put(code, "<a " + href + ">" + link + "</a>");
            
        }//while 
        
         
        for (String key : FIRST_MAP.keySet()) stringFlavor = stringFlavor.replace(key, FIRST_MAP.get(key));
        
        for (String key : FINAL_MAP.keySet()) stringFlavor = stringFlavor.replace(key, FINAL_MAP.get(key));
        
        Scanner scanner = new Scanner(stringFlavor);
        
        StringBuilder sb = new StringBuilder(HEADER);
        
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.isEmpty())
                sb.append(line);
            else
                sb.append("<span style=\"\">").append(line).append("</span>");
            sb.append("<br>\n");
        }
        
        stringFlavor = sb.append("\n</div></body></html>").toString();
        
        try {
            
            Files.writeString(
                Path.of("clip.html"),
                stringFlavor,
                Charset.forName("UTF-8"), 
                StandardOpenOption.CREATE
            );
            
            Runtime.getRuntime().exec(HTML_EDITOR + " clip.html");
        }
        catch (IOException e) { }      
        
        String display = 
            "    <<<<< HTML no clipboard. Se preciso, edite este bloco e salve como arquivo HTML >>>>>\n\n" +
            stringFlavor  +      
            "\n\n    <<<<< Este era o texto plain que o browser copiou para o clipboard >>>>>\n\n" + 
            originalStringFlavor + 
            "\n\n    <<<<< Este era o HTML que o browser copiou para o clipboard >>>>>\n\n" +
            htmlFlavor;
            
        TransferArea.setContents(display, stringFlavor);         
        
    }//main
    
private static class Comp implements Comparator<String> {
    
    public int compare(final String s1, final String s2) { return -(s1.compareTo(s2)); }   
    
    public boolean equals(final String s) { return false; }
    
}//classe interna Comp

}//classe FixYTClipboard
