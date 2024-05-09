import toolbox.textfile.TextFileHandler;
import javax.swing.filechooser.FileFilter;
import javax.management.modelmbean.XMLParseException;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/******************************************************************************
 * Aplicacao para excluir um tipo selecionado de tag e seu escopo em arquivos
 * HTML.
 * 
 * @author "Pedro Reis"
 * @version 1.0 (7 de maio de 2024)
 * @since 1.0
 ******************************************************************************/
public final class RemoveX extends JFrame {
    
    static {
        
        toolbox.locale.Localization.setLocale(new Locale("pt", "BR")); 
    }
    
    private String htmlCharset;
    
    private final JPanel northPanel;
    
    private final JTextArea jTextArea;
    
    private final JPanel centerPanel;
    
    private final JFileChooser jFileChooser;
    
    private final JPanel southPanel;
    
    private String tag;
    private String attr;
    private String value;
    
    private JTextField tagField;
    private JLabel tagLabel;
    private JTextField attrField;
    private JLabel attrLabel;
    private JTextField valueField; 
    private JLabel valueLabel;
  
    //Versoes editadas dos arqs. originais serao gravadas com ext. .ripped.html    
    private static final String OUT_EXTENSION_PREFIX = "ripped";
    
    //Da match com qualquer arquivo com ext. HTML, mas nao com .ripped.html|.ripped.HTML|.ripped.htm|.ripped.HTM
    private static final String FILE_FILTER_REGEX = "^(.(?!\\." + OUT_EXTENSION_PREFIX + "))+\\.(html?|HTML?)";
    
    private LinkedList<int[]> listOfDeletionIndexes;
    
    private File[] selectedFiles;
    
