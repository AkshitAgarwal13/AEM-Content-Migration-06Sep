$( document ).ready(function() {
    $('.tab-content').each(function(i, obj) {
        $(this).find('.tab-pane').first().addClass("active");
    });

});