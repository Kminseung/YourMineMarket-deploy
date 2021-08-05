window.onbeforeunload = function () {
    $.ajax({
        type: 'get',
        url: "/pageOut"
    });
};