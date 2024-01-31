import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

public final class TestClipboard implements Transferable {
    
    private static List<DataFlavor> htmlFlavors = new ArrayList<>(3);
    private String html;
    private String plainText;
    
    public static void main(String[] args) throws java.io.IOException, UnsupportedFlavorException {

        String plainText = "Um simples texto. O notepad escolhe colar este texto.";
        String htmlText = "<html><body><h1>O Word prefere colar este texto. Link para <a href=\"https://uol.com.br\">UOL</a></h1></body></html>";

        TestClipboard testClipboard = new TestClipboard(htmlText, plainText);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(testClipboard, null);
        
    }

    public TestClipboard(String html, String plainText) {
        htmlFlavors.add(DataFlavor.stringFlavor);
        htmlFlavors.add(DataFlavor.allHtmlFlavor);        
        this.html = html;
        this.plainText = plainText;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return htmlFlavors.contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {

        String toBeExported = plainText;
        if (flavor == DataFlavor.stringFlavor) {
            toBeExported = plainText;
        } else if (flavor == DataFlavor.allHtmlFlavor) {
            toBeExported = html;
        }

        if (String.class.equals(flavor.getRepresentationClass())) {
            return toBeExported;
        }
        throw new UnsupportedFlavorException(flavor);
    }
  
}
