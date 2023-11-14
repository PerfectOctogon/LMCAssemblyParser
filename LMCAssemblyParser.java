import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

final class LMCAssemblyParser{

    //We have 9 quick-access registers
    static int[] registers;

    //Which register are we treating as the accumulator
    private static int accumulatorIndex = 8;

    //We have a huge array of memory and their index indicates their memory address
    static String[] mainMemory;

    //OPCODES for some basic ASM to load into memory. Contains only the LDA, MOV, STA, INP, ADD, OUT and HLT instructions for now
    static HashMap<String, String> asmToOPCODE;

    //Registers (index values) and their names
    static HashMap<String, Integer> registerNames;

    //Our singleton instance of LMCAssemblyParser. There can only be one at a time!!!
    private static LMCAssemblyParser lmcAssemblyParser;

    //Keeps track of any labels in the program and stores their line number
    private static HashMap<String, Integer> labels;

    //Checks if the HLT instruction is called and stops the fetch-decode-execute cycle
    private boolean programEnded;

    //Keeps track of the number of executions
    private int count = 0;
    
    //Singleton of LMCAssemblyParser
    private LMCAssemblyParser(){
        registers = new int[9];
        mainMemory = new String[1000];
        programEnded = false;
        asmToOPCODE = new HashMap<String, String>();
        //Initializing hashmap with opcodes and their values
        asmToOPCODE.put("LDA", "F");
        asmToOPCODE.put("MOV", "28");
        asmToOPCODE.put("STA", "E");
        asmToOPCODE.put("INP", "760");
        asmToOPCODE.put("ADD", "60");
        asmToOPCODE.put("SUB", "18");
        asmToOPCODE.put("OUT", "718");
        asmToOPCODE.put("HLT", "0000");
        asmToOPCODE.put("BRA", "80");
        asmToOPCODE.put("BRP", "90");
        asmToOPCODE.put("LDI", "D");

        registerNames = new HashMap<String, Integer>();
        //Initializing the hashmap with registers and their register names first register is not used
        registerNames.put("EAX", accumulatorIndex);
        registerNames.put("EBX", 7);
        registerNames.put("ECX", 6);
        registerNames.put("EDX", 5);
        System.out.println();
        registerNames.put("AX", 4);
        registerNames.put("BX", 3);
        registerNames.put("CX", 2);
        registerNames.put("DX", 1);

        labels = new HashMap<String, Integer>();

        //Initializing the main memory to contain zeroes instead of nulls because a memory value cannot be null
        Arrays.fill(mainMemory, "0000");
    }

    //Singleton instance getter
    public static LMCAssemblyParser getInstance(){
        if(lmcAssemblyParser == null){lmcAssemblyParser = new LMCAssemblyParser();}
        return lmcAssemblyParser;
    }

    //The program counter (PC)
    private static int programCounter = 0;

    //Fetch next instruction, EXECUTION_CODE tells the program how many fetch decode execute cycles we go through
    public void fetchDecodeExecute(int EXECUTION_CODE){
        if(programEnded){
            System.out.println("Program ended");
            return;
        }
        switch (EXECUTION_CODE) {
            //Execute one cycle
            case 0:
                instructionRegister(fetch());
                break;
        
            default:
                while(!programEnded)
                instructionRegister(fetch());
                System.out.println("Program ended");
                break;
        }
        
    }

    //Fetch the instruction from the memory address
    private String fetch(){
        return mainMemory[programCounter];
    }

