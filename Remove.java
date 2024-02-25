import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedList;
import java.util.List;
import toolbox.terminal.InputReader;
import toolbox.terminal.InputParser;
import toolbox.terminal.ProgressBar;
import toolbox.textfile.TextFileHandler;

/**
 * Uma aplicaçao para remover tags divs (e seus escopos) que contenham o
 * atributo aria-label="Lista de conversas".
 * 
 * @author Pedro Reis
 * @version 1.0 25 de fevereiro de 2024
 */
public class Remove{
    
    private static String searchDir;
    
    private static boolean searchSubdirs;
    
    /*-------------------------------------------------------------------------
                             Le as entradas de usuario                          
    --------------------------------------------------------------------------*/ 
    private static void readInputs(final String charset) throws IOException, UnsupportedEncodingException {
            
        InputReader inputReader = new InputReader(charset);
        
        //Obtem o diretorio de pesquisa----------------------------------------
        
        inputReader.setPrompt(
            "Diret\u00f3rio a ser pesquisado",
            "Diret\u00f3rio corrente",
            ".",
            new ParseSearchDir()
        );
            
        searchDir = inputReader.readInput(); 
        
        //Define se a pesquisa deve se extender aos subdiretorios---------------
        
        inputReader.setPrompt(
            "Pesquisar subdiret\u00f3rios",
            false
        );
        
        searchSubdirs = inputReader.readInput().equals("s");         
        
    }//readInputs
    
    public static void main(String[] args) throws IOException {
        
        readInputs(Commons.parseCharset(args));
          
        String[] filenames = Commons.searchForHtmlFiles(searchDir, searchSubdirs);
        
        Pattern divPattern = Pattern.compile("</?div[\\s\\S]*?>");
        
        List<int[]> listOfIndexes;
        
        ProgressBar progressBar = new ProgressBar(filenames.length, 60);
        
        progressBar.showBar();         
        
        for (String filename: filenames) {
           
            TextFileHandler textFileHandler = new TextFileHandler(filename, "utf8");
            
            textFileHandler.read();
            
            String content = textFileHandler.getContent();
            
            StringBuilder sb = new StringBuilder(content);
            
            Matcher matcher = divPattern.matcher(sb);
            
            int scopesLevel = 0;
            
            int[] indexes = null;
            
            listOfIndexes = new LinkedList<>();
            
            while (matcher.find()) {
                
                String divTag = matcher.group();
               
                if (scopesLevel == 0) {
                    
                    if (divTag.contains("aria-label=\"Lista de conversas\"")) {
                        
                        scopesLevel++;
                        indexes = new int[2];
                        indexes[0] = matcher.start();
                           
                    }
                    
                }
                else {
                    
                    if (divTag.charAt(1) == '/') {
                        
                        scopesLevel--;
                        
                        if (scopesLevel == 0) {
                            
                            indexes[1] = matcher.start() + divTag.length();
                            listOfIndexes.add(indexes);
                           
                        }
                    }
                    else scopesLevel++;
                    
                }//if-else
                
            }//while
            
            if (listOfIndexes.size() > 0) {
                
                for (int[] i : listOfIndexes) sb.delete(i[0], i[1]);
                
                textFileHandler.setContent(sb.toString());
                
                textFileHandler.writeWithExtPrefix("rem");
            }
            
            progressBar.increment();
            
        }//for
        
    }//main
  
}//classe Remove
