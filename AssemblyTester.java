import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class AssemblyTester {
    
    public static void main(String[] args) throws FileNotFoundException{
        File fileToRead = new File("AssemblyAdd.txt");
        String code = "";
        Scanner fileReader = new Scanner(fileToRead);
        while(fileReader.hasNextLine()){
            code = code.concat(fileReader.nextLine() + "\n");
        }
        fileReader.close();

        System.out.println("Before execution:\n");
        LMCAssemblyParser.getInstance().loadProgram(code);
        LMCAssemblyParser.getInstance().showLabels();
        LMCAssemblyParser.getInstance().showMemory(90, 100);
        LMCAssemblyParser.getInstance().fetchDecodeExecute(1);
        System.out.println("After execution:\n");
        LMCAssemblyParser.getInstance().showMemory(90, 100);
        LMCAssemblyParser.getInstance().showRegisters();
    }
}
