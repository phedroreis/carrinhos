import java.util.regex.PatternSyntaxException;

/**
 */
public final class Crf {
    
    public static void main(String[] args) throws java.io.IOException, PatternSyntaxException {
        
        if (args.length == 0) {            
            System.out.println("Use java Crf <nomeDoArquivoJSON>");
            System.exit(0);
        }
        
        toolbox.textfile.TextFileHandler textFileHandler = new toolbox.textfile.TextFileHandler(args[0]);
        
        textFileHandler.read();
        
        String jsonFileContent = textFileHandler.getContent();
        
        String titleStr;
        String crfStr;
        
        toolbox.regex.Regex title = new toolbox.regex.Regex("\"@ref\": \"(.+)?\",");
        
        title.setTarget(jsonFileContent);
    
        toolbox.regex.Regex crf = new toolbox.regex.Regex("/ crf=.+? /");
        
        crf.setTarget(jsonFileContent);
        
        
        while ((titleStr = title.find()) != null) {
            
            titleStr = title.group(1);
            
            crfStr = crf.find();
            
            if (crfStr == null) crfStr = "/ CRF inexistente! /";
            
            System.out.printf("%s - %s%n", titleStr, crfStr);
            
        }
    }//main

}//classe Crf
