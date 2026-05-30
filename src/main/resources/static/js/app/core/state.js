(function (window) {
    window.FarmAppState = {
        resourceRoot: "/resources",
        imgRoot: "/resources/imgs",
        soundRoot: "/resources/sounds",
        defaults: {
            avatar: "/oss/defaults/avatar/default-avatar.png",
            seedCover: "/oss/defaults/seed/seed-cover-default.png",
            seedStage: "/oss/defaults/seed/seed-stage-default.png"
        },
        currentUser: null,
        currentModule: "home",
        motion: {
            moduleEnterMs: 260,
            pageSwitchMs: 220,
            actionFeedbackMs: 1200,
            dataRefreshDelayMs: 260
        },
        realtime: {
            enableFallbackPolling: true,
            fallbackIntervalMs: 5000
        }
    };

    window.farmResolveImg = function (relativePath) {
        var safePath = (relativePath || "").replace(/^\/+/, "");
        return window.FarmAppState.imgRoot + "/" + safePath;
    };

    window.farmResolveSound = function (relativePath) {
        var safePath = (relativePath || "").replace(/^\/+/, "");
        return window.FarmAppState.soundRoot + "/" + safePath;
    };

    window.farmDefaultAsset = function (key) {
        var defaults = (window.FarmAppState && window.FarmAppState.defaults) || {};
        return defaults[key] || "";
    };

    window.farmBuildRequestId = function (prefix) {
        var p = prefix || "req";
        var stamp = new Date().getTime();
        var rand = Math.floor(Math.random() * 1000000);
        return p + "_" + stamp + "_" + rand;
    };

    window.farmMotion = function () {
        var motion = window.FarmAppState.motion || {};
        return {
            moduleEnterMs: Number(motion.moduleEnterMs || 260),
            pageSwitchMs: Number(motion.pageSwitchMs || 220),
            actionFeedbackMs: Number(motion.actionFeedbackMs || 1200),
            dataRefreshDelayMs: Number(motion.dataRefreshDelayMs || 260)
        };
    };

    if (window.document && window.document.documentElement) {
        var root = window.document.documentElement;
        var m = window.farmMotion();
        root.style.setProperty("--farm-motion-module-ms", m.moduleEnterMs + "ms");
        root.style.setProperty("--farm-motion-page-ms", m.pageSwitchMs + "ms");
        root.style.setProperty("--farm-motion-feedback-ms", m.actionFeedbackMs + "ms");
    }
})(window);
