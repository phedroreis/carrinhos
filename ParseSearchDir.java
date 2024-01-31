import java.io.File;
import toolbox.terminal.InputParser;
 
final class ParseSearchDir extends InputParser{
    
    @Override
    public String parse(final String input) 
        throws IllegalArgumentException {

        File dir = new File(input);
        
        if (dir.exists() && dir.canRead() && dir.canWrite())
            return input;
        else 
            throw new IllegalArgumentException(
                "Diret\u00f3rio inexistente ou acesso negado!"
            );
            
    }//parse
    
}//classe ParseSearchDir