(function (window, $) {
    if (!$ || !$.messager) {
        return;
    }

    function bottomRightStyle() {
        return {
            right: "14px",
            bottom: "10px"
        };
    }

    function placeLatestMessagerBottomRight() {
        var $wins = $("body > div.messager-window:visible");
        if ($wins.length === 0) {
            return;
        }
        $wins.eq($wins.length - 1).css({
            left: "auto",
            top: "auto",
            right: bottomRightStyle().right,
            bottom: bottomRightStyle().bottom
        });
    }

    function popupDefaults() {
        return {
            title: "提示",
            timeout: 1600,
            showType: "slide",
            showSpeed: 260,
            style: bottomRightStyle()
        };
    }

    var rawShow = $.messager.show;
    if ($.isFunction(rawShow)) {
        $.messager.show = function (options) {
            var opts = $.extend(true, {}, popupDefaults(), options || {});
            var result = rawShow.call($.messager, opts);
            window.setTimeout(placeLatestMessagerBottomRight, 0);
            return result;
        };
    }

    var rawAlert = $.messager.alert;
    if ($.isFunction(rawAlert)) {
        $.messager.alert = function () {
            var result = rawAlert.apply($.messager, arguments);
            window.setTimeout(placeLatestMessagerBottomRight, 0);
            return result;
        };
    }

    var rawConfirm = $.messager.confirm;
    if ($.isFunction(rawConfirm)) {
        $.messager.confirm = function () {
            var result = rawConfirm.apply($.messager, arguments);
            window.setTimeout(placeLatestMessagerBottomRight, 0);
            return result;
        };
    }
})(window, window.jQuery);
