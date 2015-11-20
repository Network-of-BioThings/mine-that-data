import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import java.io.File;

import java.io.*;
import java.util.Random;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class DecisionTreeRunner {

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
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Decision tree runner");
        parser.addArgument("-binarySplits").type(Boolean.class).help("Allow only binary splits").required(true);
        parser.addArgument("-confidence").type(Float.class).help("Confidence factor").required(true);
        parser.addArgument("-minNumObj").type(Integer.class).help("Minimum number of instances per leaf").required(true);
        parser.addArgument("-numFolds").type(Integer.class).help("Determines the amount of data used for reduced-error pruning. One fold is used for pruning, the rest for growing the tree.").required(true);
        parser.addArgument("-reducedErrorPruning").type(Boolean.class).help("Whether reduced-error pruning is used instead of C.4.5 pruning.").required(true);
        parser.addArgument("-subTreeRaising").type(Boolean.class).help(" Whether to consider the subtree raising operation when pruning.").required(true);
        parser.addArgument("-unpruned").type(Boolean.class).help("Whether pruning is performed.").required(true);
        parser.addArgument("-useLaplace").type(Boolean.class).help("Whether counts at leaves are smoothed based on Laplace.").required(true);

        parser.addArgument("-analysisName").type(String.class).help("The name of the analysis").required(true);
        parser.addArgument("-numIterations").type(Integer.class).help("Number of iterations to perform").required(true);
        parser.addArgument("-numCVFolds").type(Integer.class).help("Number of folds to use in cross validation").required(true);

        parser.addArgument("-targetClassIndex").type(Integer.class).help("The class used for FPR, TPR, ...").setDefault(1);
        parser.addArgument("-dataFile").type(String.class).help("The file to use");

        try {
            Namespace res = parser.parseArgs(args);

            // REMOVE THIS:
//            BufferedReader datafile = readDataFile((String)res.get("dataFile"));
//            Instances data = new Instances(datafile);

            // REPLACE WITH THIS:
            CSVLoader loader = new CSVLoader();
            File df = new File((String)res.get("dataFile"));
            if (df.exists())
            {
                loader.setSource(df);
            }
            Instances data = loader.getDataSet();

            data.setClassIndex(data.numAttributes() - 1);
            Evaluation eval = new Evaluation(data);

            J48 j48 = new J48();

            j48.setBinarySplits((Boolean)res.get("binarySplits"));
            j48.setConfidenceFactor((Float)res.get("confidence"));
            j48.setMinNumObj((Integer)res.get("minNumObj"));
            j48.setNumFolds((Integer)res.get("numFolds"));
            j48.setReducedErrorPruning((Boolean)res.get("reducedErrorPruning"));
            j48.setSubtreeRaising((Boolean)res.get("subTreeRaising"));
            j48.setUnpruned((Boolean)res.get("unpruned"));

            int classIndex = res.get("targetClassIndex");

            PrintWriter writer = new PrintWriter("Output.tsv", "UTF-8");
            writer.println("Analysis name\tRun\tNumber_correct\tNumber_incorrect\tAccuracy\troot_mean_squared_error\tmean_squared_error\tnum_true_pos\tnum_true_neg\tnum_false_pos\tnum_false_neg\tAUC");
            for (int i=0; i<(Integer)res.get("numIterations"); i++){
                eval.crossValidateModel(j48, data, (Integer)res.get("numCVFolds"), new Random());
                double accuracy = (eval.correct()) / (eval.correct() + eval.incorrect());
                writer.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                        res.get("analysisName"),
                        i,
                        eval.correct(),
                        eval.incorrect(),
                        accuracy,
                        eval.rootMeanSquaredError(),
                        eval.rootMeanSquaredError(),
                        eval.numTruePositives(classIndex),
                        eval.numTrueNegatives(classIndex),
                        eval.numFalsePositives(classIndex),
                        eval.numFalseNegatives(classIndex),
                        eval.areaUnderROC(classIndex)));
            }
            writer.close();
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
