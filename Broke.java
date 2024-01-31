import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.Charset;
import toolbox.terminal.ProgressBar;
import toolbox.terminal.InputReader;
import toolbox.terminal.InputParser;
import toolbox.textfile.TextFileHandler;
import toolbox.regex.Regex;

/**
 * O objetivo desta aplicação é tentar quebrar as linhas de um arquivo HTML que
 * ultrapassem (em número de caracteres) um determinado valor especificado. Até
 * que restem apenas linhas no arquivo com número menor de caracteres que este
 * valor  especificado.
 * 
 * <p>É uma aplicação de "melhor esforço", já que linhas de um documento HTML
 * nem sempre podem ser quebradas em qualquer ponto sem alterar a própria 
 * apresentação do documento no navegador.</p>
 * 
 * 
 * @version 1.0
 * @author Pedro Reis
 */
public final class Broke {
    
    //O tam. max. desejavel para linhas do arquivo
    private static int maxLength;
    
    //O diretorio por onde iniciar a pesquisa por arquivos
    private static String searchDir;
    
    //Se inclui ou nao subdiretorios ao pesquisar
    private static boolean searchSubdirs;
    
    /*-------------------------------------------------------------------------
              Obtem o ponto possivel de quebra de linha mais proximo que
              puder do final da string
    --------------------------------------------------------------------------*/     
    private static int getMaxBreakPoint(final String test) {

        return Math.max(test.lastIndexOf('>'), test.lastIndexOf('<') - 1);
    
    }//getMaxBreakPoint
    
    /*-------------------------------------------------------------------------
             Obtem o ponto possivel de quebra de linha mais proximo que
             puder do inicio da String
    --------------------------------------------------------------------------*/     
    private static int getMinBreakPoint(final String test) {
        
        int a = test.indexOf('>');
        int b = test.indexOf('<') - 1;
          
        int min = Math.min(a, b);
        
        if (min == -1) return Math.max(a, b); else return min;
      
    }//getMinBreakPoint
    
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

        //Obtem o tam. max. para uma linha-------------------------------------       
        
        inputReader.setPrompt(
            "Tamanho m\u00e1ximo desejado para uma linha do arquivo",
            "1000",
            "1000",
            new ParseMaxLength()
        );
            
