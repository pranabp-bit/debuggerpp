package team57.debuggerpp.dbgcontroller;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DppJavaDebugProcessTest {

    public DppJavaDebugProcessTest() throws IOException {
    }

    @Test
    public void testCreate() {
        try {
            FileWriter myWriter = new FileWriter("sliceTest.log");
            myWriter.write(
                "team57.debuggerpp.Main:35\n" +
                    "team57.debuggerpp.Main:18\n" +
                    "team57.debuggerpp.Main:20\n" +
                    "team57.debuggerpp.Main:21\n" +
                    "team57.debuggerpp.Main:22\n" +
                    "team57.debuggerpp.Main:28\n" +
                    "team57.debuggerpp.Main:31\n" +
                    "team57.debuggerpp.Main:36\n");
            myWriter.close();
            //check slice.log
            File file1 = new File("sliceTest.log");
            // TODO: change this to programmable path
            Path generatedFile = Paths.get("C:\\Users\\robin\\Documents\\CPEN 491\\debuggerpp\\src\\test\\kotlin\\team57\\debuggerpp\\execute\\generatedFile\\slice.log");
            File file2 = new File(String.valueOf(generatedFile));
            Desktop.getDesktop().open(file1);
            Desktop.getDesktop().open(file2);
            boolean isTwoEqual = FileUtils.contentEquals(file1, file2);
            assertEquals(isTwoEqual, true);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void testRunToPosition() {

    }
}