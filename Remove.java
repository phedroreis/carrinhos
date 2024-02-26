import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
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
        
        Pattern divPattern = Pattern.compile("</?div[\\s\\S]*?>");
        
        List<int[]> listOfIndexes;
        
        ProgressBar progressBar = new ProgressBar(filenames.length, 60);
        
        progressBar.showBar();         
        
        for (String filename: filenames) {
           
            TextFileHandler textFileHandler = new TextFileHandler(filename, htmlCharset);
            
            textFileHandler.read();
            
            String content = textFileHandler.getContent();
            
            StringBuilder sb = new StringBuilder(content);
            
            Matcher divMatcher = divPattern.matcher(sb);
            
            int divLevel = 0;
            
            int divStart = -1;
             
            listOfIndexes = new LinkedList<>();
            
            while (divMatcher.find()) {
                
                String divTag = divMatcher.group();
               
                if (divLevel == 0) {
                    
                    if (divTag.contains("aria-label=\"Lista de conversas\"")) {
                        
                        divLevel++;
                        divStart = divMatcher.start();
                           
                    }
                    
                }
                else {
                    
                    if (divTag.charAt(1) == '/') {
                        
                        divLevel--;
                        
                        if (divLevel == 0) {
 
                            int[] divBoundary = new int[2];
                            divBoundary[0] = divStart;
                            divBoundary[1] = divMatcher.start() + divTag.length();
                            listOfIndexes.add(divBoundary);
                           
                        }
                    }
                    else divLevel++;
                    
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
    
    /*=======================================================
     *                  Classe privada
     ======================================================*/
    private static class ParseCharset extends InputParser {
        
        @Override
        public String parse(final String input) 
            throws IllegalArgumentException {

            try {
                
                if (Charset.isSupported(input)) 
                    return input; 
                else 
                    throw new IllegalArgumentException("Charset inexistente!");
            }
            catch (IllegalCharsetNameException e) {
                
                throw new IllegalArgumentException("Charset ilegal!");
            }
            
        }//parse
        
    }//classe privada ParseCharset
  
}//classe Remove
