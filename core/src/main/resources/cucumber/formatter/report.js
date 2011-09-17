$(document).ready(function() {
    function feature(keyword, title, description) {
        var e = $('#templates .feature').clone().appendTo('body');
        e.find('.keyword').text(keyword);
        e.find('.title').text(title);
        e.find('.description').text(description);
    }

    feature('Feature', 'English', 'Hello\nWorld');
    feature('Feature', 'French', 'Bonjour\nMonde');
});