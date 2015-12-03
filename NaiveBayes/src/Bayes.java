import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import weka.classifiers.Evaluation;

import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

public class Bayes {
    public static void main(String[] args) throws Exception {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Decision tree runner");

        parser.addArgument("-useKernelEstimator").type(Boolean.class).help("Use a kernel estimator for numeric attributes rather than a normal distribution.").required(true);
        parser.addArgument("-useSupervisedDiscretization").type(String.class).help("Use supervised discretization to convert numeric attributes to nominal ones.").required(true);

        parser.addArgument("-analysisName").type(String.class).help("The name of the analysis").required(true);
        parser.addArgument("-numIterations").type(Integer.class).help("Number of iterations to perform").required(true);
        parser.addArgument("-numCVFolds").type(Integer.class).help("Number of folds to use in cross validation").required(true);

        parser.addArgument("-targetClassIndex").type(Integer.class).help("The class used for FPR, TPR, ...").setDefault(1);
        parser.addArgument("-dataFile").type(String.class).help("The file to use");

        try {
            Namespace res = parser.parseArgs(args);
            CSVLoader loader = new CSVLoader();
            File df = new File((String)res.get("dataFile"));
            if (df.exists())
            {
                loader.setSource(df);
            }
            else{
                throw new Exception("File does not exist");
            }
            Instances data = loader.getDataSet();

            data.setClassIndex(data.numAttributes() - 1);
            Evaluation eval = new Evaluation(data);

            NaiveBayes bayes = new NaiveBayes();
            bayes.setUseKernelEstimator((Boolean)res.get("useKernelEstimator"));
            bayes.setUseSupervisedDiscretization((Boolean)res.get("useSupervisedDiscretization"));

            int classIndex = res.get("targetClassIndex");

            PrintWriter writer = new PrintWriter("Output.tsv", "UTF-8");
            writer.println("Analysis name\tRun\tNumber_correct\tNumber_incorrect\tAccuracy\troot_mean_squared_error\tmean_squared_error\tnum_true_pos\tnum_true_neg\tnum_false_pos\tnum_false_neg\tAUC");            for (int i=0; i<(Integer)res.get("numIterations"); i++){
                eval.crossValidateModel(bayes, data, (Integer)res.get("numCVFolds"), new Random());
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