        maxLength = Integer.parseInt(inputReader.readInput());    
        
    }//readInputs
    
    /**
     * Metodo principal. Inicia execucao do programa.
     * 
     * @param args Nao utilizado
     * @throws IOException Em caso de erro de IO.
     * @throws UnsupportedEncodingException Para encoding nao suportado.
     */
    /*-------------------------------------------------------------------------
                                    
    --------------------------------------------------------------------------*/ 
    public static void main(String[] args) throws IOException, UnsupportedEncodingException{ 

        readInputs(Commons.parseCharset(args)); 
        
        final Map<String, Integer> overflowsMap = new HashMap<>(256);
    
        final String[] tags = {"pre", "style", "script"};        

        String[] filenames = Commons.searchForHtmlFiles(searchDir, searchSubdirs);
     
        ProgressBar progressBar = new ProgressBar(filenames.length, 60);
        
        progressBar.showBar();
        
        try {    

            /*
            Processa cada arquivo HTML no diretorio especificado
            e, opcionalmente, nos subdiretorios deste 
            */
            for (String filename: filenames) {
                
                if (filename.matches(".+?\\.broke-\\d{2}\\d*\\.(HTML?|html?)")) {
                    progressBar.increment();
                    continue;
                }
                
                int brokenLines = 0;
                
                TextFileHandler textFileHandler = 
                    new TextFileHandler(filename, "utf8");
                
                textFileHandler.read();
                
                String content = textFileHandler.getContent();
                
                Map<String, String> lock = new HashMap<>(2048);
                
                int countLocks = 0;
                
                for (String tag : tags) {

                    Regex regex = 
                        new Regex(
                            "<" + tag + "[\\s\\S]*?>[\\s\\S]*?<\\/" + tag + ">"
                        );

                    regex.setTarget(content);

                    String match;

                    while ((match = regex.find()) != null) {

                        lock.put(
                            "\u0ca0\u13c8" + countLocks++ + "\u13da\u0ca0",
                            match
                        );
                        
                    }//while ((match = regex.find()) != null)

                    for (String key : lock.keySet()) {

                        content = content.replace(lock.get(key), key);
                        
                    }//for (String key : lock.keySet())
                    
                }//for (String tag : tags) 
                
                textFileHandler.setContent(content);
                    
                StringBuilder sb = new StringBuilder(65536);

                //Le linha por linha do arquivo 
                while (textFileHandler.hasNext()) {

                    String line = textFileHandler.nextLine();

                    //Linhas maiores que maxLength sao quebradas sucessivamente
                    while (line.length() > maxLength) {
                          
                        int breakPoint = 
                            getMaxBreakPoint(line.substring(0, maxLength));
                        
                        if (breakPoint < 0) {
                            
                            breakPoint = getMinBreakPoint(line);
                            if (breakPoint < 0) break;                               
                        }
                        
                        brokenLines++;
                        
                        sb.append(line.substring(0, breakPoint+1)).append('\n');
                        
                        line = line.substring(breakPoint + 1, line.length());
                        
                    }//while
                    
                    sb.append(line).append('\n');

                }//while

                //Grava em um arquivo com o nome do arquivo original e a 
                //extensao broke.html
                if (brokenLines > 0) {
                    
                    content = sb.toString();
                    
                    for (String key : lock.keySet()) {

                        content = content.replace(key, '\n' + lock.get(key) + '\n');
                    }                      
                    
                    //Recebe o conteudo do arquivo com as linhas quebradas
                    textFileHandler.setContent(content);
                    
                    String prefix = "broke-" + maxLength;                    

                    textFileHandler.writeWithExtPrefix(prefix);                    
                    
                    int overflows = 0;                    
                    
                    while (textFileHandler.hasNext()) {
                        
                        if (textFileHandler.nextLine().length() > maxLength) 
                            overflows++;
                    }
  
                    overflowsMap.put(
                        textFileHandler.getFilenameWithExtPrefix(prefix),
                        overflows
                    );
                }
                
                progressBar.increment();
      
            }//for
            
            System.out.println("\n\nFeito!\n");
            
            for (String key : overflowsMap.keySet()) {
                
                int numberOfOverflows = overflowsMap.get(key);
                
                System.out.printf(
                    "%s : Transbordamento em %d linha%s\n",
                    key,
                    numberOfOverflows,
                    (numberOfOverflows > 1 ? "s" : "")
                );
                
                if (numberOfOverflows > 0) {
                    
                    String[] countCharArgs = new String[2];
                    countCharArgs[0] = "\u0ca0\u13c8broke";
                    countCharArgs[1] = key;
                    System.out.println("\nGerando relat\u00f3rio para " + key + " ...\n");
                    CountChars.main(countCharArgs);
                }
                
            }//for
         
        }//try
        catch (IOException e) {
            
            System.out.println(e);
            
        }
           
    }//main
    
    /*=================================================================
     *               Classes privadas da classe Broke
     =================================================================*/
    private static class ParseMaxLength extends InputParser{
        
        @Override
        public String parse(final String input) 
            throws IllegalArgumentException {
            
            try {    
                
                int value = Integer.parseInt(input);
                
                if (value < 10)
                    throw new IllegalArgumentException(
                        "Tamanho m\u00e1ximo deve ser maior ou igual a 10!"
                    );
            }
            catch (NumberFormatException e) {
            
                throw new IllegalArgumentException(
                    "Digite um valor num\u00e9rico!"
                );
            }
            
            return input;
                
        }//parse
        
    }//classe privada ParseMaxLength-----------------------------------
     
}//classe Broke
