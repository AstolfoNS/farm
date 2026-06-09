(function (window, $) {
    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, pageSwitchMs: 220, actionFeedbackMs: 1200, dataRefreshDelayMs: 260};
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function escapeHtml(value) {
        return $("<div/>").text(value == null ? "" : String(value)).html();
    }

    function escapeAttr(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    }

    function toArray(list) {
        return $.isArray(list) ? list : [];
    }

    function totalPages(pageResult, defaultSize) {
        var total = asNumber(pageResult && pageResult.total, 0);
        var pageSize = asNumber(pageResult && pageResult.pageSize, defaultSize || 10);
        if (pageSize <= 0) {
            pageSize = defaultSize || 10;
        }
        var pages = Math.ceil(total / pageSize);
        return pages <= 0 ? 1 : pages;
    }

    function showPanel($el) {
        $el.stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
    }

    function hidePanel($el) {
        if (!$el || $el.length <= 0 || !$el.is(":visible")) {
            return;
        }
        $el.stop(true, true).fadeOut(motion().moduleEnterMs);
    }

    function renderButtonPager(options) {
        var opts = $.extend({
            container: "",
            pageData: {},
            buttonClass: "",
            prevClass: "",
            nextClass: "",
            labelClass: "",
            onPrev: null,
            onNext: null
        }, options || {});
        var $container = $(opts.container);
        if ($container.length <= 0) {
            return;
        }
        var pageNo = asNumber(opts.pageData && opts.pageData.pageNo, 1);
        var pages = totalPages(opts.pageData || {});
        var prevDisabled = pageNo <= 1 ? "disabled" : "";
        var nextDisabled = pageNo >= pages ? "disabled" : "";
        var buttonClass = trimText(opts.buttonClass);
        var prevClass = trimText(opts.prevClass);
        var nextClass = trimText(opts.nextClass);
        var labelClass = trimText(opts.labelClass);
        var html = "" +
            "<button type='button' class='" + escapeAttr(buttonClass + " prev " + prevClass) + "' " + prevDisabled + "></button>" +
            "<span class='" + escapeAttr(labelClass) + "'>第 " + pageNo + " / " + pages + " 页</span>" +
            "<button type='button' class='" + escapeAttr(buttonClass + " next " + nextClass) + "' " + nextDisabled + "></button>";
        $container.html(html);
        if (prevClass) {
            $container.find("." + prevClass).off("click.farmPager").on("click.farmPager", function () {
                if (pageNo > 1 && $.isFunction(opts.onPrev)) {
                    opts.onPrev(pageNo, pages);
                }
            });
        }
        if (nextClass) {
            $container.find("." + nextClass).off("click.farmPager").on("click.farmPager", function () {
                if (pageNo < pages && $.isFunction(opts.onNext)) {
                    opts.onNext(pageNo, pages);
                }
            });
        }
    }

    window.FarmUi = {
        motion: motion,
        asNumber: asNumber,
        trimText: trimText,
        escapeHtml: escapeHtml,
        escapeAttr: escapeAttr,
        toArray: toArray,
        totalPages: totalPages,
        showPanel: showPanel,
        hidePanel: hidePanel,
        renderButtonPager: renderButtonPager
    };
})(window, window.jQuery);
