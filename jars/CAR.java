import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;

import weka.core.Instances;
import java.io.File;
import java.io.*;
import java.util.Random;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import weka.classifiers.rules.*;
import weka.associations.CARuleMiner;
import weka.classifiers.rules.car.utils.*;
import weka.classifiers.rules.car.WeightedClassifier;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CAR {

    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("CAR runner");
        
        parser.addArgument("-support").type(Integer.class).help("Minimum support").required(true);
        parser.addArgument("-confidence").type(Integer.class).help("confidence").required(true);
        parser.addArgument("-numofRules").type(Integer.class).help("Number of rules").required(true);
        parser.addArgument("-numIterations").type(Integer.class).help("Number of iterations to perform").required(true);
        parser.addArgument("-numCVFolds").type(Integer.class).help("Number of folds to use in cross validation").required(true);

        parser.addArgument("-targetClassIndex").type(Integer.class).help("The class used for FPR, TPR, ...").setDefault(1);
        parser.addArgument("-dataFile").type(String.class).help("The file to use");

        
        
     // REMOVE THIS:
//      BufferedReader datafile = readDataFile((String)res.get("dataFile"));
//      Instances data = new Instances(datafile);

      // REPLACE WITH THIS:
      
        try {
            Namespace res = parser.parseArgs(args);
            CSVLoader loader = new CSVLoader();
            File df = new File((String)res.get("dataFile"));
            if (df.exists())
            {
                loader.setSource(df);
            }
            Instances data = loader.getDataSet();
            
            data.setClassIndex(data.numAttributes() - 1);
            Evaluation eval = new Evaluation(data);

            
           
            WeightedClassifier carruleminer = new WeightedClassifier();
            

            int classIndex = res.get("targetClassIndex");

            PrintWriter writer = new PrintWriter("Output.tsv", "UTF-8");
            writer.println("Run\tNumber_correct\tNumber_incorrect\tAccuracy\troot_mean_squared_error\tmean_squared_error\tnum_true_pos\tnum_true_neg\tnum_false_pos\tnum_false_neg");
            for (int i=0; i<(Integer)res.get("numIterations"); i++){
                eval.crossValidateModel(carruleminer, data, (Integer)res.get("numCVFolds"), new Random());
                double accuracy = (eval.correct()) / (eval.correct() + eval.incorrect());
                writer.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", i,
                        eval.correct(),
                        eval.incorrect(),
                        accuracy,
                        eval.rootMeanSquaredError(),
                        eval.rootMeanSquaredError(),
                        eval.numTruePositives(classIndex),
                        eval.numTrueNegatives(classIndex),
                        eval.numFalsePositives(classIndex),
                        eval.numFalseNegatives(classIndex)));
            }
            writer.close();
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
