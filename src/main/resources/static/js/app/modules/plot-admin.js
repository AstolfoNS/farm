(function (window, $) {
    var FarmPlotAdminModule = {};
    var state = {
        active: false
    };

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            $("#plotAdminPanel").stop(true, true).css("display", "none").fadeIn(window.farmMotion().moduleEnterMs);
            return;
        }
        $("#plotAdminPanel").stop(true, true).fadeOut(window.farmMotion().moduleEnterMs);
    }

    FarmPlotAdminModule.setActive = setActive;
    window.FarmPlotAdminModule = FarmPlotAdminModule;
})(window, window.jQuery);
