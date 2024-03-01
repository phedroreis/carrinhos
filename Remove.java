import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import toolbox.textfile.TextFileHandler;
import java.io.FilenameFilter;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/******************************************************************************
 * Aplicacao para excluir DIVs e seus escopos.
 * 
 * @author "Pedro Reis"
 * @version 2.0 (29 de fevereiro de 2024)
 * @since 1.0
 ******************************************************************************/
public final class Remove extends JFrame {
  
    //Barra de progresso ocupa toda a janela
    private final JProgressBar jpb;
    
    //Exlcui divs e seus escopos localizadas pela regexp
    private static final String OPENDIV_EXCLUDE_PATTERN = 
        "<div class=\"(_3HZor _3kF8H|_1-iDe _1xXdX|i5ly3 _2NwAr|_1Flk2 _2DPZK|ldL67 _2i3T7|_3Bc7H _20c87|_2Ts6i _3RGKj)\"[\\s\\S]*?>";  
    
    //Versoes editadas dos arqs. originais serao gravadas com ext. .ripped.html    
    private static final String OUT_EXTENSION_PREFIX = "ripped";
    
    //Da match com qualquer arquivo com ext. HTML, mas nao com .ripped.html|.ripped.HTML|.ripped.htm|.ripped.HTM
    private static final String FILE_FILTER_REGEX = "^(.(?!\\." + OUT_EXTENSION_PREFIX + "))+\\.(html?|HTML?)";
    
