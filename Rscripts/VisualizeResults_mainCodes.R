CleanDataResults <- function(DataResults,NrIterations,KfoldCV,Algorithms){
  Parameters <- c("IR_precision","IR_recall","Area_under_ROC","Mean_absolute_error","Root_mean_squared_error","Num_false_negatives","Num_false_positives","Num_true_positives","Num_true_negatives","Number_correct","Number_incorrect")
  DataResults <- DataResults[,Parameters]
  colnames(DataResults) <- c("Precision","Recall","AUC","MAE","MSE","FN","FP","TP","TN","Corrects","Incorrects")
  Results_perIterations <- matrix(0,nrow=(length(Algorithms)*NrIterations),ncol=ncol(DataResults))
  for (i in (1:(nrow(DataResults)/NrIterations))){
    meanresults <- colMeans(DataResults[((i-1)*KfoldCV+1):(i*KfoldCV),])
    Results_perIterations[i,] <- meanresults
  }
  colnames(Results_perIterations) <- colnames(DataResults)
  rownames(Results_perIterations) <- rep(Algorithms,each=NrIterations)
  Results_perIterations
}

CompareAlgorithms <- function(DataResults,Algorithms,Parameters,NrIterations){
  ### 1st plot : barplot algorithms for each parameter ###  ### only for numerical parameters
  DataResults <- DataResults[,Parameters]
  AveragedResults <- matrix(0,nrow=length(Algorithms),ncol=ncol(DataResults))
  for (i in (1:length(Algorithms))){
    meanresults <- colMeans(DataResults[((i-1)*NrIterations+1):(i*NrIterations),])
    AveragedResults[i,] <- meanresults
  }
  colnames(AveragedResults) <- colnames(DataResults)
  rownames(AveragedResults) <- Algorithms
  color <- heat.colors(length(Algorithms))
  # if you prefer, you can use rainbow
  par("mar")  # borders, see ?par
  par(mar=c(10.1, 4.1, 4.1, 2.1))
  par(xpd=TRUE)
  mp <- barplot(AveragedResults,ylab="",col=color,beside=TRUE)
  legend.text <- Algorithms
  #legend(1,-0.5, legend.text, fill=color)
  legend(mean(range(mp)), -0.5, legend.text, xjust = 0.5,fill=color,bty="n") 
}

CompareAlgorithms2 <- function(DataResults){
  ### 1st plot : barplot algorithms for each parameter ###  ### only for numerical parameters
  Algorithms <- unique(DataResults[,"Analysis.name"])
  NrIterations <- max(DataResults[,"Run"])
  Precision <- c()
  Recall <- c()
  for (i in (1:nrow(DataResults))){
    precision <- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_pos"])
    recall<- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_neg"])
    Precision <- c(Precision,precision)
    Recall <- c(Recall,recall)
  }
  DataResults <- cbind(DataResults,Precision=Precision,Recall=Recall)
  Parameters <- c("Accuracy","AUC","root_mean_squared_error","Precision","Recall")
  DataResults <- DataResults[,Parameters]
  AveragedResults <- matrix(0,nrow=length(Algorithms),ncol=ncol(DataResults))
  for (i in (1:length(Algorithms))){
    meanresults <- colMeans(DataResults[((i-1)*NrIterations+1):(i*NrIterations),])
    AveragedResults[i,] <- meanresults
  }
  colnames(AveragedResults) <- c("Accuracy","AUC","MSE","Precision","Recall")
  rownames(AveragedResults) <- Algorithms
  color <- heat.colors(length(Algorithms))
  # if you prefer, you can use rainbow
  par("mar")  # borders, see ?par
  par(mar=c(10.1, 4.1, 4.1, 2.1))
  par(xpd=TRUE)
  mp <- barplot(AveragedResults,ylab="",col=color,beside=TRUE)
  legend.text <- Algorithms
  #legend(1,-0.5, legend.text, fill=color)
  legend(mean(range(mp)), -0.5, legend.text, xjust = 0.5,fill=color,bty="n") 
}

CompareParameters <- function(DataResults){
  ### 2nd plot : barplots parameters for each method ###  ### only for numerical parameters
  Algorithms <- unique(DataResults[,"Analysis.name"])
  NrIterations <- max(DataResults[,"Run"])
  Precision <- c()
  Recall <- c()
  for (i in (1:nrow(DataResults))){
    precision <- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_pos"])
    recall<- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_neg"])
    Precision <- c(Precision,precision)
    Recall <- c(Recall,recall)
  }
  DataResults <- cbind(DataResults,Precision,Recall)
  Parameters <- c("Accuracy","AUC","root_mean_squared_error","Precision","Recall")
  
  DataResults <- DataResults[,Parameters]
  AveragedResults <- matrix(0,nrow=length(Algorithms),ncol=ncol(DataResults))
  for (i in (1:length(Algorithms))){
    meanresults <- colMeans(DataResults[((i-1)*NrIterations+1):(i*NrIterations),])
    AveragedResults[i,] <- meanresults
  }
  colnames(AveragedResults) <- c("Accuracy","AUC","MSE","Precision","Recall")
  rownames(AveragedResults) <- Algorithms
  color <- heat.colors(length(Parameters))
  # if you prefer, you can use rainbow
  par("mar")  # borders, see ?par
  par(mar=c(10.1, 4.1, 4.1, 2.1))
  par(xpd=TRUE)
  mp <- barplot(t(AveragedResults),ylab="",col=color,beside=TRUE)
  legend.text <- Parameters
  #legend(1,-0.5, legend.text, fill=color)
  legend(mean(range(mp)), -0.5, legend.text, xjust = 0.5,fill=color,bty="n") 
}

