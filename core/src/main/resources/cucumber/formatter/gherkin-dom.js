var current;

function header(keyword, title, description) {
    var header = $('#templates .header').clone();
    header.find('.keyword').text(keyword);
    header.find('.title').text(title);
    header.find('.description').text(description);
    return header;
}

function feature(keyword, title, description) {
    current = header(keyword, title, description).appendTo('#root');
}

function scenario(keyword, title, description) {
    current = header(keyword, title, description).appendTo(current);
}
