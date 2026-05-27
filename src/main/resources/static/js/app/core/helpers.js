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

    function showPanel($el) {
        $el.stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
    }

    function hidePanel($el) {
        if (!$el || $el.length <= 0 || !$el.is(":visible")) {
            return;
        }
        $el.stop(true, true).fadeOut(motion().moduleEnterMs);
    }

    window.FarmUi = {
        motion: motion,
        asNumber: asNumber,
        showPanel: showPanel,
        hidePanel: hidePanel
    };
})(window, window.jQuery);
