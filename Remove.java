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
    
    private static String htmlCharset;
    
    private static final String DIV_PATTERN = 
        "<div class=\"(_3HZor _3kF8H|_1-iDe _1xXdX|i5ly3 _2NwAr|_1Flk2 _2DPZK|ldL67 _2i3T7|_3Bc7H _20c87|_2Ts6i _3RGKj)\"[\\s\\S]*?>";
    
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
        
        //Define charset para ler e gravar os arquivos--------------------------
               
        inputReader.setPrompt(
            "Charset dos arquivos HTML",
            "UTF-8",
            "utf8",
            new ParseCharset()
        );
        
        htmlCharset = inputReader.readInput();
        
    }//readInputs
    
    /**
     * Metodo principal.
     * 
     * @param args O charset a ser usado para ler e escrever no 
     * terminal pode ser passado ao programa pela linha de comando. Se nenhum 
     * parametro for passado sera assumido que o charset do terminal e o mesmo
     * do sistema.
     * 
     * @throws IOException Em caso de erro de IO.
     */
    public static void main(String[] args) throws IOException {
        
        readInputs(Commons.parseCharset(args));
          
        String[] filenames = Commons.searchForHtmlFiles(searchDir, searchSubdirs);
        
        Pattern divTagPattern = Pattern.compile("</?div[\\s\\S]*?>");
        
        List<int[]> listOfIndexes;
        
        ProgressBar progressBar = new ProgressBar(filenames.length, 60);
        
        progressBar.showBar();         
        
        for (String filename: filenames) {
            
            progressBar.increment();
            
            if (filename.endsWith("ripped.html")) continue;
           
            TextFileHandler textFileHandler = new TextFileHandler(filename, htmlCharset);
            
            textFileHandler.read();
            
            String content = textFileHandler.getContent();
            
            StringBuilder sb = new StringBuilder(content);
            
            boolean modified = false; 
                
            Matcher divTagMatcher = divTagPattern.matcher(sb);
            
            int divLevel = 0;
        
            int divTagStart = -1;
            
            while (divTagMatcher.find()) {
                
                String divTag = divTagMatcher.group();
               
                if (divLevel == 0) {
                    
                    if (divTag.matches(DIV_PATTERN)) {
  
                            divLevel++;
                            divTagStart = divTagMatcher.start();             
                           
                    }
                    
                }
                else {
                    
                    if (divTag.charAt(1) == '/') {
                        
                        divLevel--;
                        
                        if (divLevel == 0) {

                            sb.delete(divTagStart, divTagMatcher.start() + divTag.length());
                            divTagMatcher = divTagPattern.matcher(sb);
                            divTagMatcher.region(divTagStart, sb.length());
                            modified = true;
                           
                        }
                    }
                    else divLevel++;
                    
                }//if-else
                
            }//while
                 
            if (modified) {
                
                textFileHandler.setContent(sb.toString());
                    
                textFileHandler.writeWithExtPrefix("ripped");
            }
             
        }//for filename
        
    }//main
   
}//classe Remove
