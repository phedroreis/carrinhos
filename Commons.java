import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import toolbox.file.SearchFolder;

/**
 * Classe para metodos estaticos que podem ser utilzados pelas 
 * aplicacoes do pacote.
 * 
 * 
 * @author Pedro Reis 
 * @version 1.0
 * @since 1.0 - 21  de janeiro de 2024
 */
final class Commons {
    
    private static final String CHARSET_MSG = 
"""


Tente um dos encodings abaixo:

iso-8859-1
us-ascii
utf16
utf_16be
utf_16le
utf8
""";

    /**
     * Define o charset do programa com o qual serao lidas entradas
     * do terminal e com o qual serao codificadas as saidas para o terminal.
     * 
     * <p>Se nao for especificado nenhum charset pela linha de comando sera
     * utilizado o charset default do sistema.</p>
     * 
     * @param args O array com os parametros digitados na linha de comando.
     * 
     * @return O charset selecionado.
     */
    /*-------------------------------------------------------------------------
           
    --------------------------------------------------------------------------*/   
    static String parseCharset(final String[] args) {
        
        String charset = Charset.defaultCharset().toString();
        
        if (args.length > 0) {
            
            try {
                
                if (Charset.isSupported(args[0])) 
                    charset = args[0]; 
                else 
                    System.out.println('\n' + args[0] + " -> charset inexistente!" + CHARSET_MSG);
            }
            catch (IllegalCharsetNameException e) {
                
                System.out.println('\n' + args[0] + " -> charset ilegal!" + CHARSET_MSG);
            }
        } 
        
        return charset;
        
    }//parseCharset
    
    /**
     * Retorna uma lista com todos os arquivos HTML encontrados do diretorio de pesquisa. 
     * 
     * @param searchRool O diretorio por onde iniciar a pesquisa.
     * 
     * @param searchSubdirs Se <code>true</code>, a pesquisa se estende aos subdiretorios.
     * 
     * @return Um array com os pathnames dos arquivos localizados.
     */
    /*-------------------------------------------------------------------------
                                     
    --------------------------------------------------------------------------*/    
    static String[] searchForHtmlFiles(final String searchRoot, final boolean searchSubdirs) {
        
        String[] filenames =
            new SearchFolder(searchRoot).getPathnamesList(".+\\.(HTML?|html?)", searchSubdirs);
            
        if (filenames.length == 0) {
            System.out.println("\nNenhum arquivo HTML encontrado!");
            System.exit(0);
        }
        
        return filenames;
        
    }//searchForHtmlFiles

}//classe Commons
