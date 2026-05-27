(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    var toastTimer = null;
    var toastKey = "";
    var toastAt = 0;
    var rawAlert = $.messager.alert;

    function isWsTip(text) {
        var raw = String(text || "");
        return raw.indexOf("WebSocket") >= 0 || raw.indexOf("WS:") >= 0 || raw.indexOf("轮询刷新") >= 0;
    }

    function toastBottom() {
        var bottombarHeight = $(".farm-bottombar:visible").outerHeight();
        if (!bottombarHeight || bottombarHeight <= 0) {
            bottombarHeight = 42;
        }
        return Math.round(bottombarHeight + 8);
    }

    function ensureToastEl() {
        var $el = $("#farmGlobalToast");
        if ($el.length > 0) {
            return $el;
        }
        $el = $(
            "<div id='farmGlobalToast' class='farm-global-toast'>" +
            "<div class='farm-global-toast-title'></div>" +
            "<div class='farm-global-toast-msg'></div>" +
            "</div>"
        );
        $("body").append($el);
        return $el;
    }

    function showToast(options) {
        var opts = $.extend(true, {
            title: "消息",
            msg: "",
            timeout: 1600
        }, options || {});

        if (isWsTip(opts.title) || isWsTip(opts.msg)) {
            return null;
        }

        var now = new Date().getTime();
        var key = String(opts.title || "") + "|" + String(opts.msg || "");
        if (key === toastKey && (now - toastAt) < 900) {
            return null;
        }
        toastKey = key;
        toastAt = now;

        var $toast = ensureToastEl();
        if (toastTimer) {
            window.clearTimeout(toastTimer);
            toastTimer = null;
        }

        $toast.find(".farm-global-toast-title").text(opts.title || "消息");
        $toast.find(".farm-global-toast-msg").text(opts.msg || "");
        $toast.stop(true, true).removeClass("is-show").css({
            right: "12px",
            bottom: (0 - ($toast.outerHeight() || 120) - 8) + "px"
        }).show();

        $toast.animate({
            bottom: toastBottom() + "px"
        }, 220, function () {
            $toast.addClass("is-show");
        });

        toastTimer = window.setTimeout(function () {
            $toast.removeClass("is-show").animate({
                bottom: (0 - ($toast.outerHeight() || 120) - 8) + "px"
            }, 180, function () {
                $toast.hide();
            });
        }, Number(opts.timeout || 1600));

        return $toast[0];
    }

    $.messager.show = function (options) {
        return showToast(options);
    };

    if ($.isFunction(rawAlert)) {
        $.messager.alert = function (title, msg, icon, fn) {
            if (isWsTip(title) || isWsTip(msg)) {
                return null;
            }
            var callback = null;
            if ($.isFunction(icon)) {
                callback = icon;
            } else if ($.isFunction(fn)) {
                callback = fn;
            }
            showToast({
                title: title || "提示",
                msg: msg || "",
                timeout: 1800
            });
            if ($.isFunction(callback)) {
                window.setTimeout(function () {
                    callback();
                }, 0);
            }
            return null;
        };
    }
})(window, window.jQuery);
