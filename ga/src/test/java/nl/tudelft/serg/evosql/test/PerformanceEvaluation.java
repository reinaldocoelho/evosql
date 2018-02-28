package nl.tudelft.serg.evosql.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.FileWriter;
import java.io.IOException;

public class PerformanceEvaluation {

    //
    //
    public static void main(String[] args) {


        JUnitCore core = new JUnitCore();

        core.run(AllTestsSuite.class); //WARM UP EXECUTION

        long[] results = new long[50];
        for (int i = 0; i < 50; i++) {
            long startTime = System.currentTimeMillis();
            Result result = core.run(AllTestsSuite.class);
            results[i] = System.currentTimeMillis() - startTime;
        }
        try {
            FileWriter writer = new FileWriter("testResults.txt");
            for (long result : results) {
                writer.write(result + " ");
            }
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }


    }
}
