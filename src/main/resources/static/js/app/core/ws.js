(function (window, $) {
    var FarmWsBridge = {};
    var started = false;
    var activeUserId = 0;

    function safeUserId(userId) {
        var value = Number(userId || 0);
        return isNaN(value) ? 0 : value;
    }

    function ensureStart() {
        if (started || !window.FarmRealtimeClient) {
            return;
        }
        window.FarmRealtimeClient.start({
            userId: activeUserId,
            debug: false
        });
        started = true;
    }

    FarmWsBridge.syncUser = function (userId) {
        var uid = safeUserId(userId);
        activeUserId = uid;
        if (!window.FarmRealtimeClient) {
            return;
        }
        if (uid <= 0) {
            window.FarmRealtimeClient.stop();
            started = false;
            return;
        }
        if (!started) {
            ensureStart();
            return;
        }
        window.FarmRealtimeClient.switchUser(uid);
    };

    FarmWsBridge.stop = function () {
        activeUserId = 0;
        if (window.FarmRealtimeClient) {
            window.FarmRealtimeClient.stop();
        }
        started = false;
    };

    FarmWsBridge.isConnected = function () {
        if (!window.FarmRealtimeClient) {
            return false;
        }
        return window.FarmRealtimeClient.isConnected();
    };

    window.FarmWsBridge = FarmWsBridge;
})(window, window.jQuery);
