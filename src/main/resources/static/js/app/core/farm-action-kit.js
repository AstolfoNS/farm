(function (window, $) {
    if (!window || !$) {
        return;
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function playSound(key) {
        if (window.FarmAudio && $.isFunction(window.FarmAudio.play)) {
            window.FarmAudio.play(key);
        }
    }

    function toast(message, timeout) {
        $.messager.show({
            title: "提示",
            msg: message || "操作成功",
            timeout: asNumber(timeout, 1200),
            showType: "slide"
        });
    }

    function alertError(message) {
        $.messager.alert("提示", message || "操作失败");
    }

    function ensureDialog(selector, htmlBuilder, options) {
        if ($(selector).length <= 0 && $.isFunction(htmlBuilder)) {
            $("body").append(htmlBuilder());
        }
        $(selector).dialog($.extend({
            modal: true,
            closed: true,
            resizable: false
        }, options || {}));
        return $(selector);
    }

    function closeDialog(selector) {
        var $dialog = $(selector);
        if ($dialog.length > 0) {
            $dialog.dialog("close");
        }
    }

    function bindActionButtons(containerSelector, map, namespace) {
        var ns = namespace || ".farmAction";
        var $container = $(containerSelector);
        $.each(map || {}, function (action, handler) {
            var selector = "[data-action='" + action + "']";
            $container.off("click" + ns, selector).on("click" + ns, selector, function (event) {
                event.preventDefault();
                if ($.isFunction(handler)) {
                    handler($(this), event);
                }
            });
        });
    }

    function runAction(options) {
        var opts = $.extend({
            request: null,
            successMessage: "",
            failMessage: "操作失败",
            onSuccess: null,
            onError: null,
            successSound: "click",
            failSound: "error",
            successTimeout: 1200
        }, options || {});

        if (!$.isFunction(opts.request)) {
            return;
        }

        opts.request(function (res) {
            if (!(window.FarmApi && $.isFunction(window.FarmApi.isOk) && window.FarmApi.isOk(res))) {
                alertError((res && res.msg) || opts.failMessage);
                playSound(opts.failSound);
                if ($.isFunction(opts.onError)) {
                    opts.onError(res);
                }
                return;
            }
            if (opts.successMessage) {
                toast(opts.successMessage, opts.successTimeout);
            }
            playSound(opts.successSound);
            if ($.isFunction(opts.onSuccess)) {
                opts.onSuccess(res);
            }
        }, function () {
            alertError(opts.failMessage);
            playSound(opts.failSound);
            if ($.isFunction(opts.onError)) {
                opts.onError(null);
            }
        });
    }

    window.FarmActionKit = {
        asNumber: asNumber,
        playSound: playSound,
        toast: toast,
        alertError: alertError,
        ensureDialog: ensureDialog,
        closeDialog: closeDialog,
        bindActionButtons: bindActionButtons,
        runAction: runAction
    };
})(window, window.jQuery);

