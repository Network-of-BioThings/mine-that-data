import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;


import java.io.*;
import java.util.Random;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import weka.core.SelectedTag;
import weka.core.converters.CSVLoader;
import weka.core.neighboursearch.BallTree;
import weka.core.neighboursearch.CoverTree;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.LinearNNSearch;

public class kNN {
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

        parser.addArgument("-kNN").type(Integer.class).help("The number of neighbours to use.").required(true);
        parser.addArgument("-distanceWeighting").type(String.class).help("Gets the distance weighting method used.").required(true);
        parser.addArgument("-meanSquared").type(Boolean.class).help("Whether the mean squared error is used rather than mean absolute error when doing cross-validation for regression problems.");
        parser.addArgument("-NNSearchAlgorithm").type(String.class).help("The nearest neighbour search algorithm to use").required(true);
        parser.addArgument("-windowSize").type(Integer.class).help("Gets the maximum number of instances allowed in the training pool. The addition of new instances above this value will result in old instances being removed. A value of 0 signifies no limit to the number of training instances.").required(true);

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

            IBk knn = new IBk();
            knn.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
            knn.setKNN((Integer)res.get("kNN"));
            knn.setCrossValidate(false);
            switch ((String)res.get("distanceWeighting")){
                case "1/distance": {
                    knn.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_INVERSE, IBk.TAGS_WEIGHTING));
                }
                case "1-distance": {
                    knn.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_SIMILARITY, IBk.TAGS_WEIGHTING));
                }
                case "none":{
                    knn.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
                }
            }

            switch ((String)res.get("NNSearchAlgorithm")){
                case "LinearNNSearch":{
                    knn.setNearestNeighbourSearchAlgorithm(new LinearNNSearch());
                }
                case "KDTree":{
                    knn.setNearestNeighbourSearchAlgorithm(new KDTree());
                }
                case "CoverTree":{
                    knn.setNearestNeighbourSearchAlgorithm(new CoverTree());
                }
                case "BallTree":{
                    knn.setNearestNeighbourSearchAlgorithm(new BallTree());
                }
            }
            knn.setMeanSquared((Boolean)res.get("meanSquared"));
            knn.setWindowSize((Integer)res.get("windowSize"));

            int classIndex = res.get("targetClassIndex");

            PrintWriter writer = new PrintWriter("Output.tsv", "UTF-8");
            writer.println("Analysis name\tRun\tNumber_correct\tNumber_incorrect\tAccuracy\troot_mean_squared_error\tmean_squared_error\tnum_true_pos\tnum_true_neg\tnum_false_pos\tnum_false_neg");
            for (int i=0; i<(Integer)res.get("numIterations"); i++){
                eval.crossValidateModel(knn, data, (Integer)res.get("numCVFolds"), new Random());
                double accuracy = (eval.correct()) / (eval.correct() + eval.incorrect());
                writer.println(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
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
                        eval.numFalseNegatives(classIndex)));
            }
            writer.close();
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
