(function (window, $) {
    var FarmPlotAdminModule = {};
    var state = {
        active: false
    };

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#plotAdminPanel"));
            } else {
                $("#plotAdminPanel").stop(true, true).css("display", "none").fadeIn(window.farmMotion().moduleEnterMs);
            }
            return;
        }
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#plotAdminPanel"));
        } else {
            $("#plotAdminPanel").stop(true, true).fadeOut(window.farmMotion().moduleEnterMs);
        }
    }

    FarmPlotAdminModule.setActive = setActive;
    window.FarmPlotAdminModule = FarmPlotAdminModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("plot-admin", FarmPlotAdminModule);
    }
})(window, window.jQuery);
