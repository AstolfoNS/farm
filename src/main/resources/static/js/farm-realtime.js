(function (window, $) {
    if (!window || !$) {
        return;
    }

    var wsRef = null;
    var reconnectTimer = null;
    var manualClosed = false;
    var currentUserId = 0;
    var reconnectCount = 0;
    var config = {
        path: '/ws/server',
        maxReconnectIntervalMs: 15000,
        baseReconnectIntervalMs: 1000,
        maxReconnectCount: -1,
        debug: false,
        onStatus: null
    };

    function safeNumber(val, def) {
        var n = Number(val);
        return isNaN(n) ? (def || 0) : n;
    }

    function status(text, meta) {
        if ($.isFunction(config.onStatus)) {
            config.onStatus(text, meta || {});
        }
        if (config.debug && window.console && $.isFunction(window.console.log)) {
            window.console.log('[farm-realtime]', text, meta || {});
        }
        $(document).trigger('farm:realtime:status', [text, meta || {}]);
    }

    function clearReconnectTimer() {
        if (reconnectTimer) {
            window.clearTimeout(reconnectTimer);
            reconnectTimer = null;
        }
    }

    function closeSocket() {
        if (wsRef) {
            try {
                wsRef.close();
            } catch (e) {
            }
        }
        wsRef = null;
    }

    function buildWsUrl(userId) {
        var protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
        var host = window.location.host;
        return protocol + host + config.path + '?userId=' + encodeURIComponent(userId);
    }

    function calcReconnectInterval() {
        var factor = Math.pow(2, reconnectCount);
        var delay = safeNumber(config.baseReconnectIntervalMs, 1000) * factor;
        var maxDelay = safeNumber(config.maxReconnectIntervalMs, 15000);
        if (delay > maxDelay) {
            delay = maxDelay;
        }
        var jitter = Math.floor(Math.random() * 300);
        return delay + jitter;
    }

    function shouldRetryReconnect() {
        var max = safeNumber(config.maxReconnectCount, -1);
        if (max < 0) {
            return true;
        }
        return reconnectCount < max;
    }

    function scheduleReconnect() {
        if (manualClosed || !currentUserId || !shouldRetryReconnect()) {
            return;
        }
        clearReconnectTimer();
        var interval = calcReconnectInterval();
        reconnectCount = reconnectCount + 1;
        status('reconnecting', {delay: interval, reconnectCount: reconnectCount, userId: currentUserId});
        reconnectTimer = window.setTimeout(function () {
            connectInternal(currentUserId);
        }, interval);
    }

    function dispatchMessage(payload) {
        if (!payload || payload.event !== 'FARM_OVERVIEW') {
            return;
        }
        $(document).trigger('farm:realtime:overview', [payload]);
    }

    function connectInternal(userId) {
        clearReconnectTimer();
        closeSocket();
        if (!userId || userId <= 0) {
            status('idle', {reason: 'invalid-user'});
            return;
        }
        currentUserId = userId;
        manualClosed = false;
        status('connecting', {userId: userId});

        var wsUrl = buildWsUrl(userId);
        wsRef = new WebSocket(wsUrl);

        wsRef.onopen = function () {
            reconnectCount = 0;
            status('connected', {userId: userId});
        };

        wsRef.onmessage = function (event) {
            if (!event || !event.data) {
                return;
            }
            var payload = null;
            try {
                payload = JSON.parse(event.data);
            } catch (e) {
                status('message-parse-failed', {raw: event.data});
                return;
            }
            dispatchMessage(payload);
        };

        wsRef.onerror = function () {
            status('error', {userId: userId});
        };

        wsRef.onclose = function () {
            status('closed', {userId: userId, manualClosed: manualClosed});
            wsRef = null;
            if (!manualClosed) {
                scheduleReconnect();
            }
        };
    }

    function start(opts) {
        config = $.extend({}, config, opts || {});
        var uid = safeNumber(config.userId, 0);
        connectInternal(uid);
    }

    function switchUser(userId) {
        var uid = safeNumber(userId, 0);
        if (!uid || uid <= 0) {
            stop();
            return;
        }
        if (uid === currentUserId && wsRef && wsRef.readyState === WebSocket.OPEN) {
            return;
        }
        reconnectCount = 0;
        connectInternal(uid);
    }

    function stop() {
        manualClosed = true;
        clearReconnectTimer();
        closeSocket();
        status('stopped', {userId: currentUserId});
    }

    function isConnected() {
        return !!(wsRef && wsRef.readyState === WebSocket.OPEN);
    }

    window.FarmRealtimeClient = {
        start: start,
        stop: stop,
        switchUser: switchUser,
        isConnected: isConnected
    };
})(window, window.jQuery);
