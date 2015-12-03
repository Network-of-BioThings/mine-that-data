var _ = require('lodash');
var async = require('async');
var crypto = require('crypto');
var nodemailer = require('nodemailer');
var passport = require('passport');
var User = require('../models/User');
var secrets = require('../config/secrets');
var shell = require('shelljs');
var fs = require('fs');

/**
 * GET /login
 * Login page.
 */
exports.getLogin = function(req, res) {
  if (req.user) return res.redirect('/');
  res.render('account/login', {
    title: 'Login'
  });
};

/**
 * POST /login
 * Sign in using email and password.
 */
exports.postLogin = function(req, res, next) {
  req.assert('email', 'Email is not valid').isEmail();
  req.assert('password', 'Password cannot be blank').notEmpty();

  var errors = req.validationErrors();

  if (errors) {
    req.flash('errors', errors);
    return res.redirect('/login');
  }

  passport.authenticate('local', function(err, user, info) {
    if (err) return next(err);
    if (!user) {
      req.flash('errors', { msg: info.message });
      return res.redirect('/login');
    }
    req.logIn(user, function(err) {
      if (err) return next(err);
      req.flash('success', { msg: 'Success! You are logged in.' });
      res.redirect(req.session.returnTo || '/');
    });
  })(req, res, next);
};

/**
 * GET /logout
 * Log out.
 */
exports.logout = function(req, res) {
  req.logout();
  res.redirect('/');
};

/**
 * GET /signup
 * Signup page.
 */
exports.getSignup = function(req, res) {
  if (req.user) return res.redirect('/');
  res.render('account/signup', {
    title: 'Create Account'
  });
};

/**
 * POST /signup
 * Create a new local account.
 */
exports.postSignup = function(req, res, next) {
  req.assert('email', 'Email is not valid').isEmail();
  req.assert('password', 'Password must be at least 4 characters long').len(4);
  req.assert('confirmPassword', 'Passwords do not match').equals(req.body.password);

  var errors = req.validationErrors();

  if (errors) {
    req.flash('errors', errors);
    return res.redirect('/signup');
  }

  var user = new User({
    email: req.body.email,
    password: req.body.password
  });

  User.findOne({ email: req.body.email }, function(err, existingUser) {
    if (existingUser) {
      req.flash('errors', { msg: 'Account with that email address already exists.' });
      return res.redirect('/signup');
    }
    user.save(function(err) {
      if (err) return next(err);
      req.logIn(user, function(err) {
        if (err) return next(err);
        res.redirect('/');
      });
    });
  });
};

/**
 * GET /account
 * Profile page.
 */
exports.getAccount = function(req, res) {
  res.render('account/profile', {
    title: 'Account Management'
  });
};

/**
 * POST /account/profile
 * Update profile information.
 */
exports.postUpdateProfile = function(req, res, next) {
  User.findById(req.user.id, function(err, user) {
    if (err) return next(err);
    user.email = req.body.email || '';
    user.profile.name = req.body.name || '';
    user.profile.gender = req.body.gender || '';
    user.profile.location = req.body.location || '';
    user.profile.website = req.body.website || '';

    user.save(function(err) {
      if (err) return next(err);
      req.flash('success', { msg: 'Profile information updated.' });
      res.redirect('/account');
    });
  });
};

/**
 * POST /account/password
 * Update current password.
 */
exports.postUpdatePassword = function(req, res, next) {
  req.assert('password', 'Password must be at least 4 characters long').len(4);
  req.assert('confirmPassword', 'Passwords do not match').equals(req.body.password);

  var errors = req.validationErrors();

  if (errors) {
    req.flash('errors', errors);
    return res.redirect('/account');
  }

  User.findById(req.user.id, function(err, user) {
    if (err) return next(err);

    user.password = req.body.password;

    user.save(function(err) {
      if (err) return next(err);
      req.flash('success', { msg: 'Password has been changed.' });
      res.redirect('/account');
    });
  });
};

/**
 * POST /account/delete
 * Delete user account.
 */
exports.postDeleteAccount = function(req, res, next) {
  User.remove({ _id: req.user.id }, function(err) {
    if (err) return next(err);
    req.logout();
    req.flash('info', { msg: 'Your account has been deleted.' });
    res.redirect('/');
  });
};

/**
 * GET /account/unlink/:provider
 * Unlink OAuth provider.
 */
