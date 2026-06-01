(function (window, $) {
    if (!window || !$) {
        return;
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(text) {
        return $("<div/>").text(text == null ? "" : String(text)).html();
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
        var userOptions = options || {};
        var userOnOpen = userOptions.onOpen;
        var userOnResize = userOptions.onResize;
        var mergedOptions = $.extend({
            modal: true,
            closed: true,
            resizable: false
        }, userOptions);

        mergedOptions.onOpen = function () {
            if ($.isFunction(userOnOpen)) {
                userOnOpen.apply(this, arguments);
            }
            var $dialog = $(selector);
            window.setTimeout(function () {
                try {
                    $dialog.dialog("center");
                } catch (ignoreCenterError) {}
            }, 0);
        };

        mergedOptions.onResize = function () {
            if ($.isFunction(userOnResize)) {
                userOnResize.apply(this, arguments);
            }
            var $dialog = $(selector);
            try {
                $dialog.dialog("center");
            } catch (ignoreCenterError) {}
        };

        $(selector).dialog(mergedOptions);
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

    function buildInfoRows(rows, rowClass) {
        var cls = rowClass || "farm-action-row";
        var result = [];
        $.each(rows || [], function (_, row) {
            if (!row) {
                return true;
            }
            var content = "";
            if (row.html != null) {
                content = String(row.html);
            } else {
                var label = row.label ? String(row.label) : "";
                var value = row.value == null ? "" : String(row.value);
                content = label ? (escapeHtml(label) + ": " + escapeHtml(value)) : escapeHtml(value);
            }
            if (row.strong === true) {
                content = "<strong>" + content + "</strong>";
            }
            var rowCls = cls + (row.className ? (" " + String(row.className)) : "");
            result.push("<div class='" + rowCls + "'>" + content + "</div>");
            return true;
        });
        return result.join("");
    }

    function renderInfoRows(containerSelector, rows, rowClass) {
        $(containerSelector).html(buildInfoRows(rows, rowClass));
    }

    function buildActionButtons(buttons) {
        var html = [];
        $.each(buttons || [], function (_, btn) {
            if (!btn || btn.visible === false || !btn.action || !btn.text) {
                return true;
            }
            var skin = btn.skin ? String(btn.skin) : "";
            var className = "easyui-linkbutton" + (skin ? (" " + skin) : "");
            var attrs = [];
            if (btn.id) {
                attrs.push("id='" + String(btn.id) + "'");
            }
            if (btn.title) {
                attrs.push("title='" + escapeHtml(btn.title) + "'");
            }
            html.push("<a href='javascript:void(0)' class='" + className + "' data-action='" + String(btn.action) + "' " + attrs.join(" ") + ">" + escapeHtml(btn.text) + "</a>");
            return true;
        });
        return html.join("");
    }

    function renderActionButtons(containerSelector, buttons) {
        var $container = $(containerSelector);
        $container.html(buildActionButtons(buttons));
        $container.find(".easyui-linkbutton").linkbutton();
    }

    function renderDialogTemplate(options) {
        var opts = $.extend({
            dialogSelector: "",
            title: "",
            infoSelector: "",
            rows: [],
            rowClass: "farm-action-row",
            actionSelector: "",
            buttons: []
        }, options || {});

        if (opts.dialogSelector && opts.title) {
            var $dialog = $(opts.dialogSelector);
            if ($dialog.length > 0 && $.isFunction($dialog.dialog)) {
                $dialog.dialog("setTitle", opts.title);
            }
        }
        if (opts.infoSelector) {
            renderInfoRows(opts.infoSelector, opts.rows, opts.rowClass);
        }
        if (opts.actionSelector) {
            renderActionButtons(opts.actionSelector, opts.buttons);
        }
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
        escapeHtml: escapeHtml,
        playSound: playSound,
        toast: toast,
        alertError: alertError,
        ensureDialog: ensureDialog,
        closeDialog: closeDialog,
        bindActionButtons: bindActionButtons,
        buildInfoRows: buildInfoRows,
        renderInfoRows: renderInfoRows,
        buildActionButtons: buildActionButtons,
        renderActionButtons: renderActionButtons,
        renderDialogTemplate: renderDialogTemplate,
        runAction: runAction
    };
})(window, window.jQuery);
