import toolbox.terminal.InputParser;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * Classe para a validacao de entrada de charset.
 * 
 * @author Pedro Reis
 * @version 1.0 29 de fevereiro de 2024
 */
public class ParseCharset extends InputParser {
    
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
