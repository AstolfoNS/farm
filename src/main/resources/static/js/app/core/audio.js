(function (window, $) {
    var FarmAudio = {};
    var muted = false;
    var cache = {};
    var keyToPath = {
        click: "domain/farm/ui_seed_selector_open.wav",
        open: "domain/farm/ui_seed_selector_open.wav",
        plant: "domain/farm/seed_place_ground.wav",
        harvest: "domain/farm/crop_harvest.wav",
        care: "domain/farm/crop_bug_remove.wav",
        clean: "domain/farm/crop_clean_dead_leaf.wav",
        error: "domain/farm/error_taunt.wav",
        buy: "domain/farm/seed_place_ground.wav",
        sell: "domain/farm/crop_harvest.wav"
    };

    function resolveUrl(key) {
        var relativePath = keyToPath[key] || "";
        if (!relativePath) {
            return "";
        }
        return farmResolveSound(relativePath);
    }

    function safeCreateAudio(url) {
        if (!url || !window.Audio) {
            return null;
        }
        var audio = new Audio(url);
        audio.preload = "auto";
        return audio;
    }

    function ensureAudio(key) {
        if (cache[key]) {
            return cache[key];
        }
        var audio = safeCreateAudio(resolveUrl(key));
        if (audio) {
            cache[key] = audio;
        }
        return audio;
    }

    function refreshToggleText() {
        $("#audioToggleBtn").text(muted ? "音效:关" : "音效:开");
    }

    FarmAudio.play = function (key) {
        if (muted) {
            return;
        }
        var mainAudio = ensureAudio(key);
        if (!mainAudio) {
            return;
        }
        try {
            var audio = mainAudio.cloneNode();
            var promise = audio.play();
            if (promise && $.isFunction(promise.catch)) {
                promise.catch(function () {
                });
            }
        } catch (e) {
        }
    };

    FarmAudio.setMuted = function (flag) {
        muted = !!flag;
        refreshToggleText();
    };

    FarmAudio.toggleMuted = function () {
        FarmAudio.setMuted(!muted);
        return muted;
    };

    FarmAudio.isMuted = function () {
        return muted;
    };

    FarmAudio.init = function () {
        $.each(keyToPath, function (key) {
            ensureAudio(key);
        });
        refreshToggleText();
        $("#audioToggleBtn").off("click").on("click", function () {
            FarmAudio.toggleMuted();
            FarmAudio.play("click");
        });
    };

    window.FarmAudio = FarmAudio;

    $(function () {
        FarmAudio.init();
    });
})(window, window.jQuery);
