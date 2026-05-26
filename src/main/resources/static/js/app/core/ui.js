(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    function toastStyle() {
        return {
            right: "12px",
            bottom: "50px",
            top: ""
        };
    }

    function popupDefaults() {
        return {
            title: "消息",
            timeout: 1600,
            showType: "slide",
            showSpeed: 260,
            style: toastStyle()
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