    //Decodes our instruction (And executes it, even though the instruction register doesn't do that!)
    private void instructionRegister(String instruction){
        Scanner s1 = new Scanner(instruction);
        String OPCODE = s1.next();
        String OPERAND;

        switch (OPCODE) {

            //LDA operation takes the value at the index indicated by the OPERAND and puts it in the ACC
            case "F":
                OPERAND = s1.next();
                registers[accumulatorIndex] = Integer.parseInt(mainMemory[Integer.parseInt(OPERAND, 16)], 16);
                break;
            
            //MOV instruction takes the value from the source address/register and puts it in the destination address/register
            case "28":
                OPERAND = s1.next();
                String destination = s1.next();
                //We know that the first operand is not a register if this is null (It's a memory address)
                if(registerNames.get(OPERAND) == null){
                    //If this is null, the destination is not a register, but a memory address
                    if(registerNames.get(destination) == null){
                        mainMemory[Integer.parseInt(destination)] = mainMemory[Integer.parseInt(OPERAND)];
                    }
                    //It is a register
                    else{
                        registers[registerNames.get(destination)] = Integer.parseInt(mainMemory[Integer.parseInt(OPERAND)]);
                    }
                }
                else{
                    if(registerNames.get(destination) == null){
                        mainMemory[Integer.parseInt(destination)] = Integer.toString(registers[Integer.parseInt(OPERAND)]);
                    }
                    //It is a register
                    else{
                        registers[registerNames.get(destination)] = registers[Integer.parseInt(OPERAND)];
                    }
                }
                break;
            
            //STA instruction takes the value in the ACC and puts it in the main memory indicated by the OPERAND
            case "E":
                OPERAND = s1.next();
                mainMemory[Integer.parseInt(OPERAND, 16)] = Integer.toHexString(registers[accumulatorIndex]);
                break;
            
            //INP instruction takes the input from the user and puts it in the accumulator
            case "760":
                Scanner s2 = new Scanner(System.in);
                System.out.print("INPUT REQUIRED: ");
                int input = s2.nextInt();
                registers[accumulatorIndex] = input;
                break;
            
            //ADD 
            case "60":
                OPERAND = s1.next();
                int x = Integer.parseInt(mainMemory[Integer.parseInt(OPERAND, 16)], 16);
                registers[accumulatorIndex] = arithmeticLogicUnit(0, registers[accumulatorIndex], x);
                break;
            
            case "18":
                OPERAND = s1.next();
                int y = Integer.parseInt(mainMemory[Integer.parseInt(OPERAND, 16)], 16);
                registers[accumulatorIndex] = arithmeticLogicUnit(1, registers[accumulatorIndex], y);
                break;
            
            //OUTPUT the value of the accumulator
            case "718":
                System.out.println("OUTPUT: " + registers[accumulatorIndex]);
                break;

            case "0000":
                //Halt the program
                programEnded = true;
                break;

            //Jump back to the line specified by the Label ALWAYS
            case "80":
                OPERAND = s1.next();
                programCounter = labels.get(OPERAND);
                //We are returning here so that we don't modify program counter later on in the program
                return;
            
            //Jump back to the line specified by the Label if the accumulator is greater than or equal to zero
            case "90":
                OPERAND = s1.next();
                if(registers[accumulatorIndex] >= 0){programCounter = labels.get(OPERAND); return;}
                break;

            //Immediate addressing, we want to load the immediate value of the OPERAND into the accumulator
            case "D":
                OPERAND = s1.next();
                registers[accumulatorIndex] = Integer.parseInt(OPERAND, 16);
                break;

            default:
                break;
        }
        s1.close();
        //Increment program counter
        count++;
        programCounter =  arithmeticLogicUnit(0, programCounter, 1);
    }

    //Arithmetic logic unit
    public int arithmeticLogicUnit(int OPCODE, int x, int y){
        int answer = 0;
        switch (OPCODE) {
            //ADD
            case 0:
                answer = x + y;
                break;
            //SUB
            case 1:
                answer = x - y;
                break;
            //MUL
            case 2:
                answer = x * y;
                break;
            //DIV
            case 3:
                answer = x / y;
                break;
            //CANT BE ANYTHING ELSE!
            default:
                answer = 0;
                break;
        }
        return answer;
    }

    //Load a program into memory
    public void loadProgram(String program){
        Scanner s1 = new Scanner(program);
        int iterator = 0;
        while(s1.hasNextLine()){
            String instruction = s1.nextLine();
            Scanner s2 = new Scanner(instruction);
            String OPCODE = s2.next();
            String code = "";

            //Probably a label
            if(asmToOPCODE.get(OPCODE) == null){
                //Store this in our label hashmap
                labels.put(OPCODE, iterator);
                String next = asmToOPCODE.get(s2.next()) + " ";
                //We need to read one more time to get the instruction OPCODE into the register
                code = code.concat(next);
            }
            //If not a label, this IS an opcode
            else{code = code.concat(asmToOPCODE.get(OPCODE)) + " ";}

            while(s2.hasNext()){
                //We have an integer OPERAND, so we convert it to hex and put it along OPCODE
                if(s2.hasNextInt()){
                    code = code.concat(Integer.toHexString(s2.nextInt())) + " ";
                }
                //If we have the name of a register as our OPERAND, we have to store it in a string
                else if(s2.hasNext()){
                    code = code.concat(s2.next()) + " ";
                }
            }
            //Store the instruction into mainMemory
            mainMemory[iterator] = code;
            iterator++;
            s2.close();
        }
        s1.close();
    }

    //Show the contents of the memory in the specified index range
    public void showMemory(int startingPoint, int endingPoint){
        for(int i = startingPoint; i < endingPoint; i++){
            System.out.printf("Memory cell #%d : %s\n", i, mainMemory[i]);
        }
    }

    //Show the contents of the memory from start to a specified range
    public void showMemory(int range){
        for(int i = 0; i < range; i++){
            System.out.printf("Memory cell #%d : %s\n", i, mainMemory[i]);
        }
    }

    //Show the contents of all the memory cells in the main memory
    public void showMemory(){
        for(int i = 0; i < mainMemory.length; i++){
            System.out.printf("Memory cell #%d : %s\n", i, mainMemory[i]);
        }
    }

    //Show the contents of all the registers
    public void showRegisters(){
        for(int i = 0; i < registers.length; i++){
            System.out.printf("Register %d: %s\n", i, registers[i]);
        }
    }

    //Show all the labels and their corresponding lines
    public void showLabels(){
        for (Map.Entry<String, Integer> entry : labels.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.printf("Label: %s, Line: %d\n", key, value);
        }
    }

    public int getTotalExecutions(){
        return count;
    }
}