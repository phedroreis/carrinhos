import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import toolbox.terminal.InputReader;
import toolbox.terminal.InputParser;
import toolbox.terminal.ProgressBar;
import toolbox.textfile.TextFileHandler;
import java.util.Locale;


/**
 * Mostra quantos caracteres tem em cada linha de um arquivo HTML.
 *  
 * @version 1.0
 * @author Pedro Reis
 */
public final class CountChars {
    
    static {
        
        toolbox.locale.Localization.setLocale(new Locale("pt", "BR")); 
    }
         
    private static boolean sortedByLength;
    
    private static boolean ascendingSort;
    
    private static boolean searchSubdirs;
    
    private static String searchDir;
      
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
        
       //Define se a saida eh ordenada pelo tam. das linhas--------------
        
        inputReader.setPrompt(
            "Saida ordenada por comprimento de linhas",
            true
        );
        
        sortedByLength = inputReader.readInput().equals("s"); 
        
        if (sortedByLength) {
            
            inputReader.setPrompt(
                "Ordem ascendente de tamanho",
                 true
            );
        
            ascendingSort= inputReader.readInput().equals("s");             
        }
        
    }//readInputs   
    
    /**
     * Para cada arquivo tipo HTML encontrado no diretório corrente (ou, 
     * opcionalmente, nos subdiretórios deste) grava um arquivo .txt indicando
     * quantos caracteres tem cada linha deste arquivo HTML.
     * 
     * @throws IOException Em caso de erro de IO.
     */
    /*-------------------------------------------------------------------------
                                Metodo principal
    --------------------------------------------------------------------------*/         
    public static void main(String[] args) throws IOException {
        
        String[] filenames;
        ProgressBar progressBar;
        
        if (args.length > 0 && args[0].equals("\u0ca0\u13c8broke")) {
            
            filenames = new String[1]; filenames[0] = args[1];
            
            progressBar = null;
            
            sortedByLength = true; ascendingSort = false;
            
        }
        else {
            
            readInputs(Commons.parseCharset(args));
              
            filenames = Commons.searchForHtmlFiles(searchDir, searchSubdirs);
            
            progressBar = new ProgressBar(filenames.length, 60);
            
            progressBar.showBar(); 
        
        }
       
        for (String filename: filenames) {
            
            List<int[]> sortedList = new LinkedList<>();    
            
            TextFileHandler textFileHandler = new TextFileHandler(filename, "utf8");
            
            textFileHandler.read(); 
            
            int greaterLength = 0;
            
            int countLines = 0;
            
            while(textFileHandler.hasNext()) {
                
                String line = textFileHandler.nextLine();
                
                int lineLength = line.length();
                
                int[] data = new int[2];                
                            
                if (sortedByLength) { 
                    
                    data[0] = lineLength; data[1] = ++countLines; 
                      
                }
                else {  
                    
                    data[0] = ++countLines; data[1] = lineLength; 
                   
                }//if-else
                
                if (lineLength > greaterLength) greaterLength = lineLength;
               
                sortedList.add(data);
                
            }//while(textFileHandler.hasNext()) 
               
            StringBuilder sb = new StringBuilder(262144);
            
            int tab = 12;//Deslocamento da tabela a partir da margem esquerda
            
            sb.append('\n').append(" ".repeat(tab - 11)).append(countLines).append(countLines == 1 ? " linha" : " linhas").
            append(" no arquivo. Maior linha possui ").append(String.format("%,d", greaterLength)).append(" caracteres.\n\n");
            
            int cellsInARow; int greaterData;
            
            if (sortedByLength) {
                
                Collections.sort(sortedList, new Compare(ascendingSort)); 
                sb.append(String.format("%" + tab + "s : %s", "comprimento", "linhas"));
                cellsInARow = 10;
                greaterData = countLines;
            }                
            else {
                
                sb.append(String.format("%" + tab + "s : %s", "linha", "comprimento")); 
                cellsInARow = 1;
                greaterData = greaterLength;
                
            }//if-else (sortedByLength)
            
            int cellsLength = 2;
            
            //Calcula largura das celulas em funcao do dado com 
            //maior num. de digitos que sera exibido na planilha
            while (greaterData > 0) { greaterData = greaterData / 10; cellsLength++; }
               
            String emptyCell = " ".repeat(cellsLength) + '|'; 
            String borderTop = ('+' + "-".repeat(cellsLength)).repeat(cellsInARow) + '+';
            String borderLeft = '\n' + " ".repeat(tab + 2) + '|';

            int previousData = -1; int countData = 0; int cellsToFill = 0; boolean rowFilled = false;
            
            for (int[] data : sortedList) { 
                
                if (data[0] != previousData) {
                   
                    if (rowFilled) //A ultima linha acima foi totalmente preenchida?
                    
                        sb.delete(sb.length() - tab - 4, sb.length());//Delete a borda esquerda acrescentada indevidamente
                        
                    else 
                    
                        sb.append(emptyCell.repeat(cellsToFill));//Preencha a linha acima com celulas vazias 
                        
                    if (!sortedByLength && data[1] == greaterLength)                    
                        sb.append(String.format("\n<%0," + (tab - 2) + "d> :", data[0]));
                    else
                        sb.append(String.format("\n%," + tab + "d :", data[0]));
                        
                    sb.append(borderTop).append(borderLeft);
                    
                    previousData = data[0];
                    countData = 0;
                }
                

                sb.append(String.format("%" + cellsLength + "d|", data[1]));   
                   
                cellsToFill = cellsInARow - (++countData % cellsInARow);
                
                rowFilled = (cellsToFill == cellsInARow);
                
                if (rowFilled) sb.append(borderLeft);//Imprime a borda esquerda da nova linha

            } 
            
            if (rowFilled) sb.delete(sb.length() - tab - 4, sb.length()); else sb.append(emptyCell.repeat(cellsToFill));
            
            sb.append(String.format("\n%" + (tab + 2) + "s", " ")).append(borderTop);
            
            try (PrintWriter pw = new PrintWriter(filename + ".txt")) {
                
                pw.print(sb.toString());
                
            }
            
            if (progressBar != null) progressBar.increment();             
            
        }//for
        
        if (progressBar != null) System.out.println("\n\nFeito!\n");
           
    }//main
    
/*=============================================================================
                 Classes privadas da classe CountChars
==============================================================================*/      
    private static final class Compare implements Comparator<int[]> {
        
        private final boolean ascend;
        
        public Compare(final boolean ascend) {
            this.ascend = ascend;
        }

        @Override
        public int compare(int[] a, int[] b) {
            
            if (ascend) return ((int[])a)[0] - ((int[])b)[0];
            
            return ((int[])b)[0] - ((int[])a)[0];               
        } 
        
    }//classe Compare-----------------------------------------------------------

    
}//classe CountChars