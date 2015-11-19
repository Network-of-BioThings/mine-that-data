/**
 * GET /
 * Home page.
 */
exports.index = function(req, res) {
  console.log(req.user);
  if (req.user)
    return res.redirect('/portal');
  else
    res.render('home', {
      title: 'Home'
    });
};