exports.getOauthUnlink = function(req, res, next) {
  var provider = req.params.provider;
  User.findById(req.user.id, function(err, user) {
    if (err) return next(err);

    user[provider] = undefined;
    user.tokens = _.reject(user.tokens, function(token) { return token.kind === provider; });

    user.save(function(err) {
      if (err) return next(err);
      req.flash('info', { msg: provider + ' account has been unlinked.' });
      res.redirect('/account');
    });
  });
};

/**
 * GET /reset/:token
 * Reset Password page.
 */
exports.getReset = function(req, res) {
  if (req.isAuthenticated()) {
    return res.redirect('/');
  }
  User
    .findOne({ resetPasswordToken: req.params.token })
    .where('resetPasswordExpires').gt(Date.now())
    .exec(function(err, user) {
      if (!user) {
        req.flash('errors', { msg: 'Password reset token is invalid or has expired.' });
        return res.redirect('/forgot');
      }
      res.render('account/reset', {
        title: 'Password Reset'
      });
    });
};

/**
 * POST /reset/:token
 * Process the reset password request.
 */
exports.postReset = function(req, res, next) {
  req.assert('password', 'Password must be at least 4 characters long.').len(4);
  req.assert('confirm', 'Passwords must match.').equals(req.body.password);

  var errors = req.validationErrors();

  if (errors) {
    req.flash('errors', errors);
    return res.redirect('back');
  }

  async.waterfall([
    function(done) {
      User
        .findOne({ resetPasswordToken: req.params.token })
        .where('resetPasswordExpires').gt(Date.now())
        .exec(function(err, user) {
          if (!user) {
            req.flash('errors', { msg: 'Password reset token is invalid or has expired.' });
            return res.redirect('back');
          }

          user.password = req.body.password;
          user.resetPasswordToken = undefined;
          user.resetPasswordExpires = undefined;

          user.save(function(err) {
            if (err) return next(err);
            req.logIn(user, function(err) {
              done(err, user);
            });
          });
        });
    },
    function(user, done) {
      var transporter = nodemailer.createTransport({
        service: 'SendGrid',
        auth: {
          user: secrets.sendgrid.user,
          pass: secrets.sendgrid.password
        }
      });
      var mailOptions = {
        to: user.email,
        from: 'hackathon@starter.com',
        subject: 'Your Hackathon Starter password has been changed',
        text: 'Hello,\n\n' +
          'This is a confirmation that the password for your account ' + user.email + ' has just been changed.\n'
      };
      transporter.sendMail(mailOptions, function(err) {
        req.flash('success', { msg: 'Success! Your password has been changed.' });
        done(err);
      });
    }
  ], function(err) {
    if (err) return next(err);
    res.redirect('/');
  });
};

/**
 * GET /portal
 * Portal page.
 */
exports.getPortal = function(req, res) {
  res.render('account/portal', {
    title: 'My Portal'
  });
};

/**
 * GET /analysis
 * Portal page.
 */
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
      }
    }
  });
};

function getJarParams(req){
  var details = JSON.parse(fs.readFileSync("/home/jkralj/mine-that-data/configs/" + req.body.algorithm + ".JSON").toString());
  var retStr = "java -jar " + req.body.algorithm
  for (var value in details){
    retStr = retStr + " -" + value + " "  + req.body[req.body.algorithm + "_" + value];
  }
  retStr = retStr + "-numCVFolds 10 -numIterations 10 -analysisName Test_name -targetClassIndex 0 -dataFile example_input.csv";
  console.log(retStr);
  return retStr;
}
/**
 * POST /analysis
 * Analysis form page.
 */
