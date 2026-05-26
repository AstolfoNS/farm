(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    function toastStyle() {
        return {
            right: "12px",
            bottom: "50px",
            top: "auto",
            left: "auto"
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
            var userOnOpen = opts.onOpen;
            opts.onOpen = function () {
                var $dialog = $(this).dialog("dialog");
                $dialog.css({
                    position: "fixed",
                    left: "auto",
                    top: "auto",
                    right: toastStyle().right,
                    bottom: toastStyle().bottom
                });
                if ($.isFunction(userOnOpen)) {
                    userOnOpen.apply(this, arguments);
                }
            };
            var result = rawShow.call($.messager, opts);
            window.setTimeout(function () {
                $("body > div.messager-window:visible").css({
                    position: "fixed",
                    left: "auto",
                    top: "auto",
                    right: toastStyle().right,
                    bottom: toastStyle().bottom
                });
            }, 0);
            return result;
        };
    }
})(window, window.jQuery);
