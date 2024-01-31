import java.io.IOException;
import toolbox.terminal.ProgressBar;
import toolbox.textfile.TextFileHandler;
import toolbox.textfile.TextFileEditor;
import toolbox.file.SearchFolder;

public class ChangeCharset {
    
    private static final String ROOT_FOLDER = ".";
    
    private static final String HTML_REGEX = ".+?\\.(HTML?|html?)$";
    
    private static final String CHARSET_REGEX = "charset=\"?windows-1252\"";    

    public static void main(String[] args) throws IOException {
        
        String[] pathnames = 
            new SearchFolder(ROOT_FOLDER).getPathnamesList(HTML_REGEX, true);
        
        ProgressBar progressBar = new ProgressBar(pathnames.length, 50);
        
        progressBar.showBar();
        
        for (String pathname : pathnames) {
            
            TextFileHandler textFileHandler = new TextFileHandler(pathname);
 
            textFileHandler.read();
            
            textFileHandler.edit(CHARSET_REGEX, null, new CharsetEditor());
         
            textFileHandler.writeWithExtPrefix("editado");
            
            progressBar.increment();
            
        }//for 
        
        System.out.println("");
         
    }//main
    
/*==========================================================================
                             Classe interna
==========================================================================*/
private static class CharsetEditor extends TextFileEditor{

    @Override
    public String edit(final String match) {
        
        if (match.equals("charset=\"windows-1252\""))
            return "charset=\"UTF-8\"";
        else
            return "charset=UTF-8\"";
        
    }//edit
    
}//classe CharsetEditor  

/*=========================================================================*/
    
}//classe ChangeCharset

