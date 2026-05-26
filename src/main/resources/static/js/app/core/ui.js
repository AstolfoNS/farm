(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    function popupDefaults() {
        return {
            title: "提示",
            timeout: 1600,
            showType: "slide",
            showSpeed: 260,
            style: {
                right: "14px",
                bottom: "10px"
            }
        };
    }

    var rawShow = $.messager.show;
    if ($.isFunction(rawShow)) {
        $.messager.show = function (options) {
            var opts = $.extend(true, {}, popupDefaults(), options || {});
            return rawShow.call($.messager, opts);
        };
    }
})(window, window.jQuery);