ParameterOverIterations <- function(DataResults){
  Algorithms <- unique(DataResults[,"Analysis.name"])
  NrIterations <- max(DataResults[,"Run"])

  DataResults <- DataResults[,c("Analysis.name","root_mean_squared_error")]
  DataResults <- data.frame(DataResults)
  color=heat.colors(length(Algorithms))
  boxplot(DataResults$root_mean_squared_error~DataResults$Analysis.name,col=color,ylab="MSE")
}

CompareTwoParameters <- function(DataResults){
  Algorithms <- unique(DataResults[,"Analysis.name"])
  NrIterations <- max(DataResults[,"Run"])
  Precision <- c()
  Recall <- c()
  for (i in (1:nrow(DataResults))){
    precision <- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_pos"])
    recall<- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_neg"])
    Precision <- c(Precision,precision)
    Recall <- c(Recall,recall)
  }
  DataResults <- cbind(DataResults,Precision=Precision,Recall=Recall)
  Parameters <- c("Recall","Precision")
  DataResults <- DataResults[,Parameters]
  AveragedResults <- matrix(0,nrow=length(Algorithms),ncol=ncol(DataResults))
  for (i in (1:length(Algorithms))){
    meanresults <- colMeans(DataResults[((i-1)*NrIterations+1):(i*NrIterations),])
    AveragedResults[i,] <- meanresults
  }
  colnames(AveragedResults) <- colnames(DataResults)
  rownames(AveragedResults) <- Algorithms  
  x = AveragedResults[,which(colnames(AveragedResults)==Parameters[1])]
  y = AveragedResults[,which(colnames(AveragedResults)==Parameters[2])]
  plot(x,y,xlab=Parameters[1],ylab=Parameters[2])
  textxy(x,y,rownames(AveragedResults),cex=0.7) 
}

GenerateConfusionMatrix <- function(DataResults){
  Algorithms <- unique(DataResults[,"Analysis.name"])
  NrIterations <- max(DataResults[,"Run"])
  Precision <- c()
  Recall <- c()
  for (i in (1:nrow(DataResults))){
    precision <- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_pos"])
    recall<- DataResults[i,"num_true_pos"]/(DataResults[i,"num_true_pos"]+DataResults[i,"num_false_neg"])
    Precision <- c(Precision,precision)
    Recall <- c(Recall,recall)
  }
  DataResults <- cbind(DataResults,Precision=Precision,Recall=Recall)
  
  Parameters <- c("num_false_neg","num_false_pos","num_true_pos","num_true_neg")
Conf <- Results[,Parameters]
Conf_short <- matrix(0,nrow=length(Algorithms),ncol=ncol(Conf))
for (i in (1:length(Algorithms))){
  meanresults <- colMeans(Conf[((i-1)*(KfoldCV*NrIterations)+1):(i*(KfoldCV*NrIterations)),])
  Conf_short[i,] <- meanresults
}
colnames(Conf_short) <- colnames(Conf)
rownames(Conf_short) <- Algorithms
lfloor(Algorithms/2)
d=floor(length(Algorithms)/2)+1
par(mfrow=c(2,d))
for (i in (1:length(Algorithms))){
  perf <- Conf_short[i,]
  confusionMatrix <- matrix(0,2,2)
  rownames(confusionMatrix) <- c("Cancer","Normal") # Cancer is your interesting label
  colnames(confusionMatrix) <- c("Cancer","Normal")
  confusionMatrix[1,] <- c(perf[3],perf[1])
  confusionMatrix[2,] <- c(perf[2],perf[4])
  colors <- matrix(0,ncol=ncol(confusionMatrix),nrow=nrow(confusionMatrix))
  colors[,1] <- rep("orange",2)
  colors[,2] <- rep("green",2)
  #par(mar=c(0,0,1,0))
  plot_table(confusionMatrix, colors, "gray",main=paste0("Confusion Matrix for ",Algorithms[i]), text.cex=0.8)
}


Results <- read.csv("/Users/mchampion/Desktop/Hackathon/BinaryResults.csv")
NrIterations <- 10 # to be set by the users
KfoldCV <- 10 # to be set
Algorithms <- c("ZeroR","NaiveBayes","kNN","Bagging","RandomForest") # also to be set by the users
DataResults <- CleanDataResults(Results,NrIterations,KfoldCV,Algorithms)

Parameters <- c("Precision","Recall","AUC","MAE","MSE") # to be set by the users
CompareAlgorithms(DataResults,Algorithms,Parameters,NrIterations)
CompareParameters(DataResults,Algorithms,Parameters,NrIterations)
ParameterOverIterations(DataResults,Algorithms,"MSE",NrIterations)
CompareTwoParameters(DataResults,Algorithms,c("Recall","Precision"),NrIterations)

DataResults<-read.table("/Users/mchampion/Desktop/Hackathon/mine-that-data/jars/Output.tsv",sep="\t",head=TRUE)
CompareAlgorithms2(DataResults)
CompareParameters(DataResults)
ParameterOverIterations(DataResults)
CompareTwoParameters(DataResults)