exports.postAnalysis = function(req, res) {
  var userid = req.user._id;
  var dir = '~/Desktop/bd2k/'+userid+'/';
  var num = 0;
  var exec = req.body.algorithm;
  var params = "";
  var json = {};

  var jarParams=getJarParams(req);

  console.log('**************');
  console.log(req.body);
  console.log('user: '+userid);
  async.series([
    function(cbAsync){
      shell.exec('mkdir '+dir, function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        cbAsync();
      });
    },
    function(cbAsync){
      console.log('command: ls -1 '+dir+' | wc -l');
      shell.exec('ls -1 '+dir+' | wc -l', function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        num = output;
        res.redirect('analysis/'+num.toString().trim());
        cbAsync();
      });
    },
    function(cbAsync){
      console.log('command: mkdir '+dir.trim()+num.toString().trim());
      shell.exec('mkdir '+dir.trim()+num.toString().trim(), function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        json = {
          user: userid,
          task_name: num.toString().trim(),
          t: 'jar',
          p: '',
          classifier: 'ZeroR.jar'
        };
        cbAsync();
      });
    },
    function(cbAsync){
      var configFile = '~/Desktop/bd2k/'+userid+'_'+num.toString().trim()+'.json';
      var command = 'touch '+configFile+';echo \''+JSON.stringify(json)+'\'>>'+configFile;
      console.log('command: '+command);
      shell.exec(command, function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        cbAsync();
      });
    },
    function(cbAsync){
      var script = '../scripts/submit.sh';
      var jar = '../jars/ZeroR.jar';
      var command = 'bash '+script+' -u '+userid.toString()+' -n '+num.toString().trim()+' -t jar '+params+' '+ jar;
      var done = '~/Desktop/bd2k/'+req.user._id.toString().trim()+'/'+num.toString().trim()+'/done';
      var sleep = 'sleep 20;touch '+done;
      console.log('command: '+command);
      shell.exec(sleep, function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        if(code!=0){
          return;
        }
        cbAsync();

      });
    }
  ],
  function asyncComplete(err) { // the "complete" callback of `async.waterfall`
        if ( err ) { // there was an error with either `getTicker` or `writeTicker`
            console.warn('Error ',err);
        } else {
            console.info('Successfully completed operation.');
        }
  });

};

/**
 * GET /analysis
 * Portal page.
 */
exports.getAnalysis = function(req, res) {
  console.log(req.body);
  var id = req.params.id;
  async.series([
    function(cbAsync){
      var command = 'ls '+'~/Desktop/bd2k/'+req.user._id.toString().trim()+'/'+id+'/done';
      console.log('command: '+command);
      shell.exec(command, function(code, output) {
        console.log('Exit code:', code);
        console.log('Program output:', output);
        if(code==0){
          var array = fs.readFileSync('../jars/Output.tsv').toString().split("\n");
          var headers = array[0];
          var results = [];
          for(var i = 1; i < array.length; i++) {
              var tokens = array[i].split('\t');
              results.push(tokens);
          }
          console.log(results);
          res.render('account/results', {
            title: 'Analysis Results',
            taskid: id,
            head: headers,
            result: results
          });
        }else{
          res.render('account/analyzing', {
            title: 'Analyzing',
            taskid: id
          });
        }
        cbAsync();

      });
    }
  ])
};

/**
 * GET /forgot
 * Forgot Password page.
 */
exports.getForgot = function(req, res) {
  if (req.isAuthenticated()) {
    return res.redirect('/');
  }
  res.render('account/forgot', {
    title: 'Forgot Password'
  });
};

/**
 * POST /forgot
 * Create a random token, then the send user an email with a reset link.
 */
exports.postForgot = function(req, res, next) {
  req.assert('email', 'Please enter a valid email address.').isEmail();

  var errors = req.validationErrors();

  if (errors) {
    req.flash('errors', errors);
    return res.redirect('/forgot');
  }

  async.waterfall([
    function(done) {
      crypto.randomBytes(16, function(err, buf) {
        var token = buf.toString('hex');
        done(err, token);
      });
    },
    function(token, done) {
      User.findOne({ email: req.body.email.toLowerCase() }, function(err, user) {
        if (!user) {
          req.flash('errors', { msg: 'No account with that email address exists.' });
          return res.redirect('/forgot');
        }

        user.resetPasswordToken = token;
        user.resetPasswordExpires = Date.now() + 3600000; // 1 hour

        user.save(function(err) {
          done(err, token, user);
        });
      });
    },
    function(token, user, done) {
      var transporter = nodemailer.createTransport({
        service: 'SendGrid',
        auth: {
          user: secrets.sendgrid.user,
          pass: secrets.sendgrid.password
        }
      });
      var mailOptions = {
        to: user.email,
        from: 'hackathon@starter.com',
        subject: 'Reset your password on Hackathon Starter',
        text: 'You are receiving this email because you (or someone else) have requested the reset of the password for your account.\n\n' +
          'Please click on the following link, or paste this into your browser to complete the process:\n\n' +
          'http://' + req.headers.host + '/reset/' + token + '\n\n' +
          'If you did not request this, please ignore this email and your password will remain unchanged.\n'
      };
      transporter.sendMail(mailOptions, function(err) {
        req.flash('info', { msg: 'An e-mail has been sent to ' + user.email + ' with further instructions.' });
        done(err, 'done');
      });
    }
  ], function(err) {
    if (err) return next(err);
    res.redirect('/forgot');
  });


};
