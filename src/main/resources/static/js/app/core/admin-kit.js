(function (window, $) {
    if (!window || !$) {
        return;
    }

    function asNumber(value, def) {
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function boolOk(res) {
        return window.FarmApi && $.isFunction(window.FarmApi.isOk) && window.FarmApi.isOk(res);
    }

    function listFromPageData(data) {
        if (window.FarmGrid && $.isFunction(window.FarmGrid.listFromPageData)) {
            return window.FarmGrid.listFromPageData(data);
        }
        if (!data) {
            return [];
        }
        if ($.isArray(data.records)) {
            return data.records;
        }
        if ($.isArray(data.rows)) {
            return data.rows;
        }
        if ($.isArray(data.list)) {
            return data.list;
        }
        return [];
    }

    function normalizePageResult(res) {
        var data = (boolOk(res) && res && res.data) ? res.data : {};
        var rows = listFromPageData(data);
        return {
            total: asNumber(data.total, rows.length),
            rows: rows
        };
    }

    function toast(message, title) {
        $.messager.show({
            title: title || "提示",
            msg: message || "操作成功",
            timeout: 1500,
            showType: "slide"
        });
    }

    function alertError(message, title) {
        $.messager.alert(title || "提示", message || "操作失败");
    }

    function confirm(message, ok, title) {
        $.messager.confirm(title || "确认", message || "确认执行当前操作？", function (pass) {
            if (pass && $.isFunction(ok)) {
                ok();
            }
        });
    }

    function filePublicPrefix() {
        var prefix = trimText(window.FARM_FILE_PUBLIC_PREFIX || "/oss");
        if (!prefix) {
            return "/oss";
        }
        if (prefix.charAt(0) !== "/") {
            prefix = "/" + prefix;
        }
        return prefix.replace(/\/+$/, "") || "/oss";
    }

    function buildFileAccessUrl(relativePath) {
        var rel = trimText(relativePath).replace(/^\/+/, "");
        if (!rel) {
            return "";
        }
        var prefix = filePublicPrefix();
        if (rel.indexOf(prefix.replace(/^\/+/, "") + "/") === 0) {
            rel = rel.substring(prefix.length);
        } else if (rel.indexOf("oss/") === 0) {
            rel = rel.substring(4);
        }
        return prefix + "/" + rel.replace(/^\/+/, "");
    }

    function setTextboxValue($el, value) {
        var safe = value == null ? "" : value;
        try {
            $el.textbox("setValue", safe);
        } catch (ignoreTextboxSet) {
            $el.val(safe);
        }
    }

    function getTextboxValue($el, fallback) {
        try {
            return trimText($el.textbox("getValue"));
        } catch (ignoreTextboxGet) {
            return trimText($el.val()) || (fallback || "");
        }
    }

    function setNumberboxValue($el, value) {
        var safe = value == null ? "" : value;
        try {
            $el.numberbox("setValue", safe);
        } catch (ignoreNumberboxSet) {
            $el.val(safe);
        }
    }

    function getNumberboxValue($el, fallback) {
        try {
            return asNumber($el.numberbox("getValue"), fallback || 0);
        } catch (ignoreNumberboxGet) {
            return asNumber($el.val(), fallback || 0);
        }
    }

    function resolveUploadedUrl(data) {
        var payload = data || {};
        var url = trimText(payload.accessUrl || payload.path || "");
        if (!url) {
            var rel = trimText(payload.relativePath || "");
            if (rel) {
                url = buildFileAccessUrl(rel);
            }
        }
        return url;
    }

    function uploadFile(options) {
        var opts = $.extend({
            fileInput: null,
            category: "",
            onSuccess: null,
            onError: null,
            onComplete: null
        }, options || {});

        var $fileInput = opts.fileInput && opts.fileInput.jquery ? opts.fileInput : $(opts.fileInput);
        var files = $fileInput.prop("files");
        if (!files || files.length <= 0) {
            if ($.isFunction(opts.onComplete)) {
                opts.onComplete();
            }
            return;
        }

        var formData = new FormData();
        formData.append("file", files[0]);
        formData.append("category", trimText(opts.category));

        $.ajax({
            url: "/file/upload",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function (res) {
                if (!boolOk(res) || !res.data) {
                    var errText = (res && res.msg) || "上传失败";
                    if ($.isFunction(opts.onError)) {
                        opts.onError(errText, res);
                        return;
                    }
                    alertError(errText);
                    return;
                }
                var url = resolveUploadedUrl(res.data);
                if (!url) {
                    var msg = "上传成功但未返回访问地址";
                    if ($.isFunction(opts.onError)) {
                        opts.onError(msg, res);
                        return;
                    }
                    alertError(msg);
                    return;
                }
                if ($.isFunction(opts.onSuccess)) {
                    opts.onSuccess(url, res.data, res);
                } else {
                    toast((res && res.msg) || "上传成功");
                }
            },
            error: function () {
                var msg = "上传失败，请稍后重试";
                if ($.isFunction(opts.onError)) {
                    opts.onError(msg);
                    return;
                }
                alertError(msg);
            },
            complete: function () {
                $fileInput.val("");
                if ($.isFunction(opts.onComplete)) {
                    opts.onComplete();
                }
            }
        });
    }

    function bindUploadPicker(options) {
        var opts = $.extend({
            namespace: ".farmAdminUpload",
            buttonSelector: "",
            fileSelector: "",
            category: "",
            onSuccess: null,
            onError: null,
            onComplete: null
        }, options || {});

        if (!opts.buttonSelector || !opts.fileSelector) {
            return;
        }

        var clickEvt = "click" + opts.namespace;
        var changeEvt = "change" + opts.namespace;

        $(document).off(clickEvt, opts.buttonSelector).on(clickEvt, opts.buttonSelector, function (e) {
            e.preventDefault();
            var $file = $(opts.fileSelector).first();
            var input = $file.get(0);
            if (!input) {
                alertError("未找到上传控件，请刷新后重试");
                return;
            }
            input.value = "";
            input.click();
        });

        $(document).off(changeEvt, opts.fileSelector).on(changeEvt, opts.fileSelector, function () {
            uploadFile({
                fileInput: $(this),
                category: opts.category,
                onSuccess: opts.onSuccess,
                onError: opts.onError,
                onComplete: opts.onComplete
            });
        });
    }

    window.FarmAdmin = {
        asNumber: asNumber,
        trimText: trimText,
        boolOk: boolOk,
        listFromPageData: listFromPageData,
        normalizePageResult: normalizePageResult,
        toast: toast,
        alertError: alertError,
        confirm: confirm,
        setTextboxValue: setTextboxValue,
        getTextboxValue: getTextboxValue,
        setNumberboxValue: setNumberboxValue,
        getNumberboxValue: getNumberboxValue,
        filePublicPrefix: filePublicPrefix,
        buildFileAccessUrl: buildFileAccessUrl,
        resolveUploadedUrl: resolveUploadedUrl,
        uploadFile: uploadFile,
        bindUploadPicker: bindUploadPicker
    };
})(window, window.jQuery);