    /*-------------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/ 
    private RemoveX() {
        
        super("RemoveX");//Chama construtor da super classe
        
        northPanel = new JPanel();
        centerPanel = new JPanel();
        southPanel = new JPanel();
        
        northPanel.setLayout(new FlowLayout());  
        
        southPanel.setLayout(new FlowLayout());
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(java.awt.BorderLayout.NORTH, northPanel);
        getContentPane().add(java.awt.BorderLayout.CENTER, centerPanel);
        getContentPane().add(java.awt.BorderLayout.SOUTH, southPanel);
        
        jTextArea = new JTextArea("Selecione a pasta com arquivos HTML e clique em \"Processar\".");
        
        northPanel.add(jTextArea);
        
        jFileChooser = new JFileChooser(".");
        
        //Cria filtro pra aceitar apenas selecao de certos tipos de arquivos
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setApproveButtonText("Processar");
        jFileChooser.addChoosableFileFilter(new ZapFileFilter()); 
        
        centerPanel.add(jFileChooser);
      
        tag = "div";
        attr = "class";
        value = "_2Ts6i _3RGKj";
        
        tagLabel = new JLabel("Tag:");
        tagField = new JTextField(tag, 9);
        attrLabel = new JLabel("Atributo:");
        attrField = new JTextField(attr, 9);
        valueLabel = new JLabel("Valor:");
        valueField = new JTextField(value, 9);
        
        southPanel.add(tagLabel);
        southPanel.add(tagField);
        southPanel.add(attrLabel);
        southPanel.add(attrField);
        southPanel.add(valueLabel);
        southPanel.add(valueField);
        
        selectedFiles = null;
        
        setSize(550, 450);
        
        setLocationRelativeTo(null);//Abre a janela no centro da tela
        
        //Encerra aplicacao se fechar janela principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        setVisible(true);
        
    }//construtor
    
    /*-------------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/     
    private boolean fileFilter(final String filename) {
        
        return filename.matches(FILE_FILTER_REGEX);
        
    }//fileFilter
    
    /*-------------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/
    private void parseError(final XMLParseException e, final String filename) {
        
        //Exibe janela com mensagem de erro
        JOptionPane.showMessageDialog(
            null,
            e.getMessage(), 
            "Erro de sintaxe no arquivo " + filename,
            JOptionPane.ERROR_MESSAGE
        );
        
        e.printStackTrace();        
    }//parseError
    
    /*-------------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/     
    private void abort(final Exception e) {
        
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
    
    /*-------------------------------------------------------------------------
    
    --------------------------------------------------------------------------*/ 
    private void remove() {
        
        selectedFiles = 
            jFileChooser.getSelectedFile().listFiles(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename){ return fileFilter(filename); }
                }
            );//Selecionar pasta
 
        //Mas se o array estiver nulo, aborta o programa
        if (selectedFiles == null) abort(new Exception("Nenhum arquivo selecionado!"));
        
        JFrame jFrame = new JFrame("Processando arquivos...");
        
        JProgressBar jProgressBar = new JProgressBar();
        
        jProgressBar.setStringPainted(true);//Permite escrever na barra de prog.  
        
        jProgressBar.setMinimum(0);//Irah de 0 ate num. de arqs. selecionados
        
        jFrame.add(jProgressBar);
        
        jFrame.setSize(450,150);
        
        jFrame.setLocationRelativeTo(null);//Abre a janela no centro da tela
        
        jFrame.setVisible(true);
         
        //Valor max. da barra de progresso é o número de arquivos que serão processados
        jProgressBar.setMaximum(selectedFiles.length);
       
        jProgressBar.setValue(0);//Inicializa barra de progresso em 0%
     
        int indexFile = 0;//Contador de arquivos processados
        
        for (File file : selectedFiles) { //Loop for processa cada aquivo de entrada

            jProgressBar.setString("Processando " + file.getName());
            
            try {
                
                TextFileHandler textFileHandler = new TextFileHandler(file.getAbsolutePath(), htmlCharset);
                
                textFileHandler.read();
                
                String content = textFileHandler.getContent();
                
                StringBuilder sb = new StringBuilder(content);
                
                listOfDeletionIndexes = new LinkedList<>();
                
                toolbox.xml.HtmlParser htmlParser = new toolbox.xml.HtmlParser(content, new Parser());
                
                htmlParser.parse();

                if (!listOfDeletionIndexes.isEmpty()) {
                    
                    while (!listOfDeletionIndexes.isEmpty()) {
                        
                        int[] deletionIndexes = listOfDeletionIndexes.pop();
                        sb.delete(deletionIndexes[0], deletionIndexes[1]);
                    }                    
                    
                    textFileHandler.setContent(sb.toString());
                        
                    textFileHandler.writeWithExtPrefix(OUT_EXTENSION_PREFIX);
                } 
            } catch (XMLParseException e) {
                
                parseError(e, file.getName());
                
            } catch (Exception e) {
                
                abort(e);//Aborta execução
                
            }
  
            //Incrementa contador de arqs. processados e atualiza barra de progresso
            jProgressBar.setValue(++indexFile);
       
        }//Fim do loop for (Todos os arqs. de entrada foram editados)
        
        jProgressBar.setString("Feito!");//Mensagem final na barra de progresso            
    }//remove
    
    private void selectFolder(final String[] args) {
        
        //Le charset dos arquivos na linha de comando. UTF-8 como default.
        htmlCharset = Commons.parseCharset(args, "utf8"); 
        jFileChooser.addActionListener(new ActionHandler());

        jFileChooser.setDialogTitle("Selecione a pasta com arquivos HTML");
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);        
    }//selectFolder

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
     * @param args Pode ser passado o charset dos arquivos. Nenhum parametro : charset utf8.
     */
    public static void main(String[] args) {
        
        RemoveX removeX = new RemoveX(); 
        
        removeX.selectFolder(args);

    }//main()
    
 /*===========================================================================
 *                          Classes privadas
 ===========================================================================*/ 
private class ZapFileFilter extends FileFilter {
    
    @Override
    public boolean accept(File file){

        if (file.isDirectory()) return true;

        return fileFilter(file.getName());
        
    }//accept()

    @Override
    public String getDescription(){
        
        return "Arquivos HTML";
        
    }//getDescription()
    
}//classe privada ZapFileFilter 


private class Parser extends toolbox.xml.TagParser {
         
    @Override
    public void openTag(final toolbox.xml.Tag t) {
        
        if (t.getTagName().equals(tag)) {
            
            HashMap<String, String> map = t.getAttrMap();
            String v = map.get(attr);
            if (value.equals(v)) t.notifyClosing();
            
        }//openTag
        
    }
    
    @Override
    public void closeTag(final toolbox.xml.Tag t) {
        
        int[] deletionIndex = new int[2];
        
        deletionIndex[0] = t.getOpenTagIndex();
        deletionIndex[1] = t.getEndBlockIndex();  
        
        listOfDeletionIndexes.push(deletionIndex);
              
    }//closeTag
    
}//classe privada Parser 


private class ActionHandler implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent action) {
        
        setVisible(false);
        dispose();
         
        if (action.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) System.exit(0);

        if (action.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
            tag = tagField.getText();
            attr = attrField.getText();
            value = valueField.getText();  
            remove();
        }
    }//actionPerformed

}//classe privada ActionHandler
    
}//classe RemoveX