    /*[00]----------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/
    private Remove() {
        
        super("Removendo DIVs...");//Chama construtor da super classe 
        
        //Barra de progresso na interface da aplicacao
        jpb = new JProgressBar();
        
        jpb.setStringPainted(true);//Permite escrever na barra de prog.  
        
        jpb.setMinimum(0);//Irah de 0 ate num. de arqs. selecionados
        
        add(jpb);//Adiciona barra de progresso na janela principal              
        
        setSize(450, 150);//Janela de 450 largura x 150 altura
        
        setLocationRelativeTo(null);//Abre a janela no centro da tela
        
        //Encerra aplicacao se fechar janela principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
    }//construtor
    
    /*[01]----------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/     
    private static boolean fileFilter(final String filename) {
        
        return filename.matches(FILE_FILTER_REGEX);
        
    }    
    
    /*[02]----------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/     
    private static void abort(Exception e) {
        
        //Exibe janela com mensagem de erro
        JOptionPane.showMessageDialog(
            null,
            e.getMessage(), 
            "Erro Fatal!",
            JOptionPane.ERROR_MESSAGE
        );
        
        e.printStackTrace();

        System.exit(1);//Aborta programa em caso de erro de IO
        
    }//abort() 

    /**
     * Programa principal.
     * 
     * <p>O método exibe, inicialmente, uma janela para o usuário escolher se
     * quer serlecionar uma pasta onde estarão os arquivos a serem editados,
     * ou se prefere selecionar individualmente estes arquivos.</p>
     * 
     * <p>Na sequência abre uma interface para que a pasta ou os arquivos sejam
     * escolhidos</p>
     * 
     * @param args Args de linha de comando nao sao utilizados nesta aplicacao.
     */
    public static void main(String[] args) {
        
        //Le charset dos arquivos na linha de comando. UTF-8 como default.
        String htmlCharset = Commons.parseCharset(args, "utf8");
             
        Object[] selectionsOptions = {"Pasta", "Arquivos do \"Zap\"", "Cancelar"};
        
        //Janela para usuário escolher se quer selecionar uma pasta ou arquivos
        int selectionOption = JOptionPane.showOptionDialog(
            null,
            "O que deseja selecionar?", 
            "Escolha",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, selectionsOptions, selectionsOptions[0]
        );
          
        if (selectionOption == 2) System.exit(0);//Usuário abortou execução do programa  
        
        //Cria um objeto JFileChooser para selecionar os arquivos de entrada.
        JFileChooser jfc = new JFileChooser("."); 
        
        //Cria filtro pra aceitar apenas selecao de certos tipos de arquivos
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.addChoosableFileFilter(new ZapFileFilter());       
        
        if (selectionOption == 0) {//Escolheu selecionar uma pasta
            
            jfc.setDialogTitle("Selecione a pasta com arquivos do ZAP");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
        }
        else {//Escolheu selecionar arquivos

            jfc.setDialogTitle("Selecione os arquivos do ZAP");           
            jfc.setMultiSelectionEnabled(true);//Permite selecionar multiplos arquivos

        }//if-else 
        
        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(0);
        
        File[] selectedFiles = null;//Array com os arqs. selecionados
        
        if (selectionOption == 0) 
     
            selectedFiles = 
                jfc.getSelectedFile().listFiles(
                    new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename){ return fileFilter(filename); }
                    }
                );//Selecionar pasta
     
        else 
        
            selectedFiles = jfc.getSelectedFiles();//Selecionar arquivos individualmente
 
        //Mas se o array estiver nulo, aborta o programa
        if (selectedFiles == null) abort(new Exception("Nenhum arquivo selecionado!"));
        
        Remove remove = new Remove();//Cria um objeto da classe principal
        
        remove.setVisible(true);//Exibe a janela principal
         
        //Valor max. da barra de progresso é o número de arquivos que serão processados
        remove.jpb.setMaximum(selectedFiles.length);
       
        remove.jpb.setValue(0);//Inicializa barra de progresso em 0%
        
        Pattern divTagPattern = Pattern.compile("</?div[\\s\\S]*?>");
        
        int indexFile = 0;//Contador de arquivos processados
  
        for (File file : selectedFiles) { //Loop for processa cada aquivo de entrada

            remove.jpb.setString("Processando " + file.getName());
            
            try {
                
                TextFileHandler textFileHandler = new TextFileHandler(file.getAbsolutePath(), htmlCharset);
                
                textFileHandler.read();
                
                String content = textFileHandler.getContent();
                
                StringBuilder sb = new StringBuilder(content);
                
                boolean modified = false; 
                    
                Matcher divTagMatcher = divTagPattern.matcher(sb);
                
                int divLevel = 0;
            
                int divTagStart = -1;
                
                //Localiza cada tag div (abertura ou fechamento) no arquivo fonte
                while (divTagMatcher.find()) {
                    
                    String divTag = divTagMatcher.group();//Tag de abertura ou de fechamento localizada
                   
                    if (divLevel == 0) {//Eh uma tag localizada fora do escopo de uma div a ser excluida
                        
                        if (divTag.matches(OPENDIV_EXCLUDE_PATTERN)) {//Essa tag abre uma div a ser excluida
      
                                divLevel++;//Escopo no nivel 1 de aninhamento
                                divTagStart = divTagMatcher.start();//Marca o inicio da regiao a ser excluida             
                               
                        }
                        
                    }
                    else {//Tag div localizada no escopo da div que sera excluida
                        
                        if (divTag.charAt(1) == '/') {//Tag de fechamento
                            
                            divLevel--;//Escopo subiu um nivel de aninhamento
                            
                            if (divLevel == 0) {//Essa tag fecha a div a ser excluida 
    
                                sb.delete(divTagStart, divTagMatcher.start() + divTag.length());//Exclui DIV
                                divTagMatcher = divTagPattern.matcher(sb);//Reseta objeto Matcher
                                divTagMatcher.region(divTagStart, sb.length());//Busca por tags continua a partir do ponto de exclusao
                                modified = true;//O conteudo do arquivo fonte foi modificado e sera gravado no arquivo destino
                               
                            }
                        }
                        else divLevel++;//Tag de abertura -> Escopo desceu um nivel de aninhamento                       
                        
                    }//if-else
                    
                }//while
                     
                if (modified) {
                    
                    textFileHandler.setContent(sb.toString());
                        
                    textFileHandler.writeWithExtPrefix(OUT_EXTENSION_PREFIX);
                }                   
            
            } catch (Exception e) {
                
                abort(e);//Aborta execução
                
            }
  
            //Incrementa contador de arqs. processados e atualiza barra de progresso
            remove.jpb.setValue(++indexFile);
       
        }//Fim do loop for (Todos os arqs. de entrada foram editados)
        
        remove.jpb.setString("Feito!");//Mensagem final na barra de progresso       
        
    }//main()
    
 /*===========================================================================
 *                          Classe privada
 ===========================================================================*/ 
private static class ZapFileFilter extends FileFilter {
    
    @Override
    public boolean accept(File file){

        if (file.isDirectory()) return true;

        return fileFilter(file.getName());
        
    }//accept()

    @Override
    public String getDescription(){
        
        return "Arquivos do ZAP";
        
    }//getDescription()
    
}//classe ZapFileFilter    
    
}//classe Remove
