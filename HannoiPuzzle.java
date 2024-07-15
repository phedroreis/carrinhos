public final class HannoiPuzzle {
    
    private final HannoiModel hannoiModel;//Objeto que modela o estado da torre de hannoi
    
    private final Display display;//Objeto que exibe snapshots da torre
    
    /*----------------------------------------------------------
     *                 Construtor da classe
     *---------------------------------------------------------*/
    private HannoiPuzzle(final int discs) {
        
        hannoiModel = new HannoiModel(discs);
        
        display = new Display(hannoiModel);
        
    }//construtor
    
    /*-------------------------------------------------------
     *   Inicia o metodo recursivo para solucionar o puzzle.
     *------------------------------------------------------*/
    private void solve() {

        display.showHannoiInitialState();
        
        solveInstance(hannoiModel.getNumberOfDiscs(), 0, 2);
        
    }//solve
    
    /*----------------------------------------------------------
     *            Resolve recursivamente o puzzle
     *---------------------------------------------------------*/
    private void solveInstance(final int discs, final int source, final int target) {
        
        if (discs == 1) {
        
            hannoiModel.move(source, target); display.showHannoiState();
        }    
        else {
            
            int aux = 3 - source - target;
            
            solveInstance(discs - 1, source, aux);
            
            hannoiModel.move(source, target); display.showHannoiState();
            
            solveInstance(discs - 1, aux, target);
        }
        
    }//solveInstance
    
    /**
     * O programa demonstra o passos necessarios para resolver um puzzle torre de Hannoi de n discos.
     * 
     * @param args Na linha de comando pode ser passado o num. de discos do puzzle. Default = 5.
     */
    public static void main(String[] args) {
        
        int numberOfDiscs;
        
        try {
            
            numberOfDiscs = Integer.valueOf(args[0]);
 
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            
            numberOfDiscs = 5;
        }  
        
        if (numberOfDiscs < 1) throw new IllegalArgumentException("A torre deve ter pelo menos um disco.");
        
        HannoiPuzzle hannoiPuzzle = new HannoiPuzzle(numberOfDiscs);
        
        hannoiPuzzle.solve();
        
    }//main
    
    /*==================================================================================================================
     *                                                CLASSES INTERNAS 
     *=================================================================================================================*/
    
    /*-----------------------------------------------------------------------------------------------------------------
     *          REPRESENTA O CONJUNTO DE PINOS E DISCOS E IMPLEMENTA OS METODOS PARA MOVER OS DISCOS
     *----------------------------------------------------------------------------------------------------------------*/ 
    private class HannoiModel {
        
         private int[][] discsArray;//Array armazenando posicao de cada disco nos pinos
        
         private int[] stackPointersArray;//Ponteiros indicando o topo do empilhamento de cada pino
        
         private int numberOfDiscs;//Num. de discos na torre
        
         private int countMoves;//Conta os movimentos realizados
         
         private static final int NO_DISC = 0;//Indica que uma posicao no pino nao possui disco
         
         private static final int EMPTY_PIN = -1;//Valor do ponteiro de pilha para um pino vazio
     
         /**
         * Construtor da classe.
         * 
         * @param discs O numero de discos do puzzle.
         */
         public HannoiModel(final int numberOfDiscs) {
            
             this.numberOfDiscs = numberOfDiscs;
           
             discsArray = new int[3][numberOfDiscs];
            
             stackPointersArray = new int[3];
             
             for (int i = numberOfDiscs - 1; i >= 0; i--) {

                 discsArray[0][i] = numberOfDiscs - i;//Valor que representa o diametro do disco
                 discsArray[1][i] = NO_DISC;//Inicializa pino 1 sem discos
                 discsArray[2][i] = NO_DISC;//Inicializa pino 2 sem discos
            
             }
            
             /*Inicializa os ponteiros de pilha de cada pino*/             
             stackPointersArray[0] = numberOfDiscs - 1;//Todos os discos inicialmente empilhados no pino 0
             stackPointersArray[1] = EMPTY_PIN;//Pino 1 inicialmente vazio
             stackPointersArray[2] = EMPTY_PIN;//Pino 2 inicialmente vazio 
            
             countMoves = 0;
            
         }//construtor
        
         /**
         * Retorna o disco (int que indica o diametro do disco) no pino 'pin' e empilhado na posicao 'position'.
         * 
         * @param pin O pino.
         * 
         * @param position A posicao do disco no empilhamento.
         * 
         * @return O diametro do disco.
         */
         public int getDiscDiameter(final int pin, final int position) {
            
             return discsArray[pin][position];
            
         }//getDiscDiameter
        
         /**
         * Retorna o numero de discos na torre.
         * 
         * @return O num. de discos.
         */
         public int getNumberOfDiscs() {
            
             return numberOfDiscs;
            
         }//getNumberOfDiscs
        
         /**
         * Retorna o num. de movimento ja realizados para resolucao do puzzle.
         * 
         * @return Num. de movimentos feitos.
         */
         public int getNumberOfMovesPerformed() {
            
             return countMoves;
            
         }//getNumberOfMovesPerformed 
        
         /*----------------------------------------------------------
         * Empilha disco de diametro 'discDiameter' no pino 'pin'
         *---------------------------------------------------------*/
         private void push(final int pin, final int discDiameter) {
            
             discsArray[pin][++stackPointersArray[pin]] = discDiameter;            
            
         }//push
        
         /*----------------------------------------------------------
         *        Desempilha o disco no topo do pino 'pin'
         *---------------------------------------------------------*/
         private int pop(final int pin) {
            
             int discDiameter = discsArray[pin][stackPointersArray[pin]];
             
             discsArray[pin][stackPointersArray[pin]--] = NO_DISC;
             
             return discDiameter;//Retorna o diametro do disco retirado
            
         }//pop 
        
         /**
          * Move um disco de um pino para outro.
          * 
          * @param source Do pino de origem...
          * 
          * @param target Para o pino de destino...
          */
         public void move(final int source, final int target) {
            
             push(target, pop(source));
            
             countMoves++;
           
         }//move  
        
    }//classe interna HannoiModel

    /*-----------------------------------------------------------------------------------------------------------------
     *                                  EXIBE SNAPSHOTS DO PUZZLE NO TERMINAL
     *----------------------------------------------------------------------------------------------------------------*/      
    private class Display {
       
        private final HannoiModel hannoiModel;//Objeto que representa o estado do conjunto.
        
        private final int pinHolderWidth;//Espacamento da posicao de cada pino
        
        private final String snapshotSeparator;//Regua separadora de snapshots 
        
        private final int numberOfDiscs;//Num. de discos no conjunto
        
        /*----------------------------------------------------------
        *              Desenha um snapshot no terminal
        *---------------------------------------------------------*/
        private void showHannoiState() {
            
            for (int discPosition = numberOfDiscs - 1; discPosition > -1; discPosition--) {
                
                System.out.print('|');
                
                for (int pin = 0; pin < 3; pin++) {
                    
                    int disc = hannoiModel.getDiscDiameter(pin, discPosition);
                    
                    int discLength = disc > 0 ? disc * 2 - 1 : 0;
                    
                    String discString = discLength > 0 ? toolbox.string.StringTools.repeatChar('=', discLength) : " ";

                    int blankLength = (pinHolderWidth - discLength) / 2;
                    
                    String blankString = toolbox.string.StringTools.repeatChar(' ', blankLength);
                    
                    System.out.print(blankString + discString + blankString + '|');                 
                    
                }
                
                System.out.println();
            }
            
            System.out.println(snapshotSeparator + " <-- " + hannoiModel.getNumberOfMovesPerformed());
            
        }//showHannoiState    
        
        /*----------------------------------------------------------
         *   Desenha snapshot da torre antes de mover 1o disco
         *---------------------------------------------------------*/        
        public void showHannoiInitialState() {
           
            System.out.println(snapshotSeparator);
           
            showHannoiState();
           
        }//showHannoiInitialState         
        
        /**
         * Construtor da classe.
         * 
         * @param discs O numero de discos do puzzle.
         */
        public Display(final HannoiModel hannoiModel) {
            
            this.hannoiModel = hannoiModel;
            
            numberOfDiscs = hannoiModel.getNumberOfDiscs();
            
            pinHolderWidth = numberOfDiscs * 2 + 1;
            
            String rule = toolbox.string.StringTools.repeatChar('-', pinHolderWidth) + '+';
            
            snapshotSeparator = '+' +  toolbox.string.StringTools.repeatString(rule, 3);

        }//construtor
       
    }//classe interna Display   
    
    /*==============================================================================================================================
    *
    ==============================================================================================================================*/

} //classe HannoiPuzzle