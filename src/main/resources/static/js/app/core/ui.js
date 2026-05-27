(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }
    var lastToastKey = "";
    var lastToastAt = 0;

    function toastOffsetBottom() {
        var bottombarHeight = $(".farm-bottombar:visible").outerHeight();
        if (!bottombarHeight || bottombarHeight <= 0) {
            bottombarHeight = 42;
        }
        return Math.round(bottombarHeight + 8);
    }

    function toastStyle() {
        return {
            right: "12px",
            bottom: toastOffsetBottom() + "px",
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

    function pinToast($dialog) {
        var style = toastStyle();
        var startBottom = 0 - ($dialog.outerHeight() || 120) - 8;
        $dialog.addClass("farm-toast-window").stop(true, true).css({
            position: "fixed",
            left: "auto",
            top: "auto",
            right: style.right,
            bottom: startBottom + "px"
        }).animate({
            bottom: style.bottom
        }, 220);
    }

    var rawShow = $.messager.show;
    var rawClose = $.messager.close;

    function isWsTip(text) {
        var raw = String(text || "");
        return raw.indexOf("WebSocket") >= 0 || raw.indexOf("WS:") >= 0 || raw.indexOf("轮询刷新") >= 0;
    }

    function closeAllTips() {
        if ($.isFunction(rawClose)) {
            $("body > .messager-tip").each(function () {
                try {
                    rawClose.call($.messager, this);
                } catch (e) {
                }
            });
            return;
        }
        $("body > .messager-tip").each(function () {
            try {
                $(this).window("close");
            } catch (e) {
            }
        });
    }

    if ($.isFunction(rawShow)) {
        $.messager.show = function (options) {
            var opts = $.extend(true, {}, popupDefaults(), options || {});
            if (isWsTip(opts.msg) || isWsTip(opts.title)) {
                return null;
            }
            var now = new Date().getTime();
            var toastKey = String(opts.title || "") + "|" + String(opts.msg || "");
            if (toastKey === lastToastKey && (now - lastToastAt) < 900) {
                return null;
            }
            lastToastKey = toastKey;
            lastToastAt = now;
            closeAllTips();
            opts.style = toastStyle();
            var userOnOpen = opts.onOpen;
            opts.onOpen = function () {
                var $dialog = $(this).dialog("dialog");
                pinToast($dialog);
                if ($.isFunction(userOnOpen)) {
                    userOnOpen.apply(this, arguments);
                }
            };
            return rawShow.call($.messager, opts);
        };
    }
})(window, window.jQuery);
