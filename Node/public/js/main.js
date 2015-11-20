function show_correct(){
	$('.paramtab').each(function(){
		$(this).hide()
  	});
  	$('#'+ $('#algorithm').children(':selected').attr('id').slice(2)).show();
}

$(document).ready(function() {
  show_correct();
  $('#algorithm').change(show_correct);

});