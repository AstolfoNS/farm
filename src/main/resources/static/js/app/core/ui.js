(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    var toastTimer = null;
    var toastKey = "";
    var toastAt = 0;
    var rawAlert = $.messager.alert;
    var DETAIL_MESSAGE_MAP = {
        "OK": "操作已完成。\n如果界面数据没有立即变化，请等待自动刷新，或重新打开当前面板确认最新结果。",
        "ok": "操作已完成。\n如果界面数据没有立即变化，请等待自动刷新，或重新打开当前面板确认最新结果。",
        "操作成功": "操作已完成。\n当前界面数据会在本次操作后同步刷新，请继续下一步操作。",
        "操作失败": "操作未完成。\n请检查当前输入内容、资源状态或网络请求结果，然后再重试一次。",
        "保存成功": "保存已完成。\n当前表单内容已经写入，并会同步回列表数据。",
        "保存失败": "保存未完成。\n请检查必填项、数值范围与当前记录状态后重新提交。",
        "删除成功": "删除已完成。\n当前记录已从可见列表移除，如仍显示请刷新当前清单。",
        "删除失败": "删除未完成。\n该记录可能仍被引用，或本次请求未成功提交，请稍后重试。",
        "上传成功": "上传已完成。\n文件地址会自动回填到当前表单，并同步刷新对应预览。",
        "上传失败，请稍后重试": "上传未完成。\n请确认已选择有效文件，并检查网络状态或上传接口后重试。",
        "购买参数无效": "购买未提交。\n请确认已经选择种子、购买数量大于 0，并且当前用户状态有效。",
        "出售参数无效": "出售未提交。\n请确认出售数量大于 0，且不能超过当前可出售库存。",
        "购买成功": "购买已完成。\n种子已放入种子仓库，金币与商店数据会同步刷新。",
        "出售成功": "出售已完成。\n库存与金币数据已经同步更新，可继续查看交易记录或仓库变化。",
        "购买失败，请稍后重试": "购买未完成。\n请求可能没有成功提交，请稍后重试，并检查金币余额与库存状态。",
        "出售失败，请稍后重试": "出售未完成。\n请求可能没有成功提交，请稍后重试，并确认当前库存仍然充足。",
        "加载商店失败，请稍后重试": "商店数据加载失败。\n请稍后重新打开商店页面，或检查后端接口与当前用户状态。",
        "请先选择一条记录": "当前还没有选中记录。\n请先在列表中选择一行数据，再执行编辑、删除或上传操作。",
        "请先选择要上传头像的用户": "尚未选中要上传头像的用户。\n请先在用户列表中选中目标用户，再进行头像上传。",
        "请选择需要上传的头像文件": "尚未选择上传文件。\n请先挑选一张有效图片，然后再执行上传操作。",
        "请先选择用户": "当前还没有选中用户。\n请先在用户选择面板中选定用户，再继续后续操作。",
        "切换失败": "用户切换未完成。\n请确认目标用户仍然存在，并稍后重新尝试切换。",
        "切换失败，请稍后重试": "用户切换未完成。\n请稍后重试；如果问题持续存在，请检查当前会话与后端接口状态。"
    };

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function containsAny(raw, keywords) {
        var text = String(raw || "");
        var matched = false;
        $.each(keywords || [], function (_, keyword) {
            if (text.indexOf(keyword) >= 0) {
                matched = true;
                return false;
            }
            return true;
        });
        return matched;
    }

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

    function detectToastTone(title, msg) {
        var raw = trimText(title) + " " + trimText(msg);
        if (containsAny(raw, ["错误", "失败", "异常", "无效", "不足", "不可", "不存在", "未完成"])) {
            return "error";
        }
        if (containsAny(raw, ["注意", "确认", "提醒", "请先"])) {
            return "warn";
        }
        if (containsAny(raw, ["成功", "完成", "已"])) {
            return "success";
        }
        return "info";
    }

    function normalizeToastTitle(title, msg) {
        var safeTitle = trimText(title);
        if (safeTitle) {
            return safeTitle;
        }
        return detectToastTone("", msg) === "error" ? "操作失败" : "操作提醒";
    }

    function normalizeToastMessage(title, msg) {
        var safeMsg = trimText(msg).replace(/\r\n/g, "\n");
        if (!safeMsg) {
            return detectToastTone(title, msg) === "error"
                ? "操作未完成。\n请检查当前输入内容、资源状态或网络请求结果，然后再重试一次。"
                : "操作已完成。\n如果界面数据没有立即变化，请等待自动刷新，或重新打开当前面板确认最新结果。";
        }
        if (DETAIL_MESSAGE_MAP[safeMsg]) {
            return DETAIL_MESSAGE_MAP[safeMsg];
        }
        if (safeMsg.length <= 6) {
            return detectToastTone(title, safeMsg) === "error"
                ? safeMsg + "。\n请检查当前输入内容、资源状态或网络请求结果，然后再重试一次。"
                : safeMsg + "。\n如果界面数据没有立即变化，请等待自动刷新，或重新打开当前面板确认最新结果。";
        }
        return safeMsg;
    }

    function resolveToastTimeout(message, timeout) {
        var base = 1800;
        var extra = Math.min(2600, Math.max(0, String(message || "").length * 24));
        var computed = base + extra;
        var explicitTimeout = Number(timeout);
        if (isNaN(explicitTimeout) || explicitTimeout <= 0) {
            return computed;
        }
        return Math.max(explicitTimeout, computed);
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

        var titleText = normalizeToastTitle(opts.title, opts.msg);
        var msgText = normalizeToastMessage(titleText, opts.msg);
        var tone = detectToastTone(titleText, msgText);
        var now = new Date().getTime();
        var key = titleText + "|" + msgText;
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

        $toast
            .removeClass("is-show is-info is-success is-warn is-error")
            .addClass("is-" + tone);
        $toast.find(".farm-global-toast-title").text(titleText);
        $toast.find(".farm-global-toast-msg").text(msgText);
        $toast.stop(true, true).css({
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
        }, resolveToastTimeout(msgText, opts.timeout));

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
