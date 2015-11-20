exports.getNewAnalysis = function(req, res) {
  res.render('account/analysis', {
    title: 'New Analysis',
    configurations: {
      "kNN": {
        "kNN": {
          "type": "Integer",
          "bounds": {
            "min": 0
          },
          "description": "The number of neighbours to use."
        },
        "distanceWeighting": {
          "type": "select",
          "options": ["1/distance", "1-distance", "none"],
          "description": "the distance weighting method used."
        },
        "meanSquared": {
          "type": "Boolean",
          "description": "Whether the mean squared error is used rather than mean absolute error when doing cross-validation for regression problems."
        },
        "NNSearchAlgorithm":{
          "type": "select",
          "options": ["LinearNNSearch", "KDTree", "CoverTree", "BallTree"],
          "description": "The nearest neighbour search algorithm to use."
        },
        "windowSize": {
          "type": "Integer",
          "description": "Gets the maximum number of instances allowed in the training pool. The addition of new instances above this value will result in old instances being removed. A value of 0 signifies no limit to the number of training instances",
          "bounds":{
            "min": 0
          }
        }
      },
      "DecisionTree": {
        "binarySplits": {
          "type": "Boolean",
          "description": "Allow only binary splits"
        },
        "confidence": {
          "type": "Float",
          "description": "Confidence factor."
        },
        "minNumObj": {
          "type": "Integer",
          "description": "Minimum number of instances per leaf"
        },
        "numFolds":{
          "type": "Integer",
          "description": "Determines the amount of data used for reduced-error pruning. One fold is used for pruning, the rest for growing the tree."
        },
        "reducedErrorPruning": {
          "type": "Boolean",
          "description": "Whether reduced-error pruning is used instead of C.4.5 pruning."
        },
        "subTreeRaising":{
          "type": "Boolean",
          "description": "Whether to consider the subtree raising operation when pruning."
        },
        "unpruned":{
          "type": "Boolean",
          "description": "Whether pruning is performed"
        },
        "useLaplace":{
          "type": "Boolean",
          "description": "Whether counts at leaves are smoothed based on Laplace"
        }
      },
      "NaiveBayes": {
		  "useKernelEstimator": {
		    "type": "Boolean",
		    "description": "Use a kernel estimator for numeric attributes rather than a normal distribution"
		  },
		  "useSupervisedDiscretization": {
		    "type": "Float",
		    "description": "Use supervised discretization to convert numeric attributes to nominal ones."
		  }
		},
		"ZeroR":{
		},
		"CAR":{
		  "support": {
		  	"type": "Float",
		  	"description": "Minimum support"
		  },
		  "confidence": {
		  	"type": "Float",
		  	"description": "Confidence"
		  },
		  "numofRules": {
		  	"type": "Integer",
		  	"description": "Confidence"
		  }
		}
    }
  });
};