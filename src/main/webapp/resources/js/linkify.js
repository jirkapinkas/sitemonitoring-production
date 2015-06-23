function linkify(inputText, baseUrl) {
	var arrowOut = "&nbsp;<i class='fa fa-share'></i>";
	var replaceText, replacePattern1, replacePattern2, replacePattern3;
	replacePattern1 = /(\b(https?|ftp):\/\/[-A-Z0-9+&amp;@#\/%?=~_|!:,.;]*[-A-Z0-9+&amp;@#\/%=~_|])/gim;
	replacedText = inputText.replace(replacePattern1,
			'<a href="$1" target="_blank">$1' + arrowOut + '</a>');
	replacePattern2 = /(^|[^\/])(www\.[\S]+(\b|$))/gim;
	replacedText = replacedText.replace(replacePattern2,
			'$1<a href="http://$2" target="_blank">$2' + arrowOut + '</a>');
	replacePattern3 = /(\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,6})/gim;
	replacedText = replacedText.replace(replacePattern3,
			'<a href="mailto:$1">$1' + arrowOut + '</a>');
	return replacedText;
}